package com.example.kmpexample.kmp.tools.bridge.annotations

import kotlin.reflect.KClass

enum class BridgeActionRole {
    STATE_JSON,
    WATCH_STATE,
    KIND,
    WATCH_KIND,
    INTENT,
    DESTROY,
}

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class ExposeToWeb(
    val name: String = "",
)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class BridgeModel(
    val name: String = "",
)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class BridgeAction(
    val name: String = "",
    val role: BridgeActionRole = BridgeActionRole.INTENT,
    val stateModel: KClass<*> = Nothing::class,
    val nullableOnEmpty: Boolean = false,
)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class BridgeStringUnion(
    val name: String,
    val values: Array<String>,
)

@Target(AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class BridgeChild(
    val parent: KClass<*>,
    val route: String = "",
)