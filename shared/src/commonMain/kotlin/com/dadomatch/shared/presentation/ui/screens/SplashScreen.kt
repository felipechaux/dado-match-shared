package com.dadomatch.shared.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dadomatch.shared.presentation.ui.theme.DeepDarkBlue
import com.dadomatch.shared.shared.generated.resources.Res
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieAnimation
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun SplashScreen(onNavigateToHome: () -> Unit) {
    val jsonString = remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        try {
            val bytes = Res.readBytes("files/cube_splash.json")
            jsonString.value = bytes.decodeToString()
        } catch (e: Exception) {
            println("Error loading Lottie: ${e.message}")
        }
        
        delay(3000) // 3 seconds splash
        onNavigateToHome()
    }

    val comp by rememberLottieComposition(
        if (jsonString.value != null) 
            LottieCompositionSpec.JsonString(jsonString.value!!) 
        else 
            LottieCompositionSpec.JsonString("{}")
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepDarkBlue),
        contentAlignment = Alignment.Center
    ) {
        if (jsonString.value != null) {
            LottieAnimation(
                composition = comp,
                modifier = Modifier.size(300.dp),
                iterations = Compottie.IterateForever
            )
        }
    }
}
