package com.example.kmpexample.kmp.app

import com.arkivanov.decompose.ComponentContext
import com.example.kmpexample.kmp.core.app.SharedApp
import com.example.kmpexample.kmp.core.app.SharedAppConfig
import com.example.kmpexample.kmp.core.component.RootComponent
import org.koin.core.Koin

object SharedAppFacade {
    fun ensureStarted(config: SharedAppConfig = SharedAppConfig()): Koin = SharedApp.ensureStarted(config)

    fun createRootComponent(
        componentContext: ComponentContext,
        config: SharedAppConfig = SharedAppConfig(),
    ): RootComponent = SharedApp.createRootComponent(componentContext, config)

    fun createRootComponent(
        config: SharedAppConfig = SharedAppConfig(),
    ): RootComponent = SharedApp.createRootComponent(config)
}
