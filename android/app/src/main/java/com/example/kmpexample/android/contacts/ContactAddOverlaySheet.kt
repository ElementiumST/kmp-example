package com.example.kmpexample.android.contacts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kmpexample.android.contacts.components.Avatar
import com.example.kmpexample.kmp.feature.contacts.model.ContactAddOverlayState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactAddOverlaySheet(
    state: ContactAddOverlayState,
    onDismiss: () -> Unit,
    onQueryChange: (String) -> Unit,
    onLoadMore: () -> Unit,
    onInvite: (String) -> Unit,
    onCreateNote: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val listState = rememberLazyListState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text("Добавить контакт", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                singleLine = true,
                label = { Text("Поиск собеседников") },
            )

            TextButton(onClick = onCreateNote) {
                Text("Создать личную заметку")
            }

            HorizontalDivider()

            val reachedBottom by remember {
                derivedStateOf {
                    val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
                    lastVisible >= state.items.size - 5
                }
            }
            LaunchedEffect(reachedBottom, state.hasMore, state.items.size) {
                if (reachedBottom && state.hasMore && !state.isLoadingMore) {
                    onLoadMore()
                }
            }

            if (state.isLoading && state.items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(state = listState) {
                    items(state.items, key = { it.profileId.ifEmpty { it.name } }) { interlocutor ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Avatar(name = interlocutor.name, avatarUrl = interlocutor.avatarUrl)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 12.dp),
                            ) {
                                Text(interlocutor.name.ifEmpty { "(без имени)" }, style = MaterialTheme.typography.titleMedium)
                                val sub = interlocutor.email.ifEmpty { interlocutor.phone.ifEmpty { interlocutor.externalDomainName } }
                                if (sub.isNotEmpty()) {
                                    Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            val inviting = state.invitingProfileIds.contains(interlocutor.profileId)
                            Button(
                                onClick = { onInvite(interlocutor.profileId) },
                                enabled = !interlocutor.isInContacts && !inviting && interlocutor.profileId.isNotEmpty(),
                            ) {
                                Text(
                                    when {
                                        inviting -> "..."
                                        interlocutor.isInContacts -> "В контактах"
                                        else -> "Пригласить"
                                    },
                                )
                            }
                        }
                        HorizontalDivider()
                    }

                    if (state.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }

            state.errorMessage?.let { message ->
                Text(
                    message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}
