package com.example.kmpexample.kmp.tools.bridge.codegen

import kotlin.test.Test
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
}
