package com.example.kmpexample.kmp.feature.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface MviComponent<STATE : Any, ACTION : Any> {
    val state: StateFlow<STATE>

    fun currentState(): STATE

    fun watchState(observer: (STATE) -> Unit): StateSubscription

    fun onAction(action: ACTION)
}

abstract class BaseMviComponent<STATE : Any, ACTION : Any>(
    initialState: STATE,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : MviComponent<STATE, ACTION> {
    protected val mutableState = MutableStateFlow(initialState)

    override val state: StateFlow<STATE> = mutableState.asStateFlow()

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
}
