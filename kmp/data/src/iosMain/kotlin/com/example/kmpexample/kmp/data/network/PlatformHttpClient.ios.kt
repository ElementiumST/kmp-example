package com.example.kmpexample.kmp.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json

internal actual fun platformHttpClient(
    sessionHeaderProvider: () -> String?,
): HttpClient {
    return HttpClient(Darwin) {
        install(ContentNegotiation) {
            json(createSharedJson())
        }

        defaultRequest {
            sessionHeaderProvider()?.let { sessionId ->
                headers.append("Session", sessionId)
            }
        }
    }
}
