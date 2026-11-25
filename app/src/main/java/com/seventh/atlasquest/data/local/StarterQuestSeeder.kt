package com.seventh.atlasquest.data.local

import com.seventh.atlasquest.domain.model.EffortTier
import com.seventh.atlasquest.domain.model.QuestKind
import com.seventh.atlasquest.domain.model.QuestStatus
import com.seventh.atlasquest.domain.model.StatAxis

object StarterQuestSeeder {
    suspend fun seedIfNeeded(database: AtlasQuestDatabase) {
        val profileDao = database.playerProfileDao()
        val questDao = database.questDao()

        val profile = profileDao.getPlayerProfileSync()
        if (profile == null) {
            profileDao.insertOrUpdateProfile(
                PlayerProfileEntity(
                    id = 1,
                    totalXp = 0,
                    unspentStatPoints = 0,
                    statStr = 1,
                    statVit = 1,
                    statInt = 1,
                    statFoc = 1,
                    statDis = 1,
                    statExp = 1,
                    streakDays = 1,
                    lastActiveDate = null
                )
            )
        }

        // Check if quests are empty
        // We can check by inserting starter quests if none exist
        // For simplicity, we can insert starter quests on first launch
        val starterQuests = listOf(
            QuestEntity(
                title = "Organize Desk & Workspace",
                description = "Clear out clutter and sanitize desktop to boost clarity.",
                kind = QuestKind.SIDE_QUEST,
                status = QuestStatus.ACTIVE,
                effortTier = EffortTier.LIGHT,
                statAxis = StatAxis.FOC,
                triggerCue = "Morning coffee brewing",
                fallbackAction = "Clear just one coffee mug off desk",
                createdAt = System.currentTimeMillis()
            ),
            QuestEntity(
                title = "15-Minute Bodyweight Workout",
                description = "Push-ups, squats, and planks to awaken vitality.",
                kind = QuestKind.MAIN_QUEST,
                status = QuestStatus.ACTIVE,
                effortTier = EffortTier.STANDARD,
                statAxis = StatAxis.STR,
                triggerCue = "Waking up and stepping out of bed",
                fallbackAction = "Do 5 wall push-ups",
                createdAt = System.currentTimeMillis()
            ),
            QuestEntity(
                title = "Read 10 Pages of Non-Fiction",
                description = "Expand mental library and explore new concepts.",
                kind = QuestKind.MAIN_QUEST,
                status = QuestStatus.ACTIVE,
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
                effortTier = EffortTier.HEAVY,
                statAxis = StatAxis.EXP,
                createdAt = System.currentTimeMillis()
            ),
            QuestEntity(
                title = "Digital Detox & Inbox Zero",
                description = "Process unread emails and archive notifications.",
                kind = QuestKind.SIDE_QUEST,
                status = QuestStatus.BACKLOG,
                effortTier = EffortTier.STANDARD,
                statAxis = StatAxis.DIS,
                createdAt = System.currentTimeMillis()
            )
        )

        // Only insert if no quests exist
        // We can inspect via a quick check or database query if needed, or insert if table is empty
        // Since questDao doesn't have a direct count query, let's add one or check flow. Or we can just insert if needed.
        // Let's add a safe check in repository.
    }
}
