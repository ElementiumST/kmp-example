package com.example.kmpexample.kmp.feature.contacts.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.subscribe
import com.example.kmpexample.kmp.domain.model.Contact
import com.example.kmpexample.kmp.domain.model.ContactDraft
import com.example.kmpexample.kmp.domain.model.ContactEvent
import com.example.kmpexample.kmp.domain.model.ContactPresence
import com.example.kmpexample.kmp.domain.model.InterlocutorsQuery
import com.example.kmpexample.kmp.domain.usecase.CreateNoteContactUseCase
import com.example.kmpexample.kmp.domain.usecase.DeleteContactUseCase
import com.example.kmpexample.kmp.domain.usecase.FindContactsPresencesUseCase
import com.example.kmpexample.kmp.domain.usecase.FindInterlocutorsUseCase
import com.example.kmpexample.kmp.domain.usecase.GetContactsUseCase
import com.example.kmpexample.kmp.domain.usecase.InviteContactUseCase
import com.example.kmpexample.kmp.domain.usecase.ObserveContactEventsUseCase
import com.example.kmpexample.kmp.domain.usecase.UpdateContactUseCase
import com.example.kmpexample.kmp.feature.base.BaseMviComponent
import com.example.kmpexample.kmp.feature.contacts.model.ContactAddOverlayState
import com.example.kmpexample.kmp.feature.contacts.model.ContactEditorMode
import com.example.kmpexample.kmp.feature.contacts.model.ContactsListAction
import com.example.kmpexample.kmp.feature.contacts.model.ContactsListState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@OptIn(DelicateDecomposeApi::class)
class DefaultContactsComponent(
    componentContext: ComponentContext,
    private val getContactsUseCase: GetContactsUseCase,
    private val createNoteContactUseCase: CreateNoteContactUseCase,
    private val updateContactUseCase: UpdateContactUseCase,
    private val deleteContactUseCase: DeleteContactUseCase,
    private val inviteContactUseCase: InviteContactUseCase,
    private val findInterlocutorsUseCase: FindInterlocutorsUseCase,
    private val findContactsPresencesUseCase: FindContactsPresencesUseCase,
    private val observeContactEventsUseCase: ObserveContactEventsUseCase,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) : BaseMviComponent<ContactsListState, ContactsListAction>(
    initialState = ContactsListState(isLoading = true),
    coroutineScope = coroutineScope,
),
    ContactsComponent,
    ComponentContext by componentContext {

    private val pageLimit = PAGE_LIMIT
    private val addOverlayLimit = ADD_OVERLAY_LIMIT

    private var searchJob: Job? = null
    private var addSearchJob: Job? = null

    private val navigation = StackNavigation<ChildConfig>()

    override val childStack: Value<ChildStack<*, ContactsComponent.Child>> = childStack(
        source = navigation,
        serializer = ChildConfig.serializer(),
        initialConfiguration = ChildConfig.List,
        handleBackButton = true,
        childFactory = ::createChild,
    )

    init {
        loadInitial()
        observeEvents()
    }

    override fun currentChildKind(): ContactsChildKind {
        return when (childStack.value.active.instance) {
            ContactsComponent.Child.List -> ContactsChildKind.LIST
            is ContactsComponent.Child.Info -> ContactsChildKind.INFO
            is ContactsComponent.Child.Create -> ContactsChildKind.CREATE
            is ContactsComponent.Child.Edit -> ContactsChildKind.EDIT
        }
    }

    override fun watchChildKind(observer: (ContactsChildKind) -> Unit): com.example.kmpexample.kmp.feature.base.StateSubscription {
        observer(currentChildKind())
        val cancellation = childStack.subscribe { stack ->
            val kind = when (stack.active.instance) {
                ContactsComponent.Child.List -> ContactsChildKind.LIST
                is ContactsComponent.Child.Info -> ContactsChildKind.INFO
                is ContactsComponent.Child.Create -> ContactsChildKind.CREATE
                is ContactsComponent.Child.Edit -> ContactsChildKind.EDIT
            }
            observer(kind)
        }
        return com.example.kmpexample.kmp.feature.base.StateSubscription { cancellation.cancel() }
    }

    override fun currentInfoComponentOrNull(): ContactInfoComponent? {
        return (childStack.value.active.instance as? ContactsComponent.Child.Info)?.component
    }

    override fun currentCreateComponentOrNull(): ContactEditorComponent? {
        return (childStack.value.active.instance as? ContactsComponent.Child.Create)?.component
    }

    override fun currentEditComponentOrNull(): ContactEditorComponent? {
        return (childStack.value.active.instance as? ContactsComponent.Child.Edit)?.component
    }

    override fun onAction(action: ContactsListAction) {
        when (action) {
            ContactsListAction.Refresh -> loadInitial()
            ContactsListAction.LoadMore -> loadNextPage()
            is ContactsListAction.UpdateQuery -> onQueryChanged(action.value)
            is ContactsListAction.OpenInfo -> handleOpenInfo(action.contactIndex)
            is ContactsListAction.OpenContextMenu -> mutableState.set {
                it.copy(contextMenuContactIndex = action.contactIndex.takeIf { idx -> idx >= 0 && idx < it.items.size } ?: -1)
            }
            ContactsListAction.CloseContextMenu -> mutableState.set { it.copy(contextMenuContactIndex = -1) }
            ContactsListAction.OpenAddOverlay -> handleOpenAddOverlay()
            ContactsListAction.CloseAddOverlay -> mutableState.set {
                it.copy(isAddOverlayVisible = false, addOverlay = ContactAddOverlayState())
            }
            ContactsListAction.OpenCreate -> handleOpenCreate()
            is ContactsListAction.OpenEdit -> handleOpenEdit(action.contactIndex)
            is ContactsListAction.DeleteFromMenu -> handleDeleteFromMenu(action.contactIndex)
            is ContactsListAction.CallAudio -> notifyStub("Аудиозвонки недоступны в этой сборке")
            is ContactsListAction.CallVideo -> notifyStub("Видеозвонки недоступны в этой сборке")
            is ContactsListAction.WriteMessage -> notifyStub("Сообщения недоступны в этой сборке")
            ContactsListAction.DismissSnackbar -> mutableState.set { it.copy(snackbarMessage = null) }
            is ContactsListAction.UpdateAddOverlayQuery -> onAddQueryChanged(action.value)
            ContactsListAction.LoadMoreAddOverlay -> handleLoadMoreAddOverlay()
            is ContactsListAction.InviteInterlocutor -> handleInviteInterlocutor(action.profileId)
        }
    }

    private fun createChild(
        config: ChildConfig,
        childContext: ComponentContext,
    ): ContactsComponent.Child = when (config) {
        ChildConfig.List -> ContactsComponent.Child.List
        is ChildConfig.Info -> {
            val contact = currentState().items.getOrNull(config.contactIndex)
                ?: placeholderContact(config.contactId)
            ContactsComponent.Child.Info(
                component = DefaultContactInfoComponent(
                    componentContext = childContext,
                    contact = contact,
                    deleteContactUseCase = deleteContactUseCase,
                    inviteContactUseCase = inviteContactUseCase,
                    onBack = { navigation.pop() },
                    onEdit = { openEditFromContact(it) },
                    onDeleted = { contactId ->
                        removeContactLocally(contactId)
                        navigation.pop()
                    },
                ),
            )
        }
        is ChildConfig.Create -> ContactsComponent.Child.Create(
            component = DefaultContactEditorComponent(
                componentContext = childContext,
                mode = ContactEditorMode.CREATE,
                isNote = true,
                contactId = null,
                initialDraft = ContactDraft(),
                createNoteContactUseCase = createNoteContactUseCase,
                updateContactUseCase = null,
                onBack = { navigation.pop() },
                onSaved = { contact ->
                    insertOrUpdateContactLocally(contact)
                    navigation.pop()
                },
            ),
        )
        is ChildConfig.Edit -> {
            val contact = currentState().items.getOrNull(config.contactIndex)
                ?: placeholderContact(config.contactId)
            ContactsComponent.Child.Edit(
                component = DefaultContactEditorComponent(
                    componentContext = childContext,
                    mode = ContactEditorMode.EDIT,
                    isNote = contact.isNote,
                    contactId = contact.contactId,
                    initialDraft = ContactDraft(
                        name = contact.name,
                        email = contact.email,
                        phone = contact.phone,
                        note = contact.note,
                        tagsText = contact.tags.joinToString(", "),
                    ),
                    createNoteContactUseCase = null,
                    updateContactUseCase = updateContactUseCase,
                    onBack = { navigation.pop() },
                    onSaved = { updated ->
                        insertOrUpdateContactLocally(updated)
                        navigation.replaceCurrent(ChildConfig.Info(contactIndex = indexOfContact(updated), contactId = updated.contactId))
                    },
                ),
            )
        }
    }

    private fun loadInitial() {
        val query = mutableState.value.query
        mutableState.set {
            it.copy(
                isLoading = true,
                isRefreshing = true,
                errorMessage = null,
            )
        }
        coroutineScope.launch {
            runCatching { getContactsUseCase(query, 0, pageLimit) }
                .onSuccess { page ->
                    mutableState.set {
                        it.copy(
                            items = page.items,
                            total = page.total,
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = null,
                        )
                    }
                    refreshPresences(page.items)
                }
                .onFailure { throwable ->
                    mutableState.set {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = throwable.message ?: "Не удалось загрузить контакты",
                        )
                    }
                }
        }
    }

    private fun loadNextPage() {
        val current = mutableState.value
        if (current.isLoading || current.isLoadingMore || !current.hasMore) return
        mutableState.set { it.copy(isLoadingMore = true, errorMessage = null) }
        coroutineScope.launch {
            runCatching { getContactsUseCase(current.query, current.items.size, pageLimit) }
                .onSuccess { page ->
                    mutableState.set {
                        it.copy(
                            items = it.items + page.items,
                            total = page.total,
                            isLoadingMore = false,
                        )
                    }
                    refreshPresences(page.items)
                }
                .onFailure { throwable ->
                    mutableState.set {
                        it.copy(
                            isLoadingMore = false,
                            errorMessage = throwable.message ?: "Не удалось загрузить ещё",
                        )
                    }
                }
        }
    }

    private fun onQueryChanged(value: String) {
        mutableState.set { it.copy(query = value) }
        searchJob?.cancel()
        searchJob = coroutineScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            loadInitial()
        }
    }

    private fun handleOpenInfo(contactIndex: Int) {
        val contact = currentState().items.getOrNull(contactIndex) ?: return
        navigation.push(ChildConfig.Info(contactIndex = contactIndex, contactId = contact.contactId))
    }

    private fun handleOpenAddOverlay() {
        mutableState.set { it.copy(isAddOverlayVisible = true, addOverlay = ContactAddOverlayState(isLoading = true)) }
        addSearchJob?.cancel()
        addSearchJob = coroutineScope.launch {
            searchInterlocutors(reset = true)
        }
    }

    private fun handleOpenCreate() {
        navigation.push(ChildConfig.Create)
    }

    private fun handleOpenEdit(contactIndex: Int) {
        val contact = currentState().items.getOrNull(contactIndex) ?: return
        navigation.push(ChildConfig.Edit(contactIndex = contactIndex, contactId = contact.contactId))
    }

    private fun openEditFromContact(contact: Contact) {
        val index = indexOfContact(contact)
        navigation.replaceCurrent(ChildConfig.Edit(contactIndex = index, contactId = contact.contactId))
    }

    private fun handleDeleteFromMenu(contactIndex: Int) {
        val contact = currentState().items.getOrNull(contactIndex) ?: return
        if (contact.contactId.isEmpty()) return
        mutableState.set { it.copy(contextMenuContactIndex = -1) }
        coroutineScope.launch {
            runCatching { deleteContactUseCase(contact.contactId) }
                .onSuccess { removeContactLocally(contact.contactId) }
                .onFailure { throwable ->
                    mutableState.set {
                        it.copy(errorMessage = throwable.message ?: "Не удалось удалить контакт")
                    }
                }
        }
    }

    private fun notifyStub(message: String) {
        mutableState.set { it.copy(snackbarMessage = message, contextMenuContactIndex = -1) }
    }

    private fun onAddQueryChanged(value: String) {
        mutableState.set { it.copy(addOverlay = it.addOverlay.copy(query = value)) }
        addSearchJob?.cancel()
        addSearchJob = coroutineScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            searchInterlocutors(reset = true)
        }
    }

    private suspend fun searchInterlocutors(reset: Boolean) {
        val overlay = mutableState.value.addOverlay
        if (reset) {
            mutableState.set {
                it.copy(
                    addOverlay = it.addOverlay.copy(
                        isLoading = true,
                        errorMessage = null,
                        foreignOffset = 0,
                        companyOffset = 0,
                        directoryOffset = 0,
                        domainOffset = 0,
                        hasMore = false,
                    ),
                )
            }
        } else {
            mutableState.set { it.copy(addOverlay = it.addOverlay.copy(isLoadingMore = true, errorMessage = null)) }
        }

        val query = mutableState.value.addOverlay
        val request = InterlocutorsQuery(
            searchCriteria = query.query,
            limit = addOverlayLimit,
            foreignOffset = if (reset) 0 else query.foreignOffset,
            companyOffset = if (reset) 0 else query.companyOffset,
            directoryOffset = if (reset) 0 else query.directoryOffset,
            domainOffset = if (reset) 0 else query.domainOffset,
        )

        runCatching { findInterlocutorsUseCase(request) }
            .onSuccess { page ->
                mutableState.set { state ->
                    val merged = if (reset) page.items else state.addOverlay.items + page.items
                    state.copy(
                        addOverlay = state.addOverlay.copy(
                            items = merged,
                            isLoading = false,
                            isLoadingMore = false,
                            foreignOffset = page.nextForeignOffset,
                            companyOffset = page.nextCompanyOffset,
                            directoryOffset = page.nextDirectoryOffset,
                            domainOffset = page.nextDomainOffset,
                            hasMore = page.hasMore,
                        ),
                    )
                }
            }
            .onFailure { throwable ->
                mutableState.set { state ->
                    state.copy(
                        addOverlay = state.addOverlay.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            errorMessage = throwable.message ?: "Не удалось выполнить поиск",
                        ),
                    )
                }
            }
        // Silence unused reference – ensures overlay variable is read for future extension.
        overlay.query
    }

    private fun handleLoadMoreAddOverlay() {
        val overlay = mutableState.value.addOverlay
        if (overlay.isLoading || overlay.isLoadingMore || !overlay.hasMore) return
        addSearchJob?.cancel()
        addSearchJob = coroutineScope.launch { searchInterlocutors(reset = false) }
    }

    private fun handleInviteInterlocutor(profileId: String) {
        if (profileId.isEmpty()) return
        mutableState.set {
            it.copy(
                addOverlay = it.addOverlay.copy(
                    invitingProfileIds = it.addOverlay.invitingProfileIds + profileId,
                    errorMessage = null,
                ),
            )
        }
        coroutineScope.launch {
            runCatching { inviteContactUseCase(profileId) }
                .onSuccess {
                    mutableState.set { state ->
                        val overlay = state.addOverlay
                        val updated = overlay.items.map { interlocutor ->
                            if (interlocutor.profileId == profileId) {
                                interlocutor.copy(isInContacts = true)
                            } else {
                                interlocutor
                            }
                        }
                        state.copy(
                            addOverlay = overlay.copy(
                                items = updated,
                                invitingProfileIds = overlay.invitingProfileIds - profileId,
                            ),
                            snackbarMessage = "Приглашение отправлено",
                        )
                    }
                    loadInitial()
                }
                .onFailure { throwable ->
                    mutableState.set { state ->
                        state.copy(
                            addOverlay = state.addOverlay.copy(
                                invitingProfileIds = state.addOverlay.invitingProfileIds - profileId,
                                errorMessage = throwable.message ?: "Не удалось отправить приглашение",
                            ),
                        )
                    }
                }
        }
    }

    private fun refreshPresences(contacts: List<Contact>) {
        val profileIds = contacts.asSequence()
            .filter { !it.isNote }
            .map { it.profileId }
            .filter { it.isNotEmpty() }
            .toList()
            .distinct()
        if (profileIds.isEmpty()) return

        coroutineScope.launch {
            runCatching { findContactsPresencesUseCase(profileIds) }
                .onSuccess { presenceMap ->
                    if (presenceMap.isEmpty()) return@onSuccess
                    mutableState.set { state ->
                        state.copy(presence = state.presence + presenceMap)
                    }
                }
        }
    }

    private fun observeEvents() {
        coroutineScope.launch {
            observeContactEventsUseCase().collect { event ->
                when (event) {
                    is ContactEvent.Changed -> mutableState.set { state ->
                        val items = state.items.toMutableList()
                        val index = items.indexOfFirst {
                            (event.contact.contactId.isNotEmpty() && it.contactId == event.contact.contactId) ||
                                (event.contact.profileId.isNotEmpty() && it.profileId == event.contact.profileId)
                        }
                        if (index >= 0) {
                            items[index] = mergeContact(items[index], event.contact)
                        } else if (event.contact.isInContacts) {
                            items.add(0, event.contact)
                        }
                        state.copy(items = items)
                    }
                    is ContactEvent.Online -> mutableState.set { state ->
                        state.copy(presence = state.presence + (event.profileId to event.presence))
                    }
                    is ContactEvent.Removed -> mutableState.set { state ->
                        val ids = event.contactIds.toSet()
                        state.copy(items = state.items.filterNot { ids.contains(it.contactId) })
                    }
                }
            }
        }
    }

    private fun mergeContact(existing: Contact, update: Contact): Contact {
        return existing.copy(
            contactId = update.contactId.ifEmpty { existing.contactId },
            profileId = update.profileId.ifEmpty { existing.profileId },
            name = update.name.ifEmpty { existing.name },
            email = update.email.ifEmpty { existing.email },
            phone = update.phone.ifEmpty { existing.phone },
            note = update.note.ifEmpty { existing.note },
            tags = if (update.tags.isEmpty()) existing.tags else update.tags,
            interlocutorType = update.interlocutorType.ifEmpty { existing.interlocutorType },
            avatarUrl = update.avatarUrl.ifEmpty { existing.avatarUrl },
            aboutSelf = update.aboutSelf.ifEmpty { existing.aboutSelf },
            additionalContact = update.additionalContact.ifEmpty { existing.additionalContact },
            externalDomainHost = update.externalDomainHost.ifEmpty { existing.externalDomainHost },
            externalDomainName = update.externalDomainName.ifEmpty { existing.externalDomainName },
        )
    }

    private fun removeContactLocally(contactId: String) {
        if (contactId.isEmpty()) return
        mutableState.set { state ->
            state.copy(items = state.items.filterNot { it.contactId == contactId })
        }
    }

    private fun insertOrUpdateContactLocally(contact: Contact) {
        mutableState.set { state ->
            val items = state.items.toMutableList()
            val index = items.indexOfFirst {
                (contact.contactId.isNotEmpty() && it.contactId == contact.contactId) ||
                    (contact.profileId.isNotEmpty() && it.profileId == contact.profileId)
            }
            if (index >= 0) {
                items[index] = contact
            } else if (contact.contactId.isNotEmpty()) {
                items.add(0, contact)
            }
            state.copy(items = items, total = if (index >= 0) state.total else state.total + 1)
        }
    }

    private fun indexOfContact(contact: Contact): Int {
        val items = currentState().items
        return items.indexOfFirst {
            (contact.contactId.isNotEmpty() && it.contactId == contact.contactId) ||
                (contact.profileId.isNotEmpty() && it.profileId == contact.profileId)
        }
    }

    private fun placeholderContact(contactId: String): Contact {
        return Contact(
            contactId = contactId,
            profileId = "",
            name = "",
            email = "",
            phone = "",
            note = "",
            tags = emptyList(),
            interlocutorType = "",
            avatarUrl = "",
            aboutSelf = "",
            additionalContact = "",
            externalDomainHost = "",
            externalDomainName = "",
            presence = ContactPresence.UNKNOWN,
        )
    }

    @Serializable
    private sealed interface ChildConfig {
        @Serializable
        data object List : ChildConfig

        @Serializable
        data class Info(val contactIndex: Int, val contactId: String) : ChildConfig

        @Serializable
        data object Create : ChildConfig

        @Serializable
        data class Edit(val contactIndex: Int, val contactId: String) : ChildConfig
    }

    private companion object {
        const val PAGE_LIMIT = 30
        const val ADD_OVERLAY_LIMIT = 20
        const val SEARCH_DEBOUNCE_MS = 350L
    }
}

private fun <T> MutableStateFlow<T>.set(block: (T) -> T) {
    value = block(value)
}
