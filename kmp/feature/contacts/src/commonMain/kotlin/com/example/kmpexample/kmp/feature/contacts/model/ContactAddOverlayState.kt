package com.example.kmpexample.kmp.feature.contacts.model

import com.example.kmpexample.kmp.domain.model.Interlocutor
import com.example.kmpexample.kmp.tools.bridge.annotations.BridgeModel

@BridgeModel
data class ContactAddOverlayState(
    val query: String = "",
    val items: List<Interlocutor> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val foreignOffset: Int = 0,
    val companyOffset: Int = 0,
    val directoryOffset: Int = 0,
    val domainOffset: Int = 0,
    val hasMore: Boolean = false,
    val invitingProfileIds: Set<String> = emptySet(),
)