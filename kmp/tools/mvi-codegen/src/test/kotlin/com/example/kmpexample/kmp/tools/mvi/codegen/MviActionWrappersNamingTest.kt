package com.example.kmpexample.kmp.tools.mvi.codegen

import kotlin.test.Test
import kotlin.test.assertEquals

class MviActionWrappersNamingTest {
    @Test
    fun `toWrapperMethodName should produce lower camel-case`() {
        assertEquals("refresh", "Refresh".toWrapperMethodName())
        assertEquals("updateQuery", "UpdateQuery".toWrapperMethodName())
        assertEquals("x", "X".toWrapperMethodName())
    }

    @Test
    fun `defaultWrapperInterfaceName should append Wrappers suffix`() {
        assertEquals("ContactsListActionWrappers", defaultWrapperInterfaceName("ContactsListAction"))
    }
}
