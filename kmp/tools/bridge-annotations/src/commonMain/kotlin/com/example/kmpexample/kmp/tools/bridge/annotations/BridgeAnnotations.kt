package com.example.kmpexample.kmp.tools.bridge.annotations

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class ExposeToWeb(
    val name: String = "",
)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class BridgeAction(
    val name: String = "",
)

@Target(AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class BridgeChild(
    val parent: KClass<*>,
    val route: String = "",
)
