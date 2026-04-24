package com.example.kmpexample.kmp.tools.bridge.codegen

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class TypeScriptEmitterTest {
    @Test
    fun `emitAlias should create deterministic output`() {
        val actual = TypeScriptEmitter.emitAlias(
            typeName = "AuthState",
            fields = listOf(
                "login" to "string",
                "isLoading" to "boolean",
            ),
        )

        val expected = """
            export type AuthState = {
              login: string;
              isLoading: boolean;
            }
        """.trimIndent()

        assertEquals(expected, actual)
    }

    @Test
    fun `emitBridgeTypes should render route table`() {
        val actual = TypeScriptEmitter.emitBridgeTypes(
            routes = listOf(
                RouteMeta(root = "AUTH", contacts = "LIST", path = "auth"),
                RouteMeta(root = "CONTACTS_LIST", contacts = "LIST", path = "contacts"),
            ),
        )

        assertContains(actual, "export type RootChildKind")
        assertContains(actual, "{ root: 'AUTH', contacts: 'LIST', path: 'auth' }")
        assertContains(actual, "{ root: 'CONTACTS_LIST', contacts: 'LIST', path: 'contacts' }")
    }

    @Test
    fun `emitAngularService should include bridge service and normalization`() {
        val actual = TypeScriptEmitter.emitAngularService()

        assertContains(actual, "export class KmpBridgeService")
        assertContains(actual, "normalizeContactsListState")
        assertContains(actual, "loadSharedCoreScripts")
    }
}