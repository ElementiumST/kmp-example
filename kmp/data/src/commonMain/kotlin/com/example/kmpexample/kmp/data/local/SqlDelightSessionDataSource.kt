package com.example.kmpexample.kmp.data.local

import com.example.kmpexample.kmp.data.db.AppDatabase
import com.example.kmpexample.kmp.domain.model.PersistedAuthSession

class SqlDelightSessionDataSource(
    private val database: AppDatabase,
) : LocalSessionDataSource {
    override fun currentPersistedSession(): PersistedAuthSession? {
        return database.appDatabaseQueries
            .selectSession()
            .executeAsOneOrNull()
            ?.let { storedSession ->
                PersistedAuthSession(
                    sessionId = storedSession.sessionId,
                    loginToken = storedSession.loginToken,
                )
            }
    }

    override suspend fun savePersistedSession(session: PersistedAuthSession) {
        database.appDatabaseQueries.upsertSession(
            id = AUTH_SESSION_ID,
            sessionId = session.sessionId,
            loginToken = session.loginToken,
        )
    }

    override suspend fun clear() {
        database.appDatabaseQueries.clearSession()
    }

    private companion object {
        private const val AUTH_SESSION_ID = "active"
    }
}
