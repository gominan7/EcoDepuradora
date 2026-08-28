package com.ecoingenieria.depuradora.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.ecoingenieria.depuradora.data.local.AppDatabase
import com.ecoingenieria.depuradora.data.repository.GameRepositoryImpl
import com.ecoingenieria.depuradora.domain.model.LevelStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.junit.Assert.*

@RunWith(RobolectricTestRunner::class)
class GameRepositoryImplTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: GameRepositoryImpl

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = GameRepositoryImpl(
            stageDao = db.stageDao(),
            pieceDao = db.pieceDao(),
            levelDao = db.levelDao(),
            progressDao = db.levelProgressDao(),
            blueprintDao = db.blueprintDao(),
            badgeDao = db.badgeDao(),
            profileDao = db.playerProfileDao()
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `sembrado en base de datos nueva crea 3 etapas`() = runBlocking {
        repository.ensureSeeded()
        val stages = repository.observeStages().first()
        assertEquals(3, stages.size)
    }

    @Test
    fun `sembrado crea al menos 12 piezas de maquinaria`() = runBlocking {
        repository.ensureSeeded()
        val pieces = repository.observePiecesForStage(1).first() +
            repository.observePiecesForStage(2).first() +
            repository.observePiecesForStage(3).first()
        assertTrue(pieces.size >= 12)
    }

    @Test
    fun `sembrado crea 10 niveles`() = runBlocking {
        repository.ensureSeeded()
        val levels = repository.observeAllLevels().first()
        assertEquals(10, levels.size)
    }

    @Test
    fun `sembrar dos veces no duplica datos`() = runBlocking {
        repository.ensureSeeded()
        repository.ensureSeeded()
        val levels = repository.observeAllLevels().first()
        assertEquals(10, levels.size)
    }

    @Test
    fun `solo el primer nivel esta disponible al empezar`() = runBlocking {
        repository.ensureSeeded()
        val levels = repository.observeAllLevels().first().sortedBy { it.id }
        assertEquals(LevelStatus.AVAILABLE, levels.first { it.id == 1 }.status)
        assertEquals(LevelStatus.LOCKED, levels.first { it.id == 2 }.status)
    }

    @Test
    fun `completar un nivel con exito desbloquea el siguiente`() = runBlocking {
        repository.ensureSeeded()
        repository.submitLevelResult(levelId = 1, finalWaterQuality = 80)
        val level2 = repository.getLevel(2)
        assertEquals(LevelStatus.AVAILABLE, level2?.status)
    }

    @Test
    fun `no superar el umbral de aprobado no desbloquea el siguiente nivel`() = runBlocking {
        repository.ensureSeeded()
        repository.submitLevelResult(levelId = 1, finalWaterQuality = 20)
        val level2 = repository.getLevel(2)
        assertEquals(LevelStatus.LOCKED, level2?.status)
    }

    @Test
    fun `completar un nivel desbloquea su plano de ingenieria`() = runBlocking {
        repository.ensureSeeded()
        repository.submitLevelResult(levelId = 1, finalWaterQuality = 90)
        val blueprints = repository.observeBlueprints().first()
        assertTrue(blueprints.first { it.id == 1 }.unlocked)
    }

    @Test
    fun `completar el primer nivel desbloquea la insignia Primeros Pasos`() = runBlocking {
        repository.ensureSeeded()
        val outcome = repository.submitLevelResult(levelId = 1, finalWaterQuality = 70)
        assertTrue(1 in outcome.newlyUnlockedBadgeIds)
    }

    @Test
    fun `calidad perfecta desbloquea la insignia Rio Cristalino`() = runBlocking {
        repository.ensureSeeded()
        val outcome = repository.submitLevelResult(levelId = 1, finalWaterQuality = 100)
        assertTrue(5 in outcome.newlyUnlockedBadgeIds)
    }

    @Test
    fun `la salud global del agua aumenta al completar niveles`() = runBlocking {
        repository.ensureSeeded()
        val before = repository.observeProfile().first()?.globalWaterHealth ?: 0
        repository.submitLevelResult(levelId = 1, finalWaterQuality = 90)
        val after = repository.observeProfile().first()?.globalWaterHealth ?: 0
        assertTrue(after > before)
    }

    @Test
    fun `reintentar un nivel guarda la mejor puntuacion, no la ultima`() = runBlocking {
        repository.ensureSeeded()
        repository.submitLevelResult(levelId = 1, finalWaterQuality = 90)
        repository.submitLevelResult(levelId = 1, finalWaterQuality = 40)
        val level = repository.getLevel(1)
        assertEquals(90, level?.bestWaterQuality)
    }

    @Test
    fun `nivel inexistente al consultar devuelve null`() = runBlocking {
        repository.ensureSeeded()
        val level = repository.getLevel(9999)
        assertNull(level)
    }

    @Test
    fun `completar onboarding persiste el alias y marca completado`() = runBlocking {
        repository.ensureSeeded()
        repository.completeOnboarding("Luz", "avatar_frog")
        val profile = repository.observeProfile().first()
        assertEquals("Luz", profile?.alias)
        assertTrue(profile?.onboardingCompleted == true)
    }

    @Test
    fun `alias en blanco se reemplaza por un valor por defecto`() = runBlocking {
        repository.ensureSeeded()
        repository.completeOnboarding("", "avatar_frog")
        val profile = repository.observeProfile().first()
        assertEquals("Eco-Ingeniero", profile?.alias)
    }

    @Test
    fun `desactivar el sonido se persiste`() = runBlocking {
        repository.ensureSeeded()
        repository.setSoundEnabled(false)
        val profile = repository.observeProfile().first()
        assertEquals(false, profile?.soundEnabled)
    }

    @Test
    fun `completar el ultimo nivel de una etapa desbloquea el primero de la siguiente`() = runBlocking {
        repository.ensureSeeded()
        // Completa los 4 niveles primarios en orden.
        repository.submitLevelResult(1, 90)
        repository.submitLevelResult(2, 90)
        repository.submitLevelResult(3, 90)
        repository.submitLevelResult(4, 90)
        val firstSecondary = repository.getLevel(5)
        assertEquals(LevelStatus.AVAILABLE, firstSecondary?.status)
    }
}
