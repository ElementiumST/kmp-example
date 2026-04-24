package com.example.kmpexample.kmp.tools.bridge.codegen

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
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

        val output = codeGenerator.createNewFile(
            dependencies = Dependencies(false),
            packageName = "com.example.kmpexample.kmp.bridge.generated",
            fileName = "GeneratedBridgeRegistry",
        )
        OutputStreamWriter(output, Charsets.UTF_8).use { writer ->
            writer.write(
                """
                package com.example.kmpexample.kmp.bridge.generated

                /**
                 * Placeholder generated file.
                 * This gets replaced by richer generation in next iterations.
                 */
                object GeneratedBridgeRegistry {
                    const val generatedAt: String = "ksp-placeholder"
                }
                """.trimIndent(),
            )
        }

        logger.info("Generated placeholder bridge registry.")
        return emptyList()
    }
}
