package com.seventh.atlasquest.ui.backlog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.seventh.atlasquest.data.local.QuestEntity
import com.seventh.atlasquest.domain.model.EffortTier
import com.seventh.atlasquest.domain.model.QuestFormat
import com.seventh.atlasquest.domain.model.QuestKind
import com.seventh.atlasquest.domain.model.QuestStatus
import com.seventh.atlasquest.domain.model.StatAxis
import com.seventh.atlasquest.domain.model.SubTask
import com.seventh.atlasquest.ui.AtlasQuestUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestBacklogScreen(
    uiState: AtlasQuestUiState,
    onAddQuest: (String, String, QuestKind, QuestStatus, QuestFormat, List<SubTask>, EffortTier, StatAxis, String?, String?) -> Unit,
    onUpdateStatus: (Long, QuestStatus) -> Unit,
    onDeleteQuest: (Long) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val backlogQuests = uiState.quests.filter { it.status == QuestStatus.BACKLOG || it.status == QuestStatus.COMPLETED }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Side Quest Backlog & Archive") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Quest")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Manage your spontaneous Side Quests and archive completed feats.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (backlogQuests.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Your backlog is empty. Tap '+' to create a new side quest!",
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
                    items(backlogQuests, key = { it.id }) { quest ->
                        BacklogQuestCard(
                            quest = quest,
                            onPromote = { onUpdateStatus(quest.id, QuestStatus.ACTIVE) },
                            onDelete = { onDeleteQuest(quest.id) }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            AddQuestDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { title, desc, kind, format, subtasks, effort, axis, cue, fallback ->
                    onAddQuest(title, desc, kind, QuestStatus.BACKLOG, format, subtasks, effort, axis, cue, fallback)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun BacklogQuestCard(
    quest: QuestEntity,
    onPromote: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                Badge {
                    Text(
                        text = quest.status.name,
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

            if (quest.format == QuestFormat.CHECKLIST && quest.subtasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${quest.subtasks.size} Checklist Steps",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (quest.status == QuestStatus.BACKLOG) {
                    TextButton(onClick = onPromote) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Activate")
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun AddQuestDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, QuestKind, QuestFormat, List<SubTask>, EffortTier, StatAxis, String?, String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf(QuestKind.SIDE_QUEST) }
    var format by remember { mutableStateOf(QuestFormat.SIMPLE) }
    var subtasksInput by remember { mutableStateOf("") }
    var effortTier by remember { mutableStateOf(EffortTier.STANDARD) }
    var statAxis by remember { mutableStateOf(StatAxis.STR) }
    var triggerCue by remember { mutableStateOf("") }
    var fallbackAction by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Quest") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Quest Format selector
                Text("Quest Format", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = format == QuestFormat.SIMPLE,
                        onClick = { format = QuestFormat.SIMPLE },
                        label = { Text("Single Line") }
                    )
                    FilterChip(
                        selected = format == QuestFormat.CHECKLIST,
                        onClick = { format = QuestFormat.CHECKLIST },
                        label = { Text("Checklist Steps") }
                    )
                }

                if (format == QuestFormat.CHECKLIST) {
                    OutlinedTextField(
                        value = subtasksInput,
                        onValueChange = { subtasksInput = it },
                        label = { Text("Checklist Items (comma separated)") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Step 1, Step 2, Step 3") }
                    )
                }

                // Kind selector
                Text("Quest Kind", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = kind == QuestKind.SIDE_QUEST,
                        onClick = { kind = QuestKind.SIDE_QUEST },
                        label = { Text("Side Quest") }
                    )
                    FilterChip(
                        selected = kind == QuestKind.MAIN_QUEST,
                        onClick = { kind = QuestKind.MAIN_QUEST },
                        label = { Text("Main Quest") }
                    )
                }
                // Effort Tier selector
                Text("Effort Tier", style = MaterialTheme.typography.labelMedium)
                EffortTier.entries.forEach { tier ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = effortTier == tier,
                            onClick = { effortTier = tier }
                        )
                        Text(tier.label)
                    }
                }
                // Stat Axis selector
                Text("Stat Axis", style = MaterialTheme.typography.labelMedium)
                StatAxis.entries.forEach { axis ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = statAxis == axis,
                            onClick = { statAxis = axis }
                        )
                        Text(axis.name)
                    }
                }
                OutlinedTextField(
                    value = triggerCue,
                    onValueChange = { triggerCue = it },
                    label = { Text("Trigger Cue (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = fallbackAction,
                    onValueChange = { fallbackAction = it },
                    label = { Text("Fallback Action (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val subtasksList = if (format == QuestFormat.CHECKLIST && subtasksInput.isNotBlank()) {
                            subtasksInput.split(",").map { SubTask(title = it.trim()) }.filter { it.title.isNotBlank() }
                        } else {
                            emptyList()
                        }
                        onConfirm(
                            title,
                            description,
                            kind,
                            format,
                            subtasksList,
                            effortTier,
                            statAxis,
                            triggerCue.ifBlank { null },
                            fallbackAction.ifBlank { null }
                        )
                    }
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
