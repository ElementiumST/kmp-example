package com.example.kmpexample.kmp.core.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.example.kmpexample.kmp.domain.usecase.LoginUseCase
import kotlinx.serialization.Serializable

class DefaultRootComponent(
    componentContext: ComponentContext,
    private val loginUseCase: LoginUseCase,
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
                    loginUseCase = loginUseCase,
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
