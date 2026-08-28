-- ============================================================
-- EcoDepuradora: Misión Agua Limpia
-- database/sample_data.sql
-- Refleja exactamente los datos semilla insertados en tiempo de ejecución
-- por data/local/AppDatabase.kt (objeto SeedData). Útil para inspeccionar
-- o recrear el contenido inicial fuera de la app.
-- ============================================================

INSERT INTO stages (id, orderIndex, name, shortDescription, colorHex) VALUES
(1, 0, 'Tratamiento Primario', 'Separación física: rejas y arena para atrapar residuos grandes.', '#8D6E45'),
(2, 1, 'Tratamiento Secundario', 'Bacterias y oxígeno limpian la materia orgánica disuelta.', '#4CAF7D'),
(3, 2, 'Tratamiento Terciario', 'Desinfección avanzada con rayos UV antes de volver al río.', '#1CA9C9');

INSERT INTO pieces (id, stageId, name, description, correctOrder, iconKey) VALUES
(101, 1, 'Reja Gruesa', 'Atrapa ramas, plásticos y basura grande.', 1, 'grille_coarse'),
(102, 1, 'Reja Fina', 'Detiene partículas más pequeñas que pasaron la reja gruesa.', 2, 'grille_fine'),
(103, 1, 'Desarenador', 'Deja caer la arena y piedras al fondo del canal.', 3, 'grit_chamber'),
(104, 1, 'Decantador Primario', 'El agua se calma y la materia pesada se hunde.', 4, 'primary_clarifier'),
(201, 2, 'Tanque de Aireación', 'Mezcla el agua con burbujas de oxígeno.', 1, 'aeration_tank'),
(202, 2, 'Biorreactor de Bacterias', 'Las bacterias buenas comen la materia orgánica.', 2, 'bioreactor'),
(203, 2, 'Decantador Secundario', 'Separa el agua limpia de los lodos biológicos.', 3, 'secondary_clarifier'),
(204, 2, 'Recirculador de Lodos', 'Devuelve bacterias útiles al biorreactor.', 4, 'sludge_return'),
(301, 3, 'Filtro de Arena Fina', 'Retiene las últimas partículas microscópicas.', 1, 'sand_filter'),
(302, 3, 'Cámara de Rayos UV', 'La luz UV elimina microorganismos dañinos.', 2, 'uv_chamber'),
(303, 3, 'Sensor de Calidad', 'Comprueba que el agua cumple los valores seguros.', 3, 'quality_sensor'),
(304, 3, 'Compuerta de Salida al Río', 'Libera el agua limpia de vuelta al ecosistema.', 4, 'river_gate');

INSERT INTO blueprints (id, name, description, stageId, iconKey) VALUES
(1, 'Plano: Reja de Retención', 'El primer paso para frenar la basura grande.', 1, 'blueprint_grille'),
(2, 'Plano: Desarenador', 'Cómo se separa la arena del agua.', 1, 'blueprint_grit'),
(3, 'Plano: Decantador Primario', 'El diseño de los tanques de reposo.', 1, 'blueprint_clarifier1'),
(4, 'Plano: Tanque de Aireación', 'Cómo se oxigena el agua para las bacterias.', 2, 'blueprint_aeration'),
(5, 'Plano: Biorreactor', 'El corazón biológico de la planta.', 2, 'blueprint_bioreactor'),
(6, 'Plano: Recirculador de Lodos', 'Cómo se reutilizan las bacterias.', 2, 'blueprint_sludge'),
(7, 'Plano: Filtro de Arena Fina', 'El último filtro físico antes del río.', 3, 'blueprint_sandfilter'),
(8, 'Plano: Cámara UV', 'El diseño de la desinfección con luz ultravioleta.', 3, 'blueprint_uv'),
(9, 'Plano: Planta Completa', 'El esquema maestro de una EcoDepuradora terminada.', 3, 'blueprint_full_plant'),
(10, 'Plano: Sensor de Calidad', 'Cómo se mide si el agua es segura.', 3, 'blueprint_sensor');

INSERT INTO badges (id, name, description, requirement, iconKey) VALUES
(1, 'Primeros Pasos', 'Completa tu primer nivel.', 'Completar 1 nivel', 'badge_first_steps'),
(2, 'Maestro de Rejas', 'Domina el tratamiento primario.', 'Completar los 4 niveles primarios', 'badge_primary_master'),
(3, 'Amigo de las Bacterias', 'Domina el tratamiento secundario.', 'Completar los 3 niveles secundarios', 'badge_bacteria_friend'),
(4, 'Ingeniero UV', 'Domina el tratamiento terciario.', 'Completar los 3 niveles terciarios', 'badge_uv_engineer'),
(5, 'Río Cristalino', 'Logra 100% de calidad de agua en un nivel.', 'Calidad de agua = 100 en un nivel', 'badge_crystal_river'),
(6, 'Coleccionista', 'Desbloquea 5 planos de ingeniería.', '5 planos desbloqueados', 'badge_collector'),
(7, 'Eco-Ingeniero Completo', 'Termina los 10 niveles de la región.', '10 niveles completados', 'badge_full_engineer'),
(8, 'Guardián del Agua', 'Alcanza salud global del agua al 100%.', 'Salud global = 100', 'badge_water_guardian');

INSERT INTO levels (id, stageId, orderIndex, zoneName, briefing, requiredPieceIdsCsv, targetOxygenMin, targetOxygenMax, targetSpeedMin, targetSpeedMax, bacteriaOrganicLoad, blueprintId) VALUES
(1, 1, 0, 'Río del Bosque', '¡Demasiada basura plástica flotando! Empecemos por lo grande.', '101', 0, 100, 0, 100, 20, 1),
(2, 1, 1, 'Arroyo de Piedra', 'Ahora hay partículas más pequeñas que colar.', '101,102', 0, 100, 0, 100, 25, 1),
(3, 1, 2, 'Curva del Molino', 'La arena se está acumulando en el cauce.', '101,102,103', 0, 100, 0, 100, 30, 2),
(4, 1, 3, 'Puente Viejo', 'Momento de dejar que el agua repose y se calme.', '101,102,103,104', 0, 100, 0, 100, 35, 3),
(5, 2, 0, 'Laguna Verde', 'El agua necesita oxígeno para que las bacterias trabajen.', '201', 40, 70, 30, 60, 40, 4),
(6, 2, 1, 'Estanque Turbio', 'Las bacterias buenas necesitan su biorreactor.', '201,202', 40, 70, 30, 60, 45, 5),
(7, 2, 2, 'Canal Central', 'Separemos el agua limpia de los lodos y reciclemos bacterias.', '201,202,203,204', 40, 70, 30, 60, 50, 6),
(8, 3, 0, 'Presa Norte', 'Filtremos las últimas partículas microscópicas.', '301', 50, 90, 40, 80, 30, 7),
(9, 3, 1, 'Estuario Azul', 'Hora de desinfectar con luz ultravioleta.', '301,302', 50, 90, 40, 80, 35, 8),
(10, 3, 2, 'Desembocadura Final', 'Comprobemos la calidad y devolvamos el agua al río.', '301,302,303,304', 50, 90, 40, 80, 40, 9);

-- Progreso inicial: nivel 1 disponible, el resto bloqueados (se genera en
-- tiempo de ejecución por GameRepositoryImpl.ensureSeeded(), aquí como referencia).
INSERT INTO level_progress (levelId, status, bestWaterQuality, attempts, lastPlayedEpochMillis) VALUES
(1, 'AVAILABLE', 0, 0, 0),
(2, 'LOCKED', 0, 0, 0),
(3, 'LOCKED', 0, 0, 0),
(4, 'LOCKED', 0, 0, 0),
(5, 'LOCKED', 0, 0, 0),
(6, 'LOCKED', 0, 0, 0),
(7, 'LOCKED', 0, 0, 0),
(8, 'LOCKED', 0, 0, 0),
(9, 'LOCKED', 0, 0, 0),
(10, 'LOCKED', 0, 0, 0);

-- Perfil de jugador local por defecto (sin datos personales reales).
INSERT INTO player_profile (id, alias, avatarKey, globalWaterHealth, onboardingCompleted, soundEnabled, hapticsEnabled) VALUES
(1, '', 'avatar_beaver_1', 0, 0, 1, 1);
