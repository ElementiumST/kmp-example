package com.example.kmpexample.kmp.core.bridge.web

import com.example.kmpexample.kmp.core.app.SharedApp
import com.example.kmpexample.kmp.core.app.SharedAppConfig
import com.example.kmpexample.kmp.core.component.RootComponent
import com.example.kmpexample.kmp.data.config.NetworkConfig
import com.example.kmpexample.kmp.feature.base.StateSubscription
import com.example.kmpexample.kmp.feature.contacts.component.ContactEditorComponent
import com.example.kmpexample.kmp.feature.contacts.component.ContactInfoComponent
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
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
class WebRootBridge private constructor() {
    private lateinit var rootComponent: RootComponent

    internal constructor(rootComponent: RootComponent) : this() {
        this.rootComponent = rootComponent
    }

    fun currentRootChildKind(): String = rootComponent.currentChildKind().name

    fun watchRootChildKind(observer: (String) -> Unit): StateSubscription {
        return rootComponent.watchChildKind { kind ->
            observer(kind.name)
        }
    }

    fun authStateJson(): String = authComponentOrNull()
        ?.currentState()
        ?.toAuthStateJson()
        ?: "{}"

    fun watchAuthState(observer: (String) -> Unit): StateSubscription {
        val component = authComponentOrNull() ?: return StateSubscription {}
        return component.watchState { state ->
            observer(state.toAuthStateJson())
        }
    }

    fun authUpdateLogin(value: String) {
        authComponentOrNull()?.updateLogin(value)
    }

    fun authUpdatePassword(value: String) {
        authComponentOrNull()?.updatePassword(value)
    }

    fun authSubmit() {
        authComponentOrNull()?.submit()
    }

    fun contactsChildKind(): String = contactsComponentOrNull()?.currentChildKind()?.name ?: "LIST"

    fun watchContactsChildKind(observer: (String) -> Unit): StateSubscription {
        val component = contactsComponentOrNull() ?: return StateSubscription {}
        return component.watchChildKind { kind ->
            observer(kind.name)
        }
    }

    fun contactsListStateJson(): String = contactsComponentOrNull()
        ?.currentState()
        ?.toContactsListJson()
        ?: "{}"

    fun watchContactsListState(observer: (String) -> Unit): StateSubscription {
        val component = contactsComponentOrNull() ?: return StateSubscription {}
        return component.watchState { state ->
            observer(state.toContactsListJson())
        }
    }

    fun contactsRefresh() {
        contactsComponentOrNull()?.refresh()
    }

    fun contactsLoadMore() {
        contactsComponentOrNull()?.loadMore()
    }

    fun contactsUpdateQuery(value: String) {
        contactsComponentOrNull()?.updateQuery(value)
    }

    fun contactsOpenInfo(contactIndex: Int) {
        contactsComponentOrNull()?.openInfo(contactIndex)
    }

    fun contactsOpenAddOverlay() {
        contactsComponentOrNull()?.openAddOverlay()
    }

    fun contactsCloseAddOverlay() {
        contactsComponentOrNull()?.closeAddOverlay()
    }

    fun contactsOpenCreate() {
        contactsComponentOrNull()?.openCreate()
    }

    fun contactsOpenEdit(contactIndex: Int) {
        contactsComponentOrNull()?.openEdit(contactIndex)
    }

    fun contactsDeleteFromMenu(contactIndex: Int) {
        contactsComponentOrNull()?.deleteFromMenu(contactIndex)
    }

    fun contactsUpdateAddOverlayQuery(value: String) {
        contactsComponentOrNull()?.updateAddOverlayQuery(value)
    }

    fun contactsLoadMoreAddOverlay() {
        contactsComponentOrNull()?.loadMoreAddOverlay()
    }

    fun contactsInviteInterlocutor(profileId: String) {
        contactsComponentOrNull()?.inviteInterlocutor(profileId)
    }

    fun contactInfoStateJson(): String = contactsComponentOrNull()
        ?.currentInfoComponentOrNull()
        ?.currentState()
        ?.toContactInfoJson()
        ?: "{}"

    fun watchContactInfoState(observer: (String) -> Unit): StateSubscription {
        val component = infoComponentOrNull() ?: return StateSubscription {}
        return component.watchState { state ->
            observer(state.toContactInfoJson())
        }
    }

    fun contactInfoBack() {
        infoComponentOrNull()?.back()
    }

    fun contactInfoEdit() {
        infoComponentOrNull()?.edit()
    }

    fun contactInfoDelete() {
        infoComponentOrNull()?.delete()
    }

    fun contactInfoInvite() {
        infoComponentOrNull()?.invite()
    }

    fun contactInfoToggleExtra() {
        infoComponentOrNull()?.toggleExtra()
    }

    fun contactInfoWriteMessage() {
        infoComponentOrNull()?.writeMessage()
    }

    fun contactInfoAudioCall() {
        infoComponentOrNull()?.audioCall()
    }

    fun contactInfoVideoCall() {
        infoComponentOrNull()?.videoCall()
    }

    fun contactEditorStateJson(): String = activeEditorComponentOrNull()
        ?.currentState()
        ?.toContactEditorJson()
        ?: "{}"

    fun watchContactEditorState(observer: (String) -> Unit): StateSubscription {
        val component = activeEditorComponentOrNull() ?: return StateSubscription {}
        return component.watchState { state ->
            observer(state.toContactEditorJson())
        }
    }

    fun contactEditorUpdateName(value: String) {
        activeEditorComponentOrNull()?.updateName(value)
    }

    fun contactEditorUpdateEmail(value: String) {
        activeEditorComponentOrNull()?.updateEmail(value)
    }

    fun contactEditorUpdatePhone(value: String) {
        activeEditorComponentOrNull()?.updatePhone(value)
    }

    fun contactEditorUpdateNote(value: String) {
        activeEditorComponentOrNull()?.updateNote(value)
    }

    fun contactEditorUpdateTags(value: String) {
        activeEditorComponentOrNull()?.updateTags(value)
    }

    fun contactEditorSave() {
        activeEditorComponentOrNull()?.save()
    }

    fun contactEditorBack() {
        activeEditorComponentOrNull()?.back()
    }

    fun contactEditorConfirmLeave() {
        activeEditorComponentOrNull()?.confirmLeave()
    }

    fun contactEditorCancelLeave() {
        activeEditorComponentOrNull()?.cancelLeave()
    }

    private fun authComponentOrNull() = rootComponent.currentAuthComponentOrNull()

    private fun contactsComponentOrNull() = rootComponent.currentContactsComponentOrNull()

    private fun infoComponentOrNull(): ContactInfoComponent? {
        return contactsComponentOrNull()?.currentInfoComponentOrNull()
    }

    private fun activeEditorComponentOrNull(): ContactEditorComponent? {
        val contactsComponent = contactsComponentOrNull() ?: return null
        return contactsComponent.currentCreateComponentOrNull()
            ?: contactsComponent.currentEditComponentOrNull()
    }
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
