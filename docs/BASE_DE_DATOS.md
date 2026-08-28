# Base de Datos — EcoDepuradora

Room sobre SQLite, base de datos local `ecodepuradora.db`, versión de
esquema 1. El DDL completo está en `database/schema.sql` y los datos
semilla en `database/sample_data.sql`; este documento resume el propósito
de cada tabla y sus relaciones.

## Diagrama conceptual

```
stages 1───* pieces
stages 1───* levels 1───1 level_progress
levels *───1 blueprints (blueprintId)
blueprints 1───* blueprint_unlocks
badges 1───* badge_unlocks
player_profile (fila única, id = 1)
```

## Tablas

### `stages`
Las 3 etapas de tratamiento (Primario, Secundario, Terciario). Datos
puramente descriptivos y de orden de presentación.

### `pieces`
Las piezas de maquinaria arrastrables del Constructor de Planta. Cada pieza
pertenece a una etapa (`stageId`) y tiene un `correctOrder` que define su
posición en la línea de ensamblaje de esa etapa.

### `levels`
Los retos jugables. Guarda, por nivel:
- `requiredPieceIdsCsv`: ids de piezas en el orden correcto (validado en
  código, no en SQL).
- `targetOxygenMin/Max`, `targetSpeedMin/Max`: rango ideal del Operador de
  Válvulas.
- `bacteriaOrganicLoad`: cuánta materia orgánica hay que limpiar en el
  Laboratorio de Bacterias.
- `blueprintId`: qué plano se desbloquea al aprobar el nivel.

### `level_progress`
Progreso del jugador por nivel: `status` (LOCKED, AVAILABLE, STARTED,
COMPLETED, MASTERED), `bestWaterQuality` (0-100, se conserva el mejor
resultado histórico), `attempts` y marca de tiempo del último intento.

### `blueprints` / `blueprint_unlocks`
Los planos de ingeniería coleccionables y una tabla de registro de cuáles
ha desbloqueado el jugador (con fecha).

### `badges` / `badge_unlocks`
Las insignias Eco-Ingeniero y su registro de desbloqueo, evaluadas por
`EvaluateBadgeUnlocksUseCase` cada vez que se aprueba un nivel.

### `player_profile`
Fila única (`id = 1`) con el alias elegido, avatar, Salud del Agua Global
(media de `bestWaterQuality` de todos los niveles), si completó el
onboarding, y las preferencias de sonido/vibración.

## Notas de diseño

- No se guardan datos personales reales en ninguna tabla (regla 27 de la
  Especificación Maestra).
- El progreso es siempre local: no hay sincronización remota ni tablas de
  usuarios/autenticación.
- `requiredPieceIdsCsv` se guarda como texto separado por comas en lugar de
  una tabla de unión adicional, por simplicidad, dado que el orden importa
  y el tamaño de la lista es pequeño (máx. 4 piezas por nivel); se parsea a
  `List<Int>` en `GameRepositoryImpl` al mapear a modelo de dominio.
