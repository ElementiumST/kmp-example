package com.example.kmpexample.kmp.data.db

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.native.NativeSqliteDriver

class IosDatabaseFactory : DatabaseFactory {
    override fun create(): AppDatabase {
        return AppDatabase(
            driver = NativeSqliteDriver(
                schema = AppDatabase.Schema.synchronous(),
                name = DATABASE_NAME,
            ),
        )
    }

    private companion object {
        private const val DATABASE_NAME = "kmp-example.db"
    }
}
