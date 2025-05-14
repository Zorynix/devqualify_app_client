package com.diploma.work.ui.feature.test

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.diploma.work.data.models.Direction
import com.diploma.work.data.models.Level
import com.diploma.work.ui.navigation.TestSession
import com.diploma.work.ui.theme.Text
import com.diploma.work.ui.theme.TextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestDetailsScreen(
    navController: NavController,
    testId: Long,
    viewModel: TestDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(testId) {
        viewModel.loadTest(testId)
    }

    LaunchedEffect(state.testSessionId) {
        state.testSessionId?.let { sessionId ->
            navController.navigate("TestSession/$sessionId")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Test Details", style = TextStyle.TitleLarge.value) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else if (state.error != null) {
                Text(
                    text = state.error ?: "An error occurred",
                    style = TextStyle.BodyLarge.value,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            } else if (state.test == null) {
                Text(
                    text = "Test not found",
                    style = TextStyle.BodyLarge.value,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = state.test?.info?.title ?: "",
                                style = TextStyle.HeadlineMedium.value,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = state.test?.info?.description ?: "",
                                style = TextStyle.BodyLarge.value,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TestInfoTag(
                                    text = state.test?.info?.technologyName ?: "",
                                    icon = getDirectionIcon(state.test?.info?.direction),
                                    contentDescription = "Technology"
                                )

                                TestInfoTag(
                                    text = state.test?.info?.level?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "",
                                    icon = getLevelIcon(state.test?.info?.level),
                                    contentDescription = "Level"
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Test Information",
                                style = TextStyle.TitleMedium.value,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Questions:",
                                    style = TextStyle.BodyMedium.value,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${state.test?.info?.questionsCount ?: 0}",
                                    style = TextStyle.BodyMedium.value,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            val questionsCount = state.test?.info?.questionsCount ?: 0
                            val test = state.test
                            val avgPointsPerQuestion = if (test?.questions?.isNotEmpty() == true) {
                                test.questions.sumOf { it.points }.toFloat() / test.questions.size
                            } else 0f
                            val totalPoints = (avgPointsPerQuestion * questionsCount).toInt()
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Total Points:",
                                    style = TextStyle.BodyMedium.value,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$totalPoints",
                                    style = TextStyle.BodyMedium.value,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.height(24.dp))

                    if (state.hasUnfinishedSession) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "Незавершенный тест",
                                        style = TextStyle.TitleMedium.value,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    Text(
                                        text = "У вас есть начатый тест на вопросе ${state.lastSavedQuestionIndex + 1}",
                                        style = TextStyle.BodyMedium.value,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Button(
                                onClick = { viewModel.continueTest() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary,
                                    contentColor = MaterialTheme.colorScheme.onTertiary
                                )
                            ) {
                                Text(
                                    "Продолжить тест", 
                                    style = TextStyle.LabelLarge.value,
                                    color = MaterialTheme.colorScheme.onTertiary
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    Button(
                        onClick = { viewModel.startTest() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isStartingTest
                    ) {
                        if (state.isStartingTest) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                if (state.hasUnfinishedSession) "Начать заново" else "Начать тест", 
                                style = TextStyle.LabelLarge.value, 
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TestInfoTag(
    text: String,
    icon: ImageVector,
    contentDescription: String
) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                style = TextStyle.LabelMedium.value,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

private fun getDirectionIcon(direction: Direction?): ImageVector {
    return when (direction) {
        Direction.BACKEND -> Icons.Default.Storage
        Direction.FRONTEND -> Icons.Default.Code
        Direction.DEVOPS -> Icons.Default.SettingsEthernet
        Direction.DATA_SCIENCE -> Icons.Default.Computer
        else -> Icons.Default.Computer
    }
}

private fun getLevelIcon(level: Level?): ImageVector {
    return when (level) {
        Level.JUNIOR -> Icons.Default.Computer
        Level.MIDDLE -> Icons.Default.Computer
        Level.SENIOR -> Icons.Default.Computer
        else -> Icons.Default.Computer
    }
}