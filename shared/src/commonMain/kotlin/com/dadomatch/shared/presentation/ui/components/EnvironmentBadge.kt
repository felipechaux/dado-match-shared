package com.dadomatch.shared.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadomatch.shared.core.config.EnvironmentConfig

/**
 * Environment badge to display in non-production builds
 * Shows which environment the app is currently running in
 */
@Composable
fun EnvironmentBadge(
    modifier: Modifier = Modifier
) {
    // Only show in non-production builds
    if (!EnvironmentConfig.isProduction) {
        Box(
            modifier = modifier
                .padding(8.dp)
                .background(
                    color = if (EnvironmentConfig.isStage) {
                        Color(0xFFFFA726) // Orange for stage
                    } else {
                        Color(0xFF66BB6A) // Green for other
                    },
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = EnvironmentConfig.getEnvironmentDisplay(),
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Example: Using environment config in a screen
 */
@Composable
fun ExampleEnvironmentUsage() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        // Show environment badge
        EnvironmentBadge()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Display environment info (for debugging)
        if (EnvironmentConfig.isDebug) {
            Text("Environment: ${EnvironmentConfig.environment}")
            Text("API Base URL: ${EnvironmentConfig.apiBaseUrl}")
            Text("Model: ${EnvironmentConfig.geminiModelName}")
        }
        
        // Environment-specific logic
        when {
            EnvironmentConfig.isProduction -> {
                // Production-specific features
                Text("Running in production mode")
            }
            EnvironmentConfig.isStage -> {
                // Stage-specific features (e.g., debug menu, test data)
                Text("Running in stage mode - Debug features enabled")
            }
        }
    }
}
