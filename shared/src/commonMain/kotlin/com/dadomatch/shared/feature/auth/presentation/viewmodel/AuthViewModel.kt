package com.dadomatch.shared.feature.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadomatch.shared.feature.auth.domain.repository.AuthRepository
import com.dadomatch.shared.feature.auth.domain.repository.AuthUser
import com.dadomatch.shared.feature.auth.domain.usecase.SignInAnonymouslyUseCase
import com.dadomatch.shared.feature.auth.domain.usecase.SignInWithAppleUseCase
import com.dadomatch.shared.feature.auth.domain.usecase.SignInWithGoogleUseCase
import com.dadomatch.shared.feature.auth.presentation.NativeAuthHandler
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val signInWithAppleUseCase: SignInWithAppleUseCase,
    private val signInAnonymouslyUseCase: SignInAnonymouslyUseCase,
    private val authRepository: AuthRepository,
    private val nativeAuthHandler: NativeAuthHandler
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvent>(
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.update { it.copy(user = user, isInitialized = true) }
            }
        }
    }

    fun triggerGoogleSignIn() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            nativeAuthHandler.signInWithGoogle()
                .onSuccess { tokens ->
                    signInWithGoogleUseCase(tokens.idToken, tokens.accessToken)
                        .onSuccess { _events.emit(AuthEvent.SignInSuccess) }
                        .onFailure { error ->
                            _uiState.update { it.copy(isLoading = false, error = error.message) }
                        }
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
                    signInWithAppleUseCase(tokens.idToken, tokens.nonce)
                        .onSuccess { _events.emit(AuthEvent.SignInSuccess) }
                        .onFailure { error ->
                            _uiState.update { it.copy(isLoading = false, error = error.message) }
                        }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    fun signInAnonymously() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            signInAnonymouslyUseCase()
                .onSuccess { _events.emit(AuthEvent.SignInSuccess) }
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
    val isInitialized: Boolean = false,
    val user: AuthUser? = null,
    val error: String? = null
)

sealed class AuthEvent {
    data object SignInSuccess : AuthEvent()
}
