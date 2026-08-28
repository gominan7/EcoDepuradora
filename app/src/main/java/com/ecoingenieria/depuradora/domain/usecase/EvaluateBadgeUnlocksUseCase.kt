package com.ecoingenieria.depuradora.domain.usecase

/**
 * Evalúa, dado un resumen del progreso del jugador tras completar un nivel,
 * qué insignias (badges) todavía no desbloqueadas deberían desbloquearse
 * ahora. Lógica pura y testeable, independiente de Room.
 */
class EvaluateBadgeUnlocksUseCase {

    data class ProgressSnapshot(
        val totalLevelsCompleted: Int,
        val primaryLevelsCompleted: Int,
        val secondaryLevelsCompleted: Int,
        val tertiaryLevelsCompleted: Int,
        val blueprintsUnlockedCount: Int,
        val lastLevelWaterQuality: Int,
        val globalWaterHealth: Int,
        val alreadyUnlockedBadgeIds: Set<Int>
    )

    operator fun invoke(snapshot: ProgressSnapshot): List<Int> {
        val candidates = mutableListOf<Int>()

        fun offer(badgeId: Int, condition: Boolean) {
            if (condition && badgeId !in snapshot.alreadyUnlockedBadgeIds) candidates += badgeId
        }

        offer(1, snapshot.totalLevelsCompleted >= 1)
        offer(2, snapshot.primaryLevelsCompleted >= 4)
        offer(3, snapshot.secondaryLevelsCompleted >= 3)
        offer(4, snapshot.tertiaryLevelsCompleted >= 3)
        offer(5, snapshot.lastLevelWaterQuality >= 100)
        offer(6, snapshot.blueprintsUnlockedCount >= 5)
        offer(7, snapshot.totalLevelsCompleted >= 10)
        offer(8, snapshot.globalWaterHealth >= 100)

        return candidates
    }
}
