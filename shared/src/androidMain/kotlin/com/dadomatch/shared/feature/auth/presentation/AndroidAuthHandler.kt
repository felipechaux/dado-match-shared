package com.dadomatch.shared.feature.auth.presentation

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.dadomatch.shared.BuildKonfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidAuthHandler(
    private val context: Context
) : NativeAuthHandler {

    override suspend fun signInWithGoogle(): Result<GoogleTokens> = withContext(Dispatchers.Main) {
        try {
            // Diagnostic logging
            println("🔍 Google Sign-In Debug Info:")
            println("   Package Name: ${context.packageName}")
            println("   Web Client ID: ${BuildKonfig.GOOGLE_WEB_CLIENT_ID}")
            
            val credentialManager = CredentialManager.create(context)
            
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(BuildKonfig.GOOGLE_WEB_CLIENT_ID)
                .setAutoSelectEnabled(false) // Allow user to choose account
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            println("🔍 Requesting credentials...")
            val result = credentialManager.getCredential(
                context = context,
                request = request
            )
            println("✅ Credentials received!")
            println("   Credential type: ${result.credential.type}")

            val credential = result.credential
            
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdTokenCredential.idToken
            
            println("✅ ID Token extracted successfully!")
            // Android Credential Manager only provides an idToken — no accessToken.
            // Firebase Android SDK accepts null for accessToken; only iOS requires it.
            Result.success(GoogleTokens(idToken = idToken, accessToken = null))
        } catch (e: GetCredentialException) {
            Result.failure(Exception(e.message, e))
        } catch (e: Exception) {
            Result.failure(Exception("Unexpected error during Google Sign-In: ${e.message}", e))
        }
    }

    override suspend fun signInWithApple(): Result<AuthTokens> {
        return try {
            val deferred = kotlinx.coroutines.CompletableDeferred<Result<AuthTokens>>()
            AppleSignInActivity.deferredResult = deferred
            
            val intent = android.content.Intent(context, AppleSignInActivity::class.java).apply {
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            context.startActivity(intent)
            
            deferred.await()
        } catch (e: Exception) {
            Result.failure(Exception("Apple Sign-In launch failed on Android: ${e.message}", e))
        }
    }
}
