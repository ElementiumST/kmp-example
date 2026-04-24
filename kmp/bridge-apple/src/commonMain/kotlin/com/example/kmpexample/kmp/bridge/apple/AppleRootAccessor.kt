package com.example.kmpexample.kmp.bridge.apple

import com.arkivanov.decompose.value.subscribe
import com.example.kmpexample.kmp.core.component.RootComponent
import com.example.kmpexample.kmp.feature.auth.component.AuthComponent
import com.example.kmpexample.kmp.feature.base.StateSubscription
import com.example.kmpexample.kmp.feature.contacts.component.ContactEditorComponent
import com.example.kmpexample.kmp.feature.contacts.component.ContactInfoComponent
import com.example.kmpexample.kmp.feature.contacts.component.ContactsComponent

/**
 * Apple-facing accessor around [RootComponent]. Keeps iOS-friendly convenience
 * methods (active child discovery, kind observation) out of the multiplatform
 * contract. Exposed to Swift through the `SharedCore` XCFramework.
 */
class AppleRootAccessor(
    private val root: RootComponent,
) {
    enum class Kind {
        AUTH,
        CONTACTS_LIST,
        CONTACT_INFO,
        CONTACT_CREATE,
        CONTACT_EDIT,
    }

    fun rootComponent(): RootComponent = root

    fun currentKind(): Kind = root.currentKind()

    fun watchKind(observer: (Kind) -> Unit): StateSubscription {
        observer(currentKind())

        var innerCancellation: (() -> Unit)? = null

        fun rebindContacts(contacts: ContactsComponent?) {
            innerCancellation?.invoke()
            innerCancellation = null
            if (contacts != null) {
                val cancel = contacts.childStack.subscribe { observer(root.currentKind()) }
                innerCancellation = { cancel.cancel() }
            }
        }

        val outerCancellation = root.stack.subscribe { childStack ->
            observer(root.currentKind())
            rebindContacts((childStack.active.instance as? RootComponent.Child.Contacts)?.component)
        }

        return StateSubscription {
            innerCancellation?.invoke()
            outerCancellation.cancel()
        }
    }

    fun authComponent(): AuthComponent? =
        (root.stack.value.active.instance as? RootComponent.Child.Auth)?.component

    fun contactsComponent(): ContactsComponent? =
        (root.stack.value.active.instance as? RootComponent.Child.Contacts)?.component

    fun contactsAccessor(): AppleContactsAccessor? =
        contactsComponent()?.let(::AppleContactsAccessor)
}

class AppleContactsAccessor(
    private val contacts: ContactsComponent,
) {
    enum class Kind {
        LIST,
        INFO,
        CREATE,
        EDIT,
    }

    fun contactsComponent(): ContactsComponent = contacts

    fun currentKind(): Kind =
        when (contacts.childStack.value.active.instance) {
            ContactsComponent.Child.List -> Kind.LIST
            is ContactsComponent.Child.Info -> Kind.INFO
            is ContactsComponent.Child.Create -> Kind.CREATE
            is ContactsComponent.Child.Edit -> Kind.EDIT
        }

    fun watchKind(observer: (Kind) -> Unit): StateSubscription {
        observer(currentKind())
        val cancellation = contacts.childStack.subscribe { observer(currentKind()) }
        return StateSubscription { cancellation.cancel() }
    }

    fun infoComponent(): ContactInfoComponent? =
        (contacts.childStack.value.active.instance as? ContactsComponent.Child.Info)?.component

    fun createComponent(): ContactEditorComponent? =
        (contacts.childStack.value.active.instance as? ContactsComponent.Child.Create)?.component

    fun editComponent(): ContactEditorComponent? =
        (contacts.childStack.value.active.instance as? ContactsComponent.Child.Edit)?.component

    fun activeEditorComponent(): ContactEditorComponent? =
        when (val active = contacts.childStack.value.active.instance) {
            is ContactsComponent.Child.Create -> active.component
            is ContactsComponent.Child.Edit -> active.component
            else -> null
        }
}

private fun RootComponent.currentKind(): AppleRootAccessor.Kind {
    val active = stack.value.active.instance
    return when (active) {
        is RootComponent.Child.Auth -> AppleRootAccessor.Kind.AUTH
        is RootComponent.Child.Contacts -> when (active.component.childStack.value.active.instance) {
            ContactsComponent.Child.List -> AppleRootAccessor.Kind.CONTACTS_LIST
            is ContactsComponent.Child.Info -> AppleRootAccessor.Kind.CONTACT_INFO
            is ContactsComponent.Child.Create -> AppleRootAccessor.Kind.CONTACT_CREATE
            is ContactsComponent.Child.Edit -> AppleRootAccessor.Kind.CONTACT_EDIT
        }
    }
}
