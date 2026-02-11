package com.dadomatch.shared.core.config

import com.dadomatch.shared.BuildKonfig

/**
 * Environment configuration helper
 * Provides easy access to environment-specific settings
 */
object EnvironmentConfig {
    
    /**
     * Current environment name (stage or production)
     */
    val environment: String = BuildKonfig.ENVIRONMENT
    
    /**
     * Whether this is a debug/development build
     */
    val isDebug: Boolean = BuildKonfig.IS_DEBUG
    
    /**
     * Whether this is production environment
     */
    val isProduction: Boolean = environment == "production"
    
    /**
     * Whether this is stage environment
     */
    val isStage: Boolean = environment == "stage"
    
    /**
     * Gemini API Key for current environment
     */
    val geminiApiKey: String = BuildKonfig.GEMINI_API_KEY
    
    /**
     * Gemini Model Name for current environment
     */
    val geminiModelName: String = BuildKonfig.GEMINI_MODEL_NAME
    
    /**
     * RevenueCat API Key for current environment
     */
    val revenueCatApiKey: String = BuildKonfig.REVENUECAT_API_KEY
    
    /**
     * API Base URL for current environment
     */
    val apiBaseUrl: String = BuildKonfig.API_BASE_URL
    
    /**
     * Get environment display name with emoji indicator
     */
    fun getEnvironmentDisplay(): String {
        return when (environment) {
            "production" -> "🟢 Production"
            "stage" -> "🟡 Stage"
            else -> "⚪ Unknown"
        }
    }
    
    /**
     * Log current environment configuration (for debugging)
     */
    fun logConfig() {
        println("=================================")
        println("Environment: ${getEnvironmentDisplay()}")
        println("Is Debug: $isDebug")
        println("API Base URL: $apiBaseUrl")
        println("Gemini Model: $geminiModelName")
        println("RevenueCat Key: ${if (revenueCatApiKey.isNotEmpty()) "***configured***" else "NOT SET"}")
        println("Gemini Key: ${if (geminiApiKey.isNotEmpty()) "***configured***" else "NOT SET"}")
        println("=================================")
    }
}
