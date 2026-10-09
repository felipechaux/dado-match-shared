package com.dadomatch.shared.feature.engagement.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.dadomatch.shared.feature.engagement.domain.NotificationKind
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.time.Instant
import kotlinx.datetime.LocalDate

/** Engagement state (streak, last open) and notification settings, in the app's DataStore. */
class EngagementLocalDataSource(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val LAST_OPENED_AT = longPreferencesKey("engagement_last_opened_at")
        val STREAK_DAYS = intPreferencesKey("engagement_streak_days")
        val LAST_ACTIVE_DAY = longPreferencesKey("engagement_last_active_epoch_day")
        val PERMISSION_ASKED = booleanPreferencesKey("notifications_permission_asked")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")

        fun kindEnabledKey(kind: NotificationKind) = booleanPreferencesKey("notifications_${kind.name.lowercase()}_enabled")
    }

    data class Streak(val days: Int, val lastActiveDay: LocalDate?)

    val streak: Flow<Streak> = dataStore.data.map { prefs ->
        Streak(
            days = prefs[STREAK_DAYS] ?: 0,
            lastActiveDay = prefs[LAST_ACTIVE_DAY]?.let { LocalDate.fromEpochDays(it) },
        )
    }

    /** Master switch plus one switch per reminder type; everything starts on. */
    val settings: Flow<NotificationSettings> = dataStore.data.map { prefs ->
        NotificationSettings(
            enabled = prefs[NOTIFICATIONS_ENABLED] ?: true,
            kinds = NotificationKind.entries.filter { prefs[kindEnabledKey(it)] ?: true }.toSet(),
        )
    }

    suspend fun lastOpenedAt(): Instant? =
        dataStore.data.first()[LAST_OPENED_AT]?.let { Instant.fromEpochMilliseconds(it) }

    suspend fun setLastOpenedAt(at: Instant) {
        dataStore.edit { it[LAST_OPENED_AT] = at.toEpochMilliseconds() }
    }

    suspend fun setStreak(days: Int, lastActiveDay: LocalDate) {
        dataStore.edit {
            it[STREAK_DAYS] = days
            it[LAST_ACTIVE_DAY] = lastActiveDay.toEpochDays()
        }
    }

    /** True the first time only — the permission prompt is shown once. */
    suspend fun markPermissionAsked(): Boolean {
        var firstTime = false
        dataStore.edit {
            firstTime = it[PERMISSION_ASKED] != true
            it[PERMISSION_ASKED] = true
        }
        return firstTime
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setKindEnabled(kind: NotificationKind, enabled: Boolean) {
        dataStore.edit { it[kindEnabledKey(kind)] = enabled }
    }
}

data class NotificationSettings(
    val enabled: Boolean,
    val kinds: Set<NotificationKind>,
)
