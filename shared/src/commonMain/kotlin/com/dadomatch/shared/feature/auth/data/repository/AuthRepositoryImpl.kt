package com.dadomatch.shared.feature.auth.data.repository

import com.dadomatch.shared.feature.auth.domain.repository.AuthRepository
import com.dadomatch.shared.feature.auth.domain.repository.AuthUser
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.OAuthProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl : AuthRepository {
    private fun getAuthSafe(): dev.gitlive.firebase.auth.FirebaseAuth? = try {
        Firebase.auth
    } catch (e: Exception) {
        null
    }

    override val currentUser: Flow<AuthUser?> = flow {
        val auth = getAuthSafe()
        if (auth == null) {
            emit(null)
        } else {
            auth.authStateChanged.collect { user ->
                emit(user?.let {
                    AuthUser(
                        id = it.uid,
                        email = it.email,
                        displayName = it.displayName,
                        isAnonymous = it.isAnonymous
                    )
                })
            }
        }
    }

    override suspend fun signInWithGoogle(idToken: String, accessToken: String?): Result<AuthUser> = try {
        val auth = getAuthSafe() ?: throw IllegalStateException(ERROR_FIREBASE_NOT_INITIALIZED)
        val result = auth.signInWithCredential(GoogleAuthProvider.credential(idToken, accessToken))
        val user = result.user!!
        Result.success(AuthUser(user.uid, user.email, user.displayName, user.isAnonymous))
    } catch (e: Exception) {
        val message = when {
            e is IllegalStateException -> ERROR_MSG_FIREBASE_NOT_INIT
            e.message?.contains(CODE_BAD_AUTHENTICATION) == true -> ERROR_MSG_BAD_AUTH
            else -> e.message ?: ERROR_MSG_GOOGLE_UNKNOWN
        }
        Result.failure(Exception(message, e))
    }

    override suspend fun signInWithApple(idToken: String, nonce: String?, displayName: String?): Result<AuthUser> = try {
        val auth = getAuthSafe() ?: throw IllegalStateException(ERROR_FIREBASE_NOT_INITIALIZED)
        if (idToken.isEmpty()) {
            val user = auth.currentUser ?: throw IllegalStateException("User not signed in after web flow")
            Result.success(AuthUser(user.uid, user.email, user.displayName, user.isAnonymous))
        } else {
            // Named args are required: the signature is
            // credential(providerId, accessToken, idToken, rawNonce). Passing the
            // identity token positionally lands it in accessToken, leaving idToken
            // null, which Firebase rejects with INVALID_CREDENTIAL_OR_PROVIDER_ID.
            val result = auth.signInWithCredential(
                OAuthProvider.credential(providerId = PROVIDER_APPLE, idToken = idToken, rawNonce = nonce)
            )
            val user = result.user!!
            // Apple delivers the name only on the first authorization and Firebase
            // does not persist it from the credential, so set it ourselves when the
            // profile has none yet.
            if (user.displayName.isNullOrBlank() && !displayName.isNullOrBlank()) {
                runCatching { user.updateProfile(displayName = displayName) }
            }
            Result.success(AuthUser(user.uid, user.email, user.displayName ?: displayName, user.isAnonymous))
        }
    } catch (e: Exception) {
        Result.failure(Exception("$ERROR_MSG_APPLE_PREFIX${e.message}", e))
    }

    override suspend fun signInAnonymously(): Result<AuthUser> = try {
        val auth = getAuthSafe() ?: throw IllegalStateException(ERROR_FIREBASE_NOT_INITIALIZED)
        val result = auth.signInAnonymously()
        val user = result.user!!
        Result.success(AuthUser(user.uid, user.email, user.displayName, user.isAnonymous))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun signOut() {
        getAuthSafe()?.signOut()
    }

    override suspend fun updateDisplayName(name: String): Result<AuthUser> = try {
        val auth = getAuthSafe() ?: throw IllegalStateException(ERROR_FIREBASE_NOT_INITIALIZED)
        val user = auth.currentUser ?: throw IllegalStateException(ERROR_NO_CURRENT_USER)
        user.updateProfile(displayName = name)
        // updateProfile does not fire authStateChanged, so return the fresh value
        // for the caller to reflect immediately.
        Result.success(AuthUser(user.uid, user.email, name, user.isAnonymous))
    } catch (e: Exception) {
        Result.failure(Exception("$ERROR_MSG_UPDATE_NAME_PREFIX${e.message}", e))
    }

    override suspend fun getSignInProviderId(): String? {
        val user = getAuthSafe()?.currentUser ?: return null
        // providerData lists the federated providers (apple.com / google.com);
        // it excludes the "firebase" pseudo-provider, but filter defensively.
        return user.providerData.firstOrNull { it.providerId != PROVIDER_FIREBASE }?.providerId
    }

    override suspend fun deleteAccount(
        providerId: String,
        idToken: String,
        accessToken: String?,
        nonce: String?
    ): Result<Unit> = try {
        val auth = getAuthSafe() ?: throw IllegalStateException(ERROR_FIREBASE_NOT_INITIALIZED)
        val user = auth.currentUser ?: throw IllegalStateException(ERROR_NO_CURRENT_USER)
        // Firebase blocks delete() on a stale session (requires-recent-login), so
        // reauthenticate with a fresh credential before deleting.
        val credential = when (providerId) {
            PROVIDER_GOOGLE -> GoogleAuthProvider.credential(idToken, accessToken)
            PROVIDER_APPLE -> OAuthProvider.credential(providerId = PROVIDER_APPLE, idToken = idToken, rawNonce = nonce)
            else -> null
        }
        if (credential != null) {
            user.reauthenticate(credential)
        }
        user.delete()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(Exception("$ERROR_MSG_DELETE_PREFIX${e.message}", e))
    }

    companion object {
        private const val PROVIDER_APPLE = "apple.com"
        private const val PROVIDER_GOOGLE = "google.com"
        private const val PROVIDER_FIREBASE = "firebase"

        private const val ERROR_FIREBASE_NOT_INITIALIZED = "Firebase not initialized"
        private const val ERROR_MSG_FIREBASE_NOT_INIT = "Error: Firebase is not initialized. Check google-services.json or GoogleService-Info.plist"
        private const val CODE_BAD_AUTHENTICATION = "BAD_AUTHENTICATION"
        private const val ERROR_MSG_BAD_AUTH = "Authentication error: Check Firebase configuration (SHA-1/Web Client ID)."
        private const val ERROR_MSG_GOOGLE_UNKNOWN = "Unknown error in Google Sign-In"
        private const val ERROR_MSG_APPLE_PREFIX = "Error in Apple Sign-In: "
        private const val ERROR_NO_CURRENT_USER = "No user is currently signed in"
        private const val ERROR_MSG_DELETE_PREFIX = "Error deleting account: "
        private const val ERROR_MSG_UPDATE_NAME_PREFIX = "Error updating name: "
    }
}
