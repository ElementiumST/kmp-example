package com.example.kmpexample.kmp.core.app

import com.example.kmpexample.kmp.data.config.NetworkConfig
import com.example.kmpexample.kmp.data.db.DatabaseFactory
import org.koin.core.module.Module

data class SharedAppConfig(
    val networkConfig: NetworkConfig = NetworkConfig(),
    val databaseFactory: DatabaseFactory? = null,
    val additionalModules: List<Module> = emptyList(),
)
