package com.dadomatch.shared.presentation.ui

import androidx.compose.runtime.Composable
import platform.Foundation.NSUserDefaults

@Composable
actual fun LocaleProvider(languageCode: String, content: @Composable () -> Unit) {
    NSUserDefaults.standardUserDefaults.setObject(listOf(languageCode), forKey = "AppleLanguages")
    content()
}
