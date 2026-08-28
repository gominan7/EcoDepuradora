package com.ecoingenieria.depuradora.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ecoingenieria.depuradora.ui.components.BeaverGuide
import com.ecoingenieria.depuradora.ui.components.BeaverMood
import com.ecoingenieria.depuradora.ui.theme.LeafGreen
import com.ecoingenieria.depuradora.ui.theme.RiverClean
import com.ecoingenieria.depuradora.ui.theme.RiverDeep

private val avatarKeys = listOf(
    "avatar_beaver_1", "avatar_beaver_2", "avatar_otter", "avatar_frog",
    "avatar_fish", "avatar_heron", "avatar_dragonfly", "avatar_turtle"
)

@Composable
fun OnboardingScreen(onFinished: (alias: String, avatarKey: String) -> Unit) {
    var page by remember { mutableIntStateOf(0) }
    var alias by remember { mutableStateOf("") }
    var selectedAvatar by remember { mutableStateOf(avatarKeys.first()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (page) {
            0 -> OnboardingPageWelcome()
            1 -> OnboardingPageWorld()
            2 -> OnboardingPageProfile(
                alias = alias,
                onAliasChange = { alias = it },
                selectedAvatar = selectedAvatar,
                onAvatarSelected = { selectedAvatar = it }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (page < 2) page++ else onFinished(alias, selectedAvatar)
            },
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text(if (page < 2) "Siguiente" else "¡Empezar mi misión!")
        }
    }
}

@Composable
private fun OnboardingPageWelcome() {
    BeaverGuide(mood = BeaverMood.HAPPY)
    Spacer(modifier = Modifier.height(16.dp))
    Text("¡Hola, futuro Eco-Ingeniero!", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        "Soy Berto, ingeniero castor. Los ríos de la ciudad están contaminados y necesito tu ayuda para construir plantas que los limpien.",
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun OnboardingPageWorld() {
    Text("Así vas a jugar", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
    Spacer(modifier = Modifier.height(12.dp))
    listOf(
        "🗺️ Explora el mapa y elige una zona contaminada.",
        "🔧 Arma la planta arrastrando las piezas en el orden correcto.",
        "🎛️ Ajusta las válvulas de oxígeno y velocidad del agua.",
        "🔬 Ayuda a las bacterias en el laboratorio a tiempo."
    ).forEach { line ->
        Text(line, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(vertical = 4.dp))
    }
}

@Composable
private fun OnboardingPageProfile(
    alias: String,
    onAliasChange: (String) -> Unit,
    selectedAvatar: String,
    onAvatarSelected: (String) -> Unit
) {
    Text("Elige tu apodo de ingeniero", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
    Text(
        "No necesitas tu nombre real, solo un apodo para tu perfil.",
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(bottom = 12.dp)
    )
    OutlinedTextField(
        value = alias,
        onValueChange = { if (it.length <= 16) onAliasChange(it) },
        label = { Text("Apodo") },
        singleLine = true
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text("Elige tu avatar", style = MaterialTheme.typography.titleMedium)
    LazyRow(modifier = Modifier.padding(top = 8.dp)) {
        items(avatarKeys) { key ->
            val selected = key == selectedAvatar
            Box(
                modifier = Modifier
                    .padding(6.dp)
                    .size(56.dp)
                    .clip(CircleShape)
                    .let {
                        if (selected) it else it
                    },
                contentAlignment = Alignment.Center
            ) {
                FilledIconToggleButton(
                    checked = selected,
                    onCheckedChange = { onAvatarSelected(key) },
                    colors = IconButtonDefaults.filledIconToggleButtonColors(
                        checkedContainerColor = LeafGreen,
                        containerColor = RiverClean.copy(alpha = 0.15f)
                    )
                ) {
                    Text(avatarEmoji(key))
                }
            }
        }
    }
}

private fun avatarEmoji(key: String): String = when (key) {
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
