package com.example.kmpexample.kmp.domain.model

import com.example.kmpexample.kmp.tools.bridge.annotations.BridgeModel

/**
 * Domain-level representation of a contact.
 *
 * A contact may be either a platform user (`interlocutorType != "NOTE_CONTACT"`, has a `profileId`)
 * or a personal note record (`isNote == true`, no profile/presence).
 */
@BridgeModel(name = "ContactItem")
data class Contact(
    val contactId: String,
    val profileId: String,
    val name: String,
    val email: String,
    val phone: String,
    val note: String,
    val tags: List<String>,
    val interlocutorType: String,
    val avatarUrl: String,
    val aboutSelf: String,
    val additionalContact: String,
    val externalDomainHost: String,
    val externalDomainName: String,
    val presence: ContactPresence,
) {
    val isNote: Boolean
        get() = interlocutorType == NOTE_CONTACT

    val isInContacts: Boolean
        get() = contactId.isNotEmpty()

    companion object {
        const val NOTE_CONTACT = "NOTE_CONTACT"
    }
}