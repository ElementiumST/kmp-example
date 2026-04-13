package com.example.kmpexample.kmp.data.network

import kotlinx.serialization.json.Json

internal fun createSharedJson(): Json {
    return Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }
}
