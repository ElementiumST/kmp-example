package com.example.kmpexample.kmp.data.session

import com.example.kmpexample.kmp.data.local.LocalSessionDataSource

class DefaultSessionStore(
    private val localSessionDataSource: LocalSessionDataSource,
) : SessionStore {
    private var sessionId: String? = localSessionDataSource.currentSessionId()

    override fun currentSessionId(): String? = sessionId

    override suspend fun saveSessionId(sessionId: String) {
        localSessionDataSource.saveSessionId(sessionId)
        this.sessionId = sessionId
    }

    override suspend fun clear() {
        localSessionDataSource.clear()
        sessionId = null
    }
}
