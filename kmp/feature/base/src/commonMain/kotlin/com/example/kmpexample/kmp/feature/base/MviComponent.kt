package com.example.kmpexample.kmp.feature.base

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface MviComponent<STATE : Any, ACTION : Any> {
    val state: StateFlow<STATE>

    fun onAction(action: ACTION)
}

abstract class BaseMviComponent<STATE : Any, ACTION : Any>(
    initialState: STATE,
) : MviComponent<STATE, ACTION> {
    protected val mutableState = MutableStateFlow(initialState)

    override val state: StateFlow<STATE> = mutableState.asStateFlow()
}
