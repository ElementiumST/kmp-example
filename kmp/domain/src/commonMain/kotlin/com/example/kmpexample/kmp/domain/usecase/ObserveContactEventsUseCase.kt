package com.example.kmpexample.kmp.domain.usecase

import com.example.kmpexample.kmp.domain.model.ContactEvent
import com.example.kmpexample.kmp.domain.repository.ContactsRepository
import kotlinx.coroutines.flow.Flow

class ObserveContactEventsUseCase(
    private val repository: ContactsRepository,
) {
    operator fun invoke(): Flow<ContactEvent> {
        return repository.observeContactEvents()
    }
}
