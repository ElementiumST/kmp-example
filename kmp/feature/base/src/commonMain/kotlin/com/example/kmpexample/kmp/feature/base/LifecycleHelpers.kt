package com.example.kmpexample.kmp.feature.base

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

fun featureCoroutineScope(lifecycle: Lifecycle): CoroutineScope {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    lifecycle.doOnDestroy {
        scope.coroutineContext.cancel()
    }
    return scope
}
