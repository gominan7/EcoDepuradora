package com.ecoingenieria.depuradora.domain

import com.ecoingenieria.depuradora.domain.model.AssemblyResult
import com.ecoingenieria.depuradora.domain.usecase.ValidatePlantAssemblyUseCase
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ValidatePlantAssemblyUseCaseTest {

    private lateinit var useCase: ValidatePlantAssemblyUseCase

    @Before
    fun setUp() {
        useCase = ValidatePlantAssemblyUseCase()
    }

    @Test
    fun `orden correcto exacto devuelve Correct`() {
        val result = useCase(listOf(101, 102, 103), listOf(101, 102, 103))
        assertTrue(result is AssemblyResult.Correct)
    }

    @Test
    fun `lista vacia de piezas colocadas devuelve Incomplete`() {
        val result = useCase(emptyList(), listOf(101, 102))
        assertTrue(result is AssemblyResult.Incomplete)
    }

    @Test
    fun `faltan piezas por colocar devuelve Incomplete`() {
        val result = useCase(listOf(101), listOf(101, 102, 103))
        assertTrue(result is AssemblyResult.Incomplete)
    }

    @Test
    fun `primer elemento incorrecto marca indice 0`() {
        val result = useCase(listOf(102, 101), listOf(101, 102))
        assertTrue(result is AssemblyResult.Incorrect)
        assertEquals(0, (result as AssemblyResult.Incorrect).firstWrongIndex)
    }

    @Test
    fun `orden invertido en la mitad marca el indice correcto`() {
        val result = useCase(listOf(101, 103, 102, 104), listOf(101, 102, 103, 104))
        assertTrue(result is AssemblyResult.Incorrect)
        assertEquals(1, (result as AssemblyResult.Incorrect).firstWrongIndex)
    }

    @Test
    fun `piezas duplicadas no cuentan como validas si no coinciden con el requerido`() {
        val result = useCase(listOf(101, 101, 103), listOf(101, 102, 103))
        assertTrue(result is AssemblyResult.Incorrect)
    }

    @Test
    fun `piezas extra al final no afectan si el prefijo es correcto`() {
        val result = useCase(listOf(101, 102, 103, 999), listOf(101, 102, 103))
        assertTrue(result is AssemblyResult.Correct)
    }

    @Test
    fun `un solo requisito colocado correctamente es Correct`() {
        val result = useCase(listOf(301), listOf(301))
        assertTrue(result is AssemblyResult.Correct)
    }

    @Test
    fun `explicacion no esta vacia cuando es incorrecto`() {
        val result = useCase(listOf(999), listOf(101)) as AssemblyResult.Incorrect
        assertTrue(result.explanation.isNotBlank())
    }
}
