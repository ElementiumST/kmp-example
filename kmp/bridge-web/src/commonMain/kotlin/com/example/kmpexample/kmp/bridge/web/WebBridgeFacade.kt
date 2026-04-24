package com.example.kmpexample.kmp.bridge.web

import com.example.kmpexample.kmp.core.bridge.web.WebBridgeFactory
import com.example.kmpexample.kmp.core.bridge.web.WebRootBridge

object WebBridgeFacade {
    fun create(baseUrl: String = "/api/rest"): WebRootBridge = WebBridgeFactory.create(baseUrl)
}

data class GeneratedWebRoute(
    val root: String,
    val contacts: String,
    val path: String,
)

object GeneratedRouteTable {
    val routes: List<GeneratedWebRoute> = listOf(
        GeneratedWebRoute(root = "AUTH", contacts = "LIST", path = "auth"),
        GeneratedWebRoute(root = "CONTACTS_LIST", contacts = "LIST", path = "contacts"),
        GeneratedWebRoute(root = "CONTACTS_LIST", contacts = "INFO", path = "contacts/info"),
        GeneratedWebRoute(root = "CONTACTS_LIST", contacts = "CREATE", path = "contacts/create"),
        GeneratedWebRoute(root = "CONTACTS_LIST", contacts = "EDIT", path = "contacts/edit"),
        GeneratedWebRoute(root = "CONTACT_INFO", contacts = "INFO", path = "contacts/info"),
        GeneratedWebRoute(root = "CONTACT_CREATE", contacts = "CREATE", path = "contacts/create"),
        GeneratedWebRoute(root = "CONTACT_EDIT", contacts = "EDIT", path = "contacts/edit"),
    )
}
