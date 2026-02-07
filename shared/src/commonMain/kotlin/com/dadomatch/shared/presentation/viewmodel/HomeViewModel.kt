package com.dadomatch.shared.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadomatch.shared.core.Resource
import com.dadomatch.shared.domain.model.IcebreakerFeedback
import com.dadomatch.shared.domain.model.SuccessRecord
import com.dadomatch.shared.domain.usecase.AddSuccessUseCase
import com.dadomatch.shared.domain.usecase.CheckEntitlementUseCase
import com.dadomatch.shared.domain.usecase.GenerateIcebreakerUseCase
import com.dadomatch.shared.domain.usecase.NoRollsRemainingException
import com.dadomatch.shared.domain.usecase.RollDiceUseCase
import com.dadomatch.shared.domain.usecase.SubmitFeedbackUseCase
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
    private val checkEntitlementUseCase: CheckEntitlementUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeSubscriptionStatus()
    }

    private fun observeSubscriptionStatus() {
        viewModelScope.launch {
            checkEntitlementUseCase.subscriptionRepository.getSubscriptionStatus().collect { status ->
                _uiState.update { it.copy(isPremium = status.tier == com.dadomatch.shared.domain.model.SubscriptionTier.PREMIUM) }
            }
        }
    }

    private var currentEnvironment: String = ""
    private var currentIntensity: String = ""
    private var currentLanguage: String = "en"

    fun onRollComplete(environment: String, intensity: String, language: String) {
        currentEnvironment = environment
        currentIntensity = intensity
        currentLanguage = language
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            // First check if user can roll
            val rollResult = rollDiceUseCase()
            if (rollResult.isFailure) {
                val exception = rollResult.exceptionOrNull()
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        showPaywallNavigation = exception is NoRollsRemainingException,
                        error = if (exception is NoRollsRemainingException) null else exception?.message
                    ) 
                }
                return@launch
            }

            // Check category entitlements (Spicy)
            if (!checkEntitlementUseCase.canAccessCategory(intensity)) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        showPaywallNavigation = true
                    )
                }
                return@launch
            }

            when (val result = generateIcebreakerUseCase(environment, intensity, language)) {
                is Resource.Success -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            currentIcebreaker = result.data,
                            showIcebreaker = true
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
                Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun onPaywallNavigated() {
        _uiState.update { it.copy(showPaywallNavigation = false) }
    }

    fun onIcebreakerDismissed() {
        _uiState.update { it.copy(showIcebreaker = false, showActionChoices = true) }
    }

    fun onActionChoice(used: Boolean) {
        _uiState.update { it.copy(showActionChoices = false) }
        viewModelScope.launch {
            if (used) {
                // If used, we ask for feedback
                _uiState.update { it.copy(showFeedbackDialog = true) }
            } else {
                // If not used, we just reset
                resetDialogs()
            }
        }
    }

    fun onSubmitFeedback(feedback: IcebreakerFeedback) {
        _uiState.update { it.copy(showFeedbackDialog = false) }
        viewModelScope.launch {
            submitFeedbackUseCase(uiState.value.currentIcebreaker, feedback)
            
            // If feedback is good or used, we record it as a success
            val wasSuccessful = feedback == IcebreakerFeedback.GOOD || feedback == IcebreakerFeedback.USED
            addSuccessUseCase(
                SuccessRecord(
                    id = kotlin.time.Clock.System.now().toEpochMilliseconds().toString(),
                    date = kotlin.time.Clock.System.now(),
                    environment = currentEnvironment,
                    intensity = currentIntensity,
                    icebreaker = uiState.value.currentIcebreaker,
                    wasSuccessful = wasSuccessful
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

    fun onRetryRoll() {
        onRollComplete(currentEnvironment, currentIntensity, currentLanguage)
    }

    private fun resetDialogs() {
        _uiState.update { 
            it.copy(
                showIcebreaker = false,
                showActionChoices = false,
                showFeedbackDialog = false
            ) 
        }
    }
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val showIcebreaker: Boolean = false,
    val showActionChoices: Boolean = false,
    val showFeedbackDialog: Boolean = false,
    val showPaywallNavigation: Boolean = false,
    val isPremium: Boolean = false,
    val currentIcebreaker: String = "",
    val error: String? = null
)
