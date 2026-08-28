package com.ecoingenieria.depuradora.ui.splash

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ecoingenieria.depuradora.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Splash screen del prompt específico (sección 5.1): una gota de agua sucia
 * atraviesa una tubería y sale brillante, formando el logo.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var progress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1400, easing = LinearEasing),
        label = "splashProgress"
    )

    LaunchedEffect(Unit) {
        progress = 1f
        delay(1900)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Canvas(modifier = Modifier.padding(24.dp).size(width = 260.dp, height = 140.dp)) {
                val w = 260.dp.toPx()
                val h = 140.dp.toPx()
                size.let { }
                // Tubería
                drawRoundRect(
                    color = Color(0xFFB9C2C9),
                    topLeft = Offset(0f, h * 0.42f),
                    size = Size(w, h * 0.18f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f, 20f)
                )
                // Gota que avanza y cambia de color de marrón a azul
                val dropX = w * animatedProgress
                val muddy = RiverMuddy
                val clean = RiverClean
                val t = animatedProgress
                val dropColor = Color(
                    red = muddy.red + (clean.red - muddy.red) * t,
                    green = muddy.green + (clean.green - muddy.green) * t,
                    blue = muddy.blue + (clean.blue - muddy.blue) * t
                )
                drawCircle(color = dropColor, radius = 26f, center = Offset(dropX.coerceIn(26f, w - 26f), h * 0.51f))
            }
            Text(
                text = "EcoDepuradora",
                fontSize = 28.sp,
                color = RiverDeep,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "Misión Agua Limpia",
                fontSize = 16.sp,
                color = LeafGreen
            )
        }
    }
}
