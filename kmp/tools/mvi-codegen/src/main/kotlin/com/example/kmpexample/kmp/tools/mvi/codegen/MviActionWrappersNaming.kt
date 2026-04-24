package com.example.kmpexample.kmp.tools.mvi.codegen

internal fun String.toWrapperMethodName(): String {
    if (isEmpty()) return this
    if (length == 1) return lowercase()
    return replaceFirstChar { it.lowercaseChar() }
}

internal fun defaultWrapperInterfaceName(actionSimpleName: String): String {
    return "${actionSimpleName}Wrappers"
}
