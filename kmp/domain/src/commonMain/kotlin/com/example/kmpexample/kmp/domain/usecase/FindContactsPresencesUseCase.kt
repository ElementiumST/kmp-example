package com.example.kmpexample.kmp.domain.usecase

import com.example.kmpexample.kmp.domain.model.ContactPresence
import com.example.kmpexample.kmp.domain.repository.ContactsRepository

class FindContactsPresencesUseCase(
    private val repository: ContactsRepository,
) {
    suspend operator fun invoke(profileIds: List<String>): Map<String, ContactPresence> {
        if (profileIds.isEmpty()) return emptyMap()
        return repository.findPresences(profileIds)
    }
}
