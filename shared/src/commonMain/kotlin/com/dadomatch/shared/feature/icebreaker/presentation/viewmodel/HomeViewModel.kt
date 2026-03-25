package com.dadomatch.shared.feature.icebreaker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadomatch.shared.feature.icebreaker.domain.model.IcebreakerFeedback
import com.dadomatch.shared.feature.icebreaker.domain.usecase.GenerateIcebreakerUseCase
import com.dadomatch.shared.feature.icebreaker.domain.usecase.NoRollsRemainingException
import com.dadomatch.shared.feature.icebreaker.domain.usecase.RollDiceUseCase
import com.dadomatch.shared.feature.icebreaker.domain.usecase.SubmitFeedbackUseCase
import com.dadomatch.shared.feature.onboarding.domain.usecase.GetOnboardingStatusUseCase
import com.dadomatch.shared.feature.onboarding.domain.usecase.SetOnboardingStatusUseCase
import com.dadomatch.shared.feature.subscription.domain.model.SubscriptionTier
import com.dadomatch.shared.feature.subscription.domain.usecase.CheckEntitlementUseCase
import com.dadomatch.shared.feature.subscription.domain.usecase.GetLanguageUseCase
import com.dadomatch.shared.feature.subscription.domain.usecase.GetSubscriptionStatusUseCase
import com.dadomatch.shared.feature.success.domain.model.SuccessRecord
import com.dadomatch.shared.feature.success.domain.usecase.AddSuccessUseCase
import com.dadomatch.shared.core.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val generateIcebreakerUseCase: GenerateIcebreakerUseCase,
    private val submitFeedbackUseCase: SubmitFeedbackUseCase,
    private val addSuccessUseCase: AddSuccessUseCase,
    private val rollDiceUseCase: RollDiceUseCase,
    private val checkEntitlementUseCase: CheckEntitlementUseCase,
    private val getSubscriptionStatusUseCase: GetSubscriptionStatusUseCase,
    private val getOnboardingStatusUseCase: GetOnboardingStatusUseCase,
    private val setOnboardingStatusUseCase: SetOnboardingStatusUseCase,
    private val getLanguageUseCase: GetLanguageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeSubscriptionStatus()
        observeOnboardingStatus()
    }

    // ── Language ──────────────────────────────────────────────────────────────

    fun loadLanguage(deviceLanguage: String) {
        viewModelScope.launch {
            getLanguageUseCase(deviceLanguage).collect { lang ->
                _uiState.update { it.copy(selectedLanguage = lang) }
            }
        }
    }

    // ── Subscription / Onboarding ─────────────────────────────────────────────

    private fun observeOnboardingStatus() {
        viewModelScope.launch {
            getOnboardingStatusUseCase().collect { isCompleted ->
                _uiState.update { it.copy(showOnboarding = !isCompleted, isOnboardingReady = true) }
            }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            setOnboardingStatusUseCase(true)
            _uiState.update { it.copy(showOnboarding = false) }
        }
    }

    private fun observeSubscriptionStatus() {
        viewModelScope.launch {
            getSubscriptionStatusUseCase().collect { status ->
                _uiState.update {
                    it.copy(
                        isPremium = status.tier != SubscriptionTier.FREE,
                        dailyRollsRemaining = if (status.tier == SubscriptionTier.FREE) status.dailyRollsRemaining else null
                    )
                }
            }
        }
    }

    // ── Roll Flow ─────────────────────────────────────────────────────────────

    fun onRollComplete(environment: String, intensity: String, language: String) {
        _uiState.update {
            it.copy(
                isLoading = true,
                error = null,
                lastEnvironment = environment,
                lastIntensity = intensity,
                lastLanguage = language
            )
        }
        viewModelScope.launch {
            val rollResult = rollDiceUseCase()
            if (rollResult.isFailure) {
                val exception = rollResult.exceptionOrNull()
                if (exception is NoRollsRemainingException) {
                    _uiState.update { it.copy(isLoading = false, showPaywallNudge = true) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = exception?.message) }
                }
                return@launch
            }

            if (!checkEntitlementUseCase.canAccessCategory(intensity)) {
                _uiState.update { it.copy(isLoading = false, showPaywallNudge = true) }
                return@launch
            }

            when (val result = generateIcebreakerUseCase(environment, intensity, language)) {
                is Resource.Success ->
                    _uiState.update { it.copy(isLoading = false, icebreaker = result.data) }
                is Resource.Error -> {
                    val msg = result.message
                    when {
                        msg == "no_ai_calls_available" || msg == "daily_ai_limit_reached" ->
                            _uiState.update { it.copy(isLoading = false, showPaywallNudge = true) }
                        msg == "rate_limit_exceeded" ->
                            _uiState.update { it.copy(isLoading = false, rateLimitError = true) }
                        msg == "no_internet" ->
                            _uiState.update { it.copy(isLoading = false, noInternetError = true) }
                        else ->
                            _uiState.update { it.copy(isLoading = false, error = msg) }
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun onRetryRoll() {
        val state = _uiState.value
        onRollComplete(state.lastEnvironment, state.lastIntensity, state.lastLanguage)
    }

    // ── Icebreaker Dialog ─────────────────────────────────────────────────────

    fun dismissIcebreaker() {
        _uiState.update { it.copy(icebreaker = null) }
    }

    // ── Action Choice ─────────────────────────────────────────────────────────

    fun dismissActionChoice() {
        _uiState.update { it.copy(actionChoiceIcebreaker = null) }
    }

    fun copyToClipboard(@Suppress("UNUSED_PARAMETER") text: String) {
        // Clipboard is handled by ActionChoiceDialog internally via LocalClipboardManager.
        // This method signals completion and closes the action sheet.
        _uiState.update { it.copy(actionChoiceIcebreaker = null) }
    }

    fun shareIcebreaker(@Suppress("UNUSED_PARAMETER") text: String) {
        // Platform share intent is handled by expect/actual; dismiss the sheet.
        _uiState.update { it.copy(actionChoiceIcebreaker = null) }
    }

    // ── Feedback Dialog ───────────────────────────────────────────────────────

    fun showFeedbackDialog() {
        _uiState.update { it.copy(showFeedbackDialog = true) }
    }

    fun dismissFeedbackDialog() {
        _uiState.update { it.copy(showFeedbackDialog = false) }
    }

    fun submitFeedback(rating: Int, comment: String) {
        viewModelScope.launch {
            val state = _uiState.value
            val feedback = if (rating >= 3) IcebreakerFeedback.GOOD else IcebreakerFeedback.BAD
            val now = kotlin.time.Clock.System.now()
            submitFeedbackUseCase(state.icebreaker ?: state.lastEnvironment, feedback)
            addSuccessUseCase(
                SuccessRecord(
                    id = now.toEpochMilliseconds().toString(),
                    date = now,
                    environment = state.lastEnvironment,
                    intensity = state.lastIntensity,
                    icebreaker = state.icebreaker ?: "",
                    wasSuccessful = feedback == IcebreakerFeedback.GOOD
                )
            )
            _uiState.update { it.copy(showFeedbackDialog = false) }
        }
    }

    // ── Auth Sheet ────────────────────────────────────────────────────────────

    fun showAuth() {
        _uiState.update { it.copy(showAuthSheet = true) }
    }

    fun dismissAuth() {
        _uiState.update { it.copy(showAuthSheet = false) }
    }

    // ── Paywall Nudge ─────────────────────────────────────────────────────────

    fun dismissPaywallNudge() {
        _uiState.update { it.copy(showPaywallNudge = false) }
    }

    // ── Error ─────────────────────────────────────────────────────────────────

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearRateLimitError() {
        _uiState.update { it.copy(rateLimitError = false) }
    }

    fun clearNoInternetError() {
        _uiState.update { it.copy(noInternetError = false) }
    }
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val icebreaker: String? = null,
    val actionChoiceIcebreaker: String? = null,
    val showFeedbackDialog: Boolean = false,
    val isPremium: Boolean = false,
    val dailyRollsRemaining: Int? = null,
    val error: String? = null,
    val rateLimitError: Boolean = false,
    val noInternetError: Boolean = false,
    val isOnboardingReady: Boolean = false,
    val showOnboarding: Boolean = false,
    val showAuthSheet: Boolean = false,
    val showPaywallNudge: Boolean = false,
    val lastEnvironment: String = "",
    val lastIntensity: String = "",
    val lastLanguage: String = "en",
    val selectedLanguage: String = "en"
)
