package com.example.kmpexample.kmp.core.bridge.apple

import com.example.kmpexample.kmp.core.bridge.StateSubscription
import com.example.kmpexample.kmp.core.component.RootComponent
import com.example.kmpexample.kmp.core.model.AuthScreenAction
import com.example.kmpexample.kmp.core.model.AuthScreenState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AppleRootController(
    private val rootComponent: RootComponent,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    fun currentState(): AuthScreenState {
        return rootComponent.authComponent.state.value
    }

    fun watchState(observer: (AuthScreenState) -> Unit): StateSubscription {
        observer(currentState())

        val job = coroutineScope.launch {
            rootComponent.authComponent.state.collectLatest { state ->
                observer(state)
            }
        }

        return StateSubscription {
            job.cancel()
        }
    }

    fun updateLogin(value: String) {
        rootComponent.authComponent.onAction(AuthScreenAction.UpdateLogin(value))
    }

    fun updatePassword(value: String) {
        rootComponent.authComponent.onAction(AuthScreenAction.UpdatePassword(value))
    }

    fun submit() {
        rootComponent.authComponent.onAction(AuthScreenAction.Submit)
    }
}