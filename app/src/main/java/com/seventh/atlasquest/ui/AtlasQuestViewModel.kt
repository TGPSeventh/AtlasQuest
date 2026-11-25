package com.seventh.atlasquest.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.seventh.atlasquest.data.local.AtlasQuestDatabase
import com.seventh.atlasquest.data.local.PlayerProfileEntity
import com.seventh.atlasquest.data.local.QuestEntity
import com.seventh.atlasquest.data.repository.AtlasQuestRepository
import com.seventh.atlasquest.domain.model.EffortTier
import com.seventh.atlasquest.domain.model.QuestFormat
import com.seventh.atlasquest.domain.model.QuestKind
import com.seventh.atlasquest.domain.model.QuestStatus
import com.seventh.atlasquest.domain.model.StatAxis
import com.seventh.atlasquest.domain.model.SubTask
import com.seventh.atlasquest.domain.progression.LevelProgress
import com.seventh.atlasquest.domain.progression.ProgressionEngine
import com.seventh.atlasquest.domain.progression.SystemReward
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AtlasQuestUiState(
    val profile: PlayerProfileEntity = PlayerProfileEntity(id = 1),
    val quests: List<QuestEntity> = emptyList(),
    val levelProgress: LevelProgress = LevelProgress(1, 0, 100, 0f),
    val archetype: String = "Wandering Novice",
    val statMap: Map<StatAxis, Int> = emptyMap(),
    val isLoading: Boolean = false
)

class AtlasQuestViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AtlasQuestRepository

    init {
        val db = AtlasQuestDatabase.getDatabase(application)
        repository = AtlasQuestRepository(db)
        viewModelScope.launch {
            repository.initialize()
        }
    }

    val systemRewardEvents: SharedFlow<SystemReward> = repository.systemRewardEvents

    val uiState: StateFlow<AtlasQuestUiState> = combine(
        repository.playerProfile,
        repository.allQuests
    ) { profile, quests ->
        val currentProfile = profile ?: PlayerProfileEntity(id = 1)
        val levelProg = ProgressionEngine.calculateLevel(currentProfile.totalXp)
        val statMap = mapOf(
            StatAxis.STR to currentProfile.statStr,
            StatAxis.VIT to currentProfile.statVit,
            StatAxis.INT to currentProfile.statInt,
            StatAxis.FOC to currentProfile.statFoc,
            StatAxis.DIS to currentProfile.statDis,
            StatAxis.EXP to currentProfile.statExp
        )
        val archetype = ProgressionEngine.resolveArchetype(statMap)

        AtlasQuestUiState(
            profile = currentProfile,
            quests = quests,
            levelProgress = levelProg,
            archetype = archetype,
            statMap = statMap,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AtlasQuestUiState(isLoading = true)
    )

    fun addQuest(
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
        viewModelScope.launch {
            repository.addQuest(
                title = title,
                description = description,
                kind = kind,
                status = status,
                format = format,
                subtasks = subtasks,
                effortTier = effortTier,
                statAxis = statAxis,
                triggerCue = triggerCue,
                fallbackAction = fallbackAction
            )
        }
    }

    fun updateQuestStatus(questId: Long, status: QuestStatus) {
        viewModelScope.launch {
            repository.updateQuestStatus(questId, status)
        }
    }

    fun updateSubTaskCompletion(questId: Long, subTaskId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.updateSubTaskCompletion(questId, subTaskId, isCompleted)
        }
    }

    fun deleteQuest(questId: Long) {
        viewModelScope.launch {
            repository.deleteQuest(questId)
        }
    }

    fun completeQuest(questId: Long, isFallback: Boolean) {
        viewModelScope.launch {
            repository.completeQuest(questId, isFallback)
        }
    }
}
