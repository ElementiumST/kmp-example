package com.example.kmpexample.kmp.core.component

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.example.kmpexample.kmp.feature.auth.component.AuthComponent
import com.example.kmpexample.kmp.feature.base.StateSubscription
import com.example.kmpexample.kmp.feature.contacts.component.ContactsComponent

interface RootComponent {
    val stack: Value<ChildStack<*, Child>>
    val authComponent: AuthComponent
    fun currentChildKind(): RootChildKind
    fun watchChildKind(observer: (RootChildKind) -> Unit): StateSubscription
    fun currentAuthComponentOrNull(): AuthComponent?
    fun currentContactsComponentOrNull(): ContactsComponent?

    sealed interface Child {
        data class Auth(
            val component: AuthComponent,
        ) : Child

        data class Contacts(
            val component: ContactsComponent,
        ) : Child
    }
}
