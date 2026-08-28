package com.ecoingenieria.depuradora.domain.usecase

import com.ecoingenieria.depuradora.domain.model.AssemblyResult
import kotlin.math.roundToInt

/**
 * Combina las tres mecánicas del Game Loop (Constructor, Operador de
 * Válvulas y Laboratorio de Bacterias) en una única calidad de agua final
 * (0-100), que es lo que determina el feedback visual (sección 4 del
 * prompt específico: "el agua cambia de color según el éxito").
 *
 * Ponderación: el ensamblaje correcto es obligatorio (si está mal, el agua
 * se estanca y la calidad máxima queda muy limitada); el resto se reparte
 * entre válvulas y bacterias.
 */
class CalculateFinalQualityUseCase {

    operator fun invoke(assembly: AssemblyResult, valveQuality: Int, bacteriaScore: Int): Int {
        return when (assembly) {
            is AssemblyResult.Correct -> {
                (valveQuality * 0.55 + bacteriaScore * 0.45).roundToInt().coerceIn(0, 100)
            }
            is AssemblyResult.Incorrect, AssemblyResult.Incomplete -> {
                // El agua se estanca: como máximo se alcanza un 35%, para que el
                // niño perciba con claridad que debe corregir el orden.
                ((valveQuality * 0.55 + bacteriaScore * 0.45) * 0.35).roundToInt().coerceIn(0, 35)
            }
        }
    }
}
