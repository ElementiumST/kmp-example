package com.example.kmpexample.kmp.domain.usecase

import com.example.kmpexample.kmp.domain.model.ContactsPage
import com.example.kmpexample.kmp.domain.repository.ContactsRepository

class GetContactsUseCase(
    private val repository: ContactsRepository,
) {
    suspend operator fun invoke(query: String, offset: Int, limit: Int): ContactsPage {
        return repository.getContacts(query, offset, limit)
    }
}
