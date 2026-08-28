# EcoDepuradora: Misión Agua Limpia 🦫💧

Simulador de ingeniería para niños de 8 a 12 años en el que se diseña,
construye y opera una planta de tratamiento de aguas residuales para salvar
los ríos contaminados de una ciudad. Desarrollado en **Kotlin + Jetpack
Compose**, 100% nativo Android y 100% offline.

## Contenido del repositorio

```
app/                    Código fuente de la aplicación Android
database/                schema.sql y sample_data.sql (referencia del modelo Room)
docs/                    Documentación funcional y técnica
.github/workflows/       CI que compila el APK al hacer push
deliverables/            Entregables empaquetados (ver más abajo)
build.gradle.kts, settings.gradle.kts, gradle.properties, gradlew*
```

## Requisitos

- Android Studio Koala (2024.1) o superior
- JDK 17
- Android SDK con `compileSdk 34`, `minSdk 24`
- Sin conexión a Internet necesaria para jugar (la app no usa red)

## Cómo abrir el proyecto

1. Abre Android Studio → **Open** → selecciona la carpeta raíz de este
   repositorio (la que contiene `settings.gradle.kts`).
2. Deja que Gradle sincronice (descargará dependencias de Google/Maven
   Central la primera vez; a partir de ahí el juego funciona sin red).
3. Ejecuta la configuración `app` sobre un emulador o dispositivo con
   Android 7.0 (API 24) o superior.

## Cómo compilar por línea de comandos

```bash
./gradlew clean
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

El APK de depuración queda en `app/build/outputs/apk/debug/`.

> **Nota de honestidad (regla 37 de la especificación del proyecto):** el
> entorno en el que se generó este proyecto no tiene el SDK de Android ni
> acceso de red al repositorio Maven de Google, por lo que estos comandos
> **no se han podido ejecutar aquí**. El pipeline de `.github/workflows/`
> los ejecuta automáticamente al hacer `git push`. Ver
> `docs/BUILD_REPORT.md` para el detalle exacto.

## Resumen del juego

- **Personaje guía:** Berto, un castor ingeniero con casco.
- **Game loop:** elegir zona contaminada en el mapa → construir la planta
  (drag & drop de piezas en orden) → operar las válvulas de oxígeno y
  velocidad → ayudar a las bacterias en el laboratorio → ver el río
  limpiarse y recibir un Plano de Ingeniería + posibles insignias.
- **3 etapas / 10 niveles / 12 piezas de maquinaria / 10 planos / 8
  insignias**, todo precargado en una base de datos Room local.

Más detalle en `docs/MEMORIA_DESCRIPTIVA.md` (diseño), `docs/MANUAL_USUARIO.md`
(cómo se juega) y `docs/MANUAL_TECNICO.md` (arquitectura de software).
