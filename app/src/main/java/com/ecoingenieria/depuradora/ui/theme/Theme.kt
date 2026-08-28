package com.ecoingenieria.depuradora.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Paleta propia de EcoDepuradora: agua turbia -> agua cristalina, con acentos
// tierra (castor) y verde (naturaleza). No se reutiliza de otros proyectos.
val RiverMuddy = Color(0xFF8D6E45)
val RiverClean = Color(0xFF1CA9C9)
val RiverDeep = Color(0xFF0E6E86)
val BeaverBrown = Color(0xFF8B5A2B)
val BeaverLight = Color(0xFFC99A6B)
val LeafGreen = Color(0xFF4CAF7D)
val SunYellow = Color(0xFFFFC94D)
val AlertCoral = Color(0xFFFF6B5C)
val CreamBackground = Color(0xFFFBF6EC)
val InkText = Color(0xFF23303B)

private val EcoColorScheme = lightColorScheme(
    primary = RiverClean,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCDEFF6),
    secondary = LeafGreen,
    onSecondary = Color.White,
    tertiary = SunYellow,
    onTertiary = InkText,
    background = CreamBackground,
    onBackground = InkText,
    surface = Color.White,
    onSurface = InkText,
    error = AlertCoral
)

private val EcoTypography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, color = InkText),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp, color = InkText),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp, color = InkText),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = InkText),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, color = InkText),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, color = InkText),
    labelLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp, color = InkText)
)

@Composable
fun EcoDepuradoraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EcoColorScheme,
        typography = EcoTypography,
        content = content
    )
}
