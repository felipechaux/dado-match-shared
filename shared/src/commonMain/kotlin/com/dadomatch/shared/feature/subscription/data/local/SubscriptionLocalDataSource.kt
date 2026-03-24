package com.dadomatch.shared.feature.subscription.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Local data source for caching subscription data
 */
class SubscriptionLocalDataSource(
    private val dataStore: DataStore<Preferences>
) {
    
    companion object {
        private val DAILY_ROLLS_REMAINING = intPreferencesKey("daily_rolls_remaining")
        private val LAST_ROLL_RESET_DATE = longPreferencesKey("last_roll_reset_date")
        private val SUBSCRIPTION_TIER = stringPreferencesKey("subscription_tier")
        private val IS_PREMIUM = stringPreferencesKey("is_premium")
        private val DAILY_AI_CALLS_REMAINING = intPreferencesKey("daily_ai_calls_remaining")
        private val LAST_AI_CALLS_RESET_DATE = longPreferencesKey("last_ai_calls_reset_date")
        private val PREFERRED_LANGUAGE = stringPreferencesKey("preferred_language")

        const val DEFAULT_DAILY_ROLLS = 10
        // -1 sentinel means "not yet initialized — use tier default on first read"
        const val AI_CALLS_UNSET = -1
    }
    
    /**
     * Get daily rolls remaining
     */
    fun getDailyRollsRemaining(): Flow<Int> {
        return dataStore.data.map { preferences ->
            preferences[DAILY_ROLLS_REMAINING] ?: DEFAULT_DAILY_ROLLS
        }
    }
    
    /**
     * Set daily rolls remaining
     */
    suspend fun setDailyRollsRemaining(count: Int) {
        dataStore.edit { preferences ->
            preferences[DAILY_ROLLS_REMAINING] = count
        }
    }
    
    /**
     * Decrement daily rolls by 1
     */
    suspend fun decrementDailyRolls(): Int {
        var newCount = 0
        dataStore.edit { preferences ->
            val current = preferences[DAILY_ROLLS_REMAINING] ?: DEFAULT_DAILY_ROLLS
            newCount = maxOf(0, current - 1)
            preferences[DAILY_ROLLS_REMAINING] = newCount
        }
        return newCount
    }
    
    /**
     * Reset daily rolls to default
     */
    suspend fun resetDailyRolls() {
        dataStore.edit { preferences ->
            preferences[DAILY_ROLLS_REMAINING] = DEFAULT_DAILY_ROLLS
            preferences[LAST_ROLL_RESET_DATE] = kotlin.time.Clock.System.now().toEpochMilliseconds()
        }
    }
    
    /**
     * Get last reset date
     */
    fun getLastResetDate(): Flow<kotlin.time.Instant?> {
        return dataStore.data.map { preferences ->
            preferences[LAST_ROLL_RESET_DATE]?.let { kotlin.time.Instant.fromEpochMilliseconds(it) }
        }
    }
    
    /**
     * Check if daily rolls should be reset (new day)
     */
    suspend fun shouldResetDailyRolls(): Boolean {
        val lastReset = dataStore.data.map { it[LAST_ROLL_RESET_DATE] }.first()
        if (lastReset == null) return true
        
        val lastResetMillis: Long = lastReset
        val nowMillis: Long = kotlin.time.Clock.System.now().toEpochMilliseconds()
        
        // Check if it's a new day (simplified - you may want more sophisticated logic)
        val millisDiff: Long = nowMillis - lastResetMillis
        val dayInMillis: Long = 24L * 60L * 60L * 1000L
        
        return millisDiff > dayInMillis
    }
    
    // ── Daily AI Calls ─────────────────────────────────────────────────────────

    /** Returns raw stored AI calls count. -1 means "not yet initialised — use tier default". */
    fun getDailyAiCallsRemaining(): Flow<Int> {
        return dataStore.data.map { preferences ->
            preferences[DAILY_AI_CALLS_REMAINING] ?: AI_CALLS_UNSET
        }
    }

    suspend fun setDailyAiCallsRemaining(count: Int) {
        dataStore.edit { preferences ->
            preferences[DAILY_AI_CALLS_REMAINING] = count
        }
    }

    /** Decrements by 1 (floor 0) and returns the new count. */
    suspend fun decrementDailyAiCalls(): Int {
        var newCount = 0
        dataStore.edit { preferences ->
            val current = preferences[DAILY_AI_CALLS_REMAINING] ?: 0
            newCount = maxOf(0, current - 1)
            preferences[DAILY_AI_CALLS_REMAINING] = newCount
        }
        return newCount
    }

    /** Resets count to [maxCalls] and records the reset timestamp. */
    suspend fun resetDailyAiCalls(maxCalls: Int) {
        dataStore.edit { preferences ->
            preferences[DAILY_AI_CALLS_REMAINING] = maxCalls
            preferences[LAST_AI_CALLS_RESET_DATE] = kotlin.time.Clock.System.now().toEpochMilliseconds()
        }
    }

    /** Returns true if more than 24 h have passed since the last AI calls reset (or never reset). */
    suspend fun shouldResetDailyAiCalls(): Boolean {
        val lastReset = dataStore.data.map { it[LAST_AI_CALLS_RESET_DATE] }.first()
        if (lastReset == null) return true
        val millisDiff = kotlin.time.Clock.System.now().toEpochMilliseconds() - lastReset
        return millisDiff > 24L * 60L * 60L * 1000L
    }

    // ── Premium Status Cache ───────────────────────────────────────────────────

    /**
     * Cache premium status
     */
    suspend fun setPremiumStatus(isPremium: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_PREMIUM] = isPremium.toString()
        }
    }
    
    /**
     * Get cached premium status
     */
    fun getPremiumStatus(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[IS_PREMIUM]?.toBoolean() ?: false
        }
    }

    // ── Language Preference ────────────────────────────────────────────────

    /** Returns the user's preferred language code ("en" or "es"). Defaults to device locale if unset. */
    fun getLanguage(deviceLanguage: String): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[PREFERRED_LANGUAGE] ?: deviceLanguage
        }
    }

    suspend fun setLanguage(language: String) {
        dataStore.edit { preferences ->
            preferences[PREFERRED_LANGUAGE] = language
        }
    }
}
