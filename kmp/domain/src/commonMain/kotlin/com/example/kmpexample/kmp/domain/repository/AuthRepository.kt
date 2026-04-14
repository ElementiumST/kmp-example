package com.example.kmpexample.kmp.domain.repository

import com.example.kmpexample.kmp.domain.model.AuthSession
import com.example.kmpexample.kmp.domain.model.LoginRequest
import com.example.kmpexample.kmp.domain.model.PersistedAuthSession

interface AuthRepository {
    suspend fun login(request: LoginRequest): AuthSession

    suspend fun loginWithToken(token: String): AuthSession

    fun currentPersistedSession(): PersistedAuthSession?

    suspend fun clearPersistedSession()
}
