package com.ecoingenieria.depuradora.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ecoingenieria.depuradora.data.local.dao.*
import com.ecoingenieria.depuradora.data.local.entity.*

@Database(
    entities = [
        StageEntity::class,
        PieceEntity::class,
        LevelEntity::class,
        LevelProgressEntity::class,
        BlueprintEntity::class,
        BlueprintUnlockEntity::class,
        BadgeEntity::class,
        BadgeUnlockEntity::class,
        PlayerProfileEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stageDao(): StageDao
    abstract fun pieceDao(): PieceDao
    abstract fun levelDao(): LevelDao
    abstract fun levelProgressDao(): LevelProgressDao
    abstract fun blueprintDao(): BlueprintDao
    abstract fun badgeDao(): BadgeDao
    abstract fun playerProfileDao(): PlayerProfileDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ecodepuradora.db"
                ).build().also { INSTANCE = it }
            }
    }
}

/**
 * Datos semilla obligatorios (regla 6 del prompt específico / regla 24 de la
 * especificación maestra): 3 etapas, 12 piezas de maquinaria como mínimo y
 * 10 niveles/retos precargados, con sus planos y las insignias que se pueden
 * desbloquear.
 */
object SeedData {

    const val STAGE_PRIMARY = 1
    const val STAGE_SECONDARY = 2
    const val STAGE_TERTIARY = 3

    val stages = listOf(
        StageEntity(
            id = STAGE_PRIMARY,
            orderIndex = 0,
            name = "Tratamiento Primario",
            shortDescription = "Separación física: rejas y arena para atrapar residuos grandes.",
            colorHex = "#8D6E45"
        ),
        StageEntity(
            id = STAGE_SECONDARY,
            orderIndex = 1,
            name = "Tratamiento Secundario",
            shortDescription = "Bacterias y oxígeno limpian la materia orgánica disuelta.",
            colorHex = "#4CAF7D"
        ),
        StageEntity(
            id = STAGE_TERTIARY,
            orderIndex = 2,
            name = "Tratamiento Terciario",
            shortDescription = "Desinfección avanzada con rayos UV antes de volver al río.",
            colorHex = "#1CA9C9"
        )
    )

    // 12 piezas de maquinaria (mínimo exigido), 4 por etapa, cada una con un
    // orden correcto dentro de su línea de ensamblaje.
    val pieces = listOf(
        // Primario
        PieceEntity(101, STAGE_PRIMARY, "Reja Gruesa", "Atrapa ramas, plásticos y basura grande.", 1, "grille_coarse"),
        PieceEntity(102, STAGE_PRIMARY, "Reja Fina", "Detiene partículas más pequeñas que pasaron la reja gruesa.", 2, "grille_fine"),
        PieceEntity(103, STAGE_PRIMARY, "Desarenador", "Deja caer la arena y piedras al fondo del canal.", 3, "grit_chamber"),
        PieceEntity(104, STAGE_PRIMARY, "Decantador Primario", "El agua se calma y la materia pesada se hunde.", 4, "primary_clarifier"),
        // Secundario
        PieceEntity(201, STAGE_SECONDARY, "Tanque de Aireación", "Mezcla el agua con burbujas de oxígeno.", 1, "aeration_tank"),
        PieceEntity(202, STAGE_SECONDARY, "Biorreactor de Bacterias", "Las bacterias buenas comen la materia orgánica.", 2, "bioreactor"),
        PieceEntity(203, STAGE_SECONDARY, "Decantador Secundario", "Separa el agua limpia de los lodos biológicos.", 3, "secondary_clarifier"),
        PieceEntity(204, STAGE_SECONDARY, "Recirculador de Lodos", "Devuelve bacterias útiles al biorreactor.", 4, "sludge_return"),
        // Terciario
        PieceEntity(301, STAGE_TERTIARY, "Filtro de Arena Fina", "Retiene las últimas partículas microscópicas.", 1, "sand_filter"),
        PieceEntity(302, STAGE_TERTIARY, "Cámara de Rayos UV", "La luz UV elimina microorganismos dañinos.", 2, "uv_chamber"),
        PieceEntity(303, STAGE_TERTIARY, "Sensor de Calidad", "Comprueba que el agua cumple los valores seguros.", 3, "quality_sensor"),
        PieceEntity(304, STAGE_TERTIARY, "Compuerta de Salida al Río", "Libera el agua limpia de vuelta al ecosistema.", 4, "river_gate")
    )

    val blueprints = listOf(
        BlueprintEntity(1, "Plano: Reja de Retención", "El primer paso para frenar la basura grande.", STAGE_PRIMARY, "blueprint_grille"),
        BlueprintEntity(2, "Plano: Desarenador", "Cómo se separa la arena del agua.", STAGE_PRIMARY, "blueprint_grit"),
        BlueprintEntity(3, "Plano: Decantador Primario", "El diseño de los tanques de reposo.", STAGE_PRIMARY, "blueprint_clarifier1"),
        BlueprintEntity(4, "Plano: Tanque de Aireación", "Cómo se oxigena el agua para las bacterias.", STAGE_SECONDARY, "blueprint_aeration"),
        BlueprintEntity(5, "Plano: Biorreactor", "El corazón biológico de la planta.", STAGE_SECONDARY, "blueprint_bioreactor"),
        BlueprintEntity(6, "Plano: Recirculador de Lodos", "Cómo se reutilizan las bacterias.", STAGE_SECONDARY, "blueprint_sludge"),
        BlueprintEntity(7, "Plano: Filtro de Arena Fina", "El último filtro físico antes del río.", STAGE_TERTIARY, "blueprint_sandfilter"),
        BlueprintEntity(8, "Plano: Cámara UV", "El diseño de la desinfección con luz ultravioleta.", STAGE_TERTIARY, "blueprint_uv"),
        BlueprintEntity(9, "Plano: Planta Completa", "El esquema maestro de una EcoDepuradora terminada.", STAGE_TERTIARY, "blueprint_full_plant"),
        BlueprintEntity(10, "Plano: Sensor de Calidad", "Cómo se mide si el agua es segura.", STAGE_TERTIARY, "blueprint_sensor")
    )

    val badges = listOf(
        BadgeEntity(1, "Primeros Pasos", "Completa tu primer nivel.", "Completar 1 nivel", "badge_first_steps"),
        BadgeEntity(2, "Maestro de Rejas", "Domina el tratamiento primario.", "Completar los 4 niveles primarios", "badge_primary_master"),
        BadgeEntity(3, "Amigo de las Bacterias", "Domina el tratamiento secundario.", "Completar los 3 niveles secundarios", "badge_bacteria_friend"),
        BadgeEntity(4, "Ingeniero UV", "Domina el tratamiento terciario.", "Completar los 3 niveles terciarios", "badge_uv_engineer"),
        BadgeEntity(5, "Río Cristalino", "Logra 100% de calidad de agua en un nivel.", "Calidad de agua = 100 en un nivel", "badge_crystal_river"),
        BadgeEntity(6, "Coleccionista", "Desbloquea 5 planos de ingeniería.", "5 planos desbloqueados", "badge_collector"),
        BadgeEntity(7, "Eco-Ingeniero Completo", "Termina los 10 niveles de la región.", "10 niveles completados", "badge_full_engineer"),
        BadgeEntity(8, "Guardián del Agua", "Alcanza salud global del agua al 100%.", "Salud global = 100", "badge_water_guardian")
    )

    // 10 niveles/retos: 4 primarios, 3 secundarios, 3 terciarios.
    val levels = listOf(
        LevelEntity(1, STAGE_PRIMARY, 0, "Río del Bosque", "¡Demasiada basura plástica flotando! Empecemos por lo grande.", "101", 0, 100, 0, 100, 20, 1),
        LevelEntity(2, STAGE_PRIMARY, 1, "Arroyo de Piedra", "Ahora hay partículas más pequeñas que colar.", "101,102", 0, 100, 0, 100, 25, 1),
        LevelEntity(3, STAGE_PRIMARY, 2, "Curva del Molino", "La arena se está acumulando en el cauce.", "101,102,103", 0, 100, 0, 100, 30, 2),
        LevelEntity(4, STAGE_PRIMARY, 3, "Puente Viejo", "Momento de dejar que el agua repose y se calme.", "101,102,103,104", 0, 100, 0, 100, 35, 3),
        LevelEntity(5, STAGE_SECONDARY, 0, "Laguna Verde", "El agua necesita oxígeno para que las bacterias trabajen.", "201", 40, 70, 30, 60, 40, 4),
        LevelEntity(6, STAGE_SECONDARY, 1, "Estanque Turbio", "Las bacterias buenas necesitan su biorreactor.", "201,202", 40, 70, 30, 60, 45, 5),
        LevelEntity(7, STAGE_SECONDARY, 2, "Canal Central", "Separemos el agua limpia de los lodos y reciclemos bacterias.", "201,202,203,204", 40, 70, 30, 60, 50, 6),
        LevelEntity(8, STAGE_TERTIARY, 0, "Presa Norte", "Filtremos las últimas partículas microscópicas.", "301", 50, 90, 40, 80, 30, 7),
        LevelEntity(9, STAGE_TERTIARY, 1, "Estuario Azul", "Hora de desinfectar con luz ultravioleta.", "301,302", 50, 90, 40, 80, 35, 8),
        LevelEntity(10, STAGE_TERTIARY, 2, "Desembocadura Final", "Comprobemos la calidad y devolvamos el agua al río.", "301,302,303,304", 50, 90, 40, 80, 40, 9)
    )
}
