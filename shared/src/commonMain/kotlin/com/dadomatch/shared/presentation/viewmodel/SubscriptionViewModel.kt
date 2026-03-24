package com.dadomatch.shared.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadomatch.shared.feature.subscription.domain.model.Product
import com.dadomatch.shared.feature.subscription.domain.model.SubscriptionStatus
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

    fun restorePurchases() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRestoring = true, error = null) }
            val result = restorePurchasesUseCase()
            if (result.isSuccess) {
                _uiState.update { it.copy(isRestoring = false) }
                loadSubscriptionData()
            } else {
                _uiState.update {
                    it.copy(
                        isRestoring = false,
                        error = result.exceptionOrNull()?.message ?: "Restore failed"
                    )
                }
            }
        }
    }

    fun refreshStatus() {
        loadSubscriptionData()
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class SubscriptionUiState(
    val isLoading: Boolean = false,
    val products: List<Product> = emptyList(),
    val subscriptionStatus: SubscriptionStatus? = null,
    val isRestoring: Boolean = false,
    val error: String? = null
)
