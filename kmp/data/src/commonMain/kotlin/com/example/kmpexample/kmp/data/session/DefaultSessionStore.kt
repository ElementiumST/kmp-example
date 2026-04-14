package com.example.kmpexample.kmp.data.session

import com.example.kmpexample.kmp.data.local.LocalSessionDataSource
import com.example.kmpexample.kmp.domain.model.PersistedAuthSession

class DefaultSessionStore(
    private val localSessionDataSource: LocalSessionDataSource,
) : SessionStore {
    private var session: PersistedAuthSession? = localSessionDataSource.currentPersistedSession()

    override fun currentSessionId(): String? = session?.sessionId

    override fun currentPersistedSession(): PersistedAuthSession? = session

    override suspend fun savePersistedSession(session: PersistedAuthSession) {
        localSessionDataSource.savePersistedSession(session)
        this.session = session
    }

    override suspend fun clear() {
        localSessionDataSource.clear()
        session = null
    }
}
