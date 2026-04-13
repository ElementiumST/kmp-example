package com.example.kmpexample.kmp.core.app

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry

internal fun createStandaloneComponentContext(): ComponentContext {
    return DefaultComponentContext(LifecycleRegistry())
}
