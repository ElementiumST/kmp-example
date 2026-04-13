package com.example.kmpexample.kmp.data.local

class InMemorySessionDataSource : LocalSessionDataSource {
    private var sessionId: String? = null

    override fun currentSessionId(): String? = sessionId

    override suspend fun saveSessionId(sessionId: String) {
        this.sessionId = sessionId
    }

    override suspend fun clear() {
        sessionId = null
    }
}
