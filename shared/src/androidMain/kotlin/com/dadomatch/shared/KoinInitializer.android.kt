package com.dadomatch.shared

import android.content.Context
import com.dadomatch.shared.core.data.initializeDataStore
import com.dadomatch.shared.core.data.initializeDatabase
import dev.gitlive.firebase.initialize
import org.koin.android.ext.koin.androidContext

fun initKoinAndroid(context: Context) {
    dev.gitlive.firebase.Firebase.initialize(context)
    initializeDatabase(context)
    initializeDataStore(context)
    
    initKoin {
        androidContext(context)
    }
}
