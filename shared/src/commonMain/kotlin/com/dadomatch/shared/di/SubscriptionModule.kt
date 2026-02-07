package com.dadomatch.shared.di

import com.dadomatch.shared.data.local.SubscriptionLocalDataSource
import com.dadomatch.shared.data.remote.RevenueCatService
import com.dadomatch.shared.data.repository.SubscriptionRepositoryImpl
import com.dadomatch.shared.domain.repository.SubscriptionRepository
import com.dadomatch.shared.domain.usecase.*
import com.dadomatch.shared.presentation.viewmodel.SubscriptionViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Feature: Subscriptions and Entitlements
 */
val subscriptionModule = module {
    single { RevenueCatService() }
    single { SubscriptionLocalDataSource(get()) }
    singleOf(::SubscriptionRepositoryImpl) bind SubscriptionRepository::class
    
    factoryOf(::GetSubscriptionStatusUseCase)
    factoryOf(::CheckEntitlementUseCase)
    factoryOf(::PurchaseSubscriptionUseCase)
    factoryOf(::RestorePurchasesUseCase)
    factoryOf(::GetAvailableProductsUseCase)
    factoryOf(::RollDiceUseCase)
    
    viewModelOf(::SubscriptionViewModel)
}
