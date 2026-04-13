package com.example.kmpexample.kmp.data.local

import com.example.kmpexample.kmp.data.db.AppDatabase

class SqlDelightSessionDataSource(
    private val database: AppDatabase,
) : LocalSessionDataSource {
    override fun currentSessionId(): String? {
        return database.appDatabaseQueries
            .selectSession()
            .executeAsOneOrNull()
            ?.sessionId
    }

    override suspend fun saveSessionId(sessionId: String) {
        database.appDatabaseQueries.upsertSession(
            id = AUTH_SESSION_ID,
            sessionId = sessionId,
        )
    }

    override suspend fun clear() {
        database.appDatabaseQueries.clearSession()
    }

    private companion object {
        private const val AUTH_SESSION_ID = "active"
    }
}
