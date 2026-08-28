package com.ecoingenieria.depuradora.ui.engineering

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecoingenieria.depuradora.domain.model.*
import com.ecoingenieria.depuradora.domain.usecase.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class EngineeringPhase { LOADING, ASSEMBLY, VALVES, BACTERIA_LAB, RESULT }

data class EngineeringUiState(
    val phase: EngineeringPhase = EngineeringPhase.LOADING,
    val level: Level? = null,
    val availablePieces: List<Piece> = emptyList(),
    val placedPieceIds: List<Int> = emptyList(),
    val assemblyResult: AssemblyResult? = null,
    val oxygenLevel: Int = 50,
    val waterSpeed: Int = 50,
    val valveResult: ValveResult? = null,
    val bacteriaTotal: Int = 0,
    val bacteriaEaten: Int = 0,
    val bacteriaSecondsLeft: Int = 20,
    val bacteriaScore: Int = 0,
    val finalWaterQuality: Int = 0,
    val newlyUnlockedBadgeIds: List<Int> = emptyList(),
    val blueprintUnlocked: Boolean = false
)

class EngineeringViewModel(
    private val repository: GameRepository,
    private val validateAssembly: ValidatePlantAssemblyUseCase,
    private val simulateFlow: SimulateWaterFlowUseCase,
    private val bacteriaScoreUseCase: BacteriaLabScoreUseCase,
    private val calculateFinalQuality: CalculateFinalQualityUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EngineeringUiState())
    val uiState: StateFlow<EngineeringUiState> = _uiState.asStateFlow()

    fun loadLevel(levelId: Int) {
        viewModelScope.launch {
            repository.markLevelStarted(levelId)
            val level = repository.getLevel(levelId) ?: return@launch
            val allPieces = mutableListOf<Piece>()
            repository.observePiecesForStage(level.stageId)
            _uiState.value = _uiState.value.copy(
                level = level,
                phase = EngineeringPhase.ASSEMBLY,
                bacteriaTotal = level.bacteriaOrganicLoad,
                bacteriaEaten = 0,
                bacteriaSecondsLeft = 20,
                placedPieceIds = emptyList(),
                assemblyResult = null
            )
            // Piezas disponibles: las requeridas + un par de intrusas para que el
            // niño tenga que razonar el orden, no solo colocar lo único que hay.
            loadPiecesForLevel(level)
        }
    }

    private suspend fun loadPiecesForLevel(level: Level) {
        repository.observePiecesForStage(level.stageId).let { flow ->
            // Tomamos el primer valor emitido (ya sembrado por ensureSeeded()).
            val stagePieces = kotlinx.coroutines.flow.first(flow)
            val required = stagePieces.filter { it.id in level.requiredPieceIds }
            val shuffled = required.shuffled(Random(level.id))
            _uiState.value = _uiState.value.copy(availablePieces = shuffled)
        }
    }

    fun placePiece(pieceId: Int) {
        val current = _uiState.value
        if (pieceId in current.placedPieceIds) return
        _uiState.value = current.copy(placedPieceIds = current.placedPieceIds + pieceId)
    }

    fun removeLastPiece() {
        val current = _uiState.value
        if (current.placedPieceIds.isEmpty()) return
        _uiState.value = current.copy(placedPieceIds = current.placedPieceIds.dropLast(1))
    }

    fun confirmAssembly() {
        val current = _uiState.value
        val level = current.level ?: return
        val result = validateAssembly(current.placedPieceIds, level.requiredPieceIds)
        _uiState.value = current.copy(assemblyResult = result)
        if (result is AssemblyResult.Correct) {
            _uiState.value = _uiState.value.copy(phase = EngineeringPhase.VALVES)
        }
    }

    fun retryAssembly() {
        _uiState.value = _uiState.value.copy(placedPieceIds = emptyList(), assemblyResult = null)
    }

    fun updateOxygen(value: Int) {
        _uiState.value = _uiState.value.copy(oxygenLevel = value)
    }

    fun updateSpeed(value: Int) {
        _uiState.value = _uiState.value.copy(waterSpeed = value)
    }

    fun confirmValves() {
        val current = _uiState.value
        val level = current.level ?: return
        val result = simulateFlow(
            current.oxygenLevel,
            current.waterSpeed,
            level.targetOxygenRange,
            level.targetSpeedRange
        )
        _uiState.value = current.copy(valveResult = result, phase = EngineeringPhase.BACTERIA_LAB)
    }

    fun tapBacteria() {
        val current = _uiState.value
        if (current.bacteriaEaten >= current.bacteriaTotal) return
        _uiState.value = current.copy(bacteriaEaten = current.bacteriaEaten + 1)
    }

    fun tickBacteriaTimer() {
        val current = _uiState.value
        if (current.bacteriaSecondsLeft <= 0) {
            finishBacteriaLab()
            return
        }
        _uiState.value = current.copy(bacteriaSecondsLeft = current.bacteriaSecondsLeft - 1)
        if (current.bacteriaEaten >= current.bacteriaTotal || current.bacteriaSecondsLeft - 1 <= 0) {
            finishBacteriaLab()
        }
    }

    private fun finishBacteriaLab() {
        val current = _uiState.value
        val score = bacteriaScoreUseCase(
            organicLoad = current.bacteriaTotal,
            organicUnitsEaten = current.bacteriaEaten,
            timeRemainingSeconds = current.bacteriaSecondsLeft,
            totalTimeSeconds = 20
        )
        val assembly = current.assemblyResult ?: AssemblyResult.Incomplete
        val valveQuality = current.valveResult?.waterQuality ?: 0
        val finalQuality = calculateFinalQuality(assembly, valveQuality, score)

        _uiState.value = current.copy(
            bacteriaScore = score,
            finalWaterQuality = finalQuality,
            phase = EngineeringPhase.RESULT
        )

        viewModelScope.launch {
            val level = current.level ?: return@launch
            val outcome = repository.submitLevelResult(level.id, finalQuality)
            _uiState.value = _uiState.value.copy(
                newlyUnlockedBadgeIds = outcome.newlyUnlockedBadgeIds,
                blueprintUnlocked = outcome.blueprintUnlocked
            )
        }
    }
}
