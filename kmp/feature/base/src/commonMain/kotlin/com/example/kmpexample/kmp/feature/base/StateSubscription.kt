package com.example.kmpexample.kmp.feature.base

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
