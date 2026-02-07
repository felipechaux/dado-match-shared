package com.dadomatch.shared.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile

private lateinit var appContext: Context

fun initializeDataStore(context: Context) {
    appContext = context.applicationContext
}

actual fun createDataStore(): DataStore<Preferences> {
    return getDataStore {
        appContext.preferencesDataStoreFile(DATASTORE_FILE_NAME).absolutePath
    }
}
