package com.dadomatch.shared.core.log

import android.util.Log

actual object AppLogger {
    actual fun debug(tag: String, message: String) {
        Log.d(tag, message)
    }

    actual fun warn(tag: String, message: String, throwable: Throwable?) {
        Log.w(tag, message, throwable)
    }
}
