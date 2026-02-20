package com.dadomatch.shared.feature.auth.presentation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.CompletableDeferred

class AppleSignInActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val provider = OAuthProvider.newBuilder("apple.com").build()
        FirebaseAuth.getInstance()
            .startActivityForSignInWithProvider(this, provider)
            .addOnSuccessListener { result ->
                val cred = result.credential as? com.google.firebase.auth.OAuthCredential
                deferredResult?.complete(Result.success(AuthTokens(idToken = cred?.idToken ?: "", nonce = null)))
                finish()
            }
            .addOnFailureListener { e ->
                deferredResult?.complete(Result.failure(Exception("Apple Sign-In failed: ${e.message}", e)))
                finish()
            }
    }

    companion object {
        var deferredResult: CompletableDeferred<Result<AuthTokens>>? = null
    }
}
