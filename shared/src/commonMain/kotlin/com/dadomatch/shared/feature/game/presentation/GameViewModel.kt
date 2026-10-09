package com.dadomatch.shared.feature.game.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadomatch.shared.feature.game.domain.GameConfig
import com.dadomatch.shared.feature.game.domain.GameContext
import com.dadomatch.shared.feature.game.domain.GameRules
import com.dadomatch.shared.feature.game.domain.GameSession
import com.dadomatch.shared.feature.game.domain.GameTone
import com.dadomatch.shared.feature.game.domain.GameTurn
import com.dadomatch.shared.feature.game.domain.GameVariant
import com.dadomatch.shared.feature.game.domain.repository.GameRepository
import com.dadomatch.shared.feature.game.domain.usecase.FetchChallengesUseCase
import com.dadomatch.shared.feature.game.domain.usecase.StartGameResult
import com.dadomatch.shared.feature.game.domain.usecase.StartGameUseCase
import com.dadomatch.shared.feature.icebreaker.data.telemetry.AiTelemetry
import com.dadomatch.shared.feature.icebreaker.data.telemetry.NoOpAiTelemetry
import com.dadomatch.shared.feature.subscription.domain.model.SubscriptionTier
import com.dadomatch.shared.feature.subscription.domain.usecase.GetLanguageUseCase
import com.dadomatch.shared.feature.subscription.domain.usecase.GetSubscriptionStatusUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel(
    private val startGameUseCase: StartGameUseCase,
    private val fetchChallengesUseCase: FetchChallengesUseCase,
    private val repository: GameRepository,
    private val getSubscriptionStatusUseCase: GetSubscriptionStatusUseCase,
    private val getLanguageUseCase: GetLanguageUseCase,
    private val telemetry: AiTelemetry = NoOpAiTelemetry,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var session: GameSession? = null

    /** Same safety net as HomeViewModel: errors go to Crashlytics and never leave a spinner on. */
    private fun CoroutineScope.safeLaunch(stage: String, block: suspend () -> Unit): Job =
        launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                telemetry.onUnexpectedError(stage = stage, cause = e)
                _uiState.update { it.copy(isLoading = false, rolling = false) }
            }
        }

    init {
        viewModelScope.launch {
            getSubscriptionStatusUseCase().collect { status ->
                _uiState.update { it.copy(isPro = status.tier != SubscriptionTier.FREE) }
            }
        }
        loadPlayers(GameVariant.PARTY)
    }

    fun loadLanguage(deviceLanguage: String) {
        viewModelScope.launch {
            getLanguageUseCase(deviceLanguage).collect { lang -> _uiState.update { it.copy(language = lang) } }
        }
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    fun selectVariant(variant: GameVariant) {
        if (variant == _uiState.value.variant) return
        _uiState.update { it.copy(variant = variant, context = GameContext.NONE, players = emptyList()) }
        loadPlayers(variant)
    }

    /** A Pro tone picked by a free user opens the paywall instead. */
    fun selectTone(tone: GameTone) {
        if (tone.requiresPro && !_uiState.value.isPro) {
            _uiState.update { it.copy(openPaywall = true) }
            return
        }
        _uiState.update { it.copy(tone = tone) }
    }

    fun selectContext(context: GameContext) {
        _uiState.update { it.copy(context = context) }
    }

    fun addPlayer(name: String) {
        val clean = name.trim().take(GameRules.MAX_NAME_LENGTH)
        _uiState.update { state ->
            val duplicate = state.players.any { it.equals(clean, ignoreCase = true) }
            if (clean.isEmpty() || duplicate || !GameRules.canAddPlayer(state.variant, state.players.size)) state
            else state.copy(players = state.players + clean)
        }
    }

    fun removePlayer(name: String) {
        _uiState.update { it.copy(players = it.players - name) }
    }

    private fun loadPlayers(variant: GameVariant) {
        viewModelScope.safeLaunch(stage = "game_vm.loadPlayers") {
            val saved = repository.savedPlayers(variant)
            // Only if the user hasn't started typing names meanwhile
            _uiState.update { if (it.variant == variant && it.players.isEmpty()) it.copy(players = saved) else it }
        }
    }

    // ── Game ──────────────────────────────────────────────────────────────────

    fun startGame() {
        val state = _uiState.value
        if (!state.canStart || state.isLoading) return
        val config = GameConfig(
            variant = state.variant,
            tone = state.tone,
            context = state.context,
            players = state.players,
            language = state.language,
        )
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.safeLaunch(stage = "game_vm.startGame") {
            when (val result = startGameUseCase(config)) {
                StartGameResult.ToneLocked -> _uiState.update { it.copy(isLoading = false, openPaywall = true) }
                StartGameResult.DailyLimitReached -> _uiState.update { it.copy(isLoading = false, showDailyLimit = true) }
                is StartGameResult.Started -> {
                    val newSession = GameSession(
                        config = config,
                        isPro = result.isPro,
                        fetch = { level, avoid -> fetchChallengesUseCase(config, level, avoid) },
                    )
                    newSession.prepare()
                    session = newSession
                    _uiState.update { it.copy(isLoading = false, phase = GamePhase.PLAYING, turn = null) }
                }
            }
        }
    }

    /** Starts the dice animation; the turn is revealed in [onRollComplete]. */
    fun roll() {
        val state = _uiState.value
        if (state.rolling || state.isLoading || state.phase != GamePhase.PLAYING) return
        if (session?.hasNextTurn != true) {
            _uiState.update { it.copy(phase = GamePhase.FINISHED) }
            return
        }
        _uiState.update { it.copy(rolling = true) }
    }

    fun onRollComplete() {
        val current = session ?: return
        viewModelScope.safeLaunch(stage = "game_vm.nextTurn") {
            val turn = current.nextTurn()
            _uiState.update {
                if (turn == null) it.copy(rolling = false, phase = GamePhase.FINISHED)
                else it.copy(rolling = false, turn = turn, roundsLeft = roundsLeft(current))
            }
            current.prefetch()
        }
    }

    fun endGame() {
        session = null
        _uiState.update { it.copy(phase = GamePhase.SETUP, turn = null, rolling = false, roundsLeft = null) }
    }

    private fun roundsLeft(session: GameSession): Int? =
        if (_uiState.value.isPro) null else GameRules.FREE_ROUNDS_PER_GAME - session.round

    // ── Overlays ──────────────────────────────────────────────────────────────

    fun onPaywallOpened() {
        _uiState.update { it.copy(openPaywall = false) }
    }

    fun dismissDailyLimit() {
        _uiState.update { it.copy(showDailyLimit = false) }
    }
}

enum class GamePhase { SETUP, PLAYING, FINISHED }

data class GameUiState(
    val phase: GamePhase = GamePhase.SETUP,
    val variant: GameVariant = GameVariant.PARTY,
    val tone: GameTone = GameTone.FUNNY,
    val context: GameContext = GameContext.NONE,
    val players: List<String> = emptyList(),
    val language: String = "en",
    val isPro: Boolean = false,
    val isLoading: Boolean = false,
    val rolling: Boolean = false,
    val turn: GameTurn? = null,
    /** Free games only. */
    val roundsLeft: Int? = null,
    val showDailyLimit: Boolean = false,
    /** One-shot: the screen navigates to the paywall and calls onPaywallOpened(). */
    val openPaywall: Boolean = false,
) {
    val canStart: Boolean get() = GameRules.canStart(variant, players.size)
}
