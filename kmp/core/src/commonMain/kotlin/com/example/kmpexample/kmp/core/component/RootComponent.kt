package com.example.kmpexample.kmp.core.component

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value

interface RootComponent {
    val stack: Value<ChildStack<*, Child>>
    val authComponent: AuthComponent

    sealed interface Child {
        data class Auth(
            val component: AuthComponent,
        ) : Child
    }
}
