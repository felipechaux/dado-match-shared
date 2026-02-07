package com.dadomatch.shared.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadomatch.shared.feature.success.domain.model.SuccessRecord
import com.dadomatch.shared.feature.subscription.domain.usecase.CheckEntitlementUseCase
import com.dadomatch.shared.feature.success.domain.usecase.GetSuccessesUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SuccessesViewModel(
    getSuccessesUseCase: GetSuccessesUseCase,
    private val checkEntitlementUseCase: CheckEntitlementUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SuccessesUiState())
    val uiState: StateFlow<SuccessesUiState> = _uiState.asStateFlow()

    init {
        observeAnalyticsEntitlement()
        
        getSuccessesUseCase()
            .onStart { _uiState.update { it.copy(isLoading = true) } }
            .onEach { successes ->
                _uiState.update { it.copy(successes = successes, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeAnalyticsEntitlement() {
        viewModelScope.launch {
            checkEntitlementUseCase.subscriptionRepository.getSubscriptionStatus().collect { status ->
                val hasAnalytics = status.entitlements.contains(com.dadomatch.shared.feature.subscription.domain.model.Entitlement.SUCCESS_ANALYTICS)
                _uiState.update { it.copy(isRestricted = !hasAnalytics) }
            }
        }
    }
}

data class SuccessesUiState(
    val successes: List<SuccessRecord> = emptyList(),
    val isLoading: Boolean = true,
    val isRestricted: Boolean = false
)
