package com.dadomatch.shared.core

import platform.Foundation.NSProcessInfo

actual object PlatformEnv {
    actual fun get(key: String): String? = 
        NSProcessInfo.processInfo.environment[key] as? String
}
