package com.example.kmpexample.kmp.data.local

import com.example.kmpexample.kmp.domain.model.PersistedAuthSession

interface LocalSessionDataSource {
    fun currentPersistedSession(): PersistedAuthSession?

    suspend fun savePersistedSession(session: PersistedAuthSession)

    suspend fun clear()
}
