package com.dadomatch.shared.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadomatch.shared.feature.subscription.domain.model.Product
import com.dadomatch.shared.feature.subscription.domain.model.SubscriptionStatus
import com.dadomatch.shared.feature.subscription.domain.model.SubscriptionTier
import com.dadomatch.shared.feature.subscription.domain.usecase.GetAvailableProductsUseCase
import com.dadomatch.shared.feature.subscription.domain.usecase.GetSubscriptionStatusUseCase
import com.dadomatch.shared.feature.subscription.domain.usecase.PurchaseSubscriptionUseCase
import com.dadomatch.shared.feature.subscription.domain.usecase.RestorePurchasesUseCase
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SubscriptionViewModel(
    private val getSubscriptionStatusUseCase: GetSubscriptionStatusUseCase,
    private val getAvailableProductsUseCase: GetAvailableProductsUseCase,
    private val purchaseSubscriptionUseCase: PurchaseSubscriptionUseCase,
    private val restorePurchasesUseCase: RestorePurchasesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SubscriptionEvent>(
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<SubscriptionEvent> = _events.asSharedFlow()

    init {
        loadSubscriptionData()
        observeSubscriptionStatus()
    }

    private fun observeSubscriptionStatus() {
        viewModelScope.launch {
            getSubscriptionStatusUseCase().collect { status ->
                val previousTier = _uiState.value.subscriptionStatus?.tier
                _uiState.update { it.copy(subscriptionStatus = status) }
                if (previousTier == SubscriptionTier.FREE && status.tier == SubscriptionTier.PREMIUM) {
                    _events.emit(SubscriptionEvent.ShowConfetti)
                }
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

    fun purchaseProduct(productId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isPurchasing = true, error = null) }
            val result = purchaseSubscriptionUseCase(productId)
            if (result.isSuccess) {
                _uiState.update { it.copy(isPurchasing = false) }
                _events.emit(SubscriptionEvent.PurchaseSuccess)
                loadSubscriptionData()
            } else {
                _uiState.update {
                    it.copy(
                        isPurchasing = false,
                        error = result.exceptionOrNull()?.message ?: "Purchase failed"
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
                _events.emit(SubscriptionEvent.RestoreSuccess)
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
    val isPurchasing: Boolean = false,
    val isRestoring: Boolean = false,
    val error: String? = null
)

sealed class SubscriptionEvent {
    data object PurchaseSuccess : SubscriptionEvent()
    data object RestoreSuccess : SubscriptionEvent()
    data object ShowConfetti : SubscriptionEvent()
}
