package com.dadomatch.shared.feature.engagement.di

import com.dadomatch.shared.feature.engagement.data.EngagementLocalDataSource
import com.dadomatch.shared.feature.engagement.data.PushTopics
import com.dadomatch.shared.feature.engagement.domain.EngagementManager
import org.koin.dsl.module

/**
 * Feature: Engagement (reminders, streak, push topics). The platform
 * LocalNotifier comes from platformModule().
 */
val engagementModule = module {
    single { EngagementLocalDataSource(get()) }
    single { PushTopics() }
    single { EngagementManager(get(), get(), get(), get(), get()) }
}
