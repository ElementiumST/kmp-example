package com.example.kmpexample.kmp.domain.usecase

import com.example.kmpexample.kmp.domain.repository.AuthRepository

class ClearPersistedAuthSessionUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke() {
        authRepository.clearPersistedSession()
    }
}
