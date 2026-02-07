package com.dadomatch.shared.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.dadomatch.shared.data.local.createDataStore
import com.dadomatch.shared.data.local.getDatabaseBuilder
import org.koin.dsl.module

/**
 * Core infrastructure module (Database, DataStore, etc.)
 */
val coreModule = module {
    single { 
        getDatabaseBuilder()
            .setDriver(BundledSQLiteDriver())
            .build()
    }
    single { createDataStore() }
}
