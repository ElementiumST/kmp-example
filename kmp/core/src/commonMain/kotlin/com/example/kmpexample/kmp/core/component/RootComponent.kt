package com.example.kmpexample.kmp.core.component

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.example.kmpexample.kmp.feature.auth.component.AuthComponent
import com.example.kmpexample.kmp.feature.contacts.component.ContactsComponent

/**
 * Root navigation contract. Keeps ONLY what is meaningful for business logic:
 * the active child stack and explicit lifecycle teardown. All bridge-style
 * accessors (`currentXxxOrNull`, `watchChildKind`, etc.) live in platform
 * bridge modules (`kmp:bridge-web`, `kmp:bridge-apple`) and do not pollute
 * the multiplatform surface.
 */
interface RootComponent {
    val stack: Value<ChildStack<*, Child>>

    fun destroy()

    sealed interface Child {
        data class Auth(
            val component: AuthComponent,
        ) : Child

        data class Contacts(
            val component: ContactsComponent,
        ) : Child
    }
}
