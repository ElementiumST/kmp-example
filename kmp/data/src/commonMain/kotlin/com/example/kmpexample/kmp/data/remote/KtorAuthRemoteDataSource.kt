package com.example.kmpexample.kmp.data.remote

import com.example.kmpexample.kmp.data.config.NetworkConfig
import com.example.kmpexample.kmp.domain.model.AuthSession
import com.example.kmpexample.kmp.domain.model.LoginRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.http.contentType

class KtorAuthRemoteDataSource(
    private val httpClient: HttpClient,
    private val networkConfig: NetworkConfig,
) : RemoteAuthDataSource {
    override suspend fun login(request: LoginRequest): AuthSession {
        val response = httpClient.post("${networkConfig.baseUrl}/login") {
            contentType(ContentType.Application.Json)
            setBody(
                LoginRequestDto(
                    login = request.login,
                    password = request.password,
                    rememberMe = request.rememberMe,
                ),
            )
        }.parseLoginResponse()

        return response.toDomain()
    }

    override suspend fun loginWithToken(token: String): AuthSession {
        val response = httpClient.post("${networkConfig.baseUrl}/login-with-token") {
            contentType(ContentType.Application.Json)
            setBody(LoginWithTokenRequestDto(token = token))
        }.parseLoginResponse()

        return response.toDomain()
    }

    private suspend fun HttpResponse.parseLoginResponse(): LoginResponseDto {
        if (!status.isSuccess()) {
            val errorBody = bodyAsText().take(500).ifBlank { status.description }
            throw IllegalStateException("Не удалось выполнить вход (${status.value}): $errorBody")
        }

        val responseContentType = headers[HttpHeaders.ContentType].orEmpty()
        if (!responseContentType.contains(ContentType.Application.Json.toString(), ignoreCase = true)) {
            val errorBody = bodyAsText().take(500)
            throw IllegalStateException(
                "Не удалось выполнить вход: сервер вернул неподдерживаемый формат ответа ($responseContentType). $errorBody",
            )
        }

        return body()
    }
}
