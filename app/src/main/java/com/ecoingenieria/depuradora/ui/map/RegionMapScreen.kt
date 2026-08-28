package com.ecoingenieria.depuradora.ui.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ecoingenieria.depuradora.domain.model.Level
import com.ecoingenieria.depuradora.domain.model.LevelStatus
import com.ecoingenieria.depuradora.domain.model.Stage
import com.ecoingenieria.depuradora.ui.components.BeaverGuide
import com.ecoingenieria.depuradora.ui.components.WaterQualityTank
import com.ecoingenieria.depuradora.ui.components.riverBackgroundBrush
import com.ecoingenieria.depuradora.ui.theme.*

@Composable
fun RegionMapScreen(
    state: RegionMapUiState,
    onLevelSelected: (Level) -> Unit,
    onOpenOffice: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(riverBackgroundBrush(state.profile?.globalWaterHealth ?: 0))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RegionMapHeader(
                aliasText = state.profile?.alias?.ifBlank { "Eco-Ingeniero" } ?: "Eco-Ingeniero",
                globalHealth = state.profile?.globalWaterHealth ?: 0,
                onOpenOffice = onOpenOffice
            )

            if (state.loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(state.stages) { stage ->
                        StageSection(
                            stage = stage,
                            levels = state.levels.filter { it.stageId == stage.id }.sortedBy { it.orderIndex },
                            onLevelSelected = onLevelSelected
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RegionMapHeader(aliasText: String, globalHealth: Int, onOpenOffice: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BeaverGuide(modifier = Modifier.size(56.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Hola, $aliasText", style = MaterialTheme.typography.titleMedium)
            Text("Salud del Agua Global", style = MaterialTheme.typography.bodyMedium, color = RiverDeep)
            LinearProgressIndicator(
                progress = { (globalHealth / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = LeafGreen,
                trackColor = Color(0xFFE3E9EC)
            )
        }
        WaterQualityTank(qualityPercent = globalHealth, modifier = Modifier.size(40.dp, 56.dp))
        IconButton(onClick = onOpenOffice) {
            Text("🏠", style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
private fun StageSection(stage: Stage, levels: List<Level>, onLevelSelected: (Level) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.88f))
            .padding(14.dp)
    ) {
        Text(stage.name, style = MaterialTheme.typography.titleLarge)
        Text(stage.shortDescription, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(10.dp))
        levels.forEach { level ->
            LevelZoneCard(level = level, onClick = { onLevelSelected(level) })
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun LevelZoneCard(level: Level, onClick: () -> Unit) {
    val locked = level.status == LevelStatus.LOCKED
    Surface(
        onClick = { if (!locked) onClick() },
        shape = RoundedCornerShape(14.dp),
        color = when (level.status) {
            LevelStatus.LOCKED -> Color(0xFFEDEFF1)
            LevelStatus.AVAILABLE -> RiverClean.copy(alpha = 0.14f)
            LevelStatus.STARTED -> SunYellow.copy(alpha = 0.22f)
            LevelStatus.COMPLETED -> LeafGreen.copy(alpha = 0.18f)
            LevelStatus.MASTERED -> LeafGreen.copy(alpha = 0.32f)
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(level.zoneName, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = statusLabel(level.status),
                    style = MaterialTheme.typography.bodyMedium,
                    color = RiverDeep
                )
                AnimatedVisibility(visible = level.bestWaterQuality > 0) {
                    Text("Mejor calidad de agua: ${level.bestWaterQuality}%", style = MaterialTheme.typography.bodyMedium)
                }
            }
            when {
                locked -> Icon(Icons.Filled.Lock, contentDescription = "Bloqueado", tint = Color(0xFF9AA5AC))
                level.status == LevelStatus.MASTERED -> Icon(Icons.Filled.Star, contentDescription = "Dominado", tint = SunYellow)
                else -> Icon(Icons.Filled.PlayArrow, contentDescription = "Jugar", tint = RiverDeep)
            }
        }
    }
}

private fun statusLabel(status: LevelStatus): String = when (status) {
    LevelStatus.LOCKED -> "Bloqueada"
    LevelStatus.AVAILABLE -> "Disponible"
    LevelStatus.STARTED -> "En progreso"
    LevelStatus.COMPLETED -> "Completada"
    LevelStatus.MASTERED -> "¡Dominada!"
}
