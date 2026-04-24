package com.example.kmpexample.kmp.bridge.web

data class BridgeRoute(
    val root: String,
    val contacts: String,
    val path: String,
)

data class BridgeStateBinding(
    val stateJsonMethod: String,
    val modelName: String,
    val nullableOnEmpty: Boolean = false,
)

object BridgeSchema {
    val routes: List<BridgeRoute> = listOf(
        BridgeRoute(root = "AUTH", contacts = "LIST", path = "auth"),
        BridgeRoute(root = "CONTACTS_LIST", contacts = "LIST", path = "contacts"),
        BridgeRoute(root = "CONTACTS_LIST", contacts = "INFO", path = "contacts/info"),
        BridgeRoute(root = "CONTACTS_LIST", contacts = "CREATE", path = "contacts/create"),
        BridgeRoute(root = "CONTACTS_LIST", contacts = "EDIT", path = "contacts/edit"),
        BridgeRoute(root = "CONTACT_INFO", contacts = "INFO", path = "contacts/info"),
        BridgeRoute(root = "CONTACT_CREATE", contacts = "CREATE", path = "contacts/create"),
        BridgeRoute(root = "CONTACT_EDIT", contacts = "EDIT", path = "contacts/edit"),
    )

    val stateBindings: List<BridgeStateBinding> = listOf(
        BridgeStateBinding(stateJsonMethod = "authStateJson", modelName = "AuthState"),
        BridgeStateBinding(stateJsonMethod = "contactsListStateJson", modelName = "ContactsListState"),
        BridgeStateBinding(stateJsonMethod = "contactInfoStateJson", modelName = "ContactInfoState", nullableOnEmpty = true),
        BridgeStateBinding(stateJsonMethod = "contactEditorStateJson", modelName = "ContactEditorState", nullableOnEmpty = true),
    )
}