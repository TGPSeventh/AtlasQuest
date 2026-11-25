package com.seventh.atlasquest.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.seventh.atlasquest.domain.model.EffortTier
import com.seventh.atlasquest.domain.model.QuestFormat
import com.seventh.atlasquest.domain.model.QuestKind
import com.seventh.atlasquest.domain.model.QuestStatus
import com.seventh.atlasquest.domain.model.StatAxis
import com.seventh.atlasquest.domain.model.SubTask

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val id: Int = 1,
    val totalXp: Int = 0,
    val unspentStatPoints: Int = 0,
    val statStr: Int = 0,
    val statVit: Int = 0,
    val statInt: Int = 0,
    val statFoc: Int = 0,
    val statDis: Int = 0,
    val statExp: Int = 0,
    val streakDays: Int = 0,
    val dailyMainCount: Int = 0,
    val dailySideCount: Int = 0,
    val weeklyMainCount: Int = 0,
    val weeklySideCount: Int = 0,
    val bonusDailyMainCap: Int = 0,
    val bonusDailySideCap: Int = 0,
    val bonusWeeklyMainCap: Int = 0,
    val bonusWeeklySideCap: Int = 0,
    val lastResetDate: String? = null,
    val lastActiveDate: String? = null
)

@Entity(tableName = "quests")
data class QuestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val description: String,
    val kind: QuestKind,
    val status: QuestStatus,
    val format: QuestFormat = QuestFormat.SIMPLE,
    val subtasks: List<SubTask> = emptyList(),
    val effortTier: EffortTier,
    val statAxis: StatAxis,
    val triggerCue: String? = null,     // For Main Quests / habits
    val fallbackAction: String? = null, // Low-energy fallback action (60% XP, 1 stat)
    val streak: Int = 0,
    val lastCompletedDate: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quest_logs")
data class QuestLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val questId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val xpGained: Int,
    val statPointsGained: Int,
    val isFallback: Boolean = false
)
