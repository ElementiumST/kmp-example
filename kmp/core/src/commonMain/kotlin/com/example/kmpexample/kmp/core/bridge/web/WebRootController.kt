package com.example.kmpexample.kmp.core.bridge.web

import com.example.kmpexample.kmp.core.app.SharedApp
import com.example.kmpexample.kmp.core.app.createStandaloneComponentContext
import com.example.kmpexample.kmp.core.model.AuthScreenAction
import com.example.kmpexample.kmp.core.model.AuthScreenState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
class WebRootController {
    private val rootComponent = SharedApp.createRootComponent(createStandaloneComponentContext())
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var subscriptionJob: Job? = null

    fun currentState(): AuthScreenState {
        return rootComponent.authComponent.state.value
    }

    fun subscribeState(observer: (AuthScreenState) -> Unit) {
        subscriptionJob?.cancel()
        observer(currentState())

        subscriptionJob = coroutineScope.launch {
            rootComponent.authComponent.state.collectLatest { state ->
                observer(state)
            }
        }
    }

    fun clearSubscription() {
        subscriptionJob?.cancel()
        subscriptionJob = null
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

@OptIn(ExperimentalJsExport::class)
@JsExport
fun createWebRootController(): WebRootController {
    return WebRootController()
}
