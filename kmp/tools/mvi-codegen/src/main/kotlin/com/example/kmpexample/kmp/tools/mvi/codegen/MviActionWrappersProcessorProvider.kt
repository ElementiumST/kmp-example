package com.example.kmpexample.kmp.tools.mvi.codegen

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.Nullability
import com.google.devtools.ksp.validate
import java.io.OutputStreamWriter

private const val WRAPPER_ANNOTATION_FQCN =
    "com.example.kmpexample.kmp.tools.mvi.annotations.GenerateMviActionWrappers"

class MviActionWrappersProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return MviActionWrappersProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
        )
    }
}

private class MviActionWrappersProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(WRAPPER_ANNOTATION_FQCN).toList()
        val deferred = symbols.filterNot { it.validate() }
        val classes = symbols
            .filter { it.validate() }
            .filterIsInstance<KSClassDeclaration>()

        classes.forEach { actionDeclaration ->
            if (!isSupportedActionDeclaration(actionDeclaration)) {
                logger.error(
                    "@GenerateMviActionWrappers can only be placed on sealed interfaces.",
                    actionDeclaration,
                )
                return@forEach
            }
            generateWrapperInterface(actionDeclaration)
        }

        return deferred
    }

    private fun isSupportedActionDeclaration(actionDeclaration: KSClassDeclaration): Boolean {
        return actionDeclaration.classKind == ClassKind.INTERFACE &&
            actionDeclaration.modifiers.contains(Modifier.SEALED)
    }

    private fun generateWrapperInterface(actionDeclaration: KSClassDeclaration) {
        val packageName = actionDeclaration.packageName.asString()
        val actionSimpleName = actionDeclaration.simpleName.asString()
        val wrapperInterfaceName = actionDeclaration.extractInterfaceNameOverride()
            ?.takeIf { it.isNotBlank() }
            ?: defaultWrapperInterfaceName(actionSimpleName)
        val dependencies = actionDeclaration.containingFile
            ?.let { Dependencies(aggregating = false, it) }
            ?: Dependencies(aggregating = false)
        val methods = actionDeclaration.getSealedSubclasses()
            .sortedBy { it.simpleName.asString() }
            .mapNotNull { actionVariant ->
                createWrapperMethod(actionSimpleName, actionVariant)
            }

        val duplicatedMethodNames = methods
            .groupBy { it.name }
            .filterValues { it.size > 1 }
            .keys
        if (duplicatedMethodNames.isNotEmpty()) {
            logger.error(
                "Duplicate wrapper methods generated for $actionSimpleName: " +
                    duplicatedMethodNames.joinToString(),
                actionDeclaration,
            )
            return
        }

        val output = codeGenerator.createNewFile(
            dependencies = dependencies,
            packageName = packageName,
            fileName = wrapperInterfaceName,
        )
        OutputStreamWriter(output, Charsets.UTF_8).use { writer ->
            writer.write(
                buildString {
                    appendLine("package $packageName")
                    appendLine()
                    appendLine(
                        "/** Generated wrappers for [$actionSimpleName]. Do not edit manually. */",
                    )
                    appendLine("interface $wrapperInterfaceName {")
                    appendLine("    fun onAction(action: $actionSimpleName)")
                    appendLine()
                    methods.forEach { method ->
                        val params = method.parameters.joinToString(", ") { parameter ->
                            "${parameter.name}: ${parameter.type}"
                        }
                        val args = method.parameters.joinToString(", ") { parameter ->
                            parameter.name
                        }
                        if (method.parameters.isEmpty()) {
                            appendLine(
                                "    fun ${method.name}() = onAction($actionSimpleName.${method.variantName})",
                            )
                        } else {
                            appendLine(
                                "    fun ${method.name}($params) = " +
                                    "onAction($actionSimpleName.${method.variantName}($args))",
                            )
                        }
                    }
                    appendLine("}")
                },
            )
        }
    }

    private fun createWrapperMethod(
        actionSimpleName: String,
        actionVariant: KSClassDeclaration,
    ): WrapperMethod? {
        val variantName = actionVariant.simpleName.asString()
        val methodName = variantName.toWrapperMethodName()

        return when (actionVariant.classKind) {
            ClassKind.OBJECT -> WrapperMethod(
                name = methodName,
                variantName = variantName,
                parameters = emptyList(),
            )
            ClassKind.CLASS -> {
                val parameters = actionVariant.primaryConstructor
                    ?.parameters
                    ?.map { parameter ->
                        val paramName = parameter.name?.asString().orEmpty()
                        if (paramName.isBlank()) {
                            logger.error(
                                "Unsupported constructor parameter name in $actionSimpleName.$variantName",
                                actionVariant,
                            )
                        }
                        WrapperMethodParameter(
                            name = paramName,
                            type = renderType(parameter),
                        )
                    }
                    ?: emptyList()
                if (parameters.any { it.name.isBlank() }) return null

                WrapperMethod(
                    name = methodName,
                    variantName = variantName,
                    parameters = parameters,
                )
            }
            else -> {
                logger.error(
                    "Unsupported sealed action variant kind ${actionVariant.classKind} in $actionSimpleName.$variantName",
                    actionVariant,
                )
                null
            }
        }
    }

    private fun renderType(parameter: KSValueParameter): String {
        val type = parameter.type.resolve()
        val declarationName = type.declaration.qualifiedName?.asString()
            ?: type.declaration.simpleName.asString()
        val typeArguments = type.arguments
            .map { argument ->
                argument.type?.resolve()?.let { renderResolvedType(it) } ?: "*"
            }
            .joinToString(", ")
            .takeIf { it.isNotBlank() }
            ?.let { "<$it>" }
            .orEmpty()
        val nullableSuffix = if (type.nullability == Nullability.NULLABLE) "?" else ""
        return declarationName + typeArguments + nullableSuffix
    }

    private fun renderResolvedType(type: KSType): String {
        val declarationName = type.declaration.qualifiedName?.asString()
            ?: type.declaration.simpleName.asString()
        val typeArguments = type.arguments
            .map { argument ->
                argument.type?.resolve()?.let { nested -> renderResolvedType(nested) } ?: "*"
            }
            .joinToString(", ")
            .takeIf { it.isNotBlank() }
            ?.let { "<$it>" }
            .orEmpty()
        val nullableSuffix = if (type.nullability == Nullability.NULLABLE) "?" else ""
        return declarationName + typeArguments + nullableSuffix
    }

    private fun KSClassDeclaration.extractInterfaceNameOverride(): String? {
        return annotations
            .firstOrNull {
                it.annotationType.resolve().declaration.qualifiedName?.asString() == WRAPPER_ANNOTATION_FQCN
            }
            ?.arguments
            ?.firstOrNull { it.name?.asString() == "wrapperInterfaceName" }
            ?.value as? String
    }
}

private data class WrapperMethod(
    val name: String,
    val variantName: String,
    val parameters: List<WrapperMethodParameter>,
)

private data class WrapperMethodParameter(
    val name: String,
    val type: String,
)
