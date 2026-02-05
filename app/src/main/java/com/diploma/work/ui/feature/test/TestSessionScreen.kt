package com.diploma.work.ui.feature.test

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.delay
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.diploma.work.R
import com.diploma.work.data.models.Question
import com.diploma.work.data.models.QuestionType
import com.diploma.work.ui.components.CodeHighlighterText
import com.diploma.work.ui.components.ErrorCard
import com.diploma.work.ui.components.LoadingCard
import com.diploma.work.ui.theme.Text
import com.diploma.work.ui.theme.TextStyle
import com.orhanobut.logger.Logger
import dev.jeziellago.compose.markdowntext.MarkdownText
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.mutableLongStateOf
import com.diploma.work.ui.navigation.safeNavigate
import com.diploma.work.ui.navigation.safeNavigateBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestSessionScreen(
    navController: NavController,
    sessionId: String,
    modifier: Modifier = Modifier,
    viewModel: TestSessionViewModel = hiltViewModel()
) {val state by viewModel.uiState.collectAsState()
    var showConfirmDialog by remember { mutableStateOf(false) }
    var elapsedTime by remember { mutableLongStateOf(0L) }
    var startTime by remember { mutableLongStateOf(0L) }

    BackHandler {
        Logger.d("Navigation: Back button pressed in TestSessionScreen")
        showConfirmDialog = true
    }

    LaunchedEffect(sessionId) {
        Logger.d("TestSessionScreen: Loading session with ID: $sessionId")
        viewModel.loadSavedTestSession(sessionId)
    }

    LaunchedEffect(state.testSession) {
        if (state.testSession != null) {
            startTime = if (state.elapsedTimeMillis > 0) {
                System.currentTimeMillis() - state.elapsedTimeMillis
            } else {
                state.testSession!!.startedAt
            }
            elapsedTime = state.elapsedTimeMillis
        }
    }

    LaunchedEffect(startTime) {
        while (true) {
            val currentElapsedTime = System.currentTimeMillis() - startTime
            elapsedTime = currentElapsedTime
            viewModel.updateElapsedTime(currentElapsedTime)
            delay(1000)
        }
    }
    LaunchedEffect(state.testCompleted) {
        if (state.testCompleted && state.testResult != null) {
            navController.safeNavigate("TestResult/$sessionId", clearStack = true)
        }
    }

    LaunchedEffect(state.error) {
        if (state.error?.contains("session already completed") == true && !state.isLeavingTest) {
            viewModel.loadTestSession(sessionId)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                is NavigationEvent.NavigateUp -> {
                    Logger.d("Navigation: Handling NavigateUp event from TestSessionScreen")
                    navController.safeNavigate("Home", clearStack = true)
                }
            }
        }
    }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.taking_test_title), style = TextStyle.TitleLarge.value) },
                navigationIcon = {
                    IconButton(onClick = { showConfirmDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_desc)
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
                            contentDescription = stringResource(R.string.time_desc),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatTime(elapsedTime),
                            style = TextStyle.BodyMedium.value,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { paddingValues ->        Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        if (state.isLoading) {
            LoadingCard(
                message = stringResource(R.string.loading_test_session)
            )            } else if (state.isCompletingTest) {
            LoadingCard(
                message = stringResource(R.string.finalizing_test)
            )}
        else if (state.error != null) {
            ErrorCard(
                error = state.error!!,
                onRetry = { viewModel.loadTestSession(sessionId) }
            )
        } else if (state.testSession == null) {
            Text(
                text = stringResource(R.string.test_session_not_found),
                style = TextStyle.BodyLarge.value,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            QuestionScreen(
                question = viewModel.getCurrentQuestion(),
                questionNumber = state.currentQuestionIndex + 1,
                totalQuestions = state.testSession?.testInfo?.questionsCount ?: state.testSession?.questions?.size ?: 0,
                selectedOptions = state.selectedOptions,
                textAnswer = state.textAnswer,                    onOptionSelect = { viewModel.toggleOption(it) },
                onTextAnswerChange = { viewModel.setTextAnswer(it) },
                onPrevious = { viewModel.goToPreviousQuestion() },
                onNext = { viewModel.saveAnswer() },
                isSavingAnswer = state.isSavingAnswer,
                isCompletingTest = state.isCompletingTest,
                showPrevious = state.currentQuestionIndex > 0,
                isQuestionAnswered = state.isCurrentQuestionAnswered,
                isIncorrectlyAnswered = state.currentQuestionIndex >= 0 &&
                        viewModel.getCurrentQuestion()?.let {
                            state.incorrectlyAnsweredQuestions.contains(it.id)
                        } ?: false,
                isCorrectlyAnswered = state.currentQuestionIndex >= 0 &&
                        viewModel.getCurrentQuestion()?.let {
                            state.correctlyAnsweredQuestions.contains(it.id)
                        } ?: false,
                isLastQuestion = (state.currentQuestionIndex + 1) == (state.testSession?.testInfo?.questionsCount ?: state.testSession?.questions?.size ?: 0)
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
                title = { Text(stringResource(R.string.leave_test_title), style = TextStyle.TitleMedium.value) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showConfirmDialog = false
                            viewModel.handleLeaveTest()
                        }
                    ) {
                        Text(stringResource(R.string.leave), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDialog = false }) {
                        Text(stringResource(R.string.stay))
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
    onOptionSelect: (Int) -> Unit,
    onTextAnswerChange: (String) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    isSavingAnswer: Boolean,
    isCompletingTest: Boolean,
    showPrevious: Boolean,
    isQuestionAnswered: Boolean,
    isIncorrectlyAnswered: Boolean,
    isCorrectlyAnswered: Boolean,
    isLastQuestion: Boolean,
    modifier: Modifier = Modifier
) {
    if (question == null) {        Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = stringResource(R.string.complete_desc),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.ready_to_submit),
            style = TextStyle.TitleMedium.value,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onNext,
            enabled = !isSavingAnswer && !isCompletingTest
        ) {
            if (isSavingAnswer || isCompletingTest) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(stringResource(R.string.complete_test_button), style = TextStyle.LabelLarge.value, color = MaterialTheme.colorScheme.onPrimaryContainer)
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

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                        text = stringResource(R.string.question_of, questionNumber, totalQuestions),
                        style = TextStyle.TitleSmall.value,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.progress_percent, (questionNumber.toFloat() / totalQuestions * 100).toInt()),
                        style = TextStyle.BodyMedium.value,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { questionNumber.toFloat() / totalQuestions },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    strokeCap = StrokeCap.Round,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
            }
        }

        if (isIncorrectlyAnswered) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = stringResource(R.string.incorrect_desc),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.question_incorrect),
                        style = TextStyle.BodyMedium.value,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else if (isCorrectlyAnswered) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = androidx.compose.ui.graphics.Color(0xFF4CAF50).copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = stringResource(R.string.correct),
                        tint = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.question_correct),
                        style = TextStyle.BodyMedium.value,
                        color = androidx.compose.ui.graphics.Color(0xFF1B5E20),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = question.text,
                    style = TextStyle.TitleMedium.value,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 24.sp
                )

                if (question.sampleCode != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CodeHighlighterText(
                        code = question.sampleCode,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (question.type) {
                    QuestionType.MULTIPLE_CHOICE -> {
                        if (question.sampleCode != null) {
                            question.options.forEachIndexed { index, option ->
                                val isSelected = selectedOptions.contains(index)
                                val backgroundColor = when {
                                    isIncorrectlyAnswered && isSelected -> MaterialTheme.colorScheme.errorContainer
                                    isCorrectlyAnswered && isSelected -> MaterialTheme.colorScheme.primaryContainer
                                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                                val textColor = when {
                                    isIncorrectlyAnswered && isSelected -> MaterialTheme.colorScheme.onErrorContainer
                                    isCorrectlyAnswered && isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                                    isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(backgroundColor)
                                        .selectable(
                                            selected = isSelected,
                                            onClick = {
                                                if (!isQuestionAnswered) {
                                                    onOptionSelect(index)
                                                }
                                            },
                                            role = Role.RadioButton,
                                            enabled = !isQuestionAnswered
                                        )
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            if (!isQuestionAnswered) {
                                                onOptionSelect(index)
                                            }
                                        },
                                        enabled = !isQuestionAnswered
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = option,
                                        style = TextStyle.BodyMedium.value,
                                        color = textColor
                                    )
                                }
                            }
                        } else {

                            question.options.forEachIndexed { index, option ->
                                val isSelected = selectedOptions.contains(index)
                                val backgroundColor = when {
                                    isIncorrectlyAnswered && isSelected -> MaterialTheme.colorScheme.errorContainer
                                    isCorrectlyAnswered && isSelected -> MaterialTheme.colorScheme.primaryContainer
                                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                                val textColor = when {
                                    isIncorrectlyAnswered && isSelected -> MaterialTheme.colorScheme.onErrorContainer
                                    isCorrectlyAnswered && isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                                    isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(backgroundColor)
                                        .selectable(
                                            selected = isSelected,
                                            onClick = {
                                                if (!isQuestionAnswered) {
                                                    onOptionSelect(index)
                                                }
                                            },
                                            role = Role.Checkbox,
                                            enabled = !isQuestionAnswered
                                        )
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = {
                                            if (!isQuestionAnswered) {
                                                onOptionSelect(index)
                                            }
                                        },
                                        enabled = !isQuestionAnswered
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = option,
                                        style = TextStyle.BodyMedium.value,
                                        color = textColor
                                    )
                                }
                            }
                        }
                    }
                    QuestionType.SINGLE_CHOICE, QuestionType.CODE, QuestionType.UNSPECIFIED -> {
                        question.options.forEachIndexed { index, option ->
                            val isSelected = selectedOptions.contains(index)
                            val backgroundColor = when {
                                isIncorrectlyAnswered && isSelected -> MaterialTheme.colorScheme.errorContainer
                                isCorrectlyAnswered && isSelected -> MaterialTheme.colorScheme.primaryContainer
                                isSelected -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                            val textColor = when {
                                isIncorrectlyAnswered && isSelected -> MaterialTheme.colorScheme.onErrorContainer
                                isCorrectlyAnswered && isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                                isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(backgroundColor)
                                    .selectable(
                                        selected = isSelected,
                                        onClick = {
                                            if (!isQuestionAnswered) {
                                                onOptionSelect(index)
                                            }
                                        },
                                        role = Role.RadioButton,
                                        enabled = !isQuestionAnswered
                                    )
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        if (!isQuestionAnswered) {
                                            onOptionSelect(index)
                                        }
                                    },
                                    enabled = !isQuestionAnswered
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = option,
                                    style = TextStyle.BodyMedium.value,
                                    color = textColor
                                )
                            }
                        }
                    }
                    QuestionType.TEXT -> {
                        val textFieldColors = if (isIncorrectlyAnswered) {
                            OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                                unfocusedBorderColor = MaterialTheme.colorScheme.error,
                                unfocusedTextColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        } else if (isCorrectlyAnswered) {
                            OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                                unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else {
                            OutlinedTextFieldDefaults.colors()
                        }

                        OutlinedTextField(
                            value = textAnswer,
                            onValueChange = onTextAnswerChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Ваш ответ") },
                            minLines = 3,
                            enabled = !isQuestionAnswered,
                            colors = textFieldColors
                        )
                    }
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
                        contentDescription = "Назад"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Назад", style = TextStyle.LabelLarge.value, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                enabled = !isSavingAnswer && (!isLastQuestion || !isCompletingTest),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            ) {
                if (isSavingAnswer || isCompletingTest) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isLastQuestion) "Закончить" else "Далее",
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
                text = "Пояснение",
                style = TextStyle.TitleMedium.value,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column {
                Text(
                    text = "Ваш ответ неверен. Объяснение:",
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
            TextButton(onClick = onDismiss, colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )) {
                Text("Понял", color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    )
}
