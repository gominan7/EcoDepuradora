package com.ecoingenieria.depuradora.domain.usecase

import kotlin.math.roundToInt

/**
 * Calcula la puntuación del "Laboratorio de Bacterias" (vista de
 * microscopio, sección 3 del prompt específico). El niño toca la pantalla
 * para ayudar a las bacterias buenas a comer una cantidad de materia
 * orgánica (organicLoad) antes de que se acabe el tiempo.
 */
class BacteriaLabScoreUseCase {

    operator fun invoke(organicLoad: Int, organicUnitsEaten: Int, timeRemainingSeconds: Int, totalTimeSeconds: Int): Int {
        if (organicLoad <= 0) return 100
        val eatenClamped = organicUnitsEaten.coerceIn(0, organicLoad)
        val completionRatio = eatenClamped.toDouble() / organicLoad.toDouble()

        // Bono de hasta 15 puntos por terminar con tiempo restante, para premiar
        // eficiencia sin castigar duramente la lentitud (regla 10: feedback no debe humillar).
        val timeRatio = if (totalTimeSeconds <= 0) 0.0 else
            (timeRemainingSeconds.coerceIn(0, totalTimeSeconds).toDouble() / totalTimeSeconds.toDouble())
        val speedBonus = if (completionRatio >= 1.0) (timeRatio * 15) else 0.0

        val score = (completionRatio * 85 + speedBonus).roundToInt().coerceIn(0, 100)
        return score
    }
}
