package com.ecoingenieria.depuradora.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ecoingenieria.depuradora.ui.theme.LeafGreen

/**
 * Lista cerrada de avatares elegibles en el Onboarding (sección 5.3 del
 * prompt específico). Vive aquí, en un único lugar, para que tanto la
 * pantalla de Onboarding como el Mapa de la Región y la Oficina del Castor
 * usen siempre la misma representación visual del avatar elegido.
 */
val avatarKeys = listOf(
    "avatar_beaver_1", "avatar_beaver_2", "avatar_otter", "avatar_frog",
    "avatar_fish", "avatar_heron", "avatar_dragonfly", "avatar_turtle"
)

fun avatarEmoji(key: String): String = when (key) {
    "avatar_beaver_1" -> "🦫"
    "avatar_beaver_2" -> "🦦"
    "avatar_otter" -> "🦦"
    "avatar_frog" -> "🐸"
    "avatar_fish" -> "🐟"
    "avatar_heron" -> "🦢"
    "avatar_dragonfly" -> "🐞"
    "avatar_turtle" -> "🐢"
    else -> "🦫"
}

/** Insignia circular con el emoji del avatar elegido por el jugador. */
@Composable
fun PlayerAvatarBadge(avatarKey: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(LeafGreen.copy(alpha = 0.20f)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = avatarEmoji(avatarKey), fontSize = 24.sp, textAlign = TextAlign.Center)
    }
}
