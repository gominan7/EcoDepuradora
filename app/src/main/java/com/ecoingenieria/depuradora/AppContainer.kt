package com.ecoingenieria.depuradora

import android.content.Context
import com.ecoingenieria.depuradora.data.local.AppDatabase
import com.ecoingenieria.depuradora.data.repository.GameRepositoryImpl
import com.ecoingenieria.depuradora.domain.model.GameRepository
import com.ecoingenieria.depuradora.domain.usecase.*

/**
 * Contenedor de dependencias manual y simple. Se evita cualquier framework
 * de inyección remoto o librería con dependencias de red: la app es 100%
 * offline (regla 23 de la Especificación Maestra).
 */
class AppContainer(context: Context) {
    private val database = AppDatabase.getInstance(context)

    val gameRepository: GameRepository = GameRepositoryImpl(
        stageDao = database.stageDao(),
        pieceDao = database.pieceDao(),
        levelDao = database.levelDao(),
        progressDao = database.levelProgressDao(),
        blueprintDao = database.blueprintDao(),
        badgeDao = database.badgeDao(),
        profileDao = database.playerProfileDao()
    )

    val validatePlantAssemblyUseCase = ValidatePlantAssemblyUseCase()
    val simulateWaterFlowUseCase = SimulateWaterFlowUseCase()
    val bacteriaLabScoreUseCase = BacteriaLabScoreUseCase()
    val calculateFinalQualityUseCase = CalculateFinalQualityUseCase()
}

class EcoDepuradoraApp : android.app.Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
