package com.dadomatch.shared.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadomatch.shared.feature.subscription.domain.model.Product
import com.dadomatch.shared.feature.subscription.domain.model.SubscriptionStatus
import com.dadomatch.shared.feature.subscription.domain.model.SubscriptionTier
import com.dadomatch.shared.feature.subscription.domain.usecase.GetAvailableProductsUseCase
import com.dadomatch.shared.feature.subscription.domain.usecase.GetSubscriptionStatusUseCase
import com.dadomatch.shared.feature.subscription.domain.usecase.RestorePurchasesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SubscriptionViewModel(
    private val getSubscriptionStatusUseCase: GetSubscriptionStatusUseCase,
    private val getAvailableProductsUseCase: GetAvailableProductsUseCase,
    private val restorePurchasesUseCase: RestorePurchasesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    init {
        loadSubscriptionData()
        observeSubscriptionStatus()
    }

    private fun observeSubscriptionStatus() {
        viewModelScope.launch {
            getSubscriptionStatusUseCase().collect { status ->
                _uiState.update { it.copy(subscriptionStatus = status) }
            }
        }
    }

    private fun loadSubscriptionData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getSubscriptionStatusUseCase.getCurrentStatus()
            val productsResult = getAvailableProductsUseCase()
            if (productsResult.isSuccess) {
                _uiState.update {
                    it.copy(isLoading = false, products = productsResult.getOrNull() ?: emptyList())
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = productsResult.exceptionOrNull()?.message ?: "Failed to load products"
                    )
                }
            }
        }
    }

    /**
     * Restoring is reachable without an account (App Store guideline 5.1.1(v)), so it has
     * to report back on its own: a silent restore is indistinguishable from a dead button.
     * A successful call that finds nothing is NOT an error — it means this Apple ID simply
     * has no purchase to recover — so the three outcomes stay distinct.
     */
    fun restorePurchases() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRestoring = true, error = null, restoreOutcome = null) }
            restorePurchasesUseCase()
                .onSuccess { status ->
                    _uiState.update {
                        it.copy(
                            isRestoring = false,
                            restoreOutcome = if (status.tier == SubscriptionTier.PREMIUM) {
                                RestoreOutcome.RESTORED
                            } else {
                                RestoreOutcome.NOTHING_FOUND
                            }
                        )
                    }
                    loadSubscriptionData()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isRestoring = false,
                            restoreOutcome = RestoreOutcome.FAILED,
                            error = error.message ?: "Restore failed"
                        )
                    }
                }
        }
    }

    fun consumeRestoreOutcome() {
        _uiState.update { it.copy(restoreOutcome = null) }
    }

    fun refreshStatus() {
        loadSubscriptionData()
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}

/** Outcome of the last restore attempt, surfaced to the user exactly once. */
enum class RestoreOutcome { RESTORED, NOTHING_FOUND, FAILED }

data class SubscriptionUiState(
    val isLoading: Boolean = false,
    val products: List<Product> = emptyList(),
    val subscriptionStatus: SubscriptionStatus? = null,
    val isRestoring: Boolean = false,
    val restoreOutcome: RestoreOutcome? = null,
    val error: String? = null
)
