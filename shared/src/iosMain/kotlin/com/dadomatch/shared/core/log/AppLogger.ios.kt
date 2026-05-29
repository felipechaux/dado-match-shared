package com.dadomatch.shared.core.log

import platform.Foundation.NSLog

actual object AppLogger {
    actual fun debug(tag: String, message: String) {
        // Pass through %@ so a message containing % is never treated as a format spec.
        NSLog("%@", "[$tag] $message")
    }

    actual fun warn(tag: String, message: String, throwable: Throwable?) {
        val suffix = throwable?.let { " | ${it::class.simpleName}: ${it.message}" }.orEmpty()
        NSLog("%@", "[$tag] ⚠️ $message$suffix")
    }
}
