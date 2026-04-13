package com.example.kmpexample.kmp.domain.repository

import com.example.kmpexample.kmp.domain.model.AuthSession
import com.example.kmpexample.kmp.domain.model.LoginRequest

interface AuthRepository {
    suspend fun login(request: LoginRequest): AuthSession
}
