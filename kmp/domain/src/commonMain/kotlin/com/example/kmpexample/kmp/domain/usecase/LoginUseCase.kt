package com.example.kmpexample.kmp.domain.usecase

import com.example.kmpexample.kmp.domain.model.AuthSession
import com.example.kmpexample.kmp.domain.model.LoginRequest
import com.example.kmpexample.kmp.domain.repository.AuthRepository

class LoginUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(request: LoginRequest): AuthSession {
        return authRepository.login(request)
    }
}
