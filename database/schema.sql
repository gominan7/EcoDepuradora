-- ============================================================
-- EcoDepuradora: Misión Agua Limpia
-- database/schema.sql
-- Esquema equivalente al generado por Room a partir de las entidades
-- Kotlin en data/local/entity/Entities.kt. Se documenta aparte en SQL
-- puro para trazabilidad (regla 33 de la Especificación Maestra).
-- ============================================================

CREATE TABLE IF NOT EXISTS stages (
    id INTEGER NOT NULL PRIMARY KEY,
    orderIndex INTEGER NOT NULL,
    name TEXT NOT NULL,
    shortDescription TEXT NOT NULL,
    colorHex TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS pieces (
    id INTEGER NOT NULL PRIMARY KEY,
    stageId INTEGER NOT NULL,
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    correctOrder INTEGER NOT NULL,
    iconKey TEXT NOT NULL,
    FOREIGN KEY (stageId) REFERENCES stages(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS levels (
    id INTEGER NOT NULL PRIMARY KEY,
    stageId INTEGER NOT NULL,
    orderIndex INTEGER NOT NULL,
    zoneName TEXT NOT NULL,
    briefing TEXT NOT NULL,
    requiredPieceIdsCsv TEXT NOT NULL,
    targetOxygenMin INTEGER NOT NULL,
    targetOxygenMax INTEGER NOT NULL,
    targetSpeedMin INTEGER NOT NULL,
    targetSpeedMax INTEGER NOT NULL,
    bacteriaOrganicLoad INTEGER NOT NULL,
    blueprintId INTEGER NOT NULL,
    FOREIGN KEY (stageId) REFERENCES stages(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS index_levels_stageId ON levels(stageId);

CREATE TABLE IF NOT EXISTS level_progress (
    uid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    levelId INTEGER NOT NULL,
    status TEXT NOT NULL, -- LOCKED | AVAILABLE | STARTED | COMPLETED | MASTERED
    bestWaterQuality INTEGER NOT NULL,
    attempts INTEGER NOT NULL,
    lastPlayedEpochMillis INTEGER NOT NULL,
    FOREIGN KEY (levelId) REFERENCES levels(id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX IF NOT EXISTS index_level_progress_levelId ON level_progress(levelId);

CREATE TABLE IF NOT EXISTS blueprints (
    id INTEGER NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    stageId INTEGER NOT NULL,
    iconKey TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS blueprint_unlocks (
    uid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    blueprintId INTEGER NOT NULL,
    unlockedEpochMillis INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS badges (
    id INTEGER NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    requirement TEXT NOT NULL,
    iconKey TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS badge_unlocks (
    uid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    badgeId INTEGER NOT NULL,
    unlockedEpochMillis INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS player_profile (
    id INTEGER NOT NULL PRIMARY KEY,
    alias TEXT NOT NULL,
    avatarKey TEXT NOT NULL,
    globalWaterHealth INTEGER NOT NULL,
    onboardingCompleted INTEGER NOT NULL, -- boolean 0/1
    soundEnabled INTEGER NOT NULL,
    hapticsEnabled INTEGER NOT NULL
);
