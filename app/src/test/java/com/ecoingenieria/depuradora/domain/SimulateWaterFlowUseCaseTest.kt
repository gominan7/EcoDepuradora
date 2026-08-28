package com.ecoingenieria.depuradora.domain

import com.ecoingenieria.depuradora.domain.usecase.SimulateWaterFlowUseCase
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SimulateWaterFlowUseCaseTest {

    private lateinit var useCase: SimulateWaterFlowUseCase

    @Before
    fun setUp() {
        useCase = SimulateWaterFlowUseCase()
    }

    @Test
    fun `valores dentro del rango objetivo dan calidad maxima`() {
        val result = useCase(55, 45, 40..70, 30..60)
        assertEquals(100, result.waterQuality)
    }

    @Test
    fun `oxigeno por debajo del rango reduce la calidad`() {
        val result = useCase(10, 45, 40..70, 30..60)
        assertTrue(result.waterQuality < 100)
    }

    @Test
    fun `oxigeno por encima del rango consume mas energia`() {
        val dentro = useCase(60, 45, 40..70, 30..60)
        val exceso = useCase(100, 45, 40..70, 30..60)
        assertTrue(exceso.energyUsed > dentro.energyUsed)
    }

    @Test
    fun `velocidad por debajo del rango reduce la calidad`() {
        val result = useCase(55, 5, 40..70, 30..60)
        assertTrue(result.waterQuality < 100)
    }

    @Test
    fun `velocidad por encima del rango reduce la calidad`() {
        val result = useCase(55, 95, 40..70, 30..60)
        assertTrue(result.waterQuality < 100)
    }

    @Test
    fun `valores negativos se recortan a 0`() {
        val result = useCase(-20, -50, 40..70, 30..60)
        assertTrue(result.waterQuality in 0..100)
        assertTrue(result.energyUsed in 0..100)
    }

    @Test
    fun `valores mayores a 100 se recortan a 100`() {
        val result = useCase(500, 500, 40..70, 30..60)
        assertTrue(result.waterQuality in 0..100)
        assertTrue(result.energyUsed in 0..100)
    }

    @Test
    fun `limite inferior exacto del rango cuenta como ideal`() {
        val result = useCase(40, 30, 40..70, 30..60)
        assertEquals(100, result.waterQuality)
    }

    @Test
    fun `limite superior exacto del rango cuenta como ideal`() {
        val result = useCase(70, 60, 40..70, 30..60)
        assertEquals(100, result.waterQuality)
    }

    @Test
    fun `feedback distinto para exceso de oxigeno y falta de oxigeno`() {
        val exceso = useCase(90, 45, 40..70, 30..60)
        val falta = useCase(10, 45, 40..70, 30..60)
        assertNotEquals(exceso.feedback, falta.feedback)
    }

    @Test
    fun `calidad nunca es negativa con desviaciones extremas`() {
        val result = useCase(0, 0, 40..70, 30..60)
        assertTrue(result.waterQuality >= 0)
    }
}
