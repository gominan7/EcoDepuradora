package com.ecoingenieria.depuradora.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.ecoingenieria.depuradora.ui.theme.*
import kotlin.math.min

/**
 * Ilustraciones vectoriales dibujadas con Compose Canvas (prioridad 3 de la
 * sección 4 de la Especificación Maestra: nada de imágenes remotas, todo se
 * genera localmente). Reutilizables en toda la app.
 */

@Composable
fun BeaverGuide(modifier: Modifier = Modifier, mood: BeaverMood = BeaverMood.HAPPY) {
    Canvas(modifier = modifier.size(96.dp)) {
        val w = size.width
        val h = size.height
        // Cuerpo
        drawOval(color = BeaverBrown, topLeft = Offset(w * 0.12f, h * 0.30f), size = Size(w * 0.76f, h * 0.62f))
        // Vientre
        drawOval(color = BeaverLight, topLeft = Offset(w * 0.28f, h * 0.46f), size = Size(w * 0.44f, h * 0.42f))
        // Cabeza
        drawCircle(color = BeaverBrown, radius = w * 0.30f, center = Offset(w * 0.5f, h * 0.32f))
        // Orejas
        drawCircle(color = BeaverBrown, radius = w * 0.08f, center = Offset(w * 0.30f, h * 0.14f))
        drawCircle(color = BeaverBrown, radius = w * 0.08f, center = Offset(w * 0.70f, h * 0.14f))
        // Casco de ingeniero
        drawArc(
            color = SunYellow,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(w * 0.20f, h * 0.06f),
            size = Size(w * 0.60f, h * 0.34f)
        )
        drawRect(color = SunYellow, topLeft = Offset(w * 0.18f, h * 0.20f), size = Size(w * 0.64f, h * 0.05f))
        // Ojos
        val eyeY = h * 0.30f
        val eyeState = if (mood == BeaverMood.WORRIED) 0.018f else 0.03f
        drawCircle(color = InkText, radius = w * eyeState, center = Offset(w * 0.40f, eyeY))
        drawCircle(color = InkText, radius = w * eyeState, center = Offset(w * 0.60f, eyeY))
        // Nariz / dientes
        drawOval(color = BeaverLight, topLeft = Offset(w * 0.38f, h * 0.36f), size = Size(w * 0.24f, h * 0.20f))
        drawRect(color = Color.White, topLeft = Offset(w * 0.44f, h * 0.46f), size = Size(w * 0.05f, h * 0.07f))
        drawRect(color = Color.White, topLeft = Offset(w * 0.51f, h * 0.46f), size = Size(w * 0.05f, h * 0.07f))
        // Cola (aplanada, típica de un castor)
        drawOval(color = BeaverBrown, topLeft = Offset(w * 0.60f, h * 0.68f), size = Size(w * 0.34f, h * 0.24f))
    }
}

enum class BeaverMood { HAPPY, WORRIED, CELEBRATING }

@Composable
fun WaterQualityTank(modifier: Modifier = Modifier, qualityPercent: Int) {
    val clamped = qualityPercent.coerceIn(0, 100)
    Canvas(modifier = modifier.size(64.dp, 88.dp)) {
        val w = size.width
        val h = size.height
        val fillHeight = h * (clamped / 100f)
        val muddy = RiverMuddy
        val clean = RiverClean
        val t = clamped / 100f
        val waterColor = Color(
            red = muddy.red + (clean.red - muddy.red) * t,
            green = muddy.green + (clean.green - muddy.green) * t,
            blue = muddy.blue + (clean.blue - muddy.blue) * t,
            alpha = 1f
        )
        // Tanque de cristal
        drawRoundRect(
            color = Color(0xFFE8F6FA),
            topLeft = Offset(0f, 0f),
            size = Size(w, h),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
        )
        // Agua
        drawRoundRect(
            color = waterColor,
            topLeft = Offset(4f, h - fillHeight + 4f),
            size = Size(w - 8f, (fillHeight - 8f).coerceAtLeast(0f)),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
        )
        // Marco
        drawRoundRect(
            color = RiverDeep,
            topLeft = Offset(0f, 0f),
            size = Size(w, h),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
        )
    }
}

@Composable
fun BadgeMedal(modifier: Modifier = Modifier, unlocked: Boolean, accent: Color = SunYellow) {
    Canvas(modifier = modifier.size(64.dp)) {
        val d = min(size.width, size.height)
        val center = Offset(size.width / 2, size.height / 2)
        val ribbonColor = if (unlocked) LeafGreen else Color(0xFFB9C2C9)
        val medalColor = if (unlocked) accent else Color(0xFFD8DEE3)
        // Cinta
        drawRect(color = ribbonColor, topLeft = Offset(center.x - d * 0.10f, 0f), size = Size(d * 0.20f, d * 0.35f))
        // Medalla
        drawCircle(color = medalColor, radius = d * 0.34f, center = center)
        drawCircle(
            color = if (unlocked) RiverDeep else Color(0xFF9AA5AC),
            radius = d * 0.34f,
            center = center,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
        )
        drawCircle(color = Color.White.copy(alpha = if (unlocked) 0.5f else 0.2f), radius = d * 0.14f, center = center)
    }
}

@Composable
fun PieceIcon(modifier: Modifier = Modifier, iconKey: String, tint: Color = RiverDeep) {
    Canvas(modifier = modifier.size(56.dp)) {
        drawPieceGlyph(iconKey, tint)
    }
}

private fun DrawScope.drawPieceGlyph(iconKey: String, tint: Color) {
    val w = size.width
    val h = size.height
    when {
        iconKey.contains("grille") -> {
            drawRoundRect(color = tint.copy(alpha = 0.15f), size = size, cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f))
            var x = w * 0.2f
            while (x < w * 0.85f) {
                drawLine(tint, Offset(x, h * 0.15f), Offset(x, h * 0.85f), strokeWidth = 5f)
                x += w * 0.15f
            }
        }
        iconKey.contains("grit") || iconKey.contains("sand") -> {
            drawRoundRect(color = tint.copy(alpha = 0.15f), size = size, cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f))
            for (i in 0..8) {
                val cx = w * (0.15f + (i % 3) * 0.3f)
                val cy = h * (0.3f + (i / 3) * 0.25f)
                drawCircle(color = tint, radius = w * 0.03f, center = Offset(cx, cy))
            }
        }
        iconKey.contains("clarifier") -> {
            drawRoundRect(color = tint.copy(alpha = 0.15f), size = size, cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f))
            drawOval(color = tint, topLeft = Offset(w * 0.15f, h * 0.35f), size = Size(w * 0.7f, h * 0.3f))
        }
        iconKey.contains("aeration") -> {
            drawRoundRect(color = tint.copy(alpha = 0.15f), size = size, cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f))
            for (i in 0..4) {
                drawCircle(color = tint, radius = w * 0.045f, center = Offset(w * (0.2f + i * 0.15f), h * (0.75f - (i % 2) * 0.25f)))
            }
        }
        iconKey.contains("bioreactor") -> {
            drawRoundRect(color = tint.copy(alpha = 0.15f), size = size, cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f))
            drawCircle(color = tint, radius = w * 0.22f, center = Offset(w * 0.5f, h * 0.5f))
            drawCircle(color = Color.White, radius = w * 0.08f, center = Offset(w * 0.5f, h * 0.5f))
        }
        iconKey.contains("sludge") -> {
            drawRoundRect(color = tint.copy(alpha = 0.15f), size = size, cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f))
            drawLine(tint, Offset(w * 0.2f, h * 0.3f), Offset(w * 0.8f, h * 0.3f), strokeWidth = 6f)
            drawLine(tint, Offset(w * 0.8f, h * 0.3f), Offset(w * 0.8f, h * 0.7f), strokeWidth = 6f)
            drawLine(tint, Offset(w * 0.8f, h * 0.7f), Offset(w * 0.2f, h * 0.7f), strokeWidth = 6f)
        }
        iconKey.contains("uv") -> {
            drawRoundRect(color = tint.copy(alpha = 0.15f), size = size, cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f))
            for (angle in 0 until 360 step 45) {
                val rad = Math.toRadians(angle.toDouble())
                val cx = w * 0.5f + (w * 0.28f * kotlin.math.cos(rad)).toFloat()
                val cy = h * 0.5f + (h * 0.28f * kotlin.math.sin(rad)).toFloat()
                drawLine(SunYellow, Offset(w * 0.5f, h * 0.5f), Offset(cx, cy), strokeWidth = 4f)
            }
            drawCircle(color = tint, radius = w * 0.14f, center = Offset(w * 0.5f, h * 0.5f))
        }
        iconKey.contains("sensor") -> {
            drawRoundRect(color = tint.copy(alpha = 0.15f), size = size, cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f))
            drawCircle(color = tint, radius = w * 0.22f, center = Offset(w * 0.5f, h * 0.45f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f))
            drawLine(tint, Offset(w * 0.5f, h * 0.62f), Offset(w * 0.5f, h * 0.8f), strokeWidth = 5f)
        }
        iconKey.contains("gate") -> {
            drawRoundRect(color = tint.copy(alpha = 0.15f), size = size, cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f))
            drawRect(color = tint, topLeft = Offset(w * 0.3f, h * 0.2f), size = Size(w * 0.4f, h * 0.6f))
            drawRect(color = Color.White, topLeft = Offset(w * 0.4f, h * 0.3f), size = Size(w * 0.2f, h * 0.4f))
        }
        else -> {
            drawRoundRect(color = tint.copy(alpha = 0.15f), size = size, cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f))
            drawCircle(color = tint, radius = w * 0.2f, center = Offset(w * 0.5f, h * 0.5f))
        }
    }
}

@Composable
fun PipeConnector(modifier: Modifier = Modifier, flowing: Boolean, color: Color = RiverClean) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.size(width = 40.dp, height = 12.dp)) {
            drawLine(
                color = if (flowing) color else Color(0xFFD8DEE3),
                start = Offset(0f, size.height / 2),
                end = Offset(size.width, size.height / 2),
                strokeWidth = size.height * 0.8f
            )
        }
    }
}

fun riverBackgroundBrush(healthPercent: Int): Brush {
    val t = healthPercent.coerceIn(0, 100) / 100f
    val top = Color(RiverMuddy.red + (RiverClean.red - RiverMuddy.red) * t, RiverMuddy.green + (RiverClean.green - RiverMuddy.green) * t, RiverMuddy.blue + (RiverClean.blue - RiverMuddy.blue) * t, 1f)
    return Brush.verticalGradient(listOf(top.copy(alpha = 0.35f), CreamBackground))
}
