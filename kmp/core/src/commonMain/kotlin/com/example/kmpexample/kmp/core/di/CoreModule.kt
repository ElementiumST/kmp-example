package com.example.kmpexample.kmp.core.di

import com.example.kmpexample.kmp.domain.usecase.LoginUseCase
import org.koin.dsl.module

fun coreModule() = module {
    factory { LoginUseCase(get()) }
}
