package com.example.kmpexample.kmp.tools.bridge.codegen

internal data class RouteMeta(
    val root: String,
    val contacts: String,
    val path: String,
)

internal object TypeScriptEmitter {
    fun emitAlias(
        typeName: String,
        fields: List<Pair<String, String>>,
    ): String {
        val body = fields.joinToString(separator = "\n") { (name, type) ->
            "  $name: $type;"
        }
        return buildString {
            appendLine("export type $typeName = {")
            appendLine(body)
            append('}')
        }
    }

    fun emitBridgeTypes(routes: List<RouteMeta>): String {
        val template = loadTemplate("templates/bridge-types.ts.hbs")
        val renderedRoutes = routes.joinToString(separator = "\n") { route ->
            "  { root: '${route.root}', contacts: '${route.contacts}', path: '${route.path}' },"
        }
        return template.replace("{{ROUTES}}", renderedRoutes)
    }

    fun emitAngularService(): String = loadTemplate("templates/data-access-kmp-bridge.generated.ts.hbs")

    private fun loadTemplate(path: String): String {
        val stream = javaClass.classLoader.getResourceAsStream(path)
            ?: error("Template not found: $path")
        return stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
    }
}