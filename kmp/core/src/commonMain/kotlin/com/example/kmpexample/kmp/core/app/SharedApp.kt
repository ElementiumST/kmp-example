package com.example.kmpexample.kmp.core.app

import com.arkivanov.decompose.ComponentContext
import com.example.kmpexample.kmp.core.component.DefaultRootComponent
import com.example.kmpexample.kmp.core.component.RootComponent
import com.example.kmpexample.kmp.data.di.dataModule
import com.example.kmpexample.kmp.feature.auth.di.authFeatureModule
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatformTools

object SharedApp {
    fun ensureStarted(config: SharedAppConfig = SharedAppConfig()): Koin {
        KoinPlatformTools.defaultContext().getOrNull()?.let { existingKoin ->
            return existingKoin
        }

        startKoin {
            modules(
                listOf(
                    dataModule(
                        networkConfig = config.networkConfig,
                        databaseFactory = config.databaseFactory,
                    ),
                    authFeatureModule(),
                ) + config.additionalModules,
            )
        }

        return KoinPlatformTools.defaultContext().get()
    }

    fun createRootComponent(
        componentContext: ComponentContext,
        config: SharedAppConfig = SharedAppConfig(),
    ): RootComponent {
        val koin = ensureStarted(config)

        return DefaultRootComponent(
            componentContext = componentContext,
            clearPersistedAuthSessionUseCase = koin.get(),
            getPersistedAuthSessionUseCase = koin.get(),
            loginUseCase = koin.get(),
            loginWithTokenUseCase = koin.get(),
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
