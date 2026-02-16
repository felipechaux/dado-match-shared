package com.dadomatch.shared.feature.auth.presentation

class IosAuthHandler : NativeAuthHandler {
    override suspend fun signInWithGoogle(): Result<String> {
        return Result.failure(Exception("Google Sign-In not implemented for iOS in shared module yet"))
    }

    override suspend fun signInWithApple(): Result<AuthTokens> {
        return Result.failure(Exception("Apple Sign-In not implemented for iOS in shared module yet"))
    }
}
