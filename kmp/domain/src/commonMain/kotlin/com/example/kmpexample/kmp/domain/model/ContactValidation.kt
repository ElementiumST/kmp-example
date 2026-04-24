package com.example.kmpexample.kmp.domain.model

import com.example.kmpexample.kmp.tools.bridge.annotations.BridgeModel

enum class ContactFieldError {
    EMPTY,
    TOO_LONG,
    INVALID_FORMAT,
}

@BridgeModel
data class ContactValidation(
    val name: ContactFieldError? = null,
    val email: ContactFieldError? = null,
    val phone: ContactFieldError? = null,
    val note: ContactFieldError? = null,
    val tags: ContactFieldError? = null,
) {
    val isValid: Boolean
        get() = name == null && email == null && phone == null && note == null && tags == null
}

/**
 * Validate a contact draft. `isNote = true` requires a non-empty name.
 * For non-note contacts (server-managed identity) only `note`/`tags` length limits apply.
 */
fun ContactDraft.validate(isNote: Boolean): ContactValidation {
    val nameErr = when {
        isNote && name.isBlank() -> ContactFieldError.EMPTY
        name.length > ContactLimits.MAX_CHARS_NAME -> ContactFieldError.TOO_LONG
        else -> null
    }
    val emailErr = when {
        !isNote -> null
        email.length > ContactLimits.MAX_CHARS_EMAIL -> ContactFieldError.TOO_LONG
        email.isNotEmpty() && !isValidEmailFormat(email) -> ContactFieldError.INVALID_FORMAT
        else -> null
    }
    val phoneErr = when {
        !isNote -> null
        phone.length > ContactLimits.MAX_CHARS_PHONE -> ContactFieldError.TOO_LONG
        phone.isNotEmpty() && !isValidContactFormat(phone) -> ContactFieldError.INVALID_FORMAT
        else -> null
    }
    val noteErr = if (note.length > ContactLimits.MAX_CHARS_COMMENT) ContactFieldError.TOO_LONG else null
    val tagsErr = if (tagsText.length > ContactLimits.MAX_CHARS_TAGS) ContactFieldError.TOO_LONG else null
    return ContactValidation(nameErr, emailErr, phoneErr, noteErr, tagsErr)
}

private fun isValidEmailFormat(email: String): Boolean {
    val regex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    return regex.matches(email) && !email.contains("'")
}

private fun isValidContactFormat(input: String): Boolean {
    val voipProtocols = listOf("sip:", "sips:", "h323:", "rtsp:", "s4b:", "rtmp:", "vnc:")
    if (voipProtocols.any { input.startsWith(it) }) {
        val address = input.substringAfter(':')
        return address.isNotEmpty()
    }
    if (input.isBlank() || !input.any { it.isDigit() }) return false
    return Regex("^([0-9()+\\- ])*$").matches(input)
}