package com.example.kmpexample.kmp.data.local

interface LocalSessionDataSource {
    fun currentSessionId(): String?

    suspend fun saveSessionId(sessionId: String)

    suspend fun clear()
}
