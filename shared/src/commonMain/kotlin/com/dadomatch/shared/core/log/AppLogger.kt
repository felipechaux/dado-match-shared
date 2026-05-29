package com.dadomatch.shared.core.log

/**
 * Minimal cross-platform logger so shared code can surface diagnostics in the
 * platform console (Logcat on Android, NSLog/Xcode console on iOS).
 *
 * Intended for low-volume, high-signal messages — e.g. confirming a telemetry
 * call reached the SDK, or that a best-effort [runCatching] silently failed.
 */
expect object AppLogger {
    fun debug(tag: String, message: String)
    fun warn(tag: String, message: String, throwable: Throwable? = null)
}
