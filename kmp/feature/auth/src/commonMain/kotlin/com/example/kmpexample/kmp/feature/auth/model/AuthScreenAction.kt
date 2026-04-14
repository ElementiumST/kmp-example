package com.example.kmpexample.kmp.feature.auth.model

sealed interface AuthScreenAction {
    data class UpdateLogin(
        val value: String,
    ) : AuthScreenAction

    data class UpdatePassword(
        val value: String,
    ) : AuthScreenAction

    data object Submit : AuthScreenAction
}
