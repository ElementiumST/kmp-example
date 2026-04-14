package com.example.kmpexample.kmp.domain.usecase

import com.example.kmpexample.kmp.domain.model.AuthSession
import com.example.kmpexample.kmp.domain.repository.AuthRepository

class LoginWithTokenUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(token: String): AuthSession {
        return authRepository.loginWithToken(token)
    }
}
