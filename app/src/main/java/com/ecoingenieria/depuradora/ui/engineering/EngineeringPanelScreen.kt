package com.ecoingenieria.depuradora.ui.engineering

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.dp
import com.ecoingenieria.depuradora.domain.model.AssemblyResult
import com.ecoingenieria.depuradora.domain.model.Level
import com.ecoingenieria.depuradora.domain.model.Piece
import com.ecoingenieria.depuradora.ui.components.*
import com.ecoingenieria.depuradora.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun EngineeringPanelScreen(
    state: EngineeringUiState,
    onPlacePiece: (Int) -> Unit,
    onRemoveLastPiece: () -> Unit,
    onConfirmAssembly: () -> Unit,
    onRetryAssembly: () -> Unit,
    onOxygenChange: (Int) -> Unit,
    onSpeedChange: (Int) -> Unit,
    onConfirmValves: () -> Unit,
    onTapBacteria: () -> Unit,
    onBacteriaTick: () -> Unit,
    onContinue: () -> Unit
) {
    val level = state.level ?: return

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        LevelBriefingHeader(level)
        Spacer(modifier = Modifier.height(12.dp))

        when (state.phase) {
            EngineeringPhase.LOADING -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            EngineeringPhase.ASSEMBLY -> AssemblyStage(
                state = state,
                onPlacePiece = onPlacePiece,
                onRemoveLastPiece = onRemoveLastPiece,
                onConfirm = onConfirmAssembly,
                onRetry = onRetryAssembly
            )
            EngineeringPhase.VALVES -> ValveStage(
                state = state,
                onOxygenChange = onOxygenChange,
                onSpeedChange = onSpeedChange,
                onConfirm = onConfirmValves
            )
            EngineeringPhase.BACTERIA_LAB -> BacteriaLabStage(state = state, onTap = onTapBacteria, onTick = onBacteriaTick)
            EngineeringPhase.RESULT -> ResultStage(state = state, onContinue = onContinue)
        }
    }
}

@Composable
private fun LevelBriefingHeader(level: Level) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        BeaverGuide(modifier = Modifier.size(52.dp), mood = BeaverMood.WORRIED)
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(level.zoneName, style = MaterialTheme.typography.titleLarge)
            Text(level.briefing, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// ---------------------------------------------------------------------
// ETAPA 1: CONSTRUCTOR DE PLANTA (Drag & Drop)
// ---------------------------------------------------------------------

@Composable
private fun AssemblyStage(
    state: EngineeringUiState,
    onPlacePiece: (Int) -> Unit,
    onRemoveLastPiece: () -> Unit,
    onConfirm: () -> Unit,
    onRetry: () -> Unit
) {
    var dropZoneBounds by remember { mutableStateOf<Rect?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Línea de ensamblaje", style = MaterialTheme.typography.titleMedium)
        Text("Arrastra cada pieza hasta la tubería, en el orden correcto.", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(10.dp))

        // Zona de destino (tubería de ensamblaje)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(RiverClean.copy(alpha = 0.10f))
                .onGloballyPositioned { coords ->
                    val pos = coords.positionInRoot()
                    dropZoneBounds = Rect(pos, coords.size.toSize())
                }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state.placedPieceIds.isEmpty()) {
                Text("  Suelta aquí la primera pieza  ", color = Color(0xFF7C8A93))
            }
            state.placedPieceIds.forEachIndexed { index, id ->
                val piece = state.availablePieces.find { it.id == id }
                if (piece != null) {
                    PieceIcon(iconKey = piece.iconKey, modifier = Modifier.size(48.dp))
                    if (index != state.placedPieceIds.lastIndex) PipeConnector(flowing = true)
                }
            }
        }

        state.assemblyResult?.let { result ->
            Spacer(modifier = Modifier.height(8.dp))
            when (result) {
                is AssemblyResult.Correct -> Text("¡Orden correcto! El agua empieza a fluir. 💧", color = LeafGreen, style = MaterialTheme.typography.titleMedium)
                is AssemblyResult.Incorrect -> Text(
                    "El agua se estanca: ${result.explanation}",
                    color = AlertCoral,
                    style = MaterialTheme.typography.bodyLarge
                )
                AssemblyResult.Incomplete -> Text("Todavía faltan piezas por colocar.", color = SunYellow, style = MaterialTheme.typography.bodyLarge)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text("Piezas disponibles", style = MaterialTheme.typography.titleMedium)
        LazyRow(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
            items(state.availablePieces) { piece ->
                DraggablePiece(
                    piece = piece,
                    alreadyPlaced = piece.id in state.placedPieceIds,
                    dropZoneBounds = dropZoneBounds,
                    onDropped = { onPlacePiece(piece.id) }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onRemoveLastPiece, enabled = state.placedPieceIds.isNotEmpty()) {
                Text("Quitar última")
            }
            Button(
                onClick = { if (state.assemblyResult is AssemblyResult.Incorrect) onRetry() else onConfirm() },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (state.assemblyResult is AssemblyResult.Incorrect) "Reintentar" else "Confirmar orden")
            }
        }
    }
}

@Composable
private fun DraggablePiece(
    piece: Piece,
    alreadyPlaced: Boolean,
    dropZoneBounds: Rect?,
    onDropped: () -> Unit
) {
    var offset by remember { mutableStateOf(Offset.Zero) }
    var origin by remember { mutableStateOf(Offset.Zero) }
    var dragging by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(6.dp)
            .graphicsLayer {
                translationX = offset.x
                translationY = offset.y
                alpha = if (alreadyPlaced) 0.35f else 1f
            }
            .onGloballyPositioned { coords -> origin = coords.positionInRoot() }
            .pointerInput(alreadyPlaced) {
                if (alreadyPlaced) return@pointerInput
                detectDragGestures(
                    onDragStart = { dragging = true },
                    onDragEnd = {
                        dragging = false
                        val fingerPosition = origin + offset
                        val zone = dropZoneBounds
                        if (zone != null && zone.contains(fingerPosition)) {
                            onDropped()
                        }
                        offset = Offset.Zero
                    },
                    onDragCancel = { offset = Offset.Zero; dragging = false },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offset += dragAmount
                    }
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PieceIcon(iconKey = piece.iconKey, modifier = Modifier.size(48.dp))
        Text(piece.name, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun androidx.compose.ui.unit.IntSize.toSize() =
    androidx.compose.ui.geometry.Size(width.toFloat(), height.toFloat())

// ---------------------------------------------------------------------
// ETAPA 2: OPERADOR DE VÁLVULAS
// ---------------------------------------------------------------------

@Composable
private fun ValveStage(
    state: EngineeringUiState,
    onOxygenChange: (Int) -> Unit,
    onSpeedChange: (Int) -> Unit,
    onConfirm: () -> Unit
) {
    val level = state.level ?: return
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Operador de válvulas", style = MaterialTheme.typography.titleMedium)
        Text(
            "Ajusta el oxígeno y la velocidad del agua para que las bacterias trabajen bien.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(20.dp))

        ValvePipeCanvas(oxygen = state.oxygenLevel, speed = state.waterSpeed)

        Spacer(modifier = Modifier.height(20.dp))
        Text("Oxígeno: ${state.oxygenLevel}%  (ideal: ${level.targetOxygenRange.first}-${level.targetOxygenRange.last}%)")
        Slider(
            value = state.oxygenLevel.toFloat(),
            onValueChange = { onOxygenChange(it.toInt()) },
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(activeTrackColor = RiverClean)
        )

        Spacer(modifier = Modifier.height(10.dp))
        Text("Velocidad del agua: ${state.waterSpeed}%  (ideal: ${level.targetSpeedRange.first}-${level.targetSpeedRange.last}%)")
        Slider(
            value = state.waterSpeed.toFloat(),
            onValueChange = { onSpeedChange(it.toInt()) },
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(activeTrackColor = LeafGreen)
        )

        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onConfirm, modifier = Modifier.fillMaxWidth()) {
            Text("Iniciar flujo de agua")
        }
    }
}

@Composable
private fun ValvePipeCanvas(oxygen: Int, speed: Int) {
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxWidth().height(90.dp)) {
        val w = size.width
        val h = size.height
        drawRoundRect(
            color = Color(0xFFE3E9EC),
            size = size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f, 20f)
        )
        val bubbleCount = (oxygen / 12).coerceIn(1, 9)
        for (i in 0 until bubbleCount) {
            val x = w * (0.08f + i * (0.85f / bubbleCount))
            drawCircle(color = RiverClean, radius = 6f + (speed / 100f) * 4f, center = Offset(x, h * 0.5f))
        }
    }
}

// ---------------------------------------------------------------------
// ETAPA 3: LABORATORIO DE BACTERIAS
// ---------------------------------------------------------------------

@Composable
private fun BacteriaLabStage(state: EngineeringUiState, onTap: () -> Unit, onTick: () -> Unit) {
    LaunchedEffect(state.phase) {
        while (state.bacteriaSecondsLeft > 0 && state.bacteriaEaten < state.bacteriaTotal) {
            delay(1000)
            onTick()
        }
    }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Laboratorio de bacterias", style = MaterialTheme.typography.titleMedium)
        Text(
            "Toca la materia orgánica para que las bacterias buenas se la coman antes de que se acabe el tiempo.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text("⏱ ${state.bacteriaSecondsLeft}s", style = MaterialTheme.typography.titleLarge, color = AlertCoral)
        Text("Limpio: ${state.bacteriaEaten}/${state.bacteriaTotal}", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(16.dp))
        MicroscopeView(eaten = state.bacteriaEaten, total = state.bacteriaTotal, onTapOrganicMatter = onTap)
    }
}

@Composable
private fun MicroscopeView(eaten: Int, total: Int, onTapOrganicMatter: () -> Unit) {
    val remaining = (total - eaten).coerceAtLeast(0)
    val positions = remember(total) {
        (0 until total).map {
            Offset((20..280).random().toFloat(), (20..180).random().toFloat())
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(110.dp))
            .background(Color(0xFFD9F1EF))
    ) {
        for (i in 0 until remaining) {
            val pos = positions.getOrElse(i) { Offset(100f, 100f) }
            Box(
                modifier = Modifier
                    .graphicsLayer { translationX = pos.x; translationY = pos.y }
                    .size(20.dp)
                    .clip(RoundedCornerShape(50))
                    .background(RiverMuddy)
            )
        }
        androidx.compose.material3.TextButton(
            onClick = onTapOrganicMatter,
            modifier = Modifier.align(Alignment.BottomCenter).padding(10.dp)
        ) {
            Text("Tocar para limpiar 🧫")
        }
    }
}

// ---------------------------------------------------------------------
// RESULTADO
// ---------------------------------------------------------------------

@Composable
private fun ResultStage(state: EngineeringUiState, onContinue: () -> Unit) {
    val quality = state.finalWaterQuality
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(20.dp))
        WaterQualityTank(qualityPercent = quality, modifier = Modifier.size(90.dp, 130.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            when {
                quality >= 90 -> "¡Agua cristalina! Los peces ya están volviendo. 🐟"
                quality >= 60 -> "¡Buen trabajo! El río está mucho más limpio."
                else -> "El río todavía necesita ayuda. ¡Sigue intentándolo!"
            },
            style = MaterialTheme.typography.titleLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Text("Calidad del agua: $quality%", style = MaterialTheme.typography.titleMedium)

        if (state.blueprintUnlocked) {
            Spacer(modifier = Modifier.height(10.dp))
            Text("📜 ¡Nuevo Plano de Ingeniería desbloqueado!", color = LeafGreen)
        }
        if (state.newlyUnlockedBadgeIds.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text("🏅 ¡Nuevas insignias en tu Oficina!", color = SunYellow)
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
            Text("Volver al mapa")
        }
    }
}
