package com.example.kmpexample.kmp.core.bridge

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
class StateSubscription(
    private val onCancel: () -> Unit,
) {
    fun cancel() {
        onCancel()
    }
}
