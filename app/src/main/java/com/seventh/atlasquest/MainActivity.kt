package com.seventh.atlasquest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.seventh.atlasquest.ui.AtlasQuestViewModel
import com.seventh.atlasquest.ui.backlog.QuestBacklogScreen
import com.seventh.atlasquest.ui.board.QuestBoardScreen
import com.seventh.atlasquest.ui.character.CharacterSheetScreen
import com.seventh.atlasquest.ui.theme.AtlasQuestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AtlasQuestTheme {
                val viewModel: AtlasQuestViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsState()

                var selectedTab by remember { mutableIntStateOf(0) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = { Icon(Icons.Default.Assignment, contentDescription = "Quest Board") },
                                label = { Text("Board") }
                            )
                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = { Icon(Icons.Default.ListAlt, contentDescription = "Backlog") },
                                label = { Text("Backlog") }
                            )
                            NavigationBarItem(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                icon = { Icon(Icons.Default.Person, contentDescription = "Character") },
                                label = { Text("Character") }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (selectedTab) {
                            0 -> QuestBoardScreen(
                                uiState = uiState,
                                onCompleteQuest = { id, fallback -> viewModel.completeQuest(id, fallback) },
                                onToggleSubTask = { questId, subId, completed -> viewModel.updateSubTaskCompletion(questId, subId, completed) }
                            )
                            1 -> QuestBacklogScreen(
                                uiState = uiState,
                                onAddQuest = { title, desc, kind, status, format, subtasks, effort, axis, cue, fallback ->
                                    viewModel.addQuest(title, desc, kind, status, format, subtasks, effort, axis, cue, fallback)
                                },
                                onUpdateStatus = { id, status -> viewModel.updateQuestStatus(id, status) },
                                onDeleteQuest = { id -> viewModel.deleteQuest(id) }
                            )
                            2 -> CharacterSheetScreen(uiState = uiState)
                        }
                    }
                }
            }
        }
    }
}
