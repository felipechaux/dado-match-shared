package com.dadomatch.shared.presentation.ui.extensions

import androidx.compose.runtime.Composable
import com.dadomatch.shared.feature.subscription.domain.model.Product
import com.dadomatch.shared.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * Get a localized human-readable subscription period
 */
@Composable
fun Product.getLocalizedPeriod(): String? = subscriptionPeriod?.getLocalizedPeriod()

/**
 * Get a localized human-readable subscription period from a raw string
 */
@Composable
fun String.getLocalizedPeriod(): String? {
    val res = when (this) {
        "P1M" -> Res.string.period_monthly
        "P1Y" -> Res.string.period_yearly
        "P1W" -> Res.string.period_weekly
        else -> null
    }
    
    return if (res != null) stringResource(res) else this
}

