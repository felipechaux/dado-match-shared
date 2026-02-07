package com.dadomatch.shared

import android.content.Context
import com.dadomatch.shared.data.local.initializeDataStore
import com.dadomatch.shared.data.local.initializeDatabase

fun initKoinAndroid(context: Context) {
    initializeDatabase(context)
    initializeDataStore(context)
    initKoin()
}
