package com.example.kmpexample.kmp.domain.repository

import com.example.kmpexample.kmp.domain.model.Contact
import com.example.kmpexample.kmp.domain.model.ContactDraft
import com.example.kmpexample.kmp.domain.model.ContactEvent
import com.example.kmpexample.kmp.domain.model.ContactPresence
import com.example.kmpexample.kmp.domain.model.ContactsPage
import com.example.kmpexample.kmp.domain.model.InterlocutorsPage
import com.example.kmpexample.kmp.domain.model.InterlocutorsQuery
import kotlinx.coroutines.flow.Flow

interface ContactsRepository {
    suspend fun getContacts(query: String, offset: Int, limit: Int): ContactsPage

    suspend fun createNoteContact(draft: ContactDraft): Contact?

    suspend fun updateContact(contactId: String, isNote: Boolean, draft: ContactDraft): Contact?

    suspend fun deleteContact(contactId: String)

    suspend fun invite(profileId: String)

    suspend fun findInterlocutors(query: InterlocutorsQuery): InterlocutorsPage

    suspend fun findPresences(profileIds: List<String>): Map<String, ContactPresence>

    /** Stream of WebSocket-originated contact mutations. Implementation may lazily connect. */
    fun observeContactEvents(): Flow<ContactEvent>
}
