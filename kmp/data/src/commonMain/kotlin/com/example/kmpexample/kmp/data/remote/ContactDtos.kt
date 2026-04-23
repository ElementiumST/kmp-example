package com.example.kmpexample.kmp.data.remote

import com.example.kmpexample.kmp.domain.model.Contact
import com.example.kmpexample.kmp.domain.model.ContactPresence
import com.example.kmpexample.kmp.domain.model.Interlocutor
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiContactDto(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val interlocutorType: String = "",
    val profile: ApiProfileDto = ApiProfileDto(),
    val contact: ApiContactRecordDto = ApiContactRecordDto(),
    val externalInfo: ApiExternalInfoDto = ApiExternalInfoDto(),
) {
    fun toDomain(avatarBaseUrl: String): Contact {
        return Contact(
            contactId = contact.contactId,
            profileId = profile.profileId,
            name = name,
            email = email,
            phone = phone,
            note = contact.note,
            tags = contact.tags,
            interlocutorType = interlocutorType,
            avatarUrl = buildAvatarUrl(avatarBaseUrl, profile.avatarResourceId),
            aboutSelf = profile.aboutSelf,
            additionalContact = profile.additionalContact,
            externalDomainHost = externalInfo.externalDomainHost,
            externalDomainName = externalInfo.externalDomainName,
            presence = ContactPresence.UNKNOWN,
        )
    }

    fun toInterlocutor(avatarBaseUrl: String, knownContactIds: Set<String>): Interlocutor {
        val inContacts = contact.contactId.isNotEmpty() || knownContactIds.contains(profile.profileId)
        return Interlocutor(
            profileId = profile.profileId,
            contactId = contact.contactId,
            name = name,
            email = email,
            phone = phone,
            avatarUrl = buildAvatarUrl(avatarBaseUrl, profile.avatarResourceId),
            interlocutorType = interlocutorType,
            externalDomainHost = externalInfo.externalDomainHost,
            externalDomainName = externalInfo.externalDomainName,
            isInContacts = inContacts,
        )
    }
}

@Serializable
data class ApiProfileDto(
    val profileId: String = "",
    val avatarResourceId: String = "",
    val aboutSelf: String = "",
    val additionalContact: String = "",
)

@Serializable
data class ApiContactRecordDto(
    val contactId: String = "",
    val note: String = "",
    val tags: List<String> = emptyList(),
)

@Serializable
data class ApiExternalInfoDto(
    val externalDomainHost: String = "",
    val externalDomainName: String = "",
)

@Serializable
data class ApiContactsResponse(
    @SerialName("data")
    private val data: List<ApiContactDto> = emptyList(),
    @SerialName("items")
    private val legacyItems: List<ApiContactDto> = emptyList(),
    @SerialName("totalCount")
    private val totalCount: Int = 0,
    @SerialName("total")
    private val legacyTotal: Int = 0,
    val hasNext: Boolean = false,
) {
    val items: List<ApiContactDto>
        get() = if (data.isNotEmpty()) data else legacyItems

    val total: Int
        get() = if (totalCount != 0 || hasNext || items.isNotEmpty()) totalCount else legacyTotal
}

@Serializable
data class ApiContactsPresenceDto(
    val profileId: String = "",
    val presenceStatus: String = "",
)

@Serializable
data class ApiNullableString(val contents: String)

@Serializable
data class ApiNullableStringArray(val contents: List<String>)

@Serializable
data class ApiCreateNoteContactBody(
    val name: String,
    val email: String? = null,
    val phone: String? = null,
    val note: String,
    val tags: List<String>,
)

@Serializable
data class ApiContactUpdateBody(
    val name: String,
    val email: ApiNullableString,
    val phone: ApiNullableString,
    val note: ApiNullableString,
    val tags: ApiNullableStringArray,
)

@Serializable
data class ApiContactNoteUpdateBody(
    val note: ApiNullableString,
    val tags: ApiNullableStringArray,
)

@Serializable
data class ApiInterlocutorSourceRequest(
    val interlocutorType: String,
    val offset: Int,
)

@Serializable
data class ApiInterlocutorSourceResponse(
    val interlocutorType: String = "",
    val total: Int = 0,
    val offset: Int = 0,
)

@Serializable
data class ApiInterlocutorsFindRequest(
    val searchCriteria: String,
    val limit: Int,
    val sources: List<ApiInterlocutorSourceRequest>,
)

@Serializable
data class ApiInterlocutorsFindResponse(
    @SerialName("data")
    private val data: List<ApiContactDto> = emptyList(),
    @SerialName("items")
    private val legacyItems: List<ApiContactDto> = emptyList(),
    val sources: List<ApiInterlocutorSourceResponse> = emptyList(),
) {
    val items: List<ApiContactDto>
        get() = if (data.isNotEmpty()) data else legacyItems
}

/** Build `https://host/services/resource?resourceId=<id>&width=100` like kom. */
internal fun buildAvatarUrl(originUrl: String, resourceId: String?): String {
    if (resourceId.isNullOrEmpty()) return ""
    return "$originUrl/services/resource?resourceId=$resourceId&width=100"
}
