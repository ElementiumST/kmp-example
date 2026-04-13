package com.example.kmpexample.kmp.domain.model

data class LoginRequest(
    val login: String,
    val password: String,
    val rememberMe: Boolean = false,
)
