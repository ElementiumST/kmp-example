package com.example.kmpexample.kmp.feature.contacts.component

import com.arkivanov.decompose.ComponentContext
import com.example.kmpexample.kmp.domain.model.Contact
import com.example.kmpexample.kmp.domain.usecase.DeleteContactUseCase
import com.example.kmpexample.kmp.domain.usecase.InviteContactUseCase
import com.example.kmpexample.kmp.feature.base.BaseMviComponent
import com.example.kmpexample.kmp.feature.contacts.model.ContactInfoAction
import com.example.kmpexample.kmp.feature.contacts.model.ContactInfoState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DefaultContactInfoComponent(
    componentContext: ComponentContext,
    contact: Contact,
    private val deleteContactUseCase: DeleteContactUseCase,
    private val inviteContactUseCase: InviteContactUseCase,
    private val onBack: () -> Unit,
    private val onEdit: (Contact) -> Unit,
    private val onDeleted: (String) -> Unit,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : BaseMviComponent<ContactInfoState, ContactInfoAction>(
    initialState = ContactInfoState(contact = contact),
    coroutineScope = coroutineScope,
),
    ContactInfoComponent,
    ComponentContext by componentContext {

    override fun onAction(action: ContactInfoAction) {
        when (action) {
            ContactInfoAction.ToggleExtra -> mutableState.update { it.copy(isExtraExpanded = !it.isExtraExpanded) }
            ContactInfoAction.Back -> onBack()
            ContactInfoAction.Edit -> onEdit(mutableState.value.contact)
            ContactInfoAction.Delete -> performDelete()
            ContactInfoAction.Invite -> performInvite()
            ContactInfoAction.WriteMessage -> notifyStub("Сообщения недоступны в этой сборке")
            ContactInfoAction.AudioCall -> notifyStub("Аудиозвонки недоступны в этой сборке")
            ContactInfoAction.VideoCall -> notifyStub("Видеозвонки недоступны в этой сборке")
            ContactInfoAction.DismissSnackbar -> mutableState.update { it.copy(snackbarMessage = null) }
        }
    }

    private fun performDelete() {
        val current = mutableState.value
        if (current.isDeleting || current.contact.contactId.isEmpty()) return
        mutableState.update { it.copy(isDeleting = true, errorMessage = null) }
        coroutineScope.launch {
            runCatching { deleteContactUseCase(current.contact.contactId) }
                .onSuccess { onDeleted(current.contact.contactId) }
                .onFailure { throwable ->
                    mutableState.update {
                        it.copy(
                            isDeleting = false,
                            errorMessage = throwable.message ?: "Не удалось удалить контакт",
                        )
                    }
                }
        }
    }

    private fun performInvite() {
        val current = mutableState.value
        if (current.isInviting || current.contact.profileId.isEmpty()) return
        mutableState.update { it.copy(isInviting = true, errorMessage = null) }
        coroutineScope.launch {
            runCatching { inviteContactUseCase(current.contact.profileId) }
                .onSuccess {
                    mutableState.update {
                        it.copy(
                            isInviting = false,
                            snackbarMessage = "Приглашение отправлено",
                        )
                    }
                }
                .onFailure { throwable ->
                    mutableState.update {
                        it.copy(
                            isInviting = false,
                            errorMessage = throwable.message ?: "Не удалось отправить приглашение",
                        )
                    }
                }
        }
    }

    private fun notifyStub(message: String) {
        mutableState.update { it.copy(snackbarMessage = message) }
    }
}

private inline fun <T> kotlinx.coroutines.flow.MutableStateFlow<T>.update(block: (T) -> T) {
    value = block(value)
}
