package com.example.kmpexample.kmp.data.remote

import com.example.kmpexample.kmp.domain.model.Contact
import com.example.kmpexample.kmp.domain.model.ContactDraft
import com.example.kmpexample.kmp.domain.model.ContactPresence
import com.example.kmpexample.kmp.domain.model.ContactsPage
import com.example.kmpexample.kmp.domain.model.InterlocutorsPage
import com.example.kmpexample.kmp.domain.model.InterlocutorsQuery

interface RemoteContactsDataSource {
    suspend fun getContacts(query: String, offset: Int, limit: Int): ContactsPage

    suspend fun createNoteContact(draft: ContactDraft): Contact

    suspend fun updateContact(contactId: String, isNote: Boolean, draft: ContactDraft): Contact

    suspend fun deleteContact(contactId: String)

    suspend fun invite(profileId: String)

    suspend fun findInterlocutors(query: InterlocutorsQuery, knownContactIds: Set<String>): InterlocutorsPage

    suspend fun findPresences(profileIds: List<String>): Map<String, ContactPresence>
}
