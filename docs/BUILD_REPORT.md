# Build Report — EcoDepuradora

## Estado: COMPILACIÓN NO VERIFICADA (corrección aplicada tras el primer intento real en CI)

Siguiendo la regla de honestidad del proyecto (sección 37 de la
Especificación Maestra: *"Si el entorno no puede compilar: COMPILACIÓN NO
VERIFICADA. Nunca simules resultados."*), este informe documenta con
exactitud qué se intentó, qué falló y por qué, sin declarar ningún
`BUILD SUCCESSFUL` que no se haya observado realmente.

## Historial de intentos de compilación

### Intento 1 — GitHub Actions, 2026-08-28 04:32 UTC

El usuario subió el proyecto a GitHub y ejecutó el workflow real. Esta fue
la **primera compilación real** del proyecto (no simulada). El paso
`./gradlew testDebugUnitTest --no-daemon` falló en `compileDebugKotlin`
con estos errores exactos:

```
e: .../ui/engineering/EngineeringPanelScreen.kt:106:18 Unresolved reference: clip
e: .../ui/engineering/EngineeringPanelScreen.kt:108:41 Cannot infer a type for this parameter.
e: .../ui/engineering/EngineeringPanelScreen.kt:323:14 Unresolved reference: clip
e: .../ui/engineering/EngineeringPanelScreen.kt:332:22 Unresolved reference: clip
e: .../ui/engineering/EngineeringPanelScreen.kt:335:25 Unresolved reference. None of the
   following candidates is applicable because of receiver type mismatch: detectDragGestures(...)
e: .../ui/engineering/EngineeringViewModel.kt:68:55 Unresolved reference: first
e: .../ui/engineering/EngineeringViewModel.kt:69:49 Unresolved reference: it
```

**Causa raíz y corrección aplicada:**

| Error | Causa | Corrección |
|---|---|---|
| `Unresolved reference: clip` (×3) | Faltaba `import androidx.compose.ui.draw.clip` en `EngineeringPanelScreen.kt` | Import añadido |
| `Cannot infer a type for this parameter` (línea 108) | Efecto cascada del error de `clip` anterior: al romperse la cadena de `Modifier`, el compilador no podía inferir el tipo del parámetro `coords` en el `onGloballyPositioned` siguiente | Se resolvió automáticamente al corregir el import de `clip` |
| `receiver type mismatch` en `detectDragGestures` | Un `pointerInput` con `detectDragGestures { _, _ -> }` vacío y sin propósito real sobre cada partícula del microscopio del Laboratorio de Bacterias (la interacción real ya la cubre el botón "Tocar para limpiar") | Se eliminó ese bloque muerto en `MicroscopeView` |
| `Unresolved reference: first` / `Unresolved reference: it` | Faltaba `import kotlinx.coroutines.flow.first` en `EngineeringViewModel.kt`; se llamaba a `kotlinx.coroutines.flow.first(flow)` con sintaxis incorrecta | Import añadido y la llamada se reescribió como `repository.observePiecesForStage(level.stageId).first()`; también se limpió código muerto (`val allPieces = mutableListOf<Piece>()` sin uso) que quedó de un borrador anterior |

Estas correcciones ya están aplicadas en el código fuente entregado en este
zip. **No se han vuelto a ejecutar en CI todavía** desde este entorno (sigue
sin SDK/red, ver más abajo), por lo que el estado formal sigue siendo
*no verificado* hasta que el usuario vuelva a hacer push y confirme un
`BUILD SUCCESSFUL` real.

## Por qué este entorno de generación no puede compilar

El proyecto se generó dentro de un contenedor Linux (Ubuntu 24, JDK 21)
usado únicamente para escribir y organizar archivos de texto/código. Se
comprobó explícitamente:

| Comprobación | Resultado |
|---|---|
| `which gradle` | No instalado |
| `which kotlinc` | No instalado |
| Variable `ANDROID_HOME` | No definida, sin SDK de Android instalado |
| Acceso a `dl.google.com` (repositorio Maven de Google, imprescindible para AGP/Compose/Room) | `HTTP 403 host_not_allowed` (fuera de la lista de dominios permitidos en este entorno) |
| Acceso a `services.gradle.org` (para descargar la distribución de Gradle del wrapper) | `HTTP 403 host_not_allowed` |

Con estas restricciones de red y sin el SDK de Android, es técnicamente
imposible ejecutar `./gradlew assembleDebug` (ni ningún otro objetivo de
Gradle que dependa de los plugins de Android) en este entorno concreto. Por
eso el primer error real solo pudo detectarse **en GitHub Actions**, con
acceso completo a Internet y al SDK — que es exactamente para lo que se
preparó `.github/workflows/android-build.yml`.

## Lo que sí se hizo

- Se generó el código fuente completo de la app (Kotlin/Compose/Room),
  siguiendo la arquitectura MVVM + Clean Architecture pedida.
- Se generó el script wrapper oficial de Gradle (`gradlew`, `gradlew.bat`,
  `gradle-wrapper.jar`) descargado directamente del repositorio oficial de
  Gradle en GitHub (`github.com/gradle/gradle`, dominio permitido en este
  entorno).
- Se corrigieron, con evidencia real de log de CI, los 3 errores de
  compilación reportados en el primer intento (ver tabla arriba).
- Se revisó de forma proactiva el resto del proyecto en busca del mismo
  patrón de error (imports de `clip` faltantes, usos de `.first()` sin
  importar) y no se encontraron más casos.
- Se preparó un flujo de **GitHub Actions**
  (`.github/workflows/android-build.yml`) que ejecuta exactamente la
  secuencia pedida:
  ```
  ./gradlew clean
  ./gradlew testDebugUnitTest
  ./gradlew lintDebug
  ./gradlew assembleDebug
  ```


## Cómo obtener una compilación verificada real

Cualquiera de estas dos opciones producirá un resultado real (no simulado):

1. **Localmente, en un equipo con Android Studio instalado:**
   ```bash
   cd EcoDepuradora
   ./gradlew clean testDebugUnitTest lintDebug assembleDebug
   ```
   Revisa la salida de la terminal: si todo pasa, verás `BUILD SUCCESSFUL`
   y el APK en `app/build/outputs/apk/debug/app-debug.apk`. Si algo falla,
   el log de Gradle señalará el archivo y la línea exacta.

2. **Con GitHub Actions**, haciendo `git push` de este proyecto a un
   repositorio (no se ha hecho ningún push automático, conforme a la regla
   30: *"NO repositorio. NO push (hasta que se indique lo contrario)"*).
   El workflow `.github/workflows/android-build.yml` compilará el APK,
   correrá los 59 tests unitarios y el lint, y subirá el APK y los reportes
   como artefactos descargables desde la pestaña "Actions".

## Riesgos conocidos a vigilar en la primera compilación real

Sin haber compilado, no se puede garantizar al 100% que no haya errores.
Los puntos de mayor riesgo, a revisar primero si `./gradlew` falla:

- Compatibilidad exacta entre la versión de Kotlin (1.9.24), el plugin de
  Compose Compiler (`kotlinCompilerExtensionVersion = "1.5.14"`) y el AGP
  (8.5.2): si Android Studio sugiere otra combinación al sincronizar,
  aceptar su recomendación.
- El uso de `LazyRow`/`LazyColumn`/`LazyVerticalGrid` y el gesto de arrastre
  personalizado en `EngineeringPanelScreen.kt` (`detectDragGestures` +
  `onGloballyPositioned` + `positionInRoot`) es la parte más intrincada del
  código de UI; si hay un error de compilación, es el primer archivo a
  revisar.
- `EcoNavGraph.kt` construye varios `ViewModel` con una fábrica manual
  (`SimpleViewModelFactory`); confirmar que los imports de
  `androidx.lifecycle.viewmodel.compose.viewModel` resuelven correctamente
  con la versión de `navigation-compose` declarada.

## Tests

Se escribieron 59 tests unitarios (`app/src/test/java/...`), superando el
mínimo de 30-50 exigido para aplicaciones con un motor de simulación
relevante (regla 28). No se han podido ejecutar en este entorno por las
mismas razones de arriba; su resultado real se obtendrá con
`./gradlew testDebugUnitTest` localmente o en CI.

| Archivo | Nº de tests | Qué cubre |
|---|---|---|
| `ValidatePlantAssemblyUseCaseTest.kt` | 9 | Orden del Constructor de Planta |
| `SimulateWaterFlowUseCaseTest.kt` | 11 | Motor de válvulas (oxígeno/velocidad) |
| `ScoringUseCasesTest.kt` | 12 | Laboratorio de bacterias + calidad final combinada |
| `EvaluateBadgeUnlocksUseCaseTest.kt` | 11 | Reglas de desbloqueo de insignias |
| `GameRepositoryImplTest.kt` | 16 | Persistencia Room real (in-memory), progresión de niveles, desbloqueos |
| **Total** | **59** | |

## Checklist de honestidad final

- [x] No se declara `BUILD SUCCESSFUL` sin evidencia.
- [x] No se generó ningún APK simulado o falso.
- [x] Se documentó con exactitud la causa técnica del bloqueo (SDK y red).
- [x] Se dejó un mecanismo real y automatizado (CI) para obtener la
      compilación verdadera con un solo `git push`.
