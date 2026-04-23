package com.example.kmpexample.kmp.feature.contacts.component

import com.arkivanov.decompose.ComponentContext
import com.example.kmpexample.kmp.domain.model.Contact
import com.example.kmpexample.kmp.domain.model.ContactDraft
import com.example.kmpexample.kmp.domain.model.validate
import com.example.kmpexample.kmp.domain.usecase.CreateNoteContactUseCase
import com.example.kmpexample.kmp.domain.usecase.UpdateContactUseCase
import com.example.kmpexample.kmp.feature.base.BaseMviComponent
import com.example.kmpexample.kmp.feature.contacts.model.ContactEditorAction
import com.example.kmpexample.kmp.feature.contacts.model.ContactEditorMode
import com.example.kmpexample.kmp.feature.contacts.model.ContactEditorState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class DefaultContactEditorComponent(
    componentContext: ComponentContext,
    mode: ContactEditorMode,
    isNote: Boolean,
    private val contactId: String?,
    initialDraft: ContactDraft,
    private val createNoteContactUseCase: CreateNoteContactUseCase?,
    private val updateContactUseCase: UpdateContactUseCase?,
    private val onBack: () -> Unit,
    private val onSaved: (Contact) -> Unit,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : BaseMviComponent<ContactEditorState, ContactEditorAction>(
    initialState = ContactEditorState(
        mode = mode,
        isNote = isNote,
        draft = initialDraft,
        validation = initialDraft.validate(isNote),
        isDirty = false,
    ),
    coroutineScope = coroutineScope,
),
    ContactEditorComponent,
    ComponentContext by componentContext {

    private val baselineDraft: ContactDraft = initialDraft

    override fun onAction(action: ContactEditorAction) {
        when (action) {
            is ContactEditorAction.UpdateName -> updateDraft { it.copy(name = action.value) }
            is ContactEditorAction.UpdateEmail -> updateDraft { it.copy(email = action.value) }
            is ContactEditorAction.UpdatePhone -> updateDraft { it.copy(phone = action.value) }
            is ContactEditorAction.UpdateNote -> updateDraft { it.copy(note = action.value) }
            is ContactEditorAction.UpdateTags -> updateDraft { it.copy(tagsText = action.value) }
            ContactEditorAction.Save -> performSave()
            ContactEditorAction.Back -> handleBack()
            ContactEditorAction.ConfirmLeave -> {
                mutableState.setVal(mutableState.value.copy(showLeaveConfirmation = false))
                onBack()
            }
            ContactEditorAction.CancelLeave -> mutableState.setVal(
                mutableState.value.copy(showLeaveConfirmation = false),
            )
        }
    }

    private fun updateDraft(transform: (ContactDraft) -> ContactDraft) {
        val current = mutableState.value
        val nextDraft = transform(current.draft)
        val validation = nextDraft.validate(current.isNote)
        val isDirty = nextDraft != baselineDraft
        mutableState.setVal(
            current.copy(
                draft = nextDraft,
                validation = validation,
                isDirty = isDirty,
                errorMessage = null,
            ),
        )
    }

    private fun handleBack() {
        val current = mutableState.value
        if (current.isDirty) {
            mutableState.setVal(current.copy(showLeaveConfirmation = true))
        } else {
            onBack()
        }
    }

    private fun performSave() {
        val current = mutableState.value
        if (!current.canSave) return

        mutableState.setVal(current.copy(isSaving = true, errorMessage = null))
        coroutineScope.launch {
            val result = runCatching {
                when (current.mode) {
                    ContactEditorMode.CREATE -> {
                        val uc = requireNotNull(createNoteContactUseCase)
                        uc(current.draft)
                    }
                    ContactEditorMode.EDIT -> {
                        val uc = requireNotNull(updateContactUseCase)
                        val id = requireNotNull(contactId)
                        uc(id, current.isNote, current.draft)
                    }
                }
            }
            result
                .onSuccess { contact ->
                    mutableState.setVal(
                        mutableState.value.copy(
                            isSaving = false,
                            isDirty = false,
                            dismissed = true,
                        ),
                    )
                    onSaved(contact)
                }
                .onFailure { throwable ->
                    mutableState.setVal(
                        mutableState.value.copy(
                            isSaving = false,
                            errorMessage = throwable.message ?: "Не удалось сохранить контакт",
                        ),
                    )
                }
        }
    }
}

private fun <T> MutableStateFlow<T>.setVal(next: T) {
    value = next
}
