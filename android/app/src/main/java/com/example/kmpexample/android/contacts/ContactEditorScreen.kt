package com.example.kmpexample.android.contacts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kmpexample.kmp.domain.model.ContactFieldError
import com.example.kmpexample.kmp.feature.contacts.component.ContactEditorComponent
import com.example.kmpexample.kmp.feature.contacts.model.ContactEditorAction
import com.example.kmpexample.kmp.feature.contacts.model.ContactEditorMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactEditorScreen(component: ContactEditorComponent) {
    val state by component.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.mode == ContactEditorMode.CREATE) "Новая заметка" else "Редактирование",
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { component.onAction(ContactEditorAction.Back) }) {
                        Text("←", style = MaterialTheme.typography.titleLarge)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (state.isNote) {
                OutlinedTextField(
                    value = state.draft.name,
                    onValueChange = { component.onAction(ContactEditorAction.UpdateName(it)) },
                    label = { Text("Имя") },
                    isError = state.validation.name != null,
                    supportingText = { state.validation.name?.let { Text(fieldErrorMessage(it)) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.draft.email,
                    onValueChange = { component.onAction(ContactEditorAction.UpdateEmail(it)) },
                    label = { Text("Email") },
                    isError = state.validation.email != null,
                    supportingText = { state.validation.email?.let { Text(fieldErrorMessage(it)) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.draft.phone,
                    onValueChange = { component.onAction(ContactEditorAction.UpdatePhone(it)) },
                    label = { Text("Телефон / VVoIP") },
                    isError = state.validation.phone != null,
                    supportingText = { state.validation.phone?.let { Text(fieldErrorMessage(it)) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            OutlinedTextField(
                value = state.draft.note,
                onValueChange = { component.onAction(ContactEditorAction.UpdateNote(it)) },
                label = { Text("Комментарий") },
                isError = state.validation.note != null,
                supportingText = { state.validation.note?.let { Text(fieldErrorMessage(it)) } },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
            )

            OutlinedTextField(
                value = state.draft.tagsText,
                onValueChange = { component.onAction(ContactEditorAction.UpdateTags(it)) },
                label = { Text("Теги (через запятую)") },
                isError = state.validation.tags != null,
                supportingText = { state.validation.tags?.let { Text(fieldErrorMessage(it)) } },
                modifier = Modifier.fillMaxWidth(),
            )

            state.errorMessage?.let { error ->
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Button(
                onClick = { component.onAction(ContactEditorAction.Save) },
                enabled = state.canSave,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isSaving) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.padding(2.dp))
                    }
                } else {
                    Text(if (state.mode == ContactEditorMode.CREATE) "Создать" else "Сохранить")
                }
            }
        }
    }

    if (state.showLeaveConfirmation) {
        AlertDialog(
            onDismissRequest = { component.onAction(ContactEditorAction.CancelLeave) },
            title = { Text("Выйти без сохранения?") },
            text = { Text("Изменения будут потеряны.") },
            confirmButton = {
                TextButton(onClick = { component.onAction(ContactEditorAction.ConfirmLeave) }) {
                    Text("Выйти")
                }
            },
            dismissButton = {
                TextButton(onClick = { component.onAction(ContactEditorAction.CancelLeave) }) {
                    Text("Отмена")
                }
            },
        )
    }
}

private fun fieldErrorMessage(error: ContactFieldError): String = when (error) {
    ContactFieldError.EMPTY -> "Поле обязательно"
    ContactFieldError.TOO_LONG -> "Превышена максимальная длина"
    ContactFieldError.INVALID_FORMAT -> "Неверный формат"
}
