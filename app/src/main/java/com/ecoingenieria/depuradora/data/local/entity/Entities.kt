package com.ecoingenieria.depuradora.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Una etapa de tratamiento de agua (Primario, Secundario, Terciario).
 * Agrupa niveles y define qué tipo de piezas se usan en ella.
 */
@Entity(tableName = "stages")
data class StageEntity(
    @PrimaryKey val id: Int,
    val orderIndex: Int,
    val name: String,
    val shortDescription: String,
    val colorHex: String
)

/**
 * Una pieza de maquinaria que el niño puede arrastrar dentro del constructor
 * de planta (rejilla, desarenador, decantador, biorreactor, lámpara UV, etc.)
 */
@Entity(tableName = "pieces")
data class PieceEntity(
    @PrimaryKey val id: Int,
    val stageId: Int,
    val name: String,
    val description: String,
    val correctOrder: Int,
    val iconKey: String
)

/**
 * Un nivel/reto jugable dentro de una etapa. Contiene la referencia a las
 * piezas que deben ensamblarse en orden y los parámetros objetivo del
 * operador de válvulas (oxígeno / velocidad ideales).
 */
@Entity(
    tableName = "levels",
    foreignKeys = [
        ForeignKey(
            entity = StageEntity::class,
            parentColumns = ["id"],
            childColumns = ["stageId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("stageId")]
)
data class LevelEntity(
    @PrimaryKey val id: Int,
    val stageId: Int,
    val orderIndex: Int,
    val zoneName: String,
    val briefing: String,
    val requiredPieceIdsCsv: String, // ids de piezas en el orden correcto, separados por coma
    val targetOxygenMin: Int,
    val targetOxygenMax: Int,
    val targetSpeedMin: Int,
    val targetSpeedMax: Int,
    val bacteriaOrganicLoad: Int, // cuánta "materia orgánica" hay que limpiar en el laboratorio
    val blueprintId: Int
)

/** Progreso persistido de un nivel concreto. */
@Entity(
    tableName = "level_progress",
    foreignKeys = [
        ForeignKey(
            entity = LevelEntity::class,
            parentColumns = ["id"],
            childColumns = ["levelId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("levelId", unique = true)]
)
data class LevelProgressEntity(
    @PrimaryKey(autoGenerate = true) val uid: Long = 0,
    val levelId: Int,
    val status: String, // LOCKED, AVAILABLE, STARTED, COMPLETED, MASTERED
    val bestWaterQuality: Int, // 0-100
    val attempts: Int,
    val lastPlayedEpochMillis: Long
)

/** Plano de ingeniería coleccionable. */
@Entity(tableName = "blueprints")
data class BlueprintEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val description: String,
    val stageId: Int,
    val iconKey: String
)

@Entity(tableName = "blueprint_unlocks")
data class BlueprintUnlockEntity(
    @PrimaryKey(autoGenerate = true) val uid: Long = 0,
    val blueprintId: Int,
    val unlockedEpochMillis: Long
)

/** Insignia Eco-Ingeniero. */
@Entity(tableName = "badges")
data class BadgeEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val description: String,
    val requirement: String, // texto legible de la condición
    val iconKey: String
)

@Entity(tableName = "badge_unlocks")
data class BadgeUnlockEntity(
    @PrimaryKey(autoGenerate = true) val uid: Long = 0,
    val badgeId: Int,
    val unlockedEpochMillis: Long
)

/** Perfil local del jugador (sin datos personales reales). */
@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val id: Int = 1,
    val alias: String,
    val avatarKey: String,
    val globalWaterHealth: Int, // 0-100, calculado a partir del progreso
    val onboardingCompleted: Boolean,
    val soundEnabled: Boolean,
    val hapticsEnabled: Boolean
)
