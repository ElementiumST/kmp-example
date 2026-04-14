package com.example.kmpexample.kmp.core.component

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.example.kmpexample.kmp.feature.auth.component.AuthComponent

interface RootComponent {
    val stack: Value<ChildStack<*, Child>>
    val authComponent: AuthComponent

    sealed interface Child {
        data class Auth(
            val component: AuthComponent,
        ) : Child
    }
}
