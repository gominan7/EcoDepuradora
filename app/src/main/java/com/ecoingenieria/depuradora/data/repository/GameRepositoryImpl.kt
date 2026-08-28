package com.ecoingenieria.depuradora.data.repository

import com.ecoingenieria.depuradora.data.local.SeedData
import com.ecoingenieria.depuradora.data.local.dao.*
import com.ecoingenieria.depuradora.data.local.entity.*
import com.ecoingenieria.depuradora.domain.model.*
import com.ecoingenieria.depuradora.domain.usecase.EvaluateBadgeUnlocksUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class GameRepositoryImpl(
    private val stageDao: StageDao,
    private val pieceDao: PieceDao,
    private val levelDao: LevelDao,
    private val progressDao: LevelProgressDao,
    private val blueprintDao: BlueprintDao,
    private val badgeDao: BadgeDao,
    private val profileDao: PlayerProfileDao,
    private val evaluateBadgeUnlocks: EvaluateBadgeUnlocksUseCase = EvaluateBadgeUnlocksUseCase(),
    private val clock: () -> Long = { System.currentTimeMillis() }
) : GameRepository {

    override suspend fun ensureSeeded() {
        if (stageDao.count() == 0) stageDao.insertAll(SeedData.stages)
        if (pieceDao.count() == 0) pieceDao.insertAll(SeedData.pieces)
        if (levelDao.count() == 0) levelDao.insertAll(SeedData.levels)
        if (blueprintDao.count() == 0) blueprintDao.insertAll(SeedData.blueprints)
        if (badgeDao.count() == 0) badgeDao.insertAll(SeedData.badges)

        if (progressDao.count() == 0) {
            val sorted = SeedData.levels.sortedWith(compareBy({ it.stageId }, { it.orderIndex }))
            val seedProgress = sorted.mapIndexed { index, level ->
                LevelProgressEntity(
                    levelId = level.id,
                    status = if (index == 0) LevelStatus.AVAILABLE.name else LevelStatus.LOCKED.name,
                    bestWaterQuality = 0,
                    attempts = 0,
                    lastPlayedEpochMillis = 0L
                )
            }
            progressDao.insertAll(seedProgress)
        }

        if (profileDao.getProfile() == null) {
            profileDao.insertIfAbsent(
                PlayerProfileEntity(
                    alias = "",
                    avatarKey = "avatar_beaver_1",
                    globalWaterHealth = 0,
                    onboardingCompleted = false,
                    soundEnabled = true,
                    hapticsEnabled = true
                )
            )
        }
    }

    override fun observeStages(): Flow<List<Stage>> =
        stageDao.observeStages().map { list -> list.map { it.toDomain() } }

    override fun observePiecesForStage(stageId: Int): Flow<List<Piece>> =
        pieceDao.observePiecesForStage(stageId).map { list -> list.map { it.toDomain() } }

    override fun observeLevelsForStage(stageId: Int): Flow<List<Level>> =
        combine(levelDao.observeLevelsForStage(stageId), progressDao.observeAll()) { levels, progress ->
            val progressByLevel = progress.associateBy { it.levelId }
            levels.map { it.toDomain(progressByLevel[it.id]) }
        }

    override fun observeAllLevels(): Flow<List<Level>> =
        combine(levelDao.observeAllLevels(), progressDao.observeAll()) { levels, progress ->
            val progressByLevel = progress.associateBy { it.levelId }
            levels.map { it.toDomain(progressByLevel[it.id]) }
        }

    override suspend fun getLevel(levelId: Int): Level? {
        val entity = levelDao.getById(levelId) ?: return null
        val progress = progressDao.getForLevel(levelId)
        return entity.toDomain(progress)
    }

    override fun observeBlueprints(): Flow<List<Blueprint>> =
        combine(blueprintDao.observeAll(), blueprintDao.observeUnlockedIds()) { blueprints, unlockedIds ->
            val unlockedSet = unlockedIds.toSet()
            blueprints.map { it.toDomain(unlockedSet.contains(it.id)) }
        }

    override fun observeBadges(): Flow<List<Badge>> =
        combine(badgeDao.observeAll(), badgeDao.observeUnlockedIds()) { badges, unlockedIds ->
            val unlockedSet = unlockedIds.toSet()
            badges.map { it.toDomain(unlockedSet.contains(it.id)) }
        }

    override fun observeProfile(): Flow<PlayerProfile?> =
        profileDao.observeProfile().map { it?.toDomain() }

    override suspend fun markLevelStarted(levelId: Int) {
        val current = progressDao.getForLevel(levelId) ?: return
        if (current.status == LevelStatus.AVAILABLE.name) {
            progressDao.update(current.copy(status = LevelStatus.STARTED.name, lastPlayedEpochMillis = clock()))
        }
    }

    override suspend fun submitLevelResult(levelId: Int, finalWaterQuality: Int): LevelSubmissionOutcome {
        val levelEntity = levelDao.getById(levelId)
            ?: return LevelSubmissionOutcome(LevelStatus.LOCKED, false, emptyList(), 0)
        val existingProgress = progressDao.getForLevel(levelId)
        val quality = finalWaterQuality.coerceIn(0, 100)
        val passed = quality >= 60

        val newStatus = when {
            !passed -> LevelStatus.STARTED
            quality >= 90 -> LevelStatus.MASTERED
            else -> LevelStatus.COMPLETED
        }

        val newBest = maxOf(existingProgress?.bestWaterQuality ?: 0, quality)
        progressDao.upsert(
            LevelProgressEntity(
                uid = existingProgress?.uid ?: 0,
                levelId = levelId,
                status = newStatus.name,
                bestWaterQuality = newBest,
                attempts = (existingProgress?.attempts ?: 0) + 1,
                lastPlayedEpochMillis = clock()
            )
        )

        var blueprintUnlocked = false
        if (passed) {
            unlockNextLevel(levelEntity)
            blueprintUnlocked = unlockBlueprintIfNeeded(levelEntity.blueprintId)
        }

        val newlyUnlockedBadgeIds = if (passed) evaluateAndUnlockBadges(quality) else emptyList()
        val newGlobalHealth = recomputeGlobalWaterHealth()

        return LevelSubmissionOutcome(
            newStatus = newStatus,
            blueprintUnlocked = blueprintUnlocked,
            newlyUnlockedBadgeIds = newlyUnlockedBadgeIds,
            newGlobalWaterHealth = newGlobalHealth
        )
    }

    private suspend fun unlockNextLevel(completedLevel: LevelEntity) {
        val stageLevels = SeedData.levels.filter { it.stageId == completedLevel.stageId }
            .sortedBy { it.orderIndex }
        val nextInStage = stageLevels.firstOrNull { it.orderIndex == completedLevel.orderIndex + 1 }
        val target = nextInStage ?: run {
            // Si era el último nivel de la etapa, desbloquea el primero de la siguiente etapa.
            val nextStageLevels = SeedData.levels
                .filter { it.stageId == completedLevel.stageId + 1 && it.orderIndex == 0 }
            nextStageLevels.firstOrNull()
        } ?: return

        val targetProgress = progressDao.getForLevel(target.id) ?: return
        if (targetProgress.status == LevelStatus.LOCKED.name) {
            progressDao.update(targetProgress.copy(status = LevelStatus.AVAILABLE.name))
        }
    }

    private suspend fun unlockBlueprintIfNeeded(blueprintId: Int): Boolean {
        val alreadyUnlocked = blueprintDao.observeUnlockedIds().first().contains(blueprintId)
        if (alreadyUnlocked) return false
        blueprintDao.unlock(BlueprintUnlockEntity(blueprintId = blueprintId, unlockedEpochMillis = clock()))
        return true
    }

    private suspend fun evaluateAndUnlockBadges(lastLevelWaterQuality: Int): List<Int> {
        val allProgress = progressDao.observeAll().first()
        val completedOrMastered = allProgress.filter {
            it.status == LevelStatus.COMPLETED.name || it.status == LevelStatus.MASTERED.name
        }
        val completedLevelIds = completedOrMastered.map { it.levelId }.toSet()
        val allLevels = SeedData.levels.associateBy { it.id }

        val primaryCompleted = completedLevelIds.count { allLevels[it]?.stageId == SeedData.STAGE_PRIMARY }
        val secondaryCompleted = completedLevelIds.count { allLevels[it]?.stageId == SeedData.STAGE_SECONDARY }
        val tertiaryCompleted = completedLevelIds.count { allLevels[it]?.stageId == SeedData.STAGE_TERTIARY }

        val blueprintsUnlocked = blueprintDao.observeUnlockedIds().first().size
        val alreadyBadges = badgeDao.observeUnlockedIds().first().toSet()
        val globalHealth = recomputeGlobalWaterHealthFrom(allProgress)

        val snapshot = EvaluateBadgeUnlocksUseCase.ProgressSnapshot(
            totalLevelsCompleted = completedLevelIds.size,
            primaryLevelsCompleted = primaryCompleted,
            secondaryLevelsCompleted = secondaryCompleted,
            tertiaryLevelsCompleted = tertiaryCompleted,
            blueprintsUnlockedCount = blueprintsUnlocked,
            lastLevelWaterQuality = lastLevelWaterQuality,
            globalWaterHealth = globalHealth,
            alreadyUnlockedBadgeIds = alreadyBadges
        )

        val newlyUnlocked = evaluateBadgeUnlocks(snapshot)
        newlyUnlocked.forEach { badgeId ->
            badgeDao.unlock(BadgeUnlockEntity(badgeId = badgeId, unlockedEpochMillis = clock()))
        }
        return newlyUnlocked
    }

    private suspend fun recomputeGlobalWaterHealth(): Int {
        val allProgress = progressDao.observeAll().first()
        val health = recomputeGlobalWaterHealthFrom(allProgress)
        val profile = profileDao.getProfile()
        if (profile != null && profile.globalWaterHealth != health) {
            profileDao.update(profile.copy(globalWaterHealth = health))
        }
        return health
    }

    private fun recomputeGlobalWaterHealthFrom(allProgress: List<LevelProgressEntity>): Int {
        if (allProgress.isEmpty()) return 0
        val total = allProgress.sumOf { it.bestWaterQuality }
        return (total / allProgress.size).coerceIn(0, 100)
    }

    override suspend fun completeOnboarding(alias: String, avatarKey: String) {
        val profile = profileDao.getProfile() ?: return
        profileDao.update(
            profile.copy(
                alias = alias.ifBlank { "Eco-Ingeniero" },
                avatarKey = avatarKey,
                onboardingCompleted = true
            )
        )
    }

    override suspend fun setSoundEnabled(enabled: Boolean) {
        val profile = profileDao.getProfile() ?: return
        profileDao.update(profile.copy(soundEnabled = enabled))
    }

    override suspend fun setHapticsEnabled(enabled: Boolean) {
        val profile = profileDao.getProfile() ?: return
        profileDao.update(profile.copy(hapticsEnabled = enabled))
    }
}

private fun StageEntity.toDomain() = Stage(id, orderIndex, name, shortDescription, colorHex)

private fun PieceEntity.toDomain() = Piece(id, stageId, name, description, correctOrder, iconKey)

private fun LevelEntity.toDomain(progress: LevelProgressEntity?): Level = Level(
    id = id,
    stageId = stageId,
    orderIndex = orderIndex,
    zoneName = zoneName,
    briefing = briefing,
    requiredPieceIds = requiredPieceIdsCsv.split(",").mapNotNull { it.trim().toIntOrNull() },
    targetOxygenRange = targetOxygenMin..targetOxygenMax,
    targetSpeedRange = targetSpeedMin..targetSpeedMax,
    bacteriaOrganicLoad = bacteriaOrganicLoad,
    blueprintId = blueprintId,
    status = progress?.status?.let { runCatching { LevelStatus.valueOf(it) }.getOrDefault(LevelStatus.LOCKED) }
        ?: LevelStatus.LOCKED,
    bestWaterQuality = progress?.bestWaterQuality ?: 0
)

private fun BlueprintEntity.toDomain(unlocked: Boolean) = Blueprint(id, name, description, stageId, iconKey, unlocked)

private fun BadgeEntity.toDomain(unlocked: Boolean) = Badge(id, name, description, requirement, iconKey, unlocked)

private fun PlayerProfileEntity.toDomain() = PlayerProfile(
    alias = alias,
    avatarKey = avatarKey,
    globalWaterHealth = globalWaterHealth,
    onboardingCompleted = onboardingCompleted,
    soundEnabled = soundEnabled,
    hapticsEnabled = hapticsEnabled
)
