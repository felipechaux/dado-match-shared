package com.dadomatch.shared.feature.game.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dadomatch.shared.feature.game.domain.GameVariant
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate

/** Last group of players (per variant) and the free daily game count, in the app's DataStore. */
class GameLocalDataSource(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val GAMES_DAY = longPreferencesKey("game_games_epoch_day")
        val GAMES_COUNT = intPreferencesKey("game_games_count")
        // Names can't contain it: it is a control character the text field never produces
        const val SEPARATOR = "\u001F"

        fun playersKey(variant: GameVariant) = stringPreferencesKey("game_players_${variant.name.lowercase()}")
    }

    suspend fun players(variant: GameVariant): List<String> =
        dataStore.data.first()[playersKey(variant)]
            ?.split(SEPARATOR)
            ?.filter { it.isNotBlank() }
            .orEmpty()

    suspend fun setPlayers(variant: GameVariant, players: List<String>) {
        dataStore.edit { it[playersKey(variant)] = players.joinToString(SEPARATOR) }
    }

    suspend fun gamesStartedOn(day: LocalDate): Int {
        val prefs = dataStore.data.first()
        return if (prefs[GAMES_DAY] == day.toEpochDays()) prefs[GAMES_COUNT] ?: 0 else 0
    }

    suspend fun recordGameStarted(day: LocalDate) {
        dataStore.edit {
            val sameDay = it[GAMES_DAY] == day.toEpochDays()
            it[GAMES_COUNT] = (if (sameDay) it[GAMES_COUNT] ?: 0 else 0) + 1
            it[GAMES_DAY] = day.toEpochDays()
        }
    }
}
