package com.example.kmpexample.kmp.data.remote

import com.example.kmpexample.kmp.data.config.NetworkConfig
import com.example.kmpexample.kmp.domain.model.Contact
import com.example.kmpexample.kmp.domain.model.ContactDraft
import com.example.kmpexample.kmp.domain.model.ContactPresence
import com.example.kmpexample.kmp.domain.model.ContactsPage
import com.example.kmpexample.kmp.domain.model.InterlocutorsPage
import com.example.kmpexample.kmp.domain.model.InterlocutorsQuery
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class KtorContactsRemoteDataSource(
    private val httpClient: HttpClient,
    private val networkConfig: NetworkConfig,
) : RemoteContactsDataSource {
    private val contactsPath = "${networkConfig.baseUrl}/contacts"
    private val interlocutorsPath = "${networkConfig.baseUrl}/interlocutors/find"
    private val avatarOrigin = networkConfig.originUrl

    override suspend fun getContacts(query: String, offset: Int, limit: Int): ContactsPage {
        val response = httpClient.get(contactsPath) {
            parameter("searchCriteria", query)
            parameter("offset", offset)
            parameter("limit", limit)
        }.body<ApiContactsResponse>()
        return ContactsPage(
            items = response.items.map { it.toDomain(avatarOrigin) },
            total = response.total,
            offset = offset,
            limit = limit,
        )
    }

    override suspend fun createNoteContact(draft: ContactDraft): Contact {
        val body = ApiCreateNoteContactBody(
            name = draft.name,
            email = draft.email.ifEmpty { null },
            phone = draft.phone.ifEmpty { null },
            note = draft.note,
            tags = draft.tags(),
        )
        val dto = httpClient.post("$contactsPath/create-note") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body<ApiContactDto>()
        return dto.toDomain(avatarOrigin)
    }

    override suspend fun updateContact(contactId: String, isNote: Boolean, draft: ContactDraft): Contact {
        val url = "$contactsPath/$contactId"
        val dto: ApiContactDto = if (isNote) {
            val body = ApiContactUpdateBody(
                name = draft.name,
                email = ApiNullableString(draft.email),
                phone = ApiNullableString(draft.phone),
                note = ApiNullableString(draft.note),
                tags = ApiNullableStringArray(draft.tags()),
            )
            httpClient.patch(url) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }.body()
        } else {
            val body = ApiContactNoteUpdateBody(
                note = ApiNullableString(draft.note),
                tags = ApiNullableStringArray(draft.tags()),
            )
            httpClient.patch(url) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }.body()
        }
        return dto.toDomain(avatarOrigin)
    }

    override suspend fun deleteContact(contactId: String) {
        httpClient.delete("$contactsPath/$contactId")
    }

    override suspend fun invite(profileId: String) {
        httpClient.post("$contactsPath/invite") {
            contentType(ContentType.Application.Json)
            setBody(listOf(profileId))
        }
    }

    override suspend fun findInterlocutors(
        query: InterlocutorsQuery,
        knownContactIds: Set<String>,
    ): InterlocutorsPage {
        val body = ApiInterlocutorsFindRequest(
            searchCriteria = query.searchCriteria,
            limit = query.limit,
            sources = listOf(
                ApiInterlocutorSourceRequest("FOREIGN_CONTACT", query.foreignOffset),
                ApiInterlocutorSourceRequest("COMPANY_CONTACT", query.companyOffset),
                ApiInterlocutorSourceRequest("DIRECTORY_USER", query.directoryOffset),
                ApiInterlocutorSourceRequest("DOMAIN_USER", query.domainOffset),
            ),
        )
        val response = httpClient.post(interlocutorsPath) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body<ApiInterlocutorsFindResponse>()

        fun offsetFor(type: String, default: Int): Int = response.sources
            .firstOrNull { it.interlocutorType == type }
            ?.offset
            ?: default

        val items = response.items.map { it.toInterlocutor(avatarOrigin, knownContactIds) }
        val totalBySource = response.sources.sumOf { it.total }
        val nextForeign = offsetFor("FOREIGN_CONTACT", query.foreignOffset)
        val nextCompany = offsetFor("COMPANY_CONTACT", query.companyOffset)
        val nextDirectory = offsetFor("DIRECTORY_USER", query.directoryOffset)
        val nextDomain = offsetFor("DOMAIN_USER", query.domainOffset)
        val hasMore = items.isNotEmpty() && (nextForeign + nextCompany + nextDirectory + nextDomain) < totalBySource

        return InterlocutorsPage(
            items = items,
            nextForeignOffset = nextForeign,
            nextCompanyOffset = nextCompany,
            nextDirectoryOffset = nextDirectory,
            nextDomainOffset = nextDomain,
            hasMore = hasMore,
        )
    }

    override suspend fun findPresences(profileIds: List<String>): Map<String, ContactPresence> {
        if (profileIds.isEmpty()) return emptyMap()
        val presences: List<ApiContactsPresenceDto> = httpClient.post(
            "$contactsPath/presences/find-for-users",
        ) {
            contentType(ContentType.Application.Json)
            setBody(profileIds)
        }.body()
        return presences.asSequence()
            .filter { it.profileId.isNotEmpty() }
            .associate { it.profileId to ContactPresence.fromApi(it.presenceStatus) }
    }
}
