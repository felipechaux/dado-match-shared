package com.dadomatch.shared.core

expect object PlatformEnv {
    fun get(key: String): String?
}
