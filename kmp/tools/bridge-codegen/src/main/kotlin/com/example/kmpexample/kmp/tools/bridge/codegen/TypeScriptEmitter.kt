package com.example.kmpexample.kmp.tools.bridge.codegen

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
}
