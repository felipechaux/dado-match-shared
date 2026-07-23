package com.dadomatch.shared.feature.auth.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<AuthUser?>
    suspend fun signInWithGoogle(idToken: String, accessToken: String? = null): Result<AuthUser>
    suspend fun signInWithApple(idToken: String, nonce: String? = null, displayName: String? = null): Result<AuthUser>
    suspend fun signInAnonymously(): Result<AuthUser>
    suspend fun signOut()

    /**
     * Updates the current user's Firebase display name. Needed because Apple only
     * returns the name on the very first authorization, so users who signed in
     * before that name was captured (or who declined it) have no name to show.
     */
    suspend fun updateDisplayName(name: String): Result<AuthUser>

    /**
     * Provider the current user signed in with ("google.com" / "apple.com"), or
     * null if there is no user or no federated provider. Used to pick which
     * native flow to re-run for the reauthentication that account deletion needs.
     */
    suspend fun getSignInProviderId(): String?

    /**
     * Permanently deletes the current Firebase account. Firebase requires a
     * recent login to delete, so a fresh credential (built from freshly obtained
     * native tokens) is used to reauthenticate first. Satisfies App Store
     * guideline 5.1.1(v).
     */
    suspend fun deleteAccount(
        providerId: String,
        idToken: String,
        accessToken: String? = null,
        nonce: String? = null
    ): Result<Unit>
}

data class AuthUser(
    val id: String,
    val email: String?,
    val displayName: String?,
    val isAnonymous: Boolean
)
