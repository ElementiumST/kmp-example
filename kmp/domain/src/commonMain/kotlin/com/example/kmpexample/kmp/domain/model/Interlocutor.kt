package com.example.kmpexample.kmp.domain.model

import com.example.kmpexample.kmp.tools.bridge.annotations.BridgeModel

/**
 * Result row of the interlocutors search (people to add as contacts).
 */
@BridgeModel(name = "InterlocutorItem")
data class Interlocutor(
    val profileId: String,
    val contactId: String,
    val name: String,
    val email: String,
    val phone: String,
    val avatarUrl: String,
    val interlocutorType: String,
    val externalDomainHost: String,
    val externalDomainName: String,
    val isInContacts: Boolean,
)

/**
 * Pagination offsets per interlocutor source. Matches the kom search contract:
 * FOREIGN_CONTACT, COMPANY_CONTACT, DIRECTORY_USER, DOMAIN_USER.
 */
data class InterlocutorsQuery(
    val searchCriteria: String,
    val limit: Int,
    val foreignOffset: Int = 0,
    val companyOffset: Int = 0,
    val directoryOffset: Int = 0,
    val domainOffset: Int = 0,
)

data class InterlocutorsPage(
    val items: List<Interlocutor>,
    val nextForeignOffset: Int,
    val nextCompanyOffset: Int,
    val nextDirectoryOffset: Int,
    val nextDomainOffset: Int,
    val hasMore: Boolean,
)