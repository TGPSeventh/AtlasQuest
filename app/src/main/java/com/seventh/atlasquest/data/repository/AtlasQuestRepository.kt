package com.seventh.atlasquest.data.repository

import com.seventh.atlasquest.data.local.AtlasQuestDatabase
import com.seventh.atlasquest.data.local.PlayerProfileEntity
import com.seventh.atlasquest.data.local.QuestEntity
import com.seventh.atlasquest.data.local.QuestLogEntity
import com.seventh.atlasquest.data.local.StarterQuestSeeder
import com.seventh.atlasquest.domain.model.EffortTier
import com.seventh.atlasquest.domain.model.QuestFormat
import com.seventh.atlasquest.domain.model.QuestKind
import com.seventh.atlasquest.domain.model.QuestStatus
import com.seventh.atlasquest.domain.model.StatAxis
import com.seventh.atlasquest.domain.model.SubTask
import com.seventh.atlasquest.domain.progression.ProgressionEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AtlasQuestRepository(private val database: AtlasQuestDatabase) {
    private val profileDao = database.playerProfileDao()
    private val questDao = database.questDao()
    private val questLogDao = database.questLogDao()

    val playerProfile: Flow<PlayerProfileEntity?> = profileDao.getPlayerProfile()
    val allQuests: Flow<List<QuestEntity>> = questDao.getAllQuests()

    suspend fun initialize() {
        StarterQuestSeeder.seedIfNeeded(database)
        val profile = profileDao.getPlayerProfileSync()
        if (profile == null) {
            profileDao.insertOrUpdateProfile(PlayerProfileEntity(id = 1))
        }

        val existingQuests = questDao.getAllQuests().firstOrNull()
        if (existingQuests.isNullOrEmpty()) {
            val starterQuests = listOf(
                QuestEntity(
                    title = "Organize Desk & Workspace",
                    description = "Clear out clutter and sanitize desktop to boost clarity.",
                    kind = QuestKind.SIDE_QUEST,
                    status = QuestStatus.ACTIVE,
                    format = QuestFormat.SIMPLE,
                    effortTier = EffortTier.LIGHT,
                    statAxis = StatAxis.FOC,
                    triggerCue = "Morning coffee brewing",
                    fallbackAction = "Clear just one coffee mug off desk",
                    createdAt = System.currentTimeMillis()
                ),
                QuestEntity(
                    title = "Morning Epic Routine",
                    description = "Complete all multi-step morning vitality steps.",
                    kind = QuestKind.MAIN_QUEST,
                    status = QuestStatus.ACTIVE,
                    format = QuestFormat.CHECKLIST,
                    subtasks = listOf(
                        SubTask(title = "Drink 500ml water"),
                        SubTask(title = "10 minutes stretching"),
                        SubTask(title = "Review daily objectives")
                    ),
                    effortTier = EffortTier.STANDARD,
                    statAxis = StatAxis.VIT,
                    triggerCue = "Waking up",
                    fallbackAction = "Drink a glass of water",
                    createdAt = System.currentTimeMillis()
                ),
                QuestEntity(
                    title = "Read 10 Pages of Non-Fiction",
                    description = "Expand mental library and explore new concepts.",
                    kind = QuestKind.MAIN_QUEST,
                    status = QuestStatus.ACTIVE,
                    format = QuestFormat.SIMPLE,
                    effortTier = EffortTier.STANDARD,
                    statAxis = StatAxis.INT,
                    triggerCue = "Sitting on the couch after dinner",
                    fallbackAction = "Read just 1 page",
                    createdAt = System.currentTimeMillis()
                ),
                QuestEntity(
                    title = "Explore New Neighborhood Walking Route",
                    description = "Take an unfamiliar path during evening stroll.",
                    kind = QuestKind.SIDE_QUEST,
                    status = QuestStatus.BACKLOG,
                    format = QuestFormat.SIMPLE,
                    effortTier = EffortTier.HEAVY,
                    statAxis = StatAxis.EXP,
                    createdAt = System.currentTimeMillis()
                )
            )
            for (q in starterQuests) {
                questDao.insertQuest(q)
            }
        }
    }

    suspend fun addQuest(
        title: String,
        description: String,
        kind: QuestKind,
        status: QuestStatus,
        format: QuestFormat,
        subtasks: List<SubTask>,
        effortTier: EffortTier,
        statAxis: StatAxis,
        triggerCue: String?,
        fallbackAction: String?
    ) {
        val quest = QuestEntity(
            title = title,
            description = description,
            kind = kind,
            status = status,
            format = format,
            subtasks = subtasks,
            effortTier = effortTier,
            statAxis = statAxis,
            triggerCue = triggerCue,
            fallbackAction = fallbackAction,
            createdAt = System.currentTimeMillis()
        )
        questDao.insertQuest(quest)
    }

    suspend fun updateQuestStatus(questId: Long, newStatus: QuestStatus) {
        val quest = questDao.getQuestById(questId) ?: return
        questDao.updateQuest(quest.copy(status = newStatus))
    }

    suspend fun updateSubTaskCompletion(questId: Long, subTaskId: String, isCompleted: Boolean) {
        val quest = questDao.getQuestById(questId) ?: return
        val updatedSubtasks = quest.subtasks.map { sub ->
            if (sub.id == subTaskId) sub.copy(isCompleted = isCompleted) else sub
        }
        questDao.updateQuest(quest.copy(subtasks = updatedSubtasks))
    }

    suspend fun deleteQuest(questId: Long) {
        val quest = questDao.getQuestById(questId) ?: return
        questDao.deleteQuest(quest)
    }

    suspend fun completeQuest(questId: Long, isFallback: Boolean) {
        val quest = questDao.getQuestById(questId) ?: return
        val profile = profileDao.getPlayerProfileSync() ?: PlayerProfileEntity(id = 1)

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        val todayLogs = questLogDao.getLogsSince(startOfDay)
        val rewards = ProgressionEngine.calculateRewards(quest.effortTier, isFallback, todayLogs.size)

        val newTotalXp = profile.totalXp + rewards.xpGained
        val newUnspentPoints = profile.unspentStatPoints + rewards.statPointsGained

        val updatedProfile = when (quest.statAxis) {
            StatAxis.STR -> profile.copy(statStr = profile.statStr + rewards.statPointsGained, totalXp = newTotalXp, unspentStatPoints = newUnspentPoints, lastActiveDate = todayStr)
            StatAxis.VIT -> profile.copy(statVit = profile.statVit + rewards.statPointsGained, totalXp = newTotalXp, unspentStatPoints = newUnspentPoints, lastActiveDate = todayStr)
            StatAxis.INT -> profile.copy(statInt = profile.statInt + rewards.statPointsGained, totalXp = newTotalXp, unspentStatPoints = newUnspentPoints, lastActiveDate = todayStr)
            StatAxis.FOC -> profile.copy(statFoc = profile.statFoc + rewards.statPointsGained, totalXp = newTotalXp, unspentStatPoints = newUnspentPoints, lastActiveDate = todayStr)
            StatAxis.DIS -> profile.copy(statDis = profile.statDis + rewards.statPointsGained, totalXp = newTotalXp, unspentStatPoints = newUnspentPoints, lastActiveDate = todayStr)
            StatAxis.EXP -> profile.copy(statExp = profile.statExp + rewards.statPointsGained, totalXp = newTotalXp, unspentStatPoints = newUnspentPoints, lastActiveDate = todayStr)
        }

        profileDao.insertOrUpdateProfile(updatedProfile)

        questLogDao.insertLog(
            QuestLogEntity(
                questId = quest.id,
                timestamp = System.currentTimeMillis(),
                xpGained = rewards.xpGained,
                statPointsGained = rewards.statPointsGained,
                isFallback = isFallback
            )
        )

        val newStreak = quest.streak + 1
        val updatedQuest = if (quest.kind == QuestKind.SIDE_QUEST) {
            quest.copy(status = QuestStatus.COMPLETED, streak = newStreak, lastCompletedDate = todayStr)
        } else {
            quest.copy(streak = newStreak, lastCompletedDate = todayStr)
        }
        questDao.updateQuest(updatedQuest)
    }
}
