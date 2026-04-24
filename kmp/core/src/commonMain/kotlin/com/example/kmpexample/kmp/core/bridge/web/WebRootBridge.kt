package com.example.kmpexample.kmp.core.bridge.web

import com.arkivanov.decompose.value.subscribe
import com.example.kmpexample.kmp.core.app.SharedApp
import com.example.kmpexample.kmp.core.app.SharedAppConfig
import com.example.kmpexample.kmp.core.component.RootComponent
import com.example.kmpexample.kmp.data.config.NetworkConfig
import com.example.kmpexample.kmp.feature.auth.component.AuthComponent
import com.example.kmpexample.kmp.feature.auth.model.AuthScreenState
import com.example.kmpexample.kmp.feature.base.StateSubscription
import com.example.kmpexample.kmp.feature.contacts.component.ContactEditorComponent
import com.example.kmpexample.kmp.feature.contacts.component.ContactInfoComponent
import com.example.kmpexample.kmp.feature.contacts.component.ContactsComponent
import com.example.kmpexample.kmp.feature.contacts.model.ContactEditorState
import com.example.kmpexample.kmp.feature.contacts.model.ContactInfoState
import com.example.kmpexample.kmp.feature.contacts.model.ContactsListState
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import com.example.kmpexample.kmp.tools.bridge.annotations.BridgeAction
import com.example.kmpexample.kmp.tools.bridge.annotations.BridgeActionRole
import com.example.kmpexample.kmp.tools.bridge.annotations.BridgeStringUnion
import com.example.kmpexample.kmp.tools.bridge.annotations.ExposeToWeb
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
object WebBridgeFactory {
    fun create(baseUrl: String = "/api/rest"): WebRootBridge {
        val rootComponent = SharedApp.createRootComponent(
            config = SharedAppConfig(
                networkConfig = NetworkConfig(baseUrl = baseUrl),
            ),
        )
        return WebRootBridge(rootComponent)
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
@ExposeToWeb(name = "WebRootBridge")
class WebRootBridge private constructor() {
    private lateinit var rootComponent: RootComponent

    internal constructor(rootComponent: RootComponent) : this() {
        this.rootComponent = rootComponent
    }

    @BridgeAction(role = BridgeActionRole.KIND, name = "currentRootChildKind")
    @BridgeStringUnion(name = "RootChildKind", values = ["AUTH", "CONTACTS_LIST", "CONTACT_INFO", "CONTACT_CREATE", "CONTACT_EDIT"])
    fun currentRootChildKind(): String = rootComponent.currentRootChildKindString()

    @BridgeAction(role = BridgeActionRole.WATCH_KIND, name = "watchRootChildKind")
    fun watchRootChildKind(observer: (String) -> Unit): StateSubscription {
        return rootComponent.watchRootChildKindString(observer)
    }

    @BridgeAction(role = BridgeActionRole.STATE_JSON, stateModel = AuthScreenState::class, name = "authStateJson")
    fun authStateJson(): String = authComponentOrNull()
        ?.currentState()
        ?.toAuthStateJson()
        ?: "{}"

    @BridgeAction(role = BridgeActionRole.WATCH_STATE, stateModel = AuthScreenState::class, name = "watchAuthState")
    fun watchAuthState(observer: (String) -> Unit): StateSubscription {
        val component = authComponentOrNull() ?: return StateSubscription {}
        return component.watchState { state ->
            observer(state.toAuthStateJson())
        }
    }

    @BridgeAction(role = BridgeActionRole.INTENT, name = "authUpdateLogin")
    fun authUpdateLogin(value: String) {
        authComponentOrNull()?.updateLogin(value)
    }

    @BridgeAction(role = BridgeActionRole.INTENT, name = "authUpdatePassword")
    fun authUpdatePassword(value: String) {
        authComponentOrNull()?.updatePassword(value)
    }

    @BridgeAction(role = BridgeActionRole.INTENT, name = "authSubmit")
    fun authSubmit() {
        authComponentOrNull()?.submit()
    }

    @BridgeAction(role = BridgeActionRole.KIND, name = "contactsChildKind")
    @BridgeStringUnion(name = "ContactsChildKind", values = ["LIST", "INFO", "CREATE", "EDIT"])
    fun contactsChildKind(): String = contactsComponentOrNull()?.currentContactsChildKindString() ?: "LIST"

    @BridgeAction(role = BridgeActionRole.WATCH_KIND, name = "watchContactsChildKind")
    fun watchContactsChildKind(observer: (String) -> Unit): StateSubscription {
        val component = contactsComponentOrNull() ?: return StateSubscription {}
        return component.watchContactsChildKindString(observer)
    }

    @BridgeAction(role = BridgeActionRole.STATE_JSON, stateModel = ContactsListState::class, name = "contactsListStateJson")
    fun contactsListStateJson(): String = contactsComponentOrNull()
        ?.currentState()
        ?.toContactsListJson()
        ?: "{}"

    @BridgeAction(role = BridgeActionRole.WATCH_STATE, stateModel = ContactsListState::class, name = "watchContactsListState")
    fun watchContactsListState(observer: (String) -> Unit): StateSubscription {
        val component = contactsComponentOrNull() ?: return StateSubscription {}
        return component.watchState { state ->
            observer(state.toContactsListJson())
        }
    }

    @BridgeAction(role = BridgeActionRole.INTENT, name = "contactsRefresh")
    fun contactsRefresh() {
        contactsComponentOrNull()?.refresh()
    }

    @BridgeAction(role = BridgeActionRole.INTENT, name = "contactsLoadMore")
    fun contactsLoadMore() {
        contactsComponentOrNull()?.loadMore()
    }

    @BridgeAction(role = BridgeActionRole.INTENT, name = "contactsUpdateQuery")
    fun contactsUpdateQuery(value: String) {
        contactsComponentOrNull()?.updateQuery(value)
    }

    @BridgeAction(role = BridgeActionRole.INTENT, name = "contactsOpenInfo")
    fun contactsOpenInfo(contactIndex: Int) {
        contactsComponentOrNull()?.openInfo(contactIndex)
    }

    @BridgeAction(role = BridgeActionRole.INTENT, name = "contactsOpenAddOverlay")
    fun contactsOpenAddOverlay() {
        contactsComponentOrNull()?.openAddOverlay()
    }

    @BridgeAction(role = BridgeActionRole.INTENT, name = "contactsCloseAddOverlay")
    fun contactsCloseAddOverlay() {
        contactsComponentOrNull()?.closeAddOverlay()
    }

    @BridgeAction(role = BridgeActionRole.INTENT, name = "contactsOpenCreate")
    fun contactsOpenCreate() {
        contactsComponentOrNull()?.openCreate()
    }

    @BridgeAction(role = BridgeActionRole.INTENT, name = "contactsOpenEdit")
    fun contactsOpenEdit(contactIndex: Int) {
        contactsComponentOrNull()?.openEdit(contactIndex)
    }

    @BridgeAction(role = BridgeActionRole.INTENT, name = "contactsDeleteFromMenu")
    fun contactsDeleteFromMenu(contactIndex: Int) {
        contactsComponentOrNull()?.deleteFromMenu(contactIndex)
    }

    @BridgeAction(role = BridgeActionRole.INTENT, name = "contactsUpdateAddOverlayQuery")
    fun contactsUpdateAddOverlayQuery(value: String) {
        contactsComponentOrNull()?.updateAddOverlayQuery(value)
    }

    @BridgeAction(role = BridgeActionRole.INTENT, name = "contactsLoadMoreAddOverlay")
    fun contactsLoadMoreAddOverlay() {
        contactsComponentOrNull()?.loadMoreAddOverlay()
    }

    @BridgeAction(role = BridgeActionRole.INTENT, name = "contactsInviteInterlocutor")
    fun contactsInviteInterlocutor(profileId: String) {
        contactsComponentOrNull()?.inviteInterlocutor(profileId)
    }

    @BridgeAction(role = BridgeActionRole.STATE_JSON, stateModel = ContactInfoState::class, nullableOnEmpty = true, name = "contactInfoStateJson")
    fun contactInfoStateJson(): String = infoComponentOrNull()
        ?.currentState()
        ?.toContactInfoJson()
        ?: "{}"

    @BridgeAction(role = BridgeActionRole.WATCH_STATE, stateModel = ContactInfoState::class, nullableOnEmpty = true, name = "watchContactInfoState")
    fun watchContactInfoState(observer: (String) -> Unit): StateSubscription {
        val component = infoComponentOrNull() ?: return StateSubscription {}
        return component.watchState { state ->
            observer(state.toContactInfoJson())
        }
    }

    @BridgeAction(role = BridgeActionRole.INTENT, name = "contactInfoBack")
    fun contactInfoBack() {
        infoComponentOrNull()?.back()
    }

    @BridgeAction(role = BridgeActionRole.INTENT, name = "contactInfoEdit")
    fun contactInfoEdit() {
        infoComponentOrNull()?.edit()
    }

    @BridgeAction(role = BridgeActionRole.INTENT, name = "contactInfoDelete")
    fun contactInfoDelete() {
        infoComponentOrNull()?.delete()
    }

    @BridgeAction(role = BridgeActionRole.INTENT, name = "contactInfoInvite")
    fun contactInfoInvite() {
        infoComponentOrNull()?.invite()
    }

    @BridgeAction(role = BridgeActionRole.INTENT, name = "contactInfoToggleExtra")
    fun contactInfoToggleExtra() {
        infoComponentOrNull()?.toggleExtra()
    }

    @BridgeAction(role = BridgeActionRole.INTENT, name = "contactInfoWriteMessage")
    fun contactInfoWriteMessage() {
        infoComponentOrNull()?.writeMessage()
    }

    @BridgeAction(role = BridgeActionRole.INTENT, name = "contactInfoAudioCall")
    fun contactInfoAudioCall() {
        infoComponentOrNull()?.audioCall()
    }

    @BridgeAction(role = BridgeActionRole.INTENT, name = "contactInfoVideoCall")
    fun contactInfoVideoCall() {
        infoComponentOrNull()?.videoCall()
    }

    @BridgeAction(role = BridgeActionRole.STATE_JSON, stateModel = ContactEditorState::class, nullableOnEmpty = true, name = "contactEditorStateJson")
    fun contactEditorStateJson(): String = activeEditorComponentOrNull()
        ?.currentState()
        ?.toContactEditorJson()
        ?: "{}"

    @BridgeAction(role = BridgeActionRole.WATCH_STATE, stateModel = ContactEditorState::class, nullableOnEmpty = true, name = "watchContactEditorState")
    fun watchContactEditorState(observer: (String) -> Unit): StateSubscription {
        val component = activeEditorComponentOrNull() ?: return StateSubscription {}
        return component.watchState { state ->
            observer(state.toContactEditorJson())
        }
    }

    @BridgeAction(role = BridgeActionRole.INTENT, name = "contactEditorUpdateName")
    fun contactEditorUpdateName(value: String) {
        activeEditorComponentOrNull()?.updateName(value)
    }

    @BridgeAction(role = BridgeActionRole.INTENT, name = "contactEditorUpdateEmail")
    fun contactEditorUpdateEmail(value: String) {
        activeEditorComponentOrNull()?.updateEmail(value)
    }

    @BridgeAction(role = BridgeActionRole.INTENT, name = "contactEditorUpdatePhone")
    fun contactEditorUpdatePhone(value: String) {
        activeEditorComponentOrNull()?.updatePhone(value)
    }

    @BridgeAction(role = BridgeActionRole.INTENT, name = "contactEditorUpdateNote")
    fun contactEditorUpdateNote(value: String) {
        activeEditorComponentOrNull()?.updateNote(value)
    }

    @BridgeAction(role = BridgeActionRole.INTENT, name = "contactEditorUpdateTags")
    fun contactEditorUpdateTags(value: String) {
        activeEditorComponentOrNull()?.updateTags(value)
    }

    @BridgeAction(role = BridgeActionRole.INTENT, name = "contactEditorSave")
    fun contactEditorSave() {
        activeEditorComponentOrNull()?.save()
    }

    @BridgeAction(role = BridgeActionRole.INTENT, name = "contactEditorBack")
    fun contactEditorBack() {
        activeEditorComponentOrNull()?.back()
    }

    @BridgeAction(role = BridgeActionRole.INTENT, name = "contactEditorConfirmLeave")
    fun contactEditorConfirmLeave() {
        activeEditorComponentOrNull()?.confirmLeave()
    }

    @BridgeAction(role = BridgeActionRole.INTENT, name = "contactEditorCancelLeave")
    fun contactEditorCancelLeave() {
        activeEditorComponentOrNull()?.cancelLeave()
    }

    @BridgeAction(role = BridgeActionRole.DESTROY, name = "destroy")
    fun destroy() {
        rootComponent.destroy()
    }

    private fun authComponentOrNull(): AuthComponent? =
        (rootComponent.stack.value.active.instance as? RootComponent.Child.Auth)?.component

    private fun contactsComponentOrNull(): ContactsComponent? =
        (rootComponent.stack.value.active.instance as? RootComponent.Child.Contacts)?.component

    private fun infoComponentOrNull(): ContactInfoComponent? {
        val contacts = contactsComponentOrNull() ?: return null
        return (contacts.childStack.value.active.instance as? ContactsComponent.Child.Info)?.component
    }

    private fun activeEditorComponentOrNull(): ContactEditorComponent? {
        val contacts = contactsComponentOrNull() ?: return null
        return when (val active = contacts.childStack.value.active.instance) {
            is ContactsComponent.Child.Create -> active.component
            is ContactsComponent.Child.Edit -> active.component
            else -> null
        }
    }
}

private fun RootComponent.currentRootChildKindString(): String {
    val active = stack.value.active.instance
    return when (active) {
        is RootComponent.Child.Auth -> "AUTH"
        is RootComponent.Child.Contacts -> when (active.component.childStack.value.active.instance) {
            ContactsComponent.Child.List -> "CONTACTS_LIST"
            is ContactsComponent.Child.Info -> "CONTACT_INFO"
            is ContactsComponent.Child.Create -> "CONTACT_CREATE"
            is ContactsComponent.Child.Edit -> "CONTACT_EDIT"
        }
    }
}

private fun RootComponent.watchRootChildKindString(
    observer: (String) -> Unit,
): StateSubscription {
    observer(currentRootChildKindString())
    var innerCancellation: (() -> Unit)? = null

    fun rebindContacts(contacts: ContactsComponent?) {
        innerCancellation?.invoke()
        innerCancellation = null
        if (contacts != null) {
            val cancel = contacts.childStack.subscribe {
                observer(currentRootChildKindString())
            }
            innerCancellation = { cancel.cancel() }
        }
    }

    val outerCancellation = stack.subscribe { childStack ->
        observer(currentRootChildKindString())
        val active = childStack.active.instance
        rebindContacts((active as? RootComponent.Child.Contacts)?.component)
    }

    return StateSubscription {
        innerCancellation?.invoke()
        outerCancellation.cancel()
    }
}

private fun ContactsComponent.currentContactsChildKindString(): String =
    when (childStack.value.active.instance) {
        ContactsComponent.Child.List -> "LIST"
        is ContactsComponent.Child.Info -> "INFO"
        is ContactsComponent.Child.Create -> "CREATE"
        is ContactsComponent.Child.Edit -> "EDIT"
    }

private fun ContactsComponent.watchContactsChildKindString(
    observer: (String) -> Unit,
): StateSubscription {
    observer(currentContactsChildKindString())
    val cancellation = childStack.subscribe {
        observer(currentContactsChildKindString())
    }
    return StateSubscription { cancellation.cancel() }
}

private fun com.example.kmpexample.kmp.feature.auth.model.AuthScreenState.toAuthStateJson(): String {
    return buildJsonObject {
        put("login", login)
        put("password", password)
        put("isLoading", isLoading)
        put("isAuthorized", isAuthorized)
        putNullable("errorMessage", errorMessage)
        putNullable("sessionId", sessionId)
        putNullable("authorizedLogin", authorizedLogin)
        putNullable("authorizedName", authorizedName)
        put("submitLabel", submitLabel)
        put("canSubmit", canSubmit)
    }.toString()
}

private fun com.example.kmpexample.kmp.feature.contacts.model.ContactsListState.toContactsListJson(): String {
    return buildJsonObject {
        put("query", query)
        put("total", total)
        put("isLoading", isLoading)
        put("isLoadingMore", isLoadingMore)
        put("isRefreshing", isRefreshing)
        putNullable("errorMessage", errorMessage)
        put("hasMore", hasMore)
        put("isAddOverlayVisible", isAddOverlayVisible)
        put("contextMenuContactIndex", contextMenuContactIndex)
        putNullable("snackbarMessage", snackbarMessage)
        put("items", items.toJsonArray())
        put("presence", presence.toJsonObject())
        put(
            "addOverlay",
            buildJsonObject {
                put("query", addOverlay.query)
                put("isLoading", addOverlay.isLoading)
                put("isLoadingMore", addOverlay.isLoadingMore)
                putNullable("errorMessage", addOverlay.errorMessage)
                put("hasMore", addOverlay.hasMore)
                put("foreignOffset", addOverlay.foreignOffset)
                put("companyOffset", addOverlay.companyOffset)
                put("directoryOffset", addOverlay.directoryOffset)
                put("domainOffset", addOverlay.domainOffset)
                put("items", addOverlay.items.toInterlocutorsJsonArray())
                put("invitingProfileIds", addOverlay.invitingProfileIds.toJsonArray())
            },
        )
    }.toString()
}

private fun com.example.kmpexample.kmp.feature.contacts.model.ContactInfoState.toContactInfoJson(): String {
    return buildJsonObject {
        put("contact", contact.toJsonObject())
        put("isExtraExpanded", isExtraExpanded)
        put("isDeleting", isDeleting)
        put("isInviting", isInviting)
        putNullable("errorMessage", errorMessage)
        putNullable("snackbarMessage", snackbarMessage)
        put("isCallButtonsVisible", isCallButtonsVisible)
        put("isDeleteVisible", isDeleteVisible)
        put("isAddToContactsVisible", isAddToContactsVisible)
    }.toString()
}

private fun com.example.kmpexample.kmp.feature.contacts.model.ContactEditorState.toContactEditorJson(): String {
    return buildJsonObject {
        put("mode", mode.name)
        put("isNote", isNote)
        put("isDirty", isDirty)
        put("isSaving", isSaving)
        putNullable("errorMessage", errorMessage)
        put("showLeaveConfirmation", showLeaveConfirmation)
        put("dismissed", dismissed)
        put("canSave", canSave)
        put(
            "draft",
            buildJsonObject {
                put("name", draft.name)
                put("email", draft.email)
                put("phone", draft.phone)
                put("note", draft.note)
                put("tagsText", draft.tagsText)
            },
        )
        put(
            "validation",
            buildJsonObject {
                put("isValid", validation.isValid)
                putNullable("name", validation.name?.name)
                putNullable("email", validation.email?.name)
                putNullable("phone", validation.phone?.name)
                putNullable("note", validation.note?.name)
                putNullable("tags", validation.tags?.name)
            },
        )
    }.toString()
}

private fun List<com.example.kmpexample.kmp.domain.model.Contact>.toJsonArray(): JsonArray {
    return buildJsonArray {
        for (contact in this@toJsonArray) {
            add(contact.toJsonObject())
        }
    }
}

private fun List<com.example.kmpexample.kmp.domain.model.Interlocutor>.toInterlocutorsJsonArray(): JsonArray {
    return buildJsonArray {
        for (item in this@toInterlocutorsJsonArray) {
            add(
                buildJsonObject {
                    put("profileId", item.profileId)
                    put("contactId", item.contactId)
                    put("name", item.name)
                    put("email", item.email)
                    put("phone", item.phone)
                    put("avatarUrl", item.avatarUrl)
                    put("interlocutorType", item.interlocutorType)
                    put("externalDomainHost", item.externalDomainHost)
                    put("externalDomainName", item.externalDomainName)
                    put("isInContacts", item.isInContacts)
                },
            )
        }
    }
}

private fun com.example.kmpexample.kmp.domain.model.Contact.toJsonObject(): JsonObject {
    return buildJsonObject {
        put("contactId", contactId)
        put("profileId", profileId)
        put("name", name)
        put("email", email)
        put("phone", phone)
        put("note", note)
        put("tags", tags.toJsonArray())
        put("interlocutorType", interlocutorType)
        put("avatarUrl", avatarUrl)
        put("aboutSelf", aboutSelf)
        put("additionalContact", additionalContact)
        put("externalDomainHost", externalDomainHost)
        put("externalDomainName", externalDomainName)
        put("presence", presence.name)
        put("isNote", isNote)
        put("isInContacts", isInContacts)
    }
}

private fun Map<String, com.example.kmpexample.kmp.domain.model.ContactPresence>.toJsonObject(): JsonObject {
    val pairs = buildMap {
        for ((key, value) in this@toJsonObject) {
            put(key, JsonPrimitive(value.name))
        }
    }
    return JsonObject(pairs)
}

private fun Collection<String>.toJsonArray(): JsonArray {
    return buildJsonArray {
        for (value in this@toJsonArray) {
            add(JsonPrimitive(value))
        }
    }
}

private fun JsonObjectBuilder.putNullable(
    key: String,
    value: String?,
) {
    if (value == null) {
        put(key, JsonNull)
    } else {
        put(key, value)
    }
}
