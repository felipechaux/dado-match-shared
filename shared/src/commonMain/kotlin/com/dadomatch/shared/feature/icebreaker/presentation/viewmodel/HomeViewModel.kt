package com.dadomatch.shared.feature.icebreaker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadomatch.shared.core.util.Resource
import com.dadomatch.shared.feature.icebreaker.domain.model.IcebreakerFeedback
import com.dadomatch.shared.feature.icebreaker.domain.usecase.GenerateIcebreakerUseCase
import com.dadomatch.shared.feature.icebreaker.domain.usecase.NoRollsRemainingException
import com.dadomatch.shared.feature.icebreaker.domain.usecase.RollDiceUseCase
import com.dadomatch.shared.feature.icebreaker.domain.usecase.SubmitFeedbackUseCase
import com.dadomatch.shared.feature.onboarding.domain.usecase.GetOnboardingStatusUseCase
import com.dadomatch.shared.feature.onboarding.domain.usecase.SetOnboardingStatusUseCase
import com.dadomatch.shared.feature.subscription.domain.model.SubscriptionTier
import com.dadomatch.shared.feature.subscription.domain.usecase.CheckEntitlementUseCase
import com.dadomatch.shared.feature.subscription.domain.usecase.GetSubscriptionStatusUseCase
import com.dadomatch.shared.feature.success.domain.model.SuccessRecord
import com.dadomatch.shared.feature.success.domain.usecase.AddSuccessUseCase
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    private val setOnboardingStatusUseCase: SetOnboardingStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>(
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<HomeEvent> = _events.asSharedFlow()

    init {
        observeSubscriptionStatus()
        observeOnboardingStatus()
    }

    private fun observeOnboardingStatus() {
        viewModelScope.launch {
            getOnboardingStatusUseCase().collect { isCompleted ->
                _uiState.update { it.copy(showOnboarding = !isCompleted) }
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
                _uiState.update { it.copy(isPremium = status.tier == SubscriptionTier.PREMIUM) }
            }
        }
    }

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
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = if (exception is NoRollsRemainingException) null else exception?.message
                    )
                }
                if (exception is NoRollsRemainingException) _events.emit(HomeEvent.NavigateToPaywall)
                return@launch
            }

            if (!checkEntitlementUseCase.canAccessCategory(intensity)) {
                _uiState.update { it.copy(isLoading = false) }
                _events.emit(HomeEvent.NavigateToPaywall)
                return@launch
            }

            when (val result = generateIcebreakerUseCase(environment, intensity, language)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, currentIcebreaker = result.data, showIcebreaker = true)
                    }
                }
                is Resource.Error -> {
                    if (result.message == "no_ai_calls_available" || result.message == "daily_ai_limit_reached") {
                        _uiState.update { it.copy(isLoading = false) }
                        _events.emit(HomeEvent.NavigateToPaywall)
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                    }
                }
                Resource.Loading -> Unit // unreachable from a suspend call
            }
        }
    }

    fun onIcebreakerDismissed() {
        _uiState.update { it.copy(showIcebreaker = false, showActionChoices = true) }
    }

    fun onActionChoice(used: Boolean) {
        if (used) {
            _uiState.update { it.copy(showActionChoices = false, showFeedbackDialog = true) }
        } else {
            resetDialogs()
        }
    }

    fun onSubmitFeedback(feedback: IcebreakerFeedback) {
        _uiState.update { it.copy(showFeedbackDialog = false) }
        viewModelScope.launch {
            val state = _uiState.value
            val now = kotlin.time.Clock.System.now()
            submitFeedbackUseCase(state.currentIcebreaker, feedback)
            addSuccessUseCase(
                SuccessRecord(
                    id = now.toEpochMilliseconds().toString(),
                    date = now,
                    environment = state.lastEnvironment,
                    intensity = state.lastIntensity,
                    icebreaker = state.currentIcebreaker,
                    wasSuccessful = feedback == IcebreakerFeedback.GOOD || feedback == IcebreakerFeedback.USED
                )
            )
            resetDialogs()
        }
    }

    fun dismissIcebreaker() {
        _uiState.update { it.copy(showIcebreaker = false) }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    fun showAuth() {
        _uiState.update { it.copy(showAuth = true) }
    }

    fun hideAuth() {
        _uiState.update { it.copy(showAuth = false) }
    }

    fun onRetryRoll() {
        val state = _uiState.value
        onRollComplete(state.lastEnvironment, state.lastIntensity, state.lastLanguage)
    }

    private fun resetDialogs() {
        _uiState.update {
            it.copy(showIcebreaker = false, showActionChoices = false, showFeedbackDialog = false)
        }
    }
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val showIcebreaker: Boolean = false,
    val showActionChoices: Boolean = false,
    val showFeedbackDialog: Boolean = false,
    val isPremium: Boolean = false,
    val currentIcebreaker: String = "",
    val error: String? = null,
    val showOnboarding: Boolean = false,
    val showAuth: Boolean = false,
    val lastEnvironment: String = "",
    val lastIntensity: String = "",
    val lastLanguage: String = "en"
)

sealed class HomeEvent {
    data object NavigateToPaywall : HomeEvent()
}
