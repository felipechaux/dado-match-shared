package com.dadomatch.shared.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadomatch.shared.core.Resource
import com.dadomatch.shared.domain.usecase.GenerateIcebreakerUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val generateIcebreakerUseCase: GenerateIcebreakerUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onRollComplete(environment: String, intensity: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = generateIcebreakerUseCase(environment, intensity)) {
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

    fun dismissIcebreaker() {
        _uiState.update { it.copy(showIcebreaker = false) }
    }
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val showIcebreaker: Boolean = false,
    val currentIcebreaker: String = "",
    val error: String? = null
)
