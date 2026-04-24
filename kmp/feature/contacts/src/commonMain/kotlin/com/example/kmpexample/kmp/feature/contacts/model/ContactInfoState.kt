package com.example.kmpexample.kmp.feature.contacts.model

import com.example.kmpexample.kmp.domain.model.Contact
import com.example.kmpexample.kmp.tools.mvi.annotations.GenerateMviActionWrappers

data class ContactInfoState(
    val contact: Contact,
    val isExtraExpanded: Boolean = false,
    val isDeleting: Boolean = false,
    val isInviting: Boolean = false,
    val errorMessage: String? = null,
    val snackbarMessage: String? = null,
) {
    val isCallButtonsVisible: Boolean
        get() = !contact.isNote && contact.profileId.isNotEmpty()

    val isDeleteVisible: Boolean
        get() = contact.contactId.isNotEmpty()

    val isAddToContactsVisible: Boolean
        get() = !contact.isInContacts && contact.profileId.isNotEmpty()
}

@GenerateMviActionWrappers
sealed interface ContactInfoAction {
    data object ToggleExtra : ContactInfoAction

    data object Back : ContactInfoAction

    data object Edit : ContactInfoAction

    data object Delete : ContactInfoAction

    data object Invite : ContactInfoAction

    data object WriteMessage : ContactInfoAction

    data object AudioCall : ContactInfoAction

    data object VideoCall : ContactInfoAction

    data object DismissSnackbar : ContactInfoAction
}
