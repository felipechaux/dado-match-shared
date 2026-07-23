package com.dadomatch.shared.core.log

import platform.Foundation.NSLog

actual object AppLogger {
    actual fun debug(tag: String, message: String) {
        log("[$tag] $message")
    }

    actual fun warn(tag: String, message: String, throwable: Throwable?) {
        val suffix = throwable?.let { " | ${it::class.simpleName}: ${it.message}" }.orEmpty()
        log("[$tag] ⚠️ $message$suffix")
    }

    // Passing a Kotlin String as an NSLog "%@" vararg is not safely bridged to
    // NSString on Kotlin/Native and crashes with EXC_BAD_ACCESS. Instead build
    // the whole line in Kotlin and pass it as the single format string, escaping
    // "%" so no character in the message is read as a format specifier.
    private fun log(line: String) {
        NSLog(line.replace("%", "%%"))
    }
}
