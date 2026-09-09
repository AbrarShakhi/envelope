package com.abrarshakhi.envelope.common.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun LoadingScreen(
    isLoading: Boolean,
    onFinished: () -> Unit,
) {
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        animate(
            initialValue = 0f, targetValue = 0.70f,
            animationSpec = tween(
                durationMillis = 900,
                easing = FastOutSlowInEasing,
            ),
        ) { value, _ -> progress = value }

        animate(
            initialValue = 0.70f, targetValue = 0.85f,
            animationSpec = tween(
                durationMillis = 1800,
                easing = LinearOutSlowInEasing,
            ),
        ) { value, _ -> progress = value }

        animate(
            initialValue = 0.85f, targetValue = 0.90f,
            animationSpec = tween(
                durationMillis = 3000, easing = LinearOutSlowInEasing,
            ),
        ) { value, _ ->
            progress = value
        }
    }

    LaunchedEffect(isLoading) {
        if (!isLoading) {
            if (progress < 0.90f) {
                progress = 0.90f
            }

            animate(
                initialValue = progress, targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing,
                ),
            ) { value, _ ->
                progress = value
            }

            onFinished()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        LinearWavyProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(0.7f),
        )
    }
}


