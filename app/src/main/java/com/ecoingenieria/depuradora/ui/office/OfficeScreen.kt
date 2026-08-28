package com.ecoingenieria.depuradora.ui.office

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ecoingenieria.depuradora.ui.components.BadgeMedal
import com.ecoingenieria.depuradora.ui.components.BeaverGuide
import com.ecoingenieria.depuradora.ui.components.PlayerAvatarBadge
import com.ecoingenieria.depuradora.ui.theme.RiverDeep

@Composable
fun OfficeScreen(
    state: OfficeUiState,
    onBack: () -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onToggleHaptics: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Text("←", style = MaterialTheme.typography.headlineMedium) }
            BeaverGuide(modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.width(4.dp))
            PlayerAvatarBadge(avatarKey = state.profile?.avatarKey ?: "avatar_beaver_1", modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("Oficina del Castor", style = MaterialTheme.typography.headlineMedium)
                Text("Tu colección de Eco-Ingeniero", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Insignias Eco-Ingeniero", style = MaterialTheme.typography.titleLarge)
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxWidth().height(180.dp)
        ) {
            items(state.badges) { badge ->
                Column(
                    modifier = Modifier.padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BadgeMedal(unlocked = badge.unlocked)
                    Text(
                        badge.name,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Planos de Ingeniería", style = MaterialTheme.typography.titleLarge)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            items(state.blueprints) { blueprint ->
                Column(
                    modifier = Modifier
                        .padding(6.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (blueprint.unlocked) Color(0xFFE3F5EE) else Color(0xFFEDEFF1))
                        .padding(10.dp)
                ) {
                    Text(
                        if (blueprint.unlocked) blueprint.name else "??? (Bloqueado)",
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (blueprint.unlocked) {
                        Text(blueprint.description, style = MaterialTheme.typography.bodyMedium)
                    } else {
                        Text("Completa el nivel correspondiente para descubrirlo.", style = MaterialTheme.typography.bodyMedium, color = RiverDeep)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Ajustes", style = MaterialTheme.typography.titleLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Sonido")
            Switch(checked = state.profile?.soundEnabled ?: true, onCheckedChange = onToggleSound)
            Spacer(modifier = Modifier.width(16.dp))
            Text("Vibración")
            Switch(checked = state.profile?.hapticsEnabled ?: true, onCheckedChange = onToggleHaptics)
        }
    }
}
