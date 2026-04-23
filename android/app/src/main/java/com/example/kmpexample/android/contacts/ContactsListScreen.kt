package com.example.kmpexample.android.contacts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kmpexample.android.contacts.components.ContactRow
import com.example.kmpexample.kmp.domain.model.ContactPresence
import com.example.kmpexample.kmp.feature.contacts.component.ContactsComponent
import com.example.kmpexample.kmp.feature.contacts.model.ContactsListAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsListScreen(component: ContactsComponent) {
    val state by component.state.collectAsState()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    state.snackbarMessage?.let { msg ->
        LaunchedEffect(msg) {
            snackbarHostState.showSnackbar(msg)
            component.onAction(ContactsListAction.DismissSnackbar)
        }
    }

    val reachedBottom by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            lastVisible >= state.items.size - 5
        }
    }
    LaunchedEffect(reachedBottom, state.hasMore, state.items.size) {
        if (reachedBottom && state.hasMore && !state.isLoadingMore) {
            component.onAction(ContactsListAction.LoadMore)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Контакты") })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { component.onAction(ContactsListAction.OpenAddOverlay) },
                text = { Text("Добавить") },
                icon = { Text("+", style = MaterialTheme.typography.titleLarge) },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = state.query,
                onValueChange = { component.onAction(ContactsListAction.UpdateQuery(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                label = { Text("Поиск") },
            )

            if (state.isLoading && state.items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.items.isEmpty()) {
                EmptyContacts(component)
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(state.items, key = { it.contactId.ifEmpty { it.profileId.ifEmpty { it.name } } }) { contact ->
                            val index = state.items.indexOf(contact)
                            val presence = state.presence[contact.profileId] ?: ContactPresence.UNKNOWN
                            ContactRow(
                                contact = contact,
                                presence = presence,
                                onClick = { component.onAction(ContactsListAction.OpenInfo(index)) },
                                onLongClick = { component.onAction(ContactsListAction.OpenContextMenu(index)) },
                            )

                            if (state.contextMenuContactIndex == index) {
                                ContactContextMenu(component, index, contact.isNote, contact.profileId.isNotEmpty())
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
            }
        }
    }

    if (state.isAddOverlayVisible) {
        ContactAddOverlaySheet(
            state = state.addOverlay,
            onDismiss = { component.onAction(ContactsListAction.CloseAddOverlay) },
            onQueryChange = { component.onAction(ContactsListAction.UpdateAddOverlayQuery(it)) },
            onLoadMore = { component.onAction(ContactsListAction.LoadMoreAddOverlay) },
            onInvite = { component.onAction(ContactsListAction.InviteInterlocutor(it)) },
            onCreateNote = {
                component.onAction(ContactsListAction.CloseAddOverlay)
                component.onAction(ContactsListAction.OpenCreate)
            },
        )
    }

    state.errorMessage?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
        }
    }
}

@Composable
private fun EmptyContacts(component: ContactsComponent) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Пока нет контактов", style = MaterialTheme.typography.titleMedium)
            Text(
                "Нажмите «Добавить», чтобы найти собеседников или создать личную заметку.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ContactContextMenu(
    component: ContactsComponent,
    index: Int,
    isNote: Boolean,
    hasProfile: Boolean,
) {
    DropdownMenu(
        expanded = true,
        onDismissRequest = { component.onAction(ContactsListAction.CloseContextMenu) },
    ) {
        DropdownMenuItem(
            text = { Text("Открыть") },
            onClick = {
                component.onAction(ContactsListAction.CloseContextMenu)
                component.onAction(ContactsListAction.OpenInfo(index))
            },
        )
        if (hasProfile && !isNote) {
            DropdownMenuItem(
                text = { Text("Сообщение") },
                onClick = {
                    component.onAction(ContactsListAction.CloseContextMenu)
                    component.onAction(ContactsListAction.WriteMessage(index))
                },
            )
            DropdownMenuItem(
                text = { Text("Аудиозвонок") },
                onClick = {
                    component.onAction(ContactsListAction.CloseContextMenu)
                    component.onAction(ContactsListAction.CallAudio(index))
                },
            )
            DropdownMenuItem(
                text = { Text("Видеозвонок") },
                onClick = {
                    component.onAction(ContactsListAction.CloseContextMenu)
                    component.onAction(ContactsListAction.CallVideo(index))
                },
            )
        }
        DropdownMenuItem(
            text = { Text("Изменить") },
            onClick = {
                component.onAction(ContactsListAction.CloseContextMenu)
                component.onAction(ContactsListAction.OpenEdit(index))
            },
        )
        DropdownMenuItem(
            text = { Text("Удалить") },
            onClick = {
                component.onAction(ContactsListAction.CloseContextMenu)
                component.onAction(ContactsListAction.DeleteFromMenu(index))
            },
        )
    }
}
