package com.example.kmpexample.kmp.data.logging

internal object DataLogger {
    fun network(message: String) {
        println("[kmp-data][network] $message")
    }

    fun db(message: String) {
        println("[kmp-data][db] $message")
    }
}
