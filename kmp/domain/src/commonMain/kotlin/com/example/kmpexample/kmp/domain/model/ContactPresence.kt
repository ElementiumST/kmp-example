package com.example.kmpexample.kmp.domain.model

enum class ContactPresence {
    ONLINE,
    OFFLINE,
    UNAVAILABLE,
    DO_NOT_DISTURB,
    IN_CALL,
    IN_EVENT,
    UNKNOWN;

    companion object {
        fun fromApi(value: String?): ContactPresence = when (value?.uppercase()) {
            "ONLINE" -> ONLINE
            "OFFLINE" -> OFFLINE
            "UNAVAILABLE" -> UNAVAILABLE
            "DO_NOT_DISTURB" -> DO_NOT_DISTURB
            "IN_CALL" -> IN_CALL
            "IN_EVENT" -> IN_EVENT
            else -> UNKNOWN
        }
    }
}
