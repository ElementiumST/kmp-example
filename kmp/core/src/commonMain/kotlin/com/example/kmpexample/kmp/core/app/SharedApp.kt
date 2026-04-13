package com.example.kmpexample.kmp.core.app

import com.arkivanov.decompose.ComponentContext
import com.example.kmpexample.kmp.core.component.DefaultRootComponent
import com.example.kmpexample.kmp.core.component.RootComponent
import com.example.kmpexample.kmp.core.di.coreModule
import com.example.kmpexample.kmp.data.di.dataModule
import org.koin.core.Koin
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

object SharedApp {
    fun ensureStarted(config: SharedAppConfig = SharedAppConfig()): Koin {
        GlobalContext.getOrNull()?.let { existingKoin ->
            return existingKoin
        }

        startKoin {
            modules(
                listOf(
                    dataModule(
                        networkConfig = config.networkConfig,
                        databaseFactory = config.databaseFactory,
                    ),
                    coreModule(),
                ) + config.additionalModules,
            )
        }

        return GlobalContext.get()
    }

    fun createRootComponent(
        componentContext: ComponentContext,
        config: SharedAppConfig = SharedAppConfig(),
    ): RootComponent {
        val koin = ensureStarted(config)

        return DefaultRootComponent(
            componentContext = componentContext,
            loginUseCase = koin.get(),
        )
    }

    fun createRootComponent(
        config: SharedAppConfig = SharedAppConfig(),
    ): RootComponent {
        return createRootComponent(
            componentContext = createStandaloneComponentContext(),
            config = config,
        )
    }
}
