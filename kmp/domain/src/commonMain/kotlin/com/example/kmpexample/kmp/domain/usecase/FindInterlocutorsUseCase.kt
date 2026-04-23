package com.example.kmpexample.kmp.domain.usecase

import com.example.kmpexample.kmp.domain.model.InterlocutorsPage
import com.example.kmpexample.kmp.domain.model.InterlocutorsQuery
import com.example.kmpexample.kmp.domain.repository.ContactsRepository

class FindInterlocutorsUseCase(
    private val repository: ContactsRepository,
) {
    suspend operator fun invoke(query: InterlocutorsQuery): InterlocutorsPage {
        return repository.findInterlocutors(query)
    }
}
