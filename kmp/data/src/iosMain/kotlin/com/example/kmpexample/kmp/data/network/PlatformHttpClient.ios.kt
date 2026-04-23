package com.example.kmpexample.kmp.data.network

import com.example.kmpexample.kmp.data.logging.DataLogger
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json

internal actual fun platformHttpClient(
    sessionHeaderProvider: () -> String?,
): HttpClient {
    return HttpClient(Darwin) {
        install(ContentNegotiation) {
            json(createSharedJson())
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    DataLogger.network(message)
                }
            }
            level = LogLevel.ALL
            sanitizeHeader { header -> header == "Session" }
        }

        install(WebSockets)

        defaultRequest {
            sessionHeaderProvider()?.let { sessionId ->
                headers.append("Session", sessionId)
            }
        }
    }
}
