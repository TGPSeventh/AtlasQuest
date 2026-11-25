package com.seventh.atlasquest.domain.model

enum class QuestKind {
    SIDE_QUEST, // Spontaneous or one-off quest (Backlog -> Active -> Completed)
    MAIN_QUEST  // Recurring habits, workouts, routines
}

enum class QuestStatus {
    BACKLOG,
    ACTIVE,
    COMPLETED,
    ARCHIVED
}

enum class QuestFormat {
    SIMPLE,     // Single-line task description
    CHECKLIST   // Multi-step sub-tasks with checkboxes
}

data class SubTask(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val isCompleted: Boolean = false
)

enum class StatAxis {
    STR, // Strength
    VIT, // Vitality
    INT, // Intellect
    FOC, // Focus
    DIS, // Discipline
    EXP  // Exploration
}

enum class EffortTier(
    val baseXp: Int,
    val statPoints: Int,
    val label: String
) {
    LIGHT(baseXp = 15, statPoints = 1, label = "Light (+1 Stat, 15 XP)"),
    STANDARD(baseXp = 30, statPoints = 2, label = "Standard (+2 Stats, 30 XP)"),
    HEAVY(baseXp = 50, statPoints = 3, label = "Heavy (+3 Stats, 50 XP)")
}
