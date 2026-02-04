package com.dadomatch.shared.core

actual object PlatformEnv {
    actual fun get(key: String): String? = System.getenv(key)
}
