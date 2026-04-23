package com.example.kmpexample.kmp.data.remote

import com.example.kmpexample.kmp.data.config.NetworkConfig
import com.example.kmpexample.kmp.data.logging.DataLogger
import com.example.kmpexample.kmp.data.session.SessionStore
import com.example.kmpexample.kmp.domain.model.ContactEvent
import com.example.kmpexample.kmp.domain.model.ContactPresence
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Streams `ContactChangedEvent` / `ContactOnlineEvent` / `ContactRemovedEvent`
 * from the server WebSocket channel. The underlying connection is lazily opened
 * on first subscription and closed when the last subscriber goes away.
 *
 * Reconnects with exponential backoff on any error. Session header is forwarded
 * from the [SessionStore].
 */
class ContactsWebSocketClient(
    private val httpClient: HttpClient,
    private val networkConfig: NetworkConfig,
    private val sessionStore: SessionStore,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val events = MutableSharedFlow<ContactEvent>(extraBufferCapacity = 64)
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val lock = Mutex()
    private var connectionJob: Job? = null
    private var subscribers = 0

    val flow: SharedFlow<ContactEvent> = events.asSharedFlow()

    suspend fun acquire() {
        lock.withLock {
            subscribers++
            if (connectionJob == null) {
                connectionJob = scope.launch { connectLoop() }
            }
        }
    }

    suspend fun release() {
        lock.withLock {
            subscribers = (subscribers - 1).coerceAtLeast(0)
            if (subscribers == 0) {
                connectionJob?.cancelAndJoin()
                connectionJob = null
            }
        }
    }

    private suspend fun connectLoop() {
        var attempt = 0
        while (scope.isActive) {
            val session = sessionStore.currentSessionId()
            if (session.isNullOrEmpty()) {
                delay(backoffMillis(attempt++))
                continue
            }

            val result = runCatching { openSocket(session) }
            if (result.isSuccess) {
                attempt = 0
            } else {
                val failure = result.exceptionOrNull()
                if (failure != null && isMissingEndpoint(failure)) {
                    DataLogger.network(
                        "WebSocket endpoint `${networkConfig.webSocketUrl}` is unavailable; " +
                            "realtime contact updates are disabled for this session",
                    )
                    return
                }
                attempt++
            }
            delay(backoffMillis(attempt))
        }
    }

    private suspend fun openSocket(session: String) {
        val url = URLBuilder(networkConfig.webSocketUrl).apply {
            when (protocol) {
                URLProtocol.HTTP -> protocol = URLProtocol.WS
                URLProtocol.HTTPS -> protocol = URLProtocol.WSS
                else -> Unit
            }
        }.buildString()

        httpClient.webSocket(url, request = {
            headers.append("Session", session)
        }) {
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    handleTextFrame(frame.readText())
                }
            }
        }
    }

    private suspend fun handleTextFrame(raw: String) {
        val payload = runCatching { json.parseToJsonElement(raw).jsonObject }.getOrNull()
            ?: return
        val type = payload["type"]?.jsonPrimitive?.contentOrNull
            ?: payload["eventType"]?.jsonPrimitive?.contentOrNull
            ?: return
        when (type) {
            "ContactChangedEvent" -> parseChanged(payload)?.let { events.emit(it) }
            "ContactOnlineEvent" -> parseOnline(payload)?.let { events.emit(it) }
            "ContactRemovedEvent" -> parseRemoved(payload)?.let { events.emit(it) }
        }
    }

    private fun parseChanged(payload: JsonObject): ContactEvent.Changed? {
        val contactJson = payload["contact"]?.jsonObject ?: payload["data"]?.jsonObject ?: return null
        val dto = runCatching { json.decodeFromJsonElement(ApiContactDto.serializer(), contactJson) }
            .getOrNull() ?: return null
        return ContactEvent.Changed(dto.toDomain(networkConfig.originUrl))
    }

    private fun parseOnline(payload: JsonObject): ContactEvent.Online? {
        val profileId = payload["profileId"]?.jsonPrimitive?.contentOrNull ?: return null
        val status = payload["presenceStatus"]?.jsonPrimitive?.contentOrNull
        return ContactEvent.Online(
            profileId = profileId,
            presence = ContactPresence.fromApi(status),
        )
    }

    private fun parseRemoved(payload: JsonObject): ContactEvent.Removed? {
        val ids = payload["contactIds"]?.jsonArray
            ?.mapNotNull { it.jsonPrimitive.contentOrNull }
            ?: emptyList()
        if (ids.isEmpty()) return null
        return ContactEvent.Removed(ids)
    }

    private fun backoffMillis(attempt: Int): Long {
        val base = 1_000L
        val cap = 30_000L
        val exp = (base * (1 shl attempt.coerceAtMost(5)))
        return exp.coerceAtMost(cap)
    }

    private fun isMissingEndpoint(throwable: Throwable): Boolean {
        return throwable.message?.contains("404", ignoreCase = true) == true
    }
}
