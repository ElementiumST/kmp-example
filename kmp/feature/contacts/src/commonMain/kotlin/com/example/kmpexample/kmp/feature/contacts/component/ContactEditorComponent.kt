package com.example.kmpexample.kmp.feature.contacts.component

import com.example.kmpexample.kmp.feature.base.MviComponent
import com.example.kmpexample.kmp.feature.contacts.model.ContactEditorAction
import com.example.kmpexample.kmp.feature.contacts.model.ContactEditorState

interface ContactEditorComponent : MviComponent<ContactEditorState, ContactEditorAction> {
    fun updateName(value: String) = onAction(ContactEditorAction.UpdateName(value))

    fun updateEmail(value: String) = onAction(ContactEditorAction.UpdateEmail(value))

    fun updatePhone(value: String) = onAction(ContactEditorAction.UpdatePhone(value))

    fun updateNote(value: String) = onAction(ContactEditorAction.UpdateNote(value))

    fun updateTags(value: String) = onAction(ContactEditorAction.UpdateTags(value))

    fun save() = onAction(ContactEditorAction.Save)

    fun back() = onAction(ContactEditorAction.Back)

    fun confirmLeave() = onAction(ContactEditorAction.ConfirmLeave)

    fun cancelLeave() = onAction(ContactEditorAction.CancelLeave)
}
