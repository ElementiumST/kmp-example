package com.example.kmpexample.android.contacts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kmpexample.android.contacts.components.Avatar
import com.example.kmpexample.kmp.feature.contacts.component.ContactInfoComponent
import com.example.kmpexample.kmp.feature.contacts.model.ContactInfoAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactInfoScreen(component: ContactInfoComponent) {
    val state by component.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val contact = state.contact

    state.snackbarMessage?.let { msg ->
        LaunchedEffect(msg) {
            snackbarHostState.showSnackbar(msg)
            component.onAction(ContactInfoAction.DismissSnackbar)
        }
    }
    state.errorMessage?.let { msg ->
        LaunchedEffect(msg) {
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(contact.name.ifEmpty { "Контакт" }) },
                navigationIcon = {
                    IconButton(onClick = { component.onAction(ContactInfoAction.Back) }) {
                        Text("←", style = MaterialTheme.typography.titleLarge)
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(name = contact.name, avatarUrl = contact.avatarUrl, size = 72)
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    Text(contact.name.ifEmpty { "(без имени)" }, style = MaterialTheme.typography.titleLarge)
                    if (contact.isNote) {
                        Text("Личная заметка", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else if (contact.externalDomainName.isNotEmpty()) {
                        Text(contact.externalDomainName, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            HorizontalDivider()

            InfoRow(label = "Email", value = contact.email)
            InfoRow(label = "Телефон / VVoIP", value = contact.phone)
            InfoRow(label = "Комментарий", value = contact.note)
            if (contact.tags.isNotEmpty()) {
                InfoRow(label = "Теги", value = contact.tags.joinToString(", "))
            }
            if (state.isExtraExpanded) {
                InfoRow(label = "О себе", value = contact.aboutSelf)
                InfoRow(label = "Доп. контакт", value = contact.additionalContact)
            }

            TextButton(onClick = { component.onAction(ContactInfoAction.ToggleExtra) }) {
                Text(if (state.isExtraExpanded) "Скрыть подробности" else "Показать подробности")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                if (state.isCallButtonsVisible) {
                    OutlinedButton(
                        onClick = { component.onAction(ContactInfoAction.AudioCall) },
                        modifier = Modifier.weight(1f),
                    ) { Text("Аудио") }
                    OutlinedButton(
                        onClick = { component.onAction(ContactInfoAction.VideoCall) },
                        modifier = Modifier.weight(1f),
                    ) { Text("Видео") }
                    OutlinedButton(
                        onClick = { component.onAction(ContactInfoAction.WriteMessage) },
                        modifier = Modifier.weight(1f),
                    ) { Text("Сообщение") }
                }
            }

            Button(
                onClick = { component.onAction(ContactInfoAction.Edit) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Редактировать")
            }

            if (state.isAddToContactsVisible) {
                OutlinedButton(
                    onClick = { component.onAction(ContactInfoAction.Invite) },
                    enabled = !state.isInviting,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (state.isInviting) "Отправка..." else "Добавить в контакты")
                }
            }

            if (state.isDeleteVisible) {
                OutlinedButton(
                    onClick = { component.onAction(ContactInfoAction.Delete) },
                    enabled = !state.isDeleting,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (state.isDeleting) "Удаление..." else "Удалить контакт")
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    if (value.isEmpty()) return
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
