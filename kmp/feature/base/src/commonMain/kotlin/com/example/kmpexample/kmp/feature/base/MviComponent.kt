package com.example.kmpexample.kmp.feature.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface MviComponent<STATE : Any, ACTION : Any> {
    val state: StateFlow<STATE>
    val actions: SharedFlow<ACTION>

    fun currentState(): STATE

    fun watchState(observer: (STATE) -> Unit): StateSubscription

    fun onAction(action: ACTION)
}

abstract class FeatureStoreComponent<STATE : Any, ACTION : Any>(
    initialState: STATE,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    private val plugins: List<FeaturePlugin<STATE, ACTION>> = emptyList(),
) : MviComponent<STATE, ACTION> {
    protected val mutableState = MutableStateFlow(initialState)
    protected val mutableActions = MutableSharedFlow<ACTION>(extraBufferCapacity = 64)

    override val state: StateFlow<STATE> = mutableState.asStateFlow()
    override val actions = mutableActions

    override fun currentState(): STATE {
        return state.value
    }

    override fun watchState(observer: (STATE) -> Unit): StateSubscription {
        observer(currentState())

        val job = coroutineScope.launch {
            state.collectLatest { nextState ->
                observer(nextState)
            }
        }

        return StateSubscription {
            job.cancel()
        }
    }

    protected fun launchSafely(block: suspend () -> Unit) {
        coroutineScope.launch {
            runCatching { block() }
                .onFailure { throwable ->
                    plugins.forEach { plugin ->
                        plugin.onError(
                            throwable = throwable,
                            currentState = mutableState.value,
                            updateState = { transform -> mutableState.value = transform(mutableState.value) },
                        )
                    }
                }
        }
    }

    protected fun publishAction(action: ACTION) {
        mutableActions.tryEmit(action)
        plugins.forEach { it.onAction(action) }
    }
}