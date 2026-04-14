package com.example.kmpexample.kmp.data.local

import com.example.kmpexample.kmp.domain.model.PersistedAuthSession

class InMemorySessionDataSource : LocalSessionDataSource {
    private var session: PersistedAuthSession? = null

    override fun currentPersistedSession(): PersistedAuthSession? = session

    override suspend fun savePersistedSession(session: PersistedAuthSession) {
        this.session = session
    }

    override suspend fun clear() {
        session = null
    }
}
