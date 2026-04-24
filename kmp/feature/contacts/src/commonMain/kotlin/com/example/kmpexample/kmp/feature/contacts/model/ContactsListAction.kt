package com.example.kmpexample.kmp.feature.contacts.model

import com.example.kmpexample.kmp.tools.mvi.annotations.GenerateMviActionWrappers

@GenerateMviActionWrappers
sealed interface ContactsListAction {
    data object Refresh : ContactsListAction

    data object LoadMore : ContactsListAction

    data class UpdateQuery(val value: String) : ContactsListAction

    data class OpenInfo(val contactIndex: Int) : ContactsListAction

    data class OpenContextMenu(val contactIndex: Int) : ContactsListAction

    data object CloseContextMenu : ContactsListAction

    data object OpenAddOverlay : ContactsListAction

    data object CloseAddOverlay : ContactsListAction

    data object OpenCreate : ContactsListAction

    data class OpenEdit(val contactIndex: Int) : ContactsListAction

    data class DeleteFromMenu(val contactIndex: Int) : ContactsListAction

    data class CallAudio(val contactIndex: Int) : ContactsListAction

    data class CallVideo(val contactIndex: Int) : ContactsListAction

    data class WriteMessage(val contactIndex: Int) : ContactsListAction

    data object DismissSnackbar : ContactsListAction

    // Add overlay
    data class UpdateAddOverlayQuery(val value: String) : ContactsListAction

    data object LoadMoreAddOverlay : ContactsListAction

    data class InviteInterlocutor(val profileId: String) : ContactsListAction
}
