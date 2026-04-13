package com.example.kmpexample.kmp.domain.model

data class AuthSession(
    val sessionId: String,
    val user: UserProfile,
    val clientIp: String?,
    val hasAdminAccess: Boolean,
    val timestamp: Long,
    val lastSystemInfoChangedAt: Long?,
    val lastSystemMediaInfoChangedAt: Long?,
    val loginToken: String?,
)

data class UserProfile(
    val profileId: String,
    val login: String,
    val userType: String,
    val securityLevel: String,
    val avatarResourceId: String?,
    val name: String?,
    val email: ContactValue?,
    val phone: ContactValue?,
    val additionalContact: ContactValue?,
    val aboutSelf: String?,
    val isRegisteredUser: Boolean,
    val companyId: String?,
    val companyName: String?,
    val isConferenceCreationEnabled: Boolean,
    val locale: String?,
    val timeZone: String?,
    val isTimeZoneAutoSelection: Boolean,
    val externalId: String?,
    val ldapInfo: LdapInfo?,
    val disabledFeatures: List<String>,
    val customStatus: CustomStatus?,
    val hasExternalPrivateOffice: Boolean,
    val profileStatus: String?,
    val profileStatusResetAt: Long?,
)

data class ContactValue(
    val value: String,
    val privacy: String,
    val usageRule: String,
)

data class LdapInfo(
    val ldapUserId: String,
    val targets: List<String>,
)

data class CustomStatus(
    val statusText: String?,
)
