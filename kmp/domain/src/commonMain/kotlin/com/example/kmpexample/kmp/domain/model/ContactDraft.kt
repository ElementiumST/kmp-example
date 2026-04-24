package com.example.kmpexample.kmp.domain.model

import com.example.kmpexample.kmp.tools.bridge.annotations.BridgeModel

/**
 * Form data used to create or update a contact.
 * `isNote = true` enables the fields `email`, `phone`, `name` (editable identity),
 * while non-note contacts only allow updating `note` and `tags`.
 */
@BridgeModel
data class ContactDraft(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val note: String = "",
    val tagsText: String = "",
) {
    fun tags(): List<String> = splitTags(tagsText)

    companion object {
        fun splitTags(tagsText: String): List<String> {
            return tagsText
                .split(',')
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .distinct()
        }
    }
}