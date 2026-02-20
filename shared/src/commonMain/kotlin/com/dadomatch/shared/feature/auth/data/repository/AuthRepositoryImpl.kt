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

    override suspend fun signInWithApple(idToken: String, nonce: String?): Result<AuthUser> = try {
        val auth = getAuthSafe() ?: throw IllegalStateException(ERROR_FIREBASE_NOT_INITIALIZED)
        val result = auth.signInWithCredential(OAuthProvider.credential(PROVIDER_APPLE, idToken, null, nonce))
        val user = result.user!!
        Result.success(AuthUser(user.uid, user.email, user.displayName, user.isAnonymous))
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

    companion object {
        private const val PROVIDER_APPLE = "apple.com"
        
        private const val ERROR_FIREBASE_NOT_INITIALIZED = "Firebase not initialized"
        private const val ERROR_MSG_FIREBASE_NOT_INIT = "Error: Firebase is not initialized. Check google-services.json or GoogleService-Info.plist"
        private const val CODE_BAD_AUTHENTICATION = "BAD_AUTHENTICATION"
        private const val ERROR_MSG_BAD_AUTH = "Authentication error: Check Firebase configuration (SHA-1/Web Client ID)."
        private const val ERROR_MSG_GOOGLE_UNKNOWN = "Unknown error in Google Sign-In"
        private const val ERROR_MSG_APPLE_PREFIX = "Error in Apple Sign-In: "
    }
}
