package com.example.kmpexample.kmp.bridge.apple

import com.example.kmpexample.kmp.core.app.SharedApp
import com.example.kmpexample.kmp.core.app.SharedAppConfig
import com.example.kmpexample.kmp.core.component.RootComponent

class AppleAppFactory {
    fun createRootComponent(config: SharedAppConfig = SharedAppConfig()): RootComponent =
        SharedApp.createRootComponent(config = config)

    fun createRootAccessor(config: SharedAppConfig = SharedAppConfig()): AppleRootAccessor =
        AppleRootAccessor(createRootComponent(config))
}
