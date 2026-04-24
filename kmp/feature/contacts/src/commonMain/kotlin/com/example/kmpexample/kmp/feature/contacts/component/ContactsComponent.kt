package com.example.kmpexample.kmp.feature.contacts.component

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.example.kmpexample.kmp.feature.base.MviComponent
import com.example.kmpexample.kmp.feature.contacts.model.ContactsListAction
import com.example.kmpexample.kmp.feature.contacts.model.ContactsListActionWrappers
import com.example.kmpexample.kmp.feature.contacts.model.ContactsListState

/**
 * Top-level component of the contacts feature. The `list` MVI surface drives
 * the root screen (search + list + FAB + add-overlay). Nested screens
 * (info/create/edit) are exposed via [childStack].
 *
 * Bridge-style accessors (active child discovery, kind observation, platform
 * JSON shims) live in `kmp:bridge-web` / `kmp:bridge-apple` and are not part
 * of the multiplatform contract.
 */
interface ContactsComponent :
    MviComponent<ContactsListState, ContactsListAction>,
    ContactsListActionWrappers {
    val childStack: Value<ChildStack<*, Child>>

    sealed interface Child {
        data object List : Child

        data class Info(val component: ContactInfoComponent) : Child

        data class Create(val component: ContactEditorComponent) : Child

        data class Edit(val component: ContactEditorComponent) : Child
    }
}
