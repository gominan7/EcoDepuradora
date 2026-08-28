package com.ecoingenieria.depuradora.domain

import com.ecoingenieria.depuradora.domain.model.AssemblyResult
import com.ecoingenieria.depuradora.domain.usecase.BacteriaLabScoreUseCase
import com.ecoingenieria.depuradora.domain.usecase.CalculateFinalQualityUseCase
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class BacteriaLabScoreUseCaseTest {

    private lateinit var useCase: BacteriaLabScoreUseCase

    @Before
    fun setUp() {
        useCase = BacteriaLabScoreUseCase()
    }

    @Test
    fun `comer toda la materia organica con tiempo sobrante da puntuacion alta`() {
        val score = useCase(organicLoad = 20, organicUnitsEaten = 20, timeRemainingSeconds = 10, totalTimeSeconds = 20)
        assertTrue(score >= 90)
    }

    @Test
    fun `no comer nada da puntuacion cero`() {
        val score = useCase(organicLoad = 20, organicUnitsEaten = 0, timeRemainingSeconds = 0, totalTimeSeconds = 20)
        assertEquals(0, score)
    }

    @Test
    fun `comer la mitad da una puntuacion intermedia`() {
        val score = useCase(organicLoad = 20, organicUnitsEaten = 10, timeRemainingSeconds = 0, totalTimeSeconds = 20)
        assertTrue(score in 30..60)
    }

    @Test
    fun `organicLoad cero da puntuacion perfecta por definicion`() {
        val score = useCase(organicLoad = 0, organicUnitsEaten = 0, timeRemainingSeconds = 0, totalTimeSeconds = 20)
        assertEquals(100, score)
    }

    @Test
    fun `comer mas de lo disponible se recorta al maximo`() {
        val score = useCase(organicLoad = 10, organicUnitsEaten = 999, timeRemainingSeconds = 5, totalTimeSeconds = 20)
        assertTrue(score in 0..100)
    }

    @Test
    fun `terminar justo a tiempo no da bono de velocidad`() {
        val score = useCase(organicLoad = 10, organicUnitsEaten = 10, timeRemainingSeconds = 0, totalTimeSeconds = 20)
        assertEquals(85, score)
    }

    @Test
    fun `totalTimeSeconds cero no produce division por cero`() {
        val score = useCase(organicLoad = 10, organicUnitsEaten = 5, timeRemainingSeconds = 0, totalTimeSeconds = 0)
        assertTrue(score in 0..100)
    }
}

class CalculateFinalQualityUseCaseTest {

    private lateinit var useCase: CalculateFinalQualityUseCase

    @Before
    fun setUp() {
        useCase = CalculateFinalQualityUseCase()
    }

    @Test
    fun `ensamblaje correcto con valvulas y bacterias perfectas da 100`() {
        val quality = useCase(AssemblyResult.Correct, 100, 100)
        assertEquals(100, quality)
    }

    @Test
    fun `ensamblaje incorrecto limita la calidad maxima a 35`() {
        val quality = useCase(AssemblyResult.Incorrect(0, "x"), 100, 100)
        assertTrue(quality <= 35)
    }

    @Test
    fun `ensamblaje incompleto tambien limita la calidad`() {
        val quality = useCase(AssemblyResult.Incomplete, 100, 100)
        assertTrue(quality <= 35)
    }

    @Test
    fun `valvulas y bacterias en cero con ensamblaje correcto da cero`() {
        val quality = useCase(AssemblyResult.Correct, 0, 0)
        assertEquals(0, quality)
    }

    @Test
    fun `resultado siempre esta en el rango 0 a 100`() {
        val quality = useCase(AssemblyResult.Correct, 55, 77)
        assertTrue(quality in 0..100)
    }
}
