package com.ecoingenieria.depuradora.domain.model

enum class LevelStatus { LOCKED, AVAILABLE, STARTED, COMPLETED, MASTERED }

data class Stage(
    val id: Int,
    val orderIndex: Int,
    val name: String,
    val shortDescription: String,
    val colorHex: String
)

data class Piece(
    val id: Int,
    val stageId: Int,
    val name: String,
    val description: String,
    val correctOrder: Int,
    val iconKey: String
)

data class Level(
    val id: Int,
    val stageId: Int,
    val orderIndex: Int,
    val zoneName: String,
    val briefing: String,
    val requiredPieceIds: List<Int>,
    val targetOxygenRange: IntRange,
    val targetSpeedRange: IntRange,
    val bacteriaOrganicLoad: Int,
    val blueprintId: Int,
    val status: LevelStatus,
    val bestWaterQuality: Int
)

data class Blueprint(
    val id: Int,
    val name: String,
    val description: String,
    val stageId: Int,
    val iconKey: String,
    val unlocked: Boolean
)

data class Badge(
    val id: Int,
    val name: String,
    val description: String,
    val requirement: String,
    val iconKey: String,
    val unlocked: Boolean
)

data class PlayerProfile(
    val alias: String,
    val avatarKey: String,
    val globalWaterHealth: Int,
    val onboardingCompleted: Boolean,
    val soundEnabled: Boolean,
    val hapticsEnabled: Boolean
)

/** Resultado de intentar ensamblar la planta en el Constructor (drag & drop). */
sealed class AssemblyResult {
    data object Correct : AssemblyResult()
    data class Incorrect(val firstWrongIndex: Int, val explanation: String) : AssemblyResult()
    data object Incomplete : AssemblyResult()
}

/** Resultado de operar las válvulas (oxígeno / velocidad). */
data class ValveResult(
    val waterQuality: Int, // 0-100
    val energyUsed: Int, // 0-100, penaliza exceso de oxígeno
    val feedback: String
)

/** Resultado final combinado de un nivel jugado por completo. */
data class LevelOutcome(
    val level: Level,
    val assembly: AssemblyResult,
    val valve: ValveResult,
    val bacteriaScore: Int, // 0-100, del laboratorio de bacterias
    val finalWaterQuality: Int, // 0-100 combinando las tres mecánicas
    val newlyUnlockedBadgeIds: List<Int> = emptyList()
)
