package com.example.kmpexample.kmp.core.bridge

class StateSubscription(
    private val onCancel: () -> Unit,
) {
    fun cancel() {
        onCancel()
    }
}
