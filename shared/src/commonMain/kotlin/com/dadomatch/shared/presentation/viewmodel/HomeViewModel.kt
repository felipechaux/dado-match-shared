package com.dadomatch.shared.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadomatch.shared.core.Resource
import com.dadomatch.shared.domain.model.IcebreakerFeedback
import com.dadomatch.shared.domain.model.SuccessRecord
import com.dadomatch.shared.domain.usecase.AddSuccessUseCase
import com.dadomatch.shared.domain.usecase.GenerateIcebreakerUseCase
import com.dadomatch.shared.domain.usecase.SubmitFeedbackUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val generateIcebreakerUseCase: GenerateIcebreakerUseCase,
    private val submitFeedbackUseCase: SubmitFeedbackUseCase,
    private val addSuccessUseCase: AddSuccessUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var currentEnvironment: String = ""
    private var currentIntensity: String = ""

    fun onRollComplete(environment: String, intensity: String, language: String) {
        currentEnvironment = environment
        currentIntensity = intensity
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
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
    val currentIcebreaker: String = "",
    val error: String? = null
)
