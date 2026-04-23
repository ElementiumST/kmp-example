package com.example.kmpexample.kmp.feature.contacts.model

import com.example.kmpexample.kmp.domain.model.ContactDraft
import com.example.kmpexample.kmp.domain.model.ContactValidation

enum class ContactEditorMode { CREATE, EDIT }

data class ContactEditorState(
    val mode: ContactEditorMode,
    val isNote: Boolean,
    val draft: ContactDraft = ContactDraft(),
    val validation: ContactValidation = ContactValidation(),
    val isDirty: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val showLeaveConfirmation: Boolean = false,
    val dismissed: Boolean = false,
) {
    val canSave: Boolean
        get() = !isSaving && isDirty && validation.isValid
}

sealed interface ContactEditorAction {
    data class UpdateName(val value: String) : ContactEditorAction

    data class UpdateEmail(val value: String) : ContactEditorAction

    data class UpdatePhone(val value: String) : ContactEditorAction

    data class UpdateNote(val value: String) : ContactEditorAction

    data class UpdateTags(val value: String) : ContactEditorAction

    data object Save : ContactEditorAction

    data object Back : ContactEditorAction

    data object ConfirmLeave : ContactEditorAction

    data object CancelLeave : ContactEditorAction
}
