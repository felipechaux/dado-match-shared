package com.dadomatch.shared.feature.subscription.domain.usecase

import com.dadomatch.shared.feature.subscription.data.local.SubscriptionLocalDataSource
import kotlinx.coroutines.flow.Flow

class GetLanguageUseCase(private val localDataSource: SubscriptionLocalDataSource) {
    operator fun invoke(deviceLanguage: String): Flow<String> =
        localDataSource.getLanguage(deviceLanguage)
}
