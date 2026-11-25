package com.seventh.atlasquest.ui.board

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.seventh.atlasquest.data.local.QuestEntity
import com.seventh.atlasquest.domain.model.QuestFormat
import com.seventh.atlasquest.domain.model.QuestStatus
import com.seventh.atlasquest.domain.progression.ProgressionEngine
import com.seventh.atlasquest.ui.AtlasQuestUiState

@Composable
fun QuestBoardScreen(
    uiState: AtlasQuestUiState,
    onCompleteQuest: (Long, Boolean) -> Unit,
    onToggleSubTask: (Long, String, Boolean) -> Unit
) {
    val activeQuests = uiState.quests.filter { it.status == QuestStatus.ACTIVE }
    val profile = uiState.profile

    val mainDailyCap = ProgressionEngine.BASE_DAILY_MAIN_CAP + profile.bonusDailyMainCap
    val sideDailyCap = ProgressionEngine.BASE_DAILY_SIDE_CAP + profile.bonusDailySideCap

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Active Quest Board",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Zero-Punishment Progression: Complete fully or use low-energy fallback.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Daily Usage & Capacity Tracker Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Daily Quest Capacity & Usage",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TrackerPill(label = "Main Quests", used = profile.dailyMainCount, cap = mainDailyCap)
                    TrackerPill(label = "Side Quests", used = profile.dailySideCount, cap = sideDailyCap)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (activeQuests.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No active quests. Promote side quests from your backlog!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(activeQuests, key = { it.id }) { quest ->
                    ActiveQuestCard(
                        quest = quest,
                        onComplete = onCompleteQuest,
                        onToggleSubTask = onToggleSubTask
                    )
                }
            }
        }
    }
}

@Composable
fun TrackerPill(label: String, used: Int, cap: Int) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.padding(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "$used / $cap used", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun ActiveQuestCard(
    quest: QuestEntity,
    onComplete: (Long, Boolean) -> Unit,
    onToggleSubTask: (Long, String, Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = quest.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Badge(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${quest.effortTier.name} • ${quest.statAxis.name}",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = quest.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!quest.triggerCue.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Trigger Cue: ${quest.triggerCue}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Render subtasks if checklist format
            if (quest.format == QuestFormat.CHECKLIST && quest.subtasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(8.dp))

                quest.subtasks.forEach { subTask ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = subTask.isCompleted,
                            onCheckedChange = { checked ->
                                onToggleSubTask(quest.id, subTask.id, checked)
                            }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = subTask.title,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Full Complete Button
                Button(
                    onClick = { onComplete(quest.id, false) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Complete")
                }

                // Fallback / Low Energy Button
                if (!quest.fallbackAction.isNullOrBlank()) {
                    OutlinedButton(
                        onClick = { onComplete(quest.id, true) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Fallback")
                    }
                }
            }
        }
    }
}
