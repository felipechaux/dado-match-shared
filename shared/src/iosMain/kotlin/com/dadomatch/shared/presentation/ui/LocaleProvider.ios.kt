package com.dadomatch.shared.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import platform.Foundation.NSUserDefaults

@Composable
actual fun LocaleProvider(languageCode: String, content: @Composable () -> Unit) {
    NSUserDefaults.standardUserDefaults.setObject(listOf(languageCode), forKey = "AppleLanguages")
    key(languageCode) {
        content()
    }
}
