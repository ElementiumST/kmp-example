package com.example.kmpexample.kmp.domain.model

sealed interface ContactEvent {
    data class Changed(val contact: Contact) : ContactEvent

    data class Online(
        val profileId: String,
        val presence: ContactPresence,
    ) : ContactEvent

    data class Removed(val contactIds: List<String>) : ContactEvent
}
