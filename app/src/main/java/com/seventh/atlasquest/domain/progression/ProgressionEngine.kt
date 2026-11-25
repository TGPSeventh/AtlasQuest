package com.seventh.atlasquest.domain.progression

import com.seventh.atlasquest.domain.model.EffortTier
import com.seventh.atlasquest.domain.model.StatAxis

data class LevelProgress(
    val level: Int,
    val currentXp: Int,
    val nextLevelXp: Int,
    val progressPercent: Float
)

object ProgressionEngine {
    private const val DAILY_FULL_REWARD_CAP = 12
    private const val FALLBACK_XP = 10

    fun calculateLevel(totalXp: Int): LevelProgress {
        // Simple RPG curve: Level N requires N * 100 XP (or cumulative)
        // Let's use cumulative XP per level: Level 1 -> 100, Level 2 -> 250, Level 3 -> 450, etc.
        // Formula: Level L requires 100 * L baseline per level up.
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

    fun calculateRewards(
        effortTier: EffortTier,
        isFallback: Boolean,
        todayCompletionCount: Int
    ): RewardResult {
        val isCapped = todayCompletionCount >= DAILY_FULL_REWARD_CAP

        val xp = if (isFallback) {
            FALLVAL_XP_CHECK(FALLBACK_XP, isCapped)
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

    private fun FALLVAL_XP_CHECK(base: Int, capped: Boolean): Int {
        return if (capped) maxOf(5, (base * 0.6f).toInt()) else base
    }

    fun resolveArchetype(stats: Map<StatAxis, Int>): String {
        if (stats.isEmpty() || stats.values.all { it == 0 }) {
            return "Wandering Novice"
        }

        // Find top 2 stats
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
