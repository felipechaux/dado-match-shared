package com.dadomatch.shared.feature.auth.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<AuthUser?>
    suspend fun signInWithGoogle(idToken: String, accessToken: String? = null): Result<AuthUser>
    suspend fun signInWithApple(idToken: String, nonce: String? = null): Result<AuthUser>
    suspend fun signInAnonymously(): Result<AuthUser>
    suspend fun signOut()
}

data class AuthUser(
    val id: String,
    val email: String?,
    val displayName: String?,
    val isAnonymous: Boolean
)
