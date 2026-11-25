package com.seventh.atlasquest.domain.progression

import com.seventh.atlasquest.data.local.PlayerProfileEntity
import com.seventh.atlasquest.domain.model.EffortTier
import com.seventh.atlasquest.domain.model.QuestKind
import com.seventh.atlasquest.domain.model.StatAxis
import kotlin.random.Random

data class LevelProgress(
    val level: Int,
    val currentXp: Int,
    val nextLevelXp: Int,
    val progressPercent: Float
)

data class SystemReward(
    val title: String,
    val description: String,
    val bonusMainDaily: Int = 0,
    val bonusSideDaily: Int = 0,
    val bonusMainWeekly: Int = 0,
    val bonusSideWeekly: Int = 0
)

object ProgressionEngine {
    // Base Caps
    const val BASE_DAILY_MAIN_CAP = 5
    const val BASE_DAILY_SIDE_CAP = 7
    const val BASE_WEEKLY_MAIN_CAP = 35
    const val BASE_WEEKLY_SIDE_CAP = 45
    private const val FALLBACK_XP = 10

    fun calculateLevel(totalXp: Int): LevelProgress {
        var level = 1
        var xpRemaining = totalXp
        var xpForNext = 100

        while (xpRemaining >= xpForNext) {
            xpRemaining -= xpForNext
            level++
            xpForNext = level * 100
        }

        val percent = if (xpForNext > 0) xpRemaining.toFloat() / xpForNext.toFloat() else 0f
        return LevelProgress(
            level = level,
            currentXp = xpRemaining,
            nextLevelXp = xpForNext,
            progressPercent = percent.coerceIn(0f, 1f)
        )
    }

    fun isQuestCapped(kind: QuestKind, profile: PlayerProfileEntity): Boolean {
        return when (kind) {
            QuestKind.MAIN_QUEST -> {
                val effectiveDailyCap = BASE_DAILY_MAIN_CAP + profile.bonusDailyMainCap
                val effectiveWeeklyCap = BASE_WEEKLY_MAIN_CAP + profile.bonusWeeklyMainCap
                profile.dailyMainCount >= effectiveDailyCap || profile.weeklyMainCount >= effectiveWeeklyCap
            }
            QuestKind.SIDE_QUEST -> {
                val effectiveDailyCap = BASE_DAILY_SIDE_CAP + profile.bonusDailySideCap
                val effectiveWeeklyCap = BASE_WEEKLY_SIDE_CAP + profile.bonusWeeklySideCap
                profile.dailySideCount >= effectiveDailyCap || profile.weeklySideCount >= effectiveWeeklyCap
            }
        }
    }

    fun calculateRewards(
        effortTier: EffortTier,
        isFallback: Boolean,
        isCapped: Boolean
    ): RewardResult {
        val xp = if (isFallback) {
            if (isCapped) maxOf(5, (FALLBACK_XP * 0.6f).toInt()) else FALLBACK_XP
        } else {
            if (isCapped) (effortTier.baseXp * 0.5f).toInt() else effortTier.baseXp
        }

        val statPoints = if (isFallback) {
            1
        } else {
            if (isCapped) maxOf(1, effortTier.statPoints - 1) else effortTier.statPoints
        }

        return RewardResult(
            xpGained = maxOf(5, xp),
            statPointsGained = statPoints,
            isCapped = isCapped
        )
    }

    fun rollSystemReward(): SystemReward? {
        // 35% chance on completion to trigger a random system reward loot drop
        val roll = Random.nextFloat()
        if (roll > 0.35f) return null

        return when (Random.nextInt(4)) {
            0 -> SystemReward(
                title = "🎁 System Loot Drop: Main Quest Surge",
                description = "Granted +1 Daily Main Quest capacity and +2 Weekly Main Quest capacity!",
                bonusMainDaily = 1,
                bonusMainWeekly = 2
            )
            1 -> SystemReward(
                title = "🎁 System Loot Drop: Side Quest Anomaly",
                description = "Granted +2 Daily Side Quest capacity and +3 Weekly Side Quest capacity!",
                bonusSideDaily = 2,
                bonusSideWeekly = 3
            )
            2 -> SystemReward(
                title = "🎁 System Loot Drop: Chrono Expansion",
                description = "Granted +1 Daily Main Capacity and +1 Daily Side Capacity!",
                bonusMainDaily = 1,
                bonusSideDaily = 1
            )
            else -> SystemReward(
                title = "🎁 System Loot Drop: Atlas Resonance",
                description = "Granted +2 Weekly Main Capacity and +2 Weekly Side Capacity!",
                bonusMainWeekly = 2,
                bonusSideWeekly = 2
            )
        }
    }

    fun resolveArchetype(stats: Map<StatAxis, Int>): String {
        if (stats.isEmpty() || stats.values.all { it == 0 }) {
            return "Wandering Novice"
        }

        val sorted = stats.entries.sortedByDescending { it.value }
        val top1 = sorted.getOrNull(0)
        val top2 = sorted.getOrNull(1)

        val s1 = top1?.key ?: StatAxis.STR
        val s2 = top2?.key ?: StatAxis.VIT

        return when {
            s1 == StatAxis.STR && s2 == StatAxis.VIT -> "Vanguard"
            s1 == StatAxis.STR && s2 == StatAxis.DIS -> "Iron Sentinel"
            s1 == StatAxis.INT && s2 == StatAxis.FOC -> "Arcane Scholar"
            s1 == StatAxis.FOC && s2 == StatAxis.DIS -> "Focus Weaver"
            s1 == StatAxis.EXP && s2 == StatAxis.VIT -> "Pathrunner"
            s1 == StatAxis.EXP && s2 == StatAxis.INT -> "Wayfinder"
            s1 == StatAxis.STR && s2 == StatAxis.INT -> "Spellsword"
            stats.values.all { it >= (top1?.value ?: 1) - 2 && it > 0 } -> "Atlas Polymath"
            else -> when (s1) {
                StatAxis.STR -> "Titan"
                StatAxis.VIT -> "Juggernaut"
                StatAxis.INT -> "Sage"
                StatAxis.FOC -> "Sniper"
                StatAxis.DIS -> "Templar"
                StatAxis.EXP -> "Explorer"
            }
        }
    }
}

data class RewardResult(
    val xpGained: Int,
    val statPointsGained: Int,
    val isCapped: Boolean
)
