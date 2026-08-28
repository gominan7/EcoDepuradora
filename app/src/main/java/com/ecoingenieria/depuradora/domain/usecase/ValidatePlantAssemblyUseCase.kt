package com.ecoingenieria.depuradora.domain.usecase

import com.ecoingenieria.depuradora.domain.model.AssemblyResult

/**
 * Valida el orden en el que el niño colocó las piezas en la línea de
 * ensamblaje del Constructor de Planta (mecánica de drag & drop, sección 3
 * del prompt específico). Es lógica pura, sin dependencias de Android, para
 * poder testearla exhaustivamente.
 */
class ValidatePlantAssemblyUseCase {

    operator fun invoke(placedPieceIds: List<Int>, requiredPieceIds: List<Int>): AssemblyResult {
        if (placedPieceIds.size < requiredPieceIds.size) {
            return AssemblyResult.Incomplete
        }
        for (index in requiredPieceIds.indices) {
            if (placedPieceIds.getOrNull(index) != requiredPieceIds[index]) {
                return AssemblyResult.Incorrect(
                    firstWrongIndex = index,
                    explanation = explanationFor(index, requiredPieceIds.size)
                )
            }
        }
        return AssemblyResult.Correct
    }

    private fun explanationFor(index: Int, total: Int): String = when {
        index == 0 -> "El agua debe entrar primero por la pieza que retiene los objetos más grandes."
        index == total - 1 -> "Esta pieza va al final, justo antes de que el agua siga su camino."
        else -> "Revisa el paso $index: cada pieza limpia algo distinto, en un orden concreto."
    }
}
