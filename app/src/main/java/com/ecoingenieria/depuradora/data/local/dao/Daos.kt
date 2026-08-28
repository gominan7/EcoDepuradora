package com.ecoingenieria.depuradora.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.ecoingenieria.depuradora.data.local.entity.*

@Dao
interface StageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stages: List<StageEntity>)

    @Query("SELECT * FROM stages ORDER BY orderIndex ASC")
    fun observeStages(): Flow<List<StageEntity>>

    @Query("SELECT COUNT(*) FROM stages")
    suspend fun count(): Int
}

@Dao
interface PieceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pieces: List<PieceEntity>)

    @Query("SELECT * FROM pieces WHERE stageId = :stageId ORDER BY correctOrder ASC")
    fun observePiecesForStage(stageId: Int): Flow<List<PieceEntity>>

    @Query("SELECT * FROM pieces WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Int>): List<PieceEntity>

    @Query("SELECT COUNT(*) FROM pieces")
    suspend fun count(): Int
}

@Dao
interface LevelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(levels: List<LevelEntity>)

    @Query("SELECT * FROM levels WHERE stageId = :stageId ORDER BY orderIndex ASC")
    fun observeLevelsForStage(stageId: Int): Flow<List<LevelEntity>>

    @Query("SELECT * FROM levels WHERE id = :levelId LIMIT 1")
    suspend fun getById(levelId: Int): LevelEntity?

    @Query("SELECT * FROM levels ORDER BY stageId ASC, orderIndex ASC")
    fun observeAllLevels(): Flow<List<LevelEntity>>

    @Query("SELECT COUNT(*) FROM levels")
    suspend fun count(): Int
}

@Dao
interface LevelProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: LevelProgressEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(progress: List<LevelProgressEntity>)

    @Query("SELECT * FROM level_progress WHERE levelId = :levelId LIMIT 1")
    suspend fun getForLevel(levelId: Int): LevelProgressEntity?

    @Query("SELECT * FROM level_progress")
    fun observeAll(): Flow<List<LevelProgressEntity>>

    @Update
    suspend fun update(progress: LevelProgressEntity)

    @Query("SELECT COUNT(*) FROM level_progress")
    suspend fun count(): Int
}

@Dao
interface BlueprintDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(blueprints: List<BlueprintEntity>)

    @Query("SELECT * FROM blueprints ORDER BY id ASC")
    fun observeAll(): Flow<List<BlueprintEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun unlock(unlock: BlueprintUnlockEntity)

    @Query("SELECT blueprintId FROM blueprint_unlocks")
    fun observeUnlockedIds(): Flow<List<Int>>

    @Query("SELECT COUNT(*) FROM blueprints")
    suspend fun count(): Int
}

@Dao
interface BadgeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(badges: List<BadgeEntity>)

    @Query("SELECT * FROM badges ORDER BY id ASC")
    fun observeAll(): Flow<List<BadgeEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun unlock(unlock: BadgeUnlockEntity)

    @Query("SELECT badgeId FROM badge_unlocks")
    fun observeUnlockedIds(): Flow<List<Int>>

    @Query("SELECT COUNT(*) FROM badge_unlocks WHERE badgeId = :badgeId")
    suspend fun isUnlocked(badgeId: Int): Int

    @Query("SELECT COUNT(*) FROM badges")
    suspend fun count(): Int
}

@Dao
interface PlayerProfileDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(profile: PlayerProfileEntity)

    @Update
    suspend fun update(profile: PlayerProfileEntity)

    @Query("SELECT * FROM player_profile WHERE id = 1 LIMIT 1")
    fun observeProfile(): Flow<PlayerProfileEntity?>

    @Query("SELECT * FROM player_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfile(): PlayerProfileEntity?
}
