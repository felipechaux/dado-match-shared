package com.dadomatch.shared.presentation.ui

import androidx.compose.runtime.Composable

/**
 * Wraps [content] so that all [stringResource] calls resolve strings for [languageCode].
 * On Android this overrides [LocalConfiguration] so that `Locale.current` returns the
 * correct locale and compose-resources picks the right strings.xml folder.
 */
@Composable
expect fun LocaleProvider(languageCode: String, content: @Composable () -> Unit)
