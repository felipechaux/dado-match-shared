package com.dadomatch.shared.presentation.ui

import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
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

    val newConfiguration = remember(configuration, newLocale) {
        Configuration(configuration).apply { setLocale(newLocale) }
    }

    // createConfigurationContext() returns a bare context that is not backed by the
    // Activity, so anything that unwraps LocalContext to find it (RevenueCat's Paywall
    // needs it to launch the Play billing flow) would get null and silently do nothing.
    // Wrapping the original context keeps the Activity reachable while still serving
    // the localized resources.
    val newContext = remember(context, newConfiguration) {
        val localizedResources = context.createConfigurationContext(newConfiguration).resources
        object : ContextWrapper(context) {
            override fun getResources(): Resources = localizedResources
        }
    }

    CompositionLocalProvider(
        LocalContext provides newContext,
        LocalConfiguration provides newConfiguration
    ) {
        content()
    }
}
