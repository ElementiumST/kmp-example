package com.example.kmpexample.kmp.data.repository

import com.example.kmpexample.kmp.data.remote.ContactsWebSocketClient
import com.example.kmpexample.kmp.data.remote.RemoteContactsDataSource
import com.example.kmpexample.kmp.domain.model.Contact
import com.example.kmpexample.kmp.domain.model.ContactDraft
import com.example.kmpexample.kmp.domain.model.ContactEvent
import com.example.kmpexample.kmp.domain.model.ContactPresence
import com.example.kmpexample.kmp.domain.model.ContactsPage
import com.example.kmpexample.kmp.domain.model.InterlocutorsPage
import com.example.kmpexample.kmp.domain.model.InterlocutorsQuery
import com.example.kmpexample.kmp.domain.repository.ContactsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart

class ContactsRepositoryImpl(
    private val remote: RemoteContactsDataSource,
    private val webSocketClient: ContactsWebSocketClient,
) : ContactsRepository {
    private val knownContactIds = mutableSetOf<String>()

    override suspend fun getContacts(query: String, offset: Int, limit: Int): ContactsPage {
        val page = remote.getContacts(query, offset, limit)
        if (offset == 0) {
            knownContactIds.clear()
        }
        knownContactIds += page.items.map { it.contactId }.filter { it.isNotEmpty() }
        return page
    }

    override suspend fun createNoteContact(draft: ContactDraft): Contact {
        val contact = remote.createNoteContact(draft)
        if (contact.contactId.isNotEmpty()) {
            knownContactIds += contact.contactId
        }
        return contact
    }

    override suspend fun updateContact(contactId: String, isNote: Boolean, draft: ContactDraft): Contact {
        return remote.updateContact(contactId, isNote, draft)
    }

    override suspend fun deleteContact(contactId: String) {
        remote.deleteContact(contactId)
        knownContactIds -= contactId
    }

    override suspend fun invite(profileId: String) {
        remote.invite(profileId)
    }

    override suspend fun findInterlocutors(query: InterlocutorsQuery): InterlocutorsPage {
        return remote.findInterlocutors(query, knownContactIds.toSet())
    }

    override suspend fun findPresences(profileIds: List<String>): Map<String, ContactPresence> {
        return remote.findPresences(profileIds)
    }

    override fun observeContactEvents(): Flow<ContactEvent> {
        return flow {
            webSocketClient.acquire()
            try {
                webSocketClient.flow.collect { emit(it) }
            } finally {
                webSocketClient.release()
            }
        }.onStart { /* acquire counted above */ }.onCompletion { /* release counted above */ }
    }
}
