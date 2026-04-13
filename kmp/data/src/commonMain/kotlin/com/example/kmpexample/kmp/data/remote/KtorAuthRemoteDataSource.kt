package com.example.kmpexample.kmp.data.remote

import com.example.kmpexample.kmp.data.config.NetworkConfig
import com.example.kmpexample.kmp.domain.model.AuthSession
import com.example.kmpexample.kmp.domain.model.LoginRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class KtorAuthRemoteDataSource(
    private val httpClient: HttpClient,
    private val networkConfig: NetworkConfig,
) : RemoteAuthDataSource {
    override suspend fun login(request: LoginRequest): AuthSession {
        val response = httpClient.post("${networkConfig.baseUrl.trimEnd('/')}/login") {
            contentType(ContentType.Application.Json)
            setBody(
                LoginRequestDto(
                    login = request.login,
                    password = request.password,
                    rememberMe = request.rememberMe,
                ),
            )
        }.body<LoginResponseDto>()

        return response.toDomain()
    }
}
