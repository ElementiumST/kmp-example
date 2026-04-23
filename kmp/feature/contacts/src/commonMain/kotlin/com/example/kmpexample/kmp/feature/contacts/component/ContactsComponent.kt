package com.example.kmpexample.kmp.feature.contacts.component

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.example.kmpexample.kmp.feature.base.StateSubscription
import com.example.kmpexample.kmp.feature.base.MviComponent
import com.example.kmpexample.kmp.feature.contacts.model.ContactsListAction
import com.example.kmpexample.kmp.feature.contacts.model.ContactsListState

/**
 * Top-level component of the contacts feature. The `list` MVI surface drives the
 * root screen (search + list + FAB + add-overlay). Nested screens (info/create/edit)
 * are exposed via [childStack].
 */
interface ContactsComponent : MviComponent<ContactsListState, ContactsListAction> {
    val childStack: Value<ChildStack<*, Child>>
    fun currentChildKind(): ContactsChildKind
    fun watchChildKind(observer: (ContactsChildKind) -> Unit): StateSubscription
    fun currentInfoComponentOrNull(): ContactInfoComponent?
    fun currentCreateComponentOrNull(): ContactEditorComponent?
    fun currentEditComponentOrNull(): ContactEditorComponent?

    fun refresh() = onAction(ContactsListAction.Refresh)

    fun loadMore() = onAction(ContactsListAction.LoadMore)

    fun updateQuery(value: String) = onAction(ContactsListAction.UpdateQuery(value))

    fun openInfo(contactIndex: Int) = onAction(ContactsListAction.OpenInfo(contactIndex))

    fun openAddOverlay() = onAction(ContactsListAction.OpenAddOverlay)

    fun closeAddOverlay() = onAction(ContactsListAction.CloseAddOverlay)

    fun openCreate() = onAction(ContactsListAction.OpenCreate)

    fun openEdit(contactIndex: Int) = onAction(ContactsListAction.OpenEdit(contactIndex))

    fun deleteFromMenu(contactIndex: Int) = onAction(ContactsListAction.DeleteFromMenu(contactIndex))

    fun updateAddOverlayQuery(value: String) = onAction(ContactsListAction.UpdateAddOverlayQuery(value))

    fun loadMoreAddOverlay() = onAction(ContactsListAction.LoadMoreAddOverlay)

    fun inviteInterlocutor(profileId: String) = onAction(ContactsListAction.InviteInterlocutor(profileId))

    sealed interface Child {
        data object List : Child

        data class Info(val component: ContactInfoComponent) : Child

        data class Create(val component: ContactEditorComponent) : Child

        data class Edit(val component: ContactEditorComponent) : Child
    }
}
