package com.example.kmpexample.kmp.data.remote

import com.example.kmpexample.kmp.domain.model.AuthSession
import com.example.kmpexample.kmp.domain.model.LoginRequest

interface RemoteAuthDataSource {
    suspend fun login(request: LoginRequest): AuthSession
}
