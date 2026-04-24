package com.example.kmpexample.kmp.feature.base

import com.arkivanov.essenty.lifecycle.Lifecycle

interface FeaturePlugin<STATE : Any, ACTION : Any> {
    fun onAction(action: ACTION) {}

    fun onError(
        throwable: Throwable,
        currentState: STATE,
        updateState: (((STATE) -> STATE) -> Unit),
    ) {
    }
}

class RecoverPlugin<STATE : Any, ACTION : Any>(
    private val reducer: (Throwable, STATE) -> STATE,
) : FeaturePlugin<STATE, ACTION> {
    override fun onError(
        throwable: Throwable,
        currentState: STATE,
        updateState: (((STATE) -> STATE) -> Unit),
    ) {
        updateState { reducer(throwable, currentState) }
    }
}

class LoggingPlugin<STATE : Any, ACTION : Any>(
    private val logger: (String) -> Unit,
) : FeaturePlugin<STATE, ACTION> {
    override fun onAction(action: ACTION) {
        logger("action=${action::class.simpleName}")
    }

    override fun onError(
        throwable: Throwable,
        currentState: STATE,
        updateState: (((STATE) -> STATE) -> Unit),
    ) {
        logger("error=${throwable.message ?: "unknown"}")
    }
}

class RetryPlugin<STATE : Any, ACTION : Any>(
    val maxAttempts: Int = 3,
) : FeaturePlugin<STATE, ACTION>

class SavedStatePlugin<STATE : Any, ACTION : Any> : FeaturePlugin<STATE, ACTION>

data class LifecycleHandle(
    val lifecycle: Lifecycle,
    val cancel: () -> Unit,
)
