package com.example.kmpexample.kmp.domain.usecase

import com.example.kmpexample.kmp.domain.repository.ContactsRepository

class InviteContactUseCase(
    private val repository: ContactsRepository,
) {
    suspend operator fun invoke(profileId: String) {
        repository.invite(profileId)
    }
}
