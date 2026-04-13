package com.example.kmpexample.kmp.data.network

import io.ktor.client.HttpClient

internal expect fun platformHttpClient(
    sessionHeaderProvider: () -> String?,
): HttpClient
