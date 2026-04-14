package com.example.kmpexample.kmp.core.bridge.apple

import com.example.kmpexample.kmp.core.app.SharedApp
import com.example.kmpexample.kmp.core.app.SharedAppConfig
import com.example.kmpexample.kmp.core.component.RootComponent
import com.example.kmpexample.kmp.data.db.IosDatabaseFactory

class AppleAppFactory {
    fun createRootComponent(): RootComponent {
        return SharedApp.createRootComponent(
            config = SharedAppConfig(
                databaseFactory = IosDatabaseFactory(),
            ),
        )
    }
}
