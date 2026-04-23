package com.example.kmpexample.kmp.data.config

/**
 * Network configuration.
 *
 * - [baseUrl] is the REST API base (includes scheme + host + optional `/api/rest`-style prefix).
 *   Endpoints are appended relative to this value, e.g. `$baseUrl/contacts`.
 * - [originUrl] is the bare `scheme://host[:port]` origin, used for avatar URLs served under
 *   `/services/resource` or for deriving the WebSocket URL. Defaults to the origin of [baseUrl].
 * - [webSocketUrl] is the full WS endpoint. Defaults to `wss` variant of [originUrl] + `/ws`.
 */
data class NetworkConfig(
    val baseUrl: String = "https://alpha.hi-tech.org/api/rest",
    val originUrl: String = deriveOrigin(baseUrl),
    val webSocketUrl: String = deriveWebSocket(originUrl),
) {
    companion object {
        private fun deriveOrigin(baseUrl: String): String {
            val schemeEnd = baseUrl.indexOf("://")
            if (schemeEnd < 0) return baseUrl
            val pathStart = baseUrl.indexOf('/', schemeEnd + 3)
            return if (pathStart < 0) baseUrl else baseUrl.substring(0, pathStart)
        }

        private fun deriveWebSocket(originUrl: String): String {
            val wsOrigin = originUrl
                .replaceFirst("https://", "wss://")
                .replaceFirst("http://", "ws://")
            return "$wsOrigin/ws"
        }
    }
}
