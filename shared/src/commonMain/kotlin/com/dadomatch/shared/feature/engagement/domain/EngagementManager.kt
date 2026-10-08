package com.dadomatch.shared.feature.engagement.domain

import androidx.compose.ui.text.intl.Locale
import com.dadomatch.shared.feature.engagement.data.EngagementLocalDataSource
import com.dadomatch.shared.feature.engagement.data.NotificationSettings
import com.dadomatch.shared.feature.engagement.data.PushTopics
import com.dadomatch.shared.feature.subscription.data.local.SubscriptionLocalDataSource
import com.dadomatch.shared.feature.subscription.domain.model.SubscriptionTier
import com.dadomatch.shared.feature.subscription.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

/**
 * Keeps the on-device reminders and the FCM topics in line with what the user does.
 * Called when the app comes to the foreground, after every roll and when a
 * notification setting changes; each call re-plans everything from scratch.
 */
class EngagementManager(
    private val notifier: LocalNotifier,
    private val local: EngagementLocalDataSource,
    private val subscriptionRepository: SubscriptionRepository,
    private val subscriptionLocal: SubscriptionLocalDataSource,
    private val pushTopics: PushTopics,
    private val clock: Clock = Clock.System,
) {
    private val mutex = Mutex()

    val settings: Flow<NotificationSettings> = local.settings

    /** The streak as it stands today: 0 once a whole day has passed without a roll. */
    val currentStreak: Flow<Int> = local.streak.map { streak ->
        val today = today()
        val last = streak.lastActiveDay
        if (last != null && (last == today || last == today.minus(1, DateTimeUnit.DAY))) streak.days else 0
    }

    suspend fun onAppForeground() {
        local.setLastOpenedAt(clock.now())
        refresh()
    }

    /** [generated] = the roll ended in an icebreaker the user actually saw. */
    suspend fun onRollFinished(generated: Boolean) {
        if (generated) {
            recordActiveToday()
            // Users who finished onboarding before reminders existed are asked here,
            // right after seeing a result
            askPermissionOnce()
        }
        refresh()
    }

    /**
     * Shows the system prompt the first time only: the OS allows very few asks, so it
     * is spent when the user has context (end of onboarding, or first icebreaker).
     */
    suspend fun askPermissionOnce() {
        if (local.markPermissionAsked()) {
            notifier.requestPermission()
            refresh()
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        if (enabled) notifier.requestPermission()
        local.setNotificationsEnabled(enabled)
        refresh()
    }

    suspend fun setKindEnabled(kind: NotificationKind, enabled: Boolean) {
        local.setKindEnabled(kind, enabled)
        refresh()
    }

    suspend fun isPermissionGranted(): Boolean = notifier.isPermissionGranted()

    private suspend fun recordActiveToday() {
        val today = today()
        val streak = local.streak.first()
        val days = when (streak.lastActiveDay) {
            today -> return
            today.minus(1, DateTimeUnit.DAY) -> streak.days + 1
            else -> 1
        }
        local.setStreak(days, today)
    }

    private suspend fun refresh() = mutex.withLock {
        val settings = local.settings.first()
        val language = subscriptionLocal.getLanguage(Locale.current.language.take(2)).first()
        val isPro = subscriptionRepository.getSubscriptionStatus().first().tier != SubscriptionTier.FREE
        pushTopics.sync(settings.enabled, language, isPro)

        if (!settings.enabled || !notifier.isPermissionGranted()) {
            notifier.replaceAll(emptyList())
            return@withLock
        }

        val streak = local.streak.first()
        val lastOpenedAt = local.lastOpenedAt()
        val state = EngagementState(
            lastOpenedAt = lastOpenedAt,
            streakDays = streak.days,
            lastActiveDay = streak.lastActiveDay,
            rollsRefillAt = subscriptionRepository.rollsRefillAtMillis()?.let { Instant.fromEpochMilliseconds(it) },
            enabledKinds = settings.kinds,
        )
        val sampleIndex = lastOpenedAt?.toLocalDateTime(TimeZone.currentSystemDefault())?.date?.toEpochDays()?.toInt() ?: 0
        val notifications = EngagementPlanner.plan(state, clock.now(), TimeZone.currentSystemDefault()).map { planned ->
            val text = NotificationCopy.forKind(planned.kind, language, streak.days, sampleIndex)
            LocalNotification(
                id = "${planned.kind.name.lowercase()}_${planned.fireAt.toEpochMilliseconds()}",
                title = text.title,
                body = text.body,
                fireAt = planned.fireAt,
            )
        }
        notifier.replaceAll(notifications)
    }

    private fun today(): LocalDate = clock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
}
