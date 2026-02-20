package com.dadomatch.shared.feature.auth.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadomatch.shared.feature.auth.presentation.viewmodel.AuthViewModel
import com.dadomatch.shared.presentation.ui.theme.DeepDarkBlue
import com.dadomatch.shared.presentation.ui.theme.NeonCyan
import com.dadomatch.shared.presentation.ui.theme.TextGray
import com.dadomatch.shared.presentation.ui.theme.TextWhite
import com.dadomatch.shared.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun AuthBottomSheet(
    viewModel: AuthViewModel,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Glassmorphism container
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E1E2C).copy(alpha = 0.9f),
                        DeepDarkBlue.copy(alpha = 0.98f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.3f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            )
            .padding(top = 12.dp, start = 24.dp, end = 24.dp, bottom = 48.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Indicator
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.2f))
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(Res.string.auth_title),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 28.sp,
                    letterSpacing = (-0.5).sp
                ),
                color = TextWhite,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(Res.string.auth_description),
                style = MaterialTheme.typography.bodyLarge,
                color = TextGray,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = NeonCyan,
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 4.dp
                )
                Spacer(modifier = Modifier.height(40.dp))
            } else {
                // Apple Button (Strict guidelines usually require specific styling)
//                SocialAuthButton(
//                    text = stringResource(Res.string.auth_apple_button),
//                    icon = "",
//                    backgroundColor = Color.White,
//                    contentColor = Color.Black,
//                    onClick = { viewModel.triggerAppleSignIn() }
//                )
//
//                Spacer(modifier = Modifier.height(16.dp))

                // Google Button
                SocialAuthButton(
                    text = stringResource(Res.string.auth_google_button),
                    icon = "G",
                    backgroundColor = Color.White,
                    contentColor = Color.Black,
                    onClick = { viewModel.triggerGoogleSignIn() },
                    isGoogle = true
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = stringResource(Res.string.auth_terms_condition),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )

                if (uiState.error != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = uiState.error ?: "",
                        color = Color(0xFFFF4D4D),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
    
    // Auto-dismiss when user is authenticated
    LaunchedEffect(uiState.user) {
        if (uiState.user != null) {
            kotlinx.coroutines.delay(800) // Show success briefly
            onDismiss()
        }
    }
}

@Composable
fun SocialAuthButton(
    text: String,
    icon: String,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    isGoogle: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (isGoogle) {
            // Google custom "G" or just the text icon for now
            Text(
                text = icon,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF4285F4) // Google Blue
            )
        } else {
            Text(
                text = icon,
                fontSize = 24.sp,
                fontWeight = FontWeight.Normal,
                color = contentColor
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = contentColor,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}
