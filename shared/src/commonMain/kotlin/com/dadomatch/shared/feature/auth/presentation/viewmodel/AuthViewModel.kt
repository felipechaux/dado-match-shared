package com.dadomatch.shared.feature.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadomatch.shared.feature.auth.domain.repository.AuthRepository
import com.dadomatch.shared.feature.auth.domain.repository.AuthUser
import com.dadomatch.shared.feature.subscription.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.dadomatch.shared.feature.auth.presentation.NativeAuthHandler

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val nativeAuthHandler: NativeAuthHandler
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun triggerGoogleSignIn() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            nativeAuthHandler.signInWithGoogle()
                .onSuccess { idToken ->
                    signInWithGoogle(idToken)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    fun triggerAppleSignIn() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            nativeAuthHandler.signInWithApple()
                .onSuccess { tokens ->
                    signInWithApple(tokens.idToken, tokens.nonce)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    private fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            authRepository.signInWithGoogle(idToken)
                .onSuccess { user ->
                    subscriptionRepository.logIn(user.id)
                    _uiState.update { it.copy(isLoading = false, user = user) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    private fun signInWithApple(idToken: String, nonce: String? = null) {
        viewModelScope.launch {
            authRepository.signInWithApple(idToken, nonce)
                .onSuccess { user ->
                    subscriptionRepository.logIn(user.id)
                    _uiState.update { it.copy(isLoading = false, user = user) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    fun signInAnonymously() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            authRepository.signInAnonymously()
                .onSuccess { user ->
                    _uiState.update { it.copy(isLoading = false, user = user) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val user: AuthUser? = null,
    val error: String? = null
)
