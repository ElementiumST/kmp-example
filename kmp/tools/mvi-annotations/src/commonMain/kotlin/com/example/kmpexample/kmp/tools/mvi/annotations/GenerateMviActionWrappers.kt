package com.example.kmpexample.kmp.tools.mvi.annotations

/**
 * Marks sealed action contracts for KSP generation of default
 * `onAction(...)` wrapper methods.
 *
 * The processor creates an interface named `<ActionName>Wrappers` by default,
 * or [wrapperInterfaceName] when explicitly provided.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class GenerateMviActionWrappers(
    val wrapperInterfaceName: String = "",
)
