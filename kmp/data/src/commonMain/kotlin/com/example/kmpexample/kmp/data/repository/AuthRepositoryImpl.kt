package com.example.kmpexample.kmp.data.repository

import com.example.kmpexample.kmp.data.remote.RemoteAuthDataSource
import com.example.kmpexample.kmp.data.session.SessionStore
import com.example.kmpexample.kmp.domain.model.AuthSession
import com.example.kmpexample.kmp.domain.model.LoginRequest
import com.example.kmpexample.kmp.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val remoteAuthDataSource: RemoteAuthDataSource,
    private val sessionStore: SessionStore,
) : AuthRepository {
    override suspend fun login(request: LoginRequest): AuthSession {
        return remoteAuthDataSource.login(request).also { session ->
            sessionStore.saveSessionId(session.sessionId)
        }
    }
}
