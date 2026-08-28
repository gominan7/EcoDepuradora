# Manual Técnico — EcoDepuradora: Misión Agua Limpia

## 1. Stack tecnológico

| Capa | Tecnología |
|---|---|
| Lenguaje | Kotlin 1.9.24, JDK 17 |
| UI | Jetpack Compose (BOM 2024.06.00), Material 3 |
| Navegación | Navigation Compose 2.7.7 |
| Persistencia | Room 2.6.1 (KSP) sobre SQLite |
| Concurrencia | Kotlin Coroutines + StateFlow |
| Build | Gradle Kotlin DSL 8.7, Android Gradle Plugin 8.5.2 |
| minSdk / compileSdk / targetSdk | 24 / 34 / 34 |
| Tests | JUnit4, Robolectric, Room in-memory, Truth, Turbine |

No se usan Firebase, backend remoto, analíticas, anuncios ni librerías de
red: la app es 100% offline (regla 23 de la Especificación Maestra) y no
declara ningún permiso `INTERNET` en el `AndroidManifest.xml`.

## 2. Arquitectura

Clean Architecture + MVVM, con tres paquetes principales bajo
`com.ecoingenieria.depuradora`:

```
data/
  local/
    entity/        Entidades @Entity de Room
    dao/            Interfaces @Dao
    AppDatabase.kt  RoomDatabase + SeedData (datos semilla)
  repository/
    GameRepositoryImpl.kt   Implementación del contrato de dominio

domain/
  model/            Modelos de dominio puros (sin anotaciones de Room)
                      + interfaz GameRepository
  usecase/          Lógica de negocio pura y testeable:
                      - ValidatePlantAssemblyUseCase
                      - SimulateWaterFlowUseCase
                      - BacteriaLabScoreUseCase
                      - CalculateFinalQualityUseCase
                      - EvaluateBadgeUnlocksUseCase

ui/
  theme/            Material 3 theme, paleta propia
  components/       Ilustraciones Canvas reutilizables (Berto, tanque de
                      agua, medallas, piezas, tuberías)
  navigation/       NavHost + rutas + fábrica de ViewModel simple
  splash/, onboarding/, map/, engineering/, office/
                      Una pantalla + (cuando aplica) su ViewModel
```

La regla "las reglas principales deben ser testeables sin UI" (sección 22
de la Especificación Maestra) se cumple: todos los `usecase/*` son clases
Kotlin puras, sin `import android.*`, y se testean directamente con JUnit.

### Por qué no se usa Hilt/Dagger

Para mantener el proyecto con "cero dependencias externas" más allá de lo
imprescindible (regla 23), la inyección de dependencias se resuelve con un
contenedor manual (`AppContainer`, instanciado en `EcoDepuradoraApp`) que
construye el `AppDatabase`, el `GameRepositoryImpl` y los casos de uso.
Cada pantalla obtiene su `ViewModel` a través de `SimpleViewModelFactory`.

## 3. Modelo de datos (Room)

Ver `database/schema.sql` para el DDL completo y `database/sample_data.sql`
para los datos semilla. Resumen de tablas:

- `stages` (3 filas: Primario, Secundario, Terciario)
- `pieces` (12 filas: piezas de maquinaria arrastrables)
- `levels` (10 filas: retos jugables, con rango objetivo de oxígeno/velocidad
  y carga de materia orgánica del laboratorio de bacterias)
- `level_progress` (progreso por nivel: estado, mejor calidad, intentos)
- `blueprints` / `blueprint_unlocks` (planos coleccionables y su desbloqueo)
- `badges` / `badge_unlocks` (insignias y su desbloqueo)
- `player_profile` (perfil local: alias, avatar, salud global del agua,
  sonido/vibración, onboarding completado)

Todas las consultas usan `Flow` para observar cambios reactivamente; no hay
SQL embebido en los Composables (regla 22).

## 4. Motor de simulación

El "Game Loop" (construir → operar → laboratorio → resultado) se resuelve
así:

1. `ValidatePlantAssemblyUseCase` compara la lista de piezas colocadas
   contra `Level.requiredPieceIds`. Devuelve `Correct`, `Incorrect(index,
   explicación)` o `Incomplete`.
2. `SimulateWaterFlowUseCase` calcula la desviación de oxígeno y velocidad
   respecto a los rangos objetivo del nivel y produce una calidad 0-100 y
   un consumo de energía.
3. `BacteriaLabScoreUseCase` calcula una puntuación 0-100 a partir de
   cuánta materia orgánica se limpió y cuánto tiempo sobró.
4. `CalculateFinalQualityUseCase` combina los tres resultados: si el
   ensamblaje fue incorrecto o incompleto, la calidad final se limita a un
   máximo del 35%, para que el niño perciba con claridad que debe corregir
   el orden antes de optimizar válvulas y bacterias.
5. `GameRepositoryImpl.submitLevelResult()` persiste el resultado, decide
   si el nivel pasa a `COMPLETED`/`MASTERED`, desbloquea el siguiente nivel,
   el plano de ingeniería de la zona y evalúa `EvaluateBadgeUnlocksUseCase`
   para nuevas insignias, y recalcula la Salud del Agua Global (media de la
   mejor calidad obtenida en cada nivel).

## 5. Estrategia de pruebas

59 tests en `app/src/test/`:

- **43 tests de lógica pura** (`domain/`): validan el motor de ensamblaje,
  el motor de válvulas, la puntuación del laboratorio, el cálculo de
  calidad final y el desbloqueo de insignias, incluyendo casos límite
  (listas vacías, valores negativos, valores fuera de 0-100, duplicados,
  división por cero, condiciones ya cumplidas, etc.).
- **16 tests de integración** (`data/`) con Room en memoria vía Robolectric:
  siembra de base de datos nueva, no duplicar semillas en una segunda
  llamada, cadena de desbloqueo de niveles, desbloqueo de planos e
  insignias, persistencia de la mejor puntuación frente a reintentos,
  transición entre etapas, nivel inexistente, onboarding y ajustes.

Comandos:

```bash
./gradlew testDebugUnitTest
```

## 6. Accesibilidad y privacidad

- Todos los textos usan tipografía del sistema con tamaños legibles
  (Material 3 `Typography`), sin depender únicamente del color para
  transmitir estado (los niveles muestran icono + texto de estado, no solo
  color de fondo).
- No se solicitan permisos peligrosos. No hay `INTERNET`, `CAMERA`,
  `RECORD_AUDIO`, `ACCESS_FINE_LOCATION` ni acceso a contactos.
- El perfil del jugador no almacena nombre real, email, teléfono ni
  ubicación; solo un apodo elegido libremente y un avatar de una lista
  cerrada de 8 opciones.

## 7. Limitaciones conocidas / trabajo futuro

- El laboratorio de bacterias no implementa audio real (ver Memoria
  Descriptiva, sección 7): la lógica y el flag `soundEnabled` ya están
  listos para conectarlos.
- Las ilustraciones son 100% vectoriales generadas por Canvas; no hay
  recursos PNG/WebP en `res/drawable`. Esto reduce el peso del APK y
  garantiza que todo se vea correctamente offline en cualquier densidad de
  pantalla, a cambio de un estilo más geométrico que ilustrado a mano.
