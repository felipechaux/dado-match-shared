package com.dadomatch.shared.feature.subscription.domain.usecase

import com.dadomatch.shared.feature.subscription.data.local.SubscriptionLocalDataSource

class SetLanguageUseCase(private val localDataSource: SubscriptionLocalDataSource) {
    suspend operator fun invoke(language: String) = localDataSource.setLanguage(language)
}
