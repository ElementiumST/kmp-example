package com.example.kmpexample.kmp.data.di

import com.example.kmpexample.kmp.data.config.NetworkConfig
import com.example.kmpexample.kmp.data.db.DatabaseFactory
import com.example.kmpexample.kmp.data.local.InMemorySessionDataSource
import com.example.kmpexample.kmp.data.local.LocalSessionDataSource
import com.example.kmpexample.kmp.data.local.SqlDelightSessionDataSource
import com.example.kmpexample.kmp.data.network.createSharedJson
import com.example.kmpexample.kmp.data.network.platformHttpClient
import com.example.kmpexample.kmp.data.remote.KtorAuthRemoteDataSource
import com.example.kmpexample.kmp.data.remote.RemoteAuthDataSource
import com.example.kmpexample.kmp.data.repository.AuthRepositoryImpl
import com.example.kmpexample.kmp.data.session.DefaultSessionStore
import com.example.kmpexample.kmp.data.session.SessionStore
import com.example.kmpexample.kmp.domain.repository.AuthRepository
import io.ktor.client.HttpClient
import org.koin.dsl.module

fun dataModule(
    networkConfig: NetworkConfig = NetworkConfig(),
    databaseFactory: DatabaseFactory? = null,
) = module {
    single { networkConfig }
    single { createSharedJson() }
    single<LocalSessionDataSource> {
        databaseFactory?.let { SqlDelightSessionDataSource(it.create()) }
            ?: InMemorySessionDataSource()
    }
    single<SessionStore> { DefaultSessionStore(get()) }
    single<HttpClient> { platformHttpClient(get<SessionStore>()::currentSessionId) }
    single<RemoteAuthDataSource> { KtorAuthRemoteDataSource(get(), get()) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
}
