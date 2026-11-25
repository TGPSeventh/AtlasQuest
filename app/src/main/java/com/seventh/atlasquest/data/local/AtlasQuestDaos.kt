package com.seventh.atlasquest.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerProfileDao {
    @Query("SELECT * FROM player_profile WHERE id = 1")
    fun getPlayerProfile(): Flow<PlayerProfileEntity?>

    @Query("SELECT * FROM player_profile WHERE id = 1")
    suspend fun getPlayerProfileSync(): PlayerProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: PlayerProfileEntity)
}

@Dao
interface QuestDao {
    @Query("SELECT * FROM quests ORDER BY createdAt DESC")
    fun getAllQuests(): Flow<List<QuestEntity>>

    @Query("SELECT * FROM quests WHERE status = :status ORDER BY createdAt DESC")
    fun getQuestsByStatus(status: com.seventh.atlasquest.domain.model.QuestStatus): Flow<List<QuestEntity>>

    @Query("SELECT * FROM quests WHERE kind = :kind ORDER BY createdAt DESC")
    fun getQuestsByKind(kind: com.seventh.atlasquest.domain.model.QuestKind): Flow<List<QuestEntity>>

    @Query("SELECT * FROM quests WHERE id = :id")
    suspend fun getQuestById(id: Long): QuestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuest(quest: QuestEntity): Long

    @Update
    suspend fun updateQuest(quest: QuestEntity)

    @Delete
    suspend fun deleteQuest(quest: QuestEntity)
}

@Dao
interface QuestLogDao {
    @Query("SELECT * FROM quest_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<QuestLogEntity>>

    @Query("SELECT * FROM quest_logs WHERE timestamp >= :startOfDay ORDER BY timestamp DESC")
    suspend fun getLogsSince(startOfDay: Long): List<QuestLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: QuestLogEntity)
}
