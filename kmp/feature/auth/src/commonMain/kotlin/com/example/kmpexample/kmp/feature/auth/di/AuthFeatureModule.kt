package com.example.kmpexample.kmp.feature.auth.di

import com.example.kmpexample.kmp.domain.usecase.ClearPersistedAuthSessionUseCase
import com.example.kmpexample.kmp.domain.usecase.GetPersistedAuthSessionUseCase
import com.example.kmpexample.kmp.domain.usecase.LoginUseCase
import com.example.kmpexample.kmp.domain.usecase.LoginWithTokenUseCase
import org.koin.dsl.module

fun authFeatureModule() = module {
    factory { ClearPersistedAuthSessionUseCase(get()) }
    factory { GetPersistedAuthSessionUseCase(get()) }
    factory { LoginUseCase(get()) }
    factory { LoginWithTokenUseCase(get()) }
}
