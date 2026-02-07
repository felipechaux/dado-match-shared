package com.dadomatch.shared.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadomatch.shared.domain.model.SuccessRecord
import com.dadomatch.shared.domain.usecase.GetSuccessesUseCase
import kotlinx.coroutines.flow.*

class SuccessesViewModel(
    getSuccessesUseCase: GetSuccessesUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SuccessesUiState())
    val uiState: StateFlow<SuccessesUiState> = _uiState.asStateFlow()

    init {
        getSuccessesUseCase()
            .onStart { _uiState.update { it.copy(isLoading = true) } }
            .onEach { successes ->
                _uiState.update { it.copy(successes = successes, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }
}

data class SuccessesUiState(
    val successes: List<SuccessRecord> = emptyList(),
    val isLoading: Boolean = true
)
