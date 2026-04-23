package com.example.kmpexample.kmp.domain.model

data class ContactsPage(
    val items: List<Contact>,
    val total: Int,
    val offset: Int,
    val limit: Int,
) {
    val hasMore: Boolean
        get() = offset + items.size < total
}
