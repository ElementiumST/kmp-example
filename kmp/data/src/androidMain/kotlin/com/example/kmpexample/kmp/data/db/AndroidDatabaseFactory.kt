package com.example.kmpexample.kmp.data.db

import android.content.Context
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

class AndroidDatabaseFactory(
    private val context: Context,
) : DatabaseFactory {
    override fun create(): AppDatabase {
        return AppDatabase(
            driver = AndroidSqliteDriver(
                schema = AppDatabase.Schema.synchronous(),
                context = context,
                name = DATABASE_NAME,
            ),
        )
    }

    private companion object {
        private const val DATABASE_NAME = "kmp-example.db"
    }
}
