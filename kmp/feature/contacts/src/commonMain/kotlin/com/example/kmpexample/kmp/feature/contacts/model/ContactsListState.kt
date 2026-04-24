package com.example.kmpexample.kmp.feature.contacts.model

import com.example.kmpexample.kmp.domain.model.Contact
import com.example.kmpexample.kmp.domain.model.ContactPresence
import com.example.kmpexample.kmp.tools.bridge.annotations.BridgeModel

@BridgeModel
data class ContactsListState(
    val query: String = "",
    val items: List<Contact> = emptyList(),
    val total: Int = 0,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val presence: Map<String, ContactPresence> = emptyMap(),
    val isAddOverlayVisible: Boolean = false,
    val addOverlay: ContactAddOverlayState = ContactAddOverlayState(),
    val contextMenuContactIndex: Int = -1,
    val snackbarMessage: String? = null,
) {
    val hasMore: Boolean
        get() = items.size < total
}