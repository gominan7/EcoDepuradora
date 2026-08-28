package com.ecoingenieria.depuradora.domain.model

import kotlinx.coroutines.flow.Flow

/**
 * Contrato del repositorio del juego. La UI y los casos de uso solo conocen
 * esta interfaz; la implementación real con Room vive en la capa data.
 * Esto permite testear la lógica de dominio sin depender de Android.
 */
interface GameRepository {
    suspend fun ensureSeeded()

    fun observeStages(): Flow<List<Stage>>
    fun observePiecesForStage(stageId: Int): Flow<List<Piece>>
    fun observeLevelsForStage(stageId: Int): Flow<List<Level>>
    fun observeAllLevels(): Flow<List<Level>>
    suspend fun getLevel(levelId: Int): Level?

    fun observeBlueprints(): Flow<List<Blueprint>>
    fun observeBadges(): Flow<List<Badge>>
    fun observeProfile(): Flow<PlayerProfile?>

    suspend fun markLevelStarted(levelId: Int)
    suspend fun submitLevelResult(levelId: Int, finalWaterQuality: Int): LevelSubmissionOutcome
    suspend fun completeOnboarding(alias: String, avatarKey: String)
    suspend fun setSoundEnabled(enabled: Boolean)
    suspend fun setHapticsEnabled(enabled: Boolean)

    /**
     * Borra por completo el progreso del jugador (niveles, planos, insignias
     * y perfil) y vuelve a dejar la partida como recién instalada, con el
     * primer nivel disponible y el onboarding pendiente. Usado desde la
     * Oficina del Castor ("Reiniciar todo el progreso"), sin depender de
     * desinstalar la app.
     */
    suspend fun resetAllProgress()
}

data class LevelSubmissionOutcome(
    val newStatus: LevelStatus,
    val blueprintUnlocked: Boolean,
    val newlyUnlockedBadgeIds: List<Int>,
    val newGlobalWaterHealth: Int
)
