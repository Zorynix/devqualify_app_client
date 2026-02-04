package com.diploma.work.ui.feature.test

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.diploma.work.R
import com.diploma.work.data.models.QuestionResult
import com.diploma.work.ui.components.ErrorCard
import com.diploma.work.ui.components.LoadingCard
import com.diploma.work.ui.components.CodeHighlighterText
import com.diploma.work.ui.components.ClickableTextWithLinks
import com.diploma.work.ui.navigation.Home
import com.diploma.work.ui.navigation.safeNavigate
import com.diploma.work.ui.navigation.safeNavigateBack
import com.diploma.work.ui.theme.Text
import com.diploma.work.ui.theme.TextStyle
import com.orhanobut.logger.Logger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestResultScreen(
    navController: NavController,
    sessionId: String,
    modifier: Modifier = Modifier,
    viewModel: TestResultViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(sessionId) {
        viewModel.loadTestResult(sessionId)
    }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.test_results), style = TextStyle.TitleLarge.value) },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (!navController.popBackStack()) {
                            navController.safeNavigate(Home)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_short)
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
            contentAlignment = Alignment.Center        ) {
            if (state.isLoading) {
                LoadingCard(
                    message = stringResource(R.string.loading_test_results_message)
                )            }
            else if (state.error != null) {
                ErrorCard(
                    error = state.error!!,
                    onRetry = { viewModel.loadTestResult(sessionId) }
                )
            } else if (state.result == null) {
                Text(
                    text = stringResource(R.string.no_results_found),
                    style = TextStyle.BodyLarge.value,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    item {
                        ScoreSection(
                            score = state.result?.score ?: 0,
                            totalPoints = state.result?.totalPoints ?: 0,
                            durationMillis = state.result?.durationMillis ?: 0
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    item {
                        Text(
                            text = stringResource(R.string.feedback),
                            style = TextStyle.TitleMedium.value,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            ClickableTextWithLinks(
                                text = state.result?.feedback ?: "",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    item {
                        Text(
                            text = stringResource(R.string.question_results),
                            style = TextStyle.TitleMedium.value,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(state.result?.questionResults ?: emptyList()) { questionResult ->
                        QuestionResultItem(questionResult = questionResult)
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { navController.safeNavigate("Home", clearStack = true) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = stringResource(R.string.home_short)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(stringResource(R.string.back_to_home_short), style = TextStyle.LabelLarge.value, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreSection(
    score: Int, 
    totalPoints: Int, 
    durationMillis: Long,
    modifier: Modifier = Modifier
) {
    val percentage = if (totalPoints > 0) (score.toFloat() / totalPoints) * 100 else 0f
    val scoreColor = when {
        percentage >= 80 -> Color(0xFF4CAF50) // Green
        percentage >= 60 -> Color(0xFFFFA726) // Orange
        else -> Color(0xFFF44336) // Red
    }
    val scoreBgColor = when {
        percentage >= 80 -> Color(0xFF4CAF50).copy(alpha = 0.1f)
        percentage >= 60 -> Color(0xFFFFA726).copy(alpha = 0.1f)
        else -> Color(0xFFF44336).copy(alpha = 0.1f)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            val emoji = when {
                percentage >= 80 -> "🎉"
                percentage >= 60 -> "👍"
                else -> "💪"
            }
            Text(
                text = emoji,
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = androidx.compose.ui.unit.TextUnit(48f, androidx.compose.ui.unit.TextUnitType.Sp)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when {
                    percentage >= 80 -> "Отлично!"
                    percentage >= 60 -> "Хороший результат"
                    else -> "Есть над чем поработать"
                },
                style = TextStyle.TitleMedium.value,
                color = scoreColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                modifier = Modifier.size(140.dp),
                shape = CircleShape,
                color = scoreBgColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${percentage.toInt()}%",
                            style = TextStyle.HeadlineLarge.value,
                            color = scoreColor,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.points_format, score, totalPoints),
                            style = TextStyle.BodySmall.value,
                            color = scoreColor.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = stringResource(R.string.time),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Время: ${formatTime(durationMillis)}",
                        style = TextStyle.BodyMedium.value,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun formatTime(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    val hours = (millis / (1000 * 60 * 60))
    
    val formatted = if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
    
    Logger.d("formatTime: millis=$millis, seconds=${millis/1000}s, formatted=$formatted")
    
    return formatted
}

@Composable
fun QuestionResultItem(
    questionResult: QuestionResult,
    modifier: Modifier = Modifier
) {
    val bgColor = if (questionResult.isCorrect)
        Color(0xFF4CAF50).copy(alpha = 0.1f)
    else
        Color(0xFFF44336).copy(alpha = 0.1f)

    val accentColor = if (questionResult.isCorrect)
        Color(0xFF2E7D32)
    else
        Color(0xFFC62828)

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = bgColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (questionResult.isCorrect)
                            Color(0xFF4CAF50).copy(alpha = 0.2f)
                        else
                            Color(0xFFF44336).copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (questionResult.isCorrect)
                        Icons.Default.CheckCircle
                    else
                        Icons.Default.Error,
                    contentDescription = if (questionResult.isCorrect) "Correct" else "Incorrect",
                    modifier = Modifier.size(24.dp),
                    tint = accentColor
                )
            }

            Spacer(modifier = Modifier.size(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (questionResult.isCorrect) stringResource(R.string.correct) else stringResource(R.string.incorrect),
                        style = TextStyle.LabelLarge.value,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = accentColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "+${questionResult.pointsEarned}",
                            style = TextStyle.LabelMedium.value,
                            color = accentColor,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (!questionResult.isCorrect) {
                    Text(
                        text = stringResource(R.string.your_answer_format, questionResult.userAnswer),
                        style = TextStyle.BodySmall.value,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(R.string.correct_answer_format, questionResult.correctAnswer),
                        style = TextStyle.BodySmall.value,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Medium
                    )

                    if (questionResult.feedback.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        ClickableTextWithLinks(
                            text = questionResult.feedback,
                            modifier = Modifier
                        )
                    }
                }
            }
        }
    }
}