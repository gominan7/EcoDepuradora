# Build Report — EcoDepuradora

## Estado: COMPILACIÓN NO VERIFICADA

Siguiendo la regla de honestidad del proyecto (sección 37 de la
Especificación Maestra: *"Si el entorno no puede compilar: COMPILACIÓN NO
VERIFICADA. Nunca simules resultados."*), este informe documenta con
exactitud qué se intentó, qué falló y por qué, sin declarar ningún
`BUILD SUCCESSFUL` que no se haya observado realmente.

## Por qué no se pudo compilar en este entorno

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
Gradle que dependa de los plugins de Android) en este entorno concreto.

## Lo que sí se hizo

- Se generó el código fuente completo de la app (Kotlin/Compose/Room),
  siguiendo la arquitectura MVVM + Clean Architecture pedida.
- Se generó el script wrapper oficial de Gradle (`gradlew`, `gradlew.bat`)
  descargado directamente del repositorio oficial de Gradle en GitHub
  (`github.com/gradle/gradle`, dominio permitido en este entorno).
- Se revisó manualmente cada archivo Kotlin para detectar errores de
  sintaxis evidentes (imports, llaves, tipos) durante su redacción.
- Se preparó un flujo de **GitHub Actions** (`.github/workflows/android-
  build.yml`) que sí tiene acceso completo a Internet y al SDK de Android
  preinstalado en los runners de GitHub, y que ejecuta exactamente la
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
