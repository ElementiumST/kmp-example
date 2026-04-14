package com.example.kmpexample.kmp.domain.usecase

import com.example.kmpexample.kmp.domain.model.PersistedAuthSession
import com.example.kmpexample.kmp.domain.repository.AuthRepository

class GetPersistedAuthSessionUseCase(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(): PersistedAuthSession? {
        return authRepository.currentPersistedSession()
    }
}
