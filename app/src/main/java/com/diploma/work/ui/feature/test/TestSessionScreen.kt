package com.diploma.work.ui.feature.test

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.diploma.work.data.models.Question
import com.diploma.work.data.models.QuestionType
import com.diploma.work.ui.navigation.TestResult
import com.diploma.work.ui.theme.Text
import com.diploma.work.ui.theme.TextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestSessionScreen(
    navController: NavController,
    sessionId: String,
    viewModel: TestSessionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(sessionId) {
        viewModel.loadTestSession(sessionId)
    }

    LaunchedEffect(state.testCompleted) {
        if (state.testCompleted && state.testResult != null) {
            navController.navigate("TestResult/$sessionId") {
                popUpTo(navController.graph.startDestinationId)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Taking Test", style = TextStyle.TitleLarge.value) },
                navigationIcon = {
                    IconButton(onClick = { showConfirmDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Time",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatTime(
                                System.currentTimeMillis() - (state.testSession?.startedAt ?: System.currentTimeMillis())
                            ),
                            style = TextStyle.BodyMedium.value,
                            color = MaterialTheme.colorScheme.onSurface
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.error ?: "An error occurred",
                        style = TextStyle.BodyLarge.value,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.loadTestSession(sessionId) }
                    ) {
                        Text("Try Again")
                    }
                }
            } else if (state.testSession == null) {
                Text(
                    text = "Test session not found",
                    style = TextStyle.BodyLarge.value,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                QuestionScreen(
                    question = viewModel.getCurrentQuestion(),
                    questionNumber = state.currentQuestionIndex + 1,
                    totalQuestions = state.testSession?.questions?.size ?: 0,
                    selectedOptions = state.selectedOptions,
                    textAnswer = state.textAnswer,
                    codeAnswer = state.codeAnswer,
                    onOptionSelected = { viewModel.toggleOption(it) },
                    onTextAnswerChanged = { viewModel.setTextAnswer(it) },
                    onCodeAnswerChanged = { viewModel.setCodeAnswer(it) },
                    onPrevious = { viewModel.goToPreviousQuestion() },
                    onNext = { viewModel.saveAnswer() },
                    isSavingAnswer = state.isSavingAnswer,
                    showPrevious = state.currentQuestionIndex > 0
                )
            }

            if (state.showExplanation) {
                ExplanationDialog(
                    explanation = state.explanationText,
                    onDismiss = { viewModel.dismissExplanation() }
                )
            }

            if (showConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showConfirmDialog = false },
                    title = { Text("Leave Test?", style = TextStyle.TitleMedium.value) },
                    text = { Text("Are you sure you want to leave the test? Your progress will be saved, but the test will remain incomplete.", style = TextStyle.BodyMedium.value) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showConfirmDialog = false
                                navController.navigateUp()
                            }
                        ) {
                            Text("Leave", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirmDialog = false }) {
                            Text("Stay")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun QuestionScreen(
    question: Question?,
    questionNumber: Int,
    totalQuestions: Int,
    selectedOptions: List<Int>,
    textAnswer: String,
    codeAnswer: String,
    onOptionSelected: (Int) -> Unit,
    onTextAnswerChanged: (String) -> Unit,
    onCodeAnswerChanged: (String) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    isSavingAnswer: Boolean,
    showPrevious: Boolean
) {
    if (question == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Complete",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ready to submit your test?",
                style = TextStyle.TitleMedium.value,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onNext,
                enabled = !isSavingAnswer
            ) {
                if (isSavingAnswer) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Complete Test", style = TextStyle.LabelLarge.value, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Question $questionNumber of $totalQuestions",
                style = TextStyle.BodySmall.value,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            LinearProgressIndicator(
                progress = { questionNumber.toFloat() / totalQuestions },
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                strokeCap = StrokeCap.Round
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${(questionNumber.toFloat() / totalQuestions * 100).toInt()}%",
                style = TextStyle.BodySmall.value,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                    text = question.text,
                    style = TextStyle.TitleMedium.value,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )

                if (question.sampleCode != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = "Code",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = question.sampleCode,
                                style = TextStyle.BodyMedium.value,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (question.type) {
                    QuestionType.MCQ -> {
                        question.options.forEachIndexed { index, option ->
                            val isSelected = selectedOptions.contains(index)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .selectable(
                                        selected = isSelected,
                                        onClick = { onOptionSelected(index) },
                                        role = Role.Checkbox
                                    )
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { onOptionSelected(index) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = option,
                                    style = TextStyle.BodyMedium.value,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    QuestionType.TEXT -> {
                        OutlinedTextField(
                            value = textAnswer,
                            onValueChange = onTextAnswerChanged,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Your Answer") },
                            minLines = 3
                        )
                    }
//                    QuestionType.CODE -> {
//                        OutlinedTextField(
//                            value = codeAnswer,
//                            onValueChange = onCodeAnswerChanged,
//                            modifier = Modifier.fillMaxWidth(),
//                            label = { Text("Your Code") },
//                            minLines = 5,
//                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
//                        )
//                    }
                    else -> {
                        Text(
                            text = "Unsupported question type",
                            style = TextStyle.BodyMedium.value,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (showPrevious) {
                Button(
                    onClick = onPrevious,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Previous", style = TextStyle.LabelLarge.value, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                enabled = !isSavingAnswer,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            ) {
                if (isSavingAnswer) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (questionNumber == totalQuestions) "Finish" else "Next",
                        style = TextStyle.LabelLarge.value,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next"
                    )
                }
            }
        }
    }
}

@Composable
fun ExplanationDialog(
    explanation: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Hint",
                style = TextStyle.TitleMedium.value,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column {
                Text(
                    text = "Your answer was not correct. Here's a hint:",
                    style = TextStyle.BodyMedium.value,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = explanation,
                        style = TextStyle.BodyMedium.value,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Understand", color = MaterialTheme.colorScheme.primary)
            }
        }
    )
}

private fun formatTime(timeInMillis: Long): String {
    val seconds = (timeInMillis / 1000) % 60
    val minutes = (timeInMillis / (1000 * 60)) % 60
    val hours = (timeInMillis / (1000 * 60 * 60))
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
} 