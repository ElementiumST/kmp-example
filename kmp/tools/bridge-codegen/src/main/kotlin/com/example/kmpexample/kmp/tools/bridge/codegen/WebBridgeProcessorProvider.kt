package com.example.kmpexample.kmp.tools.bridge.codegen

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import java.io.OutputStream
import java.io.OutputStreamWriter

class WebBridgeProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return WebBridgeProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
        )
    }
}

private class WebBridgeProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {
    private var emitted = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (emitted) return emptyList()
        emitted = true

        val modelCount = resolver.getSymbolsWithAnnotation(BRIDGE_MODEL_FQCN).count()
        val bridgeClassCount = resolver.getSymbolsWithAnnotation(EXPOSE_TO_WEB_FQCN).count()
        val actionCount = resolver.getSymbolsWithAnnotation(BRIDGE_ACTION_FQCN).count()
        val unionCount = resolver.getSymbolsWithAnnotation(BRIDGE_STRING_UNION_FQCN).count()

        emitRegistry(modelCount, bridgeClassCount, actionCount, unionCount)
        emitTsArtifacts()

        logger.info(
            "Generated bridge artifacts: models=$modelCount, bridgeClasses=$bridgeClassCount, actions=$actionCount, unions=$unionCount",
        )
        return emptyList()
    }

    private fun emitRegistry(
        modelCount: Int,
        bridgeClassCount: Int,
        actionCount: Int,
        unionCount: Int,
    ) {
        val output = codeGenerator.createNewFile(
            dependencies = Dependencies(false),
            packageName = "com.example.kmpexample.kmp.bridge.generated",
            fileName = "GeneratedBridgeRegistry",
        )
        OutputStreamWriter(output, Charsets.UTF_8).use { writer ->
            writer.write(
                """
                package com.example.kmpexample.kmp.bridge.generated

                object GeneratedBridgeRegistry {
                    const val generatedAt: String = "bridge-ksp"
                    const val modelCount: Int = $modelCount
                    const val bridgeClassCount: Int = $bridgeClassCount
                    const val actionCount: Int = $actionCount
                    const val unionCount: Int = $unionCount
                }
                """.trimIndent(),
            )
        }
    }

    private fun emitTsArtifacts() {
        writeTsFile(
            relativePath = "bridge/ts/bridge-types",
            contents = TypeScriptEmitter.emitBridgeTypes(defaultRoutes),
        )
        writeTsFile(
            relativePath = "bridge/ts/data-access-kmp-bridge.generated",
            contents = TypeScriptEmitter.emitAngularService(),
        )
    }

    private fun writeTsFile(
        relativePath: String,
        contents: String,
    ) {
        val output = createNewFileByPathCompat(
            dependencies = Dependencies(false),
            path = relativePath,
            extensionName = "ts",
        )
        OutputStreamWriter(output, Charsets.UTF_8).use { writer ->
            writer.write(contents)
        }
    }

    private fun createNewFileByPathCompat(
        dependencies: Dependencies,
        path: String,
        extensionName: String,
    ): OutputStream {
        val method = codeGenerator::class.java.methods.firstOrNull { it.name == "createNewFileByPath" }
        if (method != null) {
            val args = method.parameterTypes.size
            return when (args) {
                3 -> method.invoke(codeGenerator, dependencies, path, extensionName) as OutputStream
                4 -> method.invoke(codeGenerator, dependencies, path, extensionName, "") as OutputStream
                else -> error("Unsupported createNewFileByPath signature: $args")
            }
        }

        return codeGenerator.createNewFile(
            dependencies = dependencies,
            packageName = "com.example.kmpexample.kmp.bridge.generated.ts",
            fileName = path.substringAfterLast('/'),
            extensionName = extensionName,
        )
    }

    private companion object {
        private const val BRIDGE_MODEL_FQCN = "com.example.kmpexample.kmp.tools.bridge.annotations.BridgeModel"
        private const val EXPOSE_TO_WEB_FQCN = "com.example.kmpexample.kmp.tools.bridge.annotations.ExposeToWeb"
        private const val BRIDGE_ACTION_FQCN = "com.example.kmpexample.kmp.tools.bridge.annotations.BridgeAction"
        private const val BRIDGE_STRING_UNION_FQCN = "com.example.kmpexample.kmp.tools.bridge.annotations.BridgeStringUnion"

        val defaultRoutes = listOf(
            RouteMeta(root = "AUTH", contacts = "LIST", path = "auth"),
            RouteMeta(root = "CONTACTS_LIST", contacts = "LIST", path = "contacts"),
            RouteMeta(root = "CONTACTS_LIST", contacts = "INFO", path = "contacts/info"),
            RouteMeta(root = "CONTACTS_LIST", contacts = "CREATE", path = "contacts/create"),
            RouteMeta(root = "CONTACTS_LIST", contacts = "EDIT", path = "contacts/edit"),
            RouteMeta(root = "CONTACT_INFO", contacts = "INFO", path = "contacts/info"),
            RouteMeta(root = "CONTACT_CREATE", contacts = "CREATE", path = "contacts/create"),
            RouteMeta(root = "CONTACT_EDIT", contacts = "EDIT", path = "contacts/edit"),
        )
    }
}