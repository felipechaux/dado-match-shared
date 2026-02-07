package com.dadomatch.shared.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadomatch.shared.domain.model.Product
import com.dadomatch.shared.domain.model.SubscriptionStatus
import com.dadomatch.shared.domain.usecase.GetAvailableProductsUseCase
import com.dadomatch.shared.domain.usecase.GetSubscriptionStatusUseCase
import com.dadomatch.shared.domain.usecase.PurchaseSubscriptionUseCase
import com.dadomatch.shared.domain.usecase.RestorePurchasesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for subscription screen
 */
class SubscriptionViewModel(
    private val getSubscriptionStatusUseCase: GetSubscriptionStatusUseCase,
    private val getAvailableProductsUseCase: GetAvailableProductsUseCase,
    private val purchaseSubscriptionUseCase: PurchaseSubscriptionUseCase,
    private val restorePurchasesUseCase: RestorePurchasesUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<SubscriptionUiState>(SubscriptionUiState.Loading)
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()
    
    private val _subscriptionStatus = MutableStateFlow<SubscriptionStatus?>(null)
    val subscriptionStatus: StateFlow<SubscriptionStatus?> = _subscriptionStatus.asStateFlow()
    
    init {
        loadSubscriptionData()
        observeSubscriptionStatus()
    }
    
    private fun observeSubscriptionStatus() {
        viewModelScope.launch {
            getSubscriptionStatusUseCase().collect { status ->
                _subscriptionStatus.value = status
            }
        }
    }
    
    private fun loadSubscriptionData() {
        viewModelScope.launch {
            _uiState.value = SubscriptionUiState.Loading
            
            // Explicitly fetch latest status to ensure sync
            getSubscriptionStatusUseCase.getCurrentStatus()
            
            val productsResult = getAvailableProductsUseCase()
            
            if (productsResult.isSuccess) {
                val products = productsResult.getOrNull() ?: emptyList()
                _uiState.value = SubscriptionUiState.Success(products)
            } else {
                _uiState.value = SubscriptionUiState.Error(
                    productsResult.exceptionOrNull()?.message ?: "Failed to load products"
                )
            }
        }
    }
    
    fun purchaseProduct(productId: String) {
        viewModelScope.launch {
            _uiState.value = SubscriptionUiState.Purchasing
            
            val result = purchaseSubscriptionUseCase(productId)
            
            if (result.isSuccess) {
                _uiState.value = SubscriptionUiState.PurchaseSuccess
                // Reload products after successful purchase
                loadSubscriptionData()
            } else {
                _uiState.value = SubscriptionUiState.PurchaseError(
                    result.exceptionOrNull()?.message ?: "Purchase failed"
                )
            }
        }
    }
    
    fun restorePurchases() {
        viewModelScope.launch {
            _uiState.value = SubscriptionUiState.Restoring
            
            val result = restorePurchasesUseCase()
            
            if (result.isSuccess) {
                _uiState.value = SubscriptionUiState.RestoreSuccess
                loadSubscriptionData()
            } else {
                _uiState.value = SubscriptionUiState.RestoreError(
                    result.exceptionOrNull()?.message ?: "Restore failed"
                )
            }
        }
    }
    
    fun dismissError() {
        loadSubscriptionData()
    }
}

/**
 * UI state for subscription screen
 */
sealed class SubscriptionUiState {
    data object Loading : SubscriptionUiState()
    data class Success(val products: List<Product>) : SubscriptionUiState()
    data class Error(val message: String) : SubscriptionUiState()
    data object Purchasing : SubscriptionUiState()
    data object PurchaseSuccess : SubscriptionUiState()
    data class PurchaseError(val message: String) : SubscriptionUiState()
    data object Restoring : SubscriptionUiState()
    data object RestoreSuccess : SubscriptionUiState()
    data class RestoreError(val message: String) : SubscriptionUiState()
}
