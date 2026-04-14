package com.example.kmpexample.kmp.data.remote

import com.example.kmpexample.kmp.domain.model.AuthSession
import com.example.kmpexample.kmp.domain.model.ContactValue
import com.example.kmpexample.kmp.domain.model.CustomStatus
import com.example.kmpexample.kmp.domain.model.LdapInfo
import com.example.kmpexample.kmp.domain.model.UserProfile
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val login: String,
    val password: String,
    val rememberMe: Boolean,
)

@Serializable
data class LoginResponseDto(
    val sessionId: String,
    val user: UserProfileDto,
    val clientIp: String? = null,
    val hasAdminAccess: Boolean = false,
    val timestamp: Long = 0,
    val lastSystemInfoChangedAt: Long? = null,
    val lastSystemMediaInfoChangedAt: Long? = null,
    val loginToken: String? = null,
) {
    fun toDomain(): AuthSession {
        return AuthSession(
            sessionId = sessionId,
            user = user.toDomain(),
            clientIp = clientIp,
            hasAdminAccess = hasAdminAccess,
            timestamp = timestamp,
            lastSystemInfoChangedAt = lastSystemInfoChangedAt,
            lastSystemMediaInfoChangedAt = lastSystemMediaInfoChangedAt,
            loginToken = loginToken,
        )
    }
}

@Serializable
data class UserProfileDto(
    val profileId: String,
    val login: String,
    val userType: String,
    val securityLevel: String,
    val avatarResourceId: String? = null,
    val name: String? = null,
    val email: ContactValueDto? = null,
    val phone: ContactValueDto? = null,
    val additionalContact: ContactValueDto? = null,
    val aboutSelf: String? = null,
    val isRegisteredUser: Boolean = false,
    val companyId: String? = null,
    val companyName: String? = null,
    val isConferenceCreationEnabled: Boolean = false,
    val locale: String? = null,
    val timeZone: String? = null,
    val isTimeZoneAutoSelection: Boolean = false,
    val externalId: String? = null,
    val ldapInfo: LdapInfoDto? = null,
    val disabledFeatures: List<String> = emptyList(),
    val customStatus: CustomStatusDto? = null,
    val hasExternalPrivateOffice: Boolean = false,
    val profileStatus: String? = null,
    val profileStatusResetAt: Long? = null,
) {
    fun toDomain(): UserProfile {
        return UserProfile(
            profileId = profileId,
            login = login,
            userType = userType,
            securityLevel = securityLevel,
            avatarResourceId = avatarResourceId,
            name = name,
            email = email?.toDomain(),
            phone = phone?.toDomain(),
            additionalContact = additionalContact?.toDomain(),
            aboutSelf = aboutSelf,
            isRegisteredUser = isRegisteredUser,
            companyId = companyId,
            companyName = companyName,
            isConferenceCreationEnabled = isConferenceCreationEnabled,
            locale = locale,
            timeZone = timeZone,
            isTimeZoneAutoSelection = isTimeZoneAutoSelection,
            externalId = externalId,
            ldapInfo = ldapInfo?.toDomain(),
            disabledFeatures = disabledFeatures,
            customStatus = customStatus?.toDomain(),
            hasExternalPrivateOffice = hasExternalPrivateOffice,
            profileStatus = profileStatus,
            profileStatusResetAt = profileStatusResetAt,
        )
    }
}

@Serializable
data class ContactValueDto(
    val value: String,
    val privacy: String,
    val usageRule: String = "",
) {
    fun toDomain(): ContactValue {
        return ContactValue(
            value = value,
            privacy = privacy,
            usageRule = usageRule,
        )
    }
}

@Serializable
data class LdapInfoDto(
    val ldapUserId: String,
    val targets: List<String> = emptyList(),
) {
    fun toDomain(): LdapInfo {
        return LdapInfo(
            ldapUserId = ldapUserId,
            targets = targets,
        )
    }
}

@Serializable
data class CustomStatusDto(
    @SerialName("statusText")
    val statusText: String? = null,
) {
    fun toDomain(): CustomStatus {
        return CustomStatus(statusText = statusText)
    }
}
