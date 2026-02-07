package com.dadomatch.shared.core.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.dadomatch.shared.core.data.createDataStore
import com.dadomatch.shared.core.data.getDatabaseBuilder
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
