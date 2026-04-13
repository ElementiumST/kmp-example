package com.example.kmpexample.kmp.data.session

interface SessionStore {
    fun currentSessionId(): String?

    suspend fun saveSessionId(sessionId: String)

    suspend fun clear()
}
