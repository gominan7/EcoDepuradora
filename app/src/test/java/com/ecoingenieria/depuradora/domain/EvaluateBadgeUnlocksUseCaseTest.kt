package com.ecoingenieria.depuradora.domain

import com.ecoingenieria.depuradora.domain.usecase.EvaluateBadgeUnlocksUseCase
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class EvaluateBadgeUnlocksUseCaseTest {

    private lateinit var useCase: EvaluateBadgeUnlocksUseCase

    @Before
    fun setUp() {
        useCase = EvaluateBadgeUnlocksUseCase()
    }

    private fun snapshot(
        total: Int = 0,
        primary: Int = 0,
        secondary: Int = 0,
        tertiary: Int = 0,
        blueprints: Int = 0,
        lastQuality: Int = 0,
        globalHealth: Int = 0,
        already: Set<Int> = emptySet()
    ) = EvaluateBadgeUnlocksUseCase.ProgressSnapshot(
        totalLevelsCompleted = total,
        primaryLevelsCompleted = primary,
        secondaryLevelsCompleted = secondary,
        tertiaryLevelsCompleted = tertiary,
        blueprintsUnlockedCount = blueprints,
        lastLevelWaterQuality = lastQuality,
        globalWaterHealth = globalHealth,
        alreadyUnlockedBadgeIds = already
    )

    @Test
    fun `sin progreso no desbloquea ninguna insignia`() {
        val result = useCase(snapshot())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `completar el primer nivel desbloquea Primeros Pasos`() {
        val result = useCase(snapshot(total = 1))
        assertTrue(1 in result)
    }

    @Test
    fun `completar los 4 niveles primarios desbloquea Maestro de Rejas`() {
        val result = useCase(snapshot(total = 4, primary = 4))
        assertTrue(2 in result)
    }

    @Test
    fun `insignia ya desbloqueada no se repite`() {
        val result = useCase(snapshot(total = 1, already = setOf(1)))
        assertFalse(1 in result)
    }

    @Test
    fun `calidad de agua perfecta desbloquea Rio Cristalino`() {
        val result = useCase(snapshot(total = 1, lastQuality = 100))
        assertTrue(5 in result)
    }

    @Test
    fun `calidad de agua menor a 100 no desbloquea Rio Cristalino`() {
        val result = useCase(snapshot(total = 1, lastQuality = 99))
        assertFalse(5 in result)
    }

    @Test
    fun `cinco planos desbloqueados dan Coleccionista`() {
        val result = useCase(snapshot(total = 1, blueprints = 5))
        assertTrue(6 in result)
    }

    @Test
    fun `diez niveles completados dan Eco-Ingeniero Completo`() {
        val result = useCase(snapshot(total = 10))
        assertTrue(7 in result)
    }

    @Test
    fun `salud global al 100 da Guardian del Agua`() {
        val result = useCase(snapshot(total = 1, globalHealth = 100))
        assertTrue(8 in result)
    }

    @Test
    fun `varias insignias pueden desbloquearse a la vez`() {
        val result = useCase(snapshot(total = 10, primary = 4, secondary = 3, tertiary = 3, blueprints = 9, lastQuality = 100, globalHealth = 100))
        assertEquals(8, result.size)
    }

    @Test
    fun `todas las insignias ya desbloqueadas no devuelve nada nuevo`() {
        val result = useCase(snapshot(total = 10, primary = 4, secondary = 3, tertiary = 3, blueprints = 9, lastQuality = 100, globalHealth = 100, already = (1..8).toSet()))
        assertTrue(result.isEmpty())
    }
}
