package com.example.kmpexample.kmp.feature.auth.model

import com.example.kmpexample.kmp.tools.bridge.annotations.BridgeModel
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
@BridgeModel(name = "AuthState")
data class AuthScreenState(
    val login: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isAuthorized: Boolean = false,
    val errorMessage: String? = null,
    val sessionId: String? = null,
    val authorizedLogin: String? = null,
    val authorizedName: String? = null,
    val submitLabel: String = "Войти",
    val canSubmit: Boolean = false,
)