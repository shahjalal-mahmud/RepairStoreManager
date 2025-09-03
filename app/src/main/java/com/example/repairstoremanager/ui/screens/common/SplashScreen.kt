package com.example.repairstoremanager.ui.screens.common

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.repairstoremanager.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit,
    errorMessage: String? = null
) {
    var scale by remember { mutableStateOf(0f) }
    val animScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = tween(
            durationMillis = 1000,
            easing = { OvershootInterpolator(2f).getInterpolation(it) }
        ), label = "ScaleAnim"
    )

    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.splash_animation)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        speed = 1.0f
    )

    // Scale animation trigger
    LaunchedEffect(Unit) { scale = 1f }

    // Timeout for navigation or error handling
    var timedOut by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(3000) // e.g. 3s splash
        if (errorMessage == null) {
            onTimeout() // ✅ Navigate to Dashboard/Login
        } else {
            timedOut = true // show error if any
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier
                    .size(300.dp)
                    .scale(animScale)
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Repair Store Manager",
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(30.dp))

            when {
                errorMessage != null || timedOut -> {
                    Text(
                        text = errorMessage ?: "Network issue, please try again.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp
                    )
                }
                else -> {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}