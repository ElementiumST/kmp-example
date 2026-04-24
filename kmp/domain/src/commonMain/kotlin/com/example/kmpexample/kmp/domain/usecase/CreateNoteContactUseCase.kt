package com.example.kmpexample.kmp.domain.usecase

import com.example.kmpexample.kmp.domain.model.Contact
import com.example.kmpexample.kmp.domain.model.ContactDraft
import com.example.kmpexample.kmp.domain.repository.ContactsRepository

class CreateNoteContactUseCase(
    private val repository: ContactsRepository,
) {
    suspend operator fun invoke(draft: ContactDraft): Contact? {
        return repository.createNoteContact(draft)
    }
}
