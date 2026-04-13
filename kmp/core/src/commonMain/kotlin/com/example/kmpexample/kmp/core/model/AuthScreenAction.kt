package com.example.kmpexample.kmp.core.model

sealed interface AuthScreenAction {
    data class UpdateLogin(
        val value: String,
    ) : AuthScreenAction

    data class UpdatePassword(
        val value: String,
    ) : AuthScreenAction

    data object Submit : AuthScreenAction
}
