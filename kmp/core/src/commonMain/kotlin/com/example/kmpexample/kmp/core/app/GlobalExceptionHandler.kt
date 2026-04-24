package com.example.kmpexample.kmp.core.app

import kotlinx.coroutines.CoroutineExceptionHandler

object GlobalExceptionHandler {
    val handler: CoroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        println("[kmp-core][error] ${throwable.message ?: throwable::class.simpleName}")
    }
}
