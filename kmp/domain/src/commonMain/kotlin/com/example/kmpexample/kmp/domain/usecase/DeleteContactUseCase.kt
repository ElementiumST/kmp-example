package com.example.kmpexample.kmp.domain.usecase

import com.example.kmpexample.kmp.domain.repository.ContactsRepository

class DeleteContactUseCase(
    private val repository: ContactsRepository,
) {
    suspend operator fun invoke(contactId: String) {
        repository.deleteContact(contactId)
    }
}
