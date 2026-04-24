package com.example.kmpexample.kmp.domain.usecase

import com.example.kmpexample.kmp.domain.model.Contact
import com.example.kmpexample.kmp.domain.model.ContactDraft
import com.example.kmpexample.kmp.domain.repository.ContactsRepository

class UpdateContactUseCase(
    private val repository: ContactsRepository,
) {
    suspend operator fun invoke(contactId: String, isNote: Boolean, draft: ContactDraft): Contact? {
        return repository.updateContact(contactId, isNote, draft)
    }
}
