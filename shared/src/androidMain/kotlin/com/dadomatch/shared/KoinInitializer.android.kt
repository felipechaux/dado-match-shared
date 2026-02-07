package com.dadomatch.shared

import android.content.Context
import com.dadomatch.shared.core.data.initializeDataStore
import com.dadomatch.shared.core.data.initializeDatabase

fun initKoinAndroid(context: Context) {
    initializeDatabase(context)
    initializeDataStore(context)
    initKoin()
}
