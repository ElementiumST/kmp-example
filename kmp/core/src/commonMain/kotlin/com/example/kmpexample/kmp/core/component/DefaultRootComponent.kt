package com.example.kmpexample.kmp.core.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.example.kmpexample.kmp.domain.usecase.ClearPersistedAuthSessionUseCase
import com.example.kmpexample.kmp.domain.usecase.GetPersistedAuthSessionUseCase
import com.example.kmpexample.kmp.domain.usecase.LoginUseCase
import com.example.kmpexample.kmp.domain.usecase.LoginWithTokenUseCase
import com.example.kmpexample.kmp.feature.auth.component.AuthComponent
import com.example.kmpexample.kmp.feature.auth.component.DefaultAuthComponent
import kotlinx.serialization.Serializable

class DefaultRootComponent(
    componentContext: ComponentContext,
    private val clearPersistedAuthSessionUseCase: ClearPersistedAuthSessionUseCase,
    private val getPersistedAuthSessionUseCase: GetPersistedAuthSessionUseCase,
    private val loginUseCase: LoginUseCase,
    private val loginWithTokenUseCase: LoginWithTokenUseCase,
) : RootComponent, ComponentContext by componentContext {
    private val navigation = StackNavigation<Config>()

    override val stack: Value<ChildStack<*, RootComponent.Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.Auth,
        handleBackButton = true,
        childFactory = ::child,
    )

    override val authComponent: AuthComponent
        get() = (stack.value.active.instance as RootComponent.Child.Auth).component

    private fun child(
        config: Config,
        childComponentContext: ComponentContext,
    ): RootComponent.Child {
        return when (config) {
            Config.Auth -> RootComponent.Child.Auth(
                component = DefaultAuthComponent(
                    componentContext = childComponentContext,
                    clearPersistedAuthSessionUseCase = clearPersistedAuthSessionUseCase,
                    getPersistedAuthSessionUseCase = getPersistedAuthSessionUseCase,
                    loginUseCase = loginUseCase,
                    loginWithTokenUseCase = loginWithTokenUseCase,
                ),
            )
        }
    }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object Auth : Config
    }
}
