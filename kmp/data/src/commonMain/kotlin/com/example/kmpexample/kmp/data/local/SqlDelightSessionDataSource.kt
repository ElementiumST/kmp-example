package com.example.kmpexample.kmp.data.local

import com.example.kmpexample.kmp.data.db.AppDatabase
import com.example.kmpexample.kmp.data.logging.DataLogger
import com.example.kmpexample.kmp.domain.model.PersistedAuthSession

class SqlDelightSessionDataSource(
    private val database: AppDatabase,
) : LocalSessionDataSource {
    override fun currentPersistedSession(): PersistedAuthSession? {
        DataLogger.db("selectSession: SELECT * FROM auth_session WHERE id = '$AUTH_SESSION_ID'")
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
        DataLogger.db(
            "upsertSession: UPSERT auth_session(id='$AUTH_SESSION_ID', sessionId='${session.sessionId}', loginToken='${session.loginToken ?: ""}')",
        )
        database.appDatabaseQueries.upsertSession(
            id = AUTH_SESSION_ID,
            sessionId = session.sessionId,
            loginToken = session.loginToken,
        )
    }

    override suspend fun clear() {
        DataLogger.db("clearSession: DELETE FROM auth_session WHERE id = '$AUTH_SESSION_ID'")
        database.appDatabaseQueries.clearSession()
    }

    private companion object {
        private const val AUTH_SESSION_ID = "active"
    }
}
