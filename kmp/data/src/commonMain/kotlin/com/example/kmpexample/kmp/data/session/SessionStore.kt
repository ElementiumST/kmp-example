package com.example.kmpexample.kmp.data.session

import com.example.kmpexample.kmp.domain.model.PersistedAuthSession

interface SessionStore {
    fun currentSessionId(): String?

    fun currentPersistedSession(): PersistedAuthSession?

    suspend fun savePersistedSession(session: PersistedAuthSession)

    suspend fun clear()
}
