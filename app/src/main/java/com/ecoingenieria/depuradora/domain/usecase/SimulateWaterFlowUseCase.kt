package com.ecoingenieria.depuradora.domain.usecase

import com.ecoingenieria.depuradora.domain.model.ValveResult
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Motor de simulación del "Operador de Válvulas" (sección 3 del prompt
 * específico). El niño mueve dos sliders (oxígeno y velocidad del agua) y
 * este caso de uso calcula qué tan limpia queda el agua y cuánta energía se
 * gastó, penalizando tanto el exceso como el defecto respecto al rango
 * objetivo del nivel.
 */
class SimulateWaterFlowUseCase {

    operator fun invoke(
        oxygenLevel: Int,
        waterSpeed: Int,
        targetOxygenRange: IntRange,
        targetSpeedRange: IntRange
    ): ValveResult {
        val oxygenClamped = oxygenLevel.coerceIn(0, 100)
        val speedClamped = waterSpeed.coerceIn(0, 100)

        val oxygenDeviation = deviationFromRange(oxygenClamped, targetOxygenRange)
        val speedDeviation = deviationFromRange(speedClamped, targetSpeedRange)

        // Cada punto de desviación fuera del rango objetivo resta calidad.
        val quality = (100 - (oxygenDeviation * 1.4 + speedDeviation * 1.0)).roundToInt().coerceIn(0, 100)

        // El exceso de oxígeno por encima del máximo objetivo consume energía extra.
        val oxygenExcess = max(0, oxygenClamped - targetOxygenRange.last)
        val energyUsed = (30 + oxygenExcess * 1.5).roundToInt().coerceIn(0, 100)

        val feedback = when {
            oxygenClamped > targetOxygenRange.last -> "Hay demasiado oxígeno: gastas energía de más y no mejora la limpieza."
            oxygenClamped < targetOxygenRange.first -> "Falta oxígeno: las bacterias no tienen lo que necesitan para trabajar."
            speedClamped > targetSpeedRange.last -> "El agua va demasiado rápido: no da tiempo a limpiarla bien."
            speedClamped < targetSpeedRange.first -> "El agua va demasiado lenta: el proceso se atasca."
            else -> "¡Ajuste perfecto! El agua fluye a un ritmo ideal."
        }

        return ValveResult(waterQuality = quality, energyUsed = energyUsed, feedback = feedback)
    }

    private fun deviationFromRange(value: Int, range: IntRange): Int = when {
        value < range.first -> range.first - value
        value > range.last -> value - range.last
        else -> 0
    }
}
