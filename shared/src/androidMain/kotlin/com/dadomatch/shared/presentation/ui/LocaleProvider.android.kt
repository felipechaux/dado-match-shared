package com.dadomatch.shared.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

@Composable
actual fun LocaleProvider(languageCode: String, content: @Composable () -> Unit) {
    val configuration = LocalConfiguration.current
    val context = LocalContext.current

    val newLocale = remember(languageCode) { Locale.forLanguageTag(languageCode) }
    Locale.setDefault(newLocale)
    configuration.setLocale(newLocale)

    val newContext = remember(languageCode) {
        context.createConfigurationContext(configuration)
    }

    CompositionLocalProvider(
        LocalContext provides newContext,
        LocalConfiguration provides configuration
    ) {
        content()
    }
}
