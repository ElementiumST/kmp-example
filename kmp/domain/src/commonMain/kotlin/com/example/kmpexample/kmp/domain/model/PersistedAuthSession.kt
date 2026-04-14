package com.example.kmpexample.kmp.domain.model

data class PersistedAuthSession(
    val sessionId: String,
    val loginToken: String?,
)
