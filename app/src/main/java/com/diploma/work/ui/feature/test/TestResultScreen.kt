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
        percentage >= 80 -> MaterialTheme.colorScheme.primary
        percentage >= 60 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.your_score),
                style = TextStyle.TitleMedium.value,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = scoreColor.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${percentage.toInt()}%",
                        style = TextStyle.HeadlineLarge.value,
                        color = scoreColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.points_format, score, totalPoints),
                    style = TextStyle.BodyLarge.value,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = stringResource(R.string.time),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.time_format, formatTime(durationMillis)),
                    style = TextStyle.BodyMedium.value,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (durationMillis > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.seconds_format, durationMillis / 1000),
                    style = TextStyle.BodySmall.value,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
) {    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (questionResult.isCorrect)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.errorContainer
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
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (questionResult.isCorrect)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.error
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
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.size(12.dp))

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (questionResult.isCorrect) stringResource(R.string.correct) else stringResource(R.string.incorrect),
                        style = TextStyle.LabelLarge.value,
                        fontWeight = FontWeight.Bold,
                        color = if (questionResult.isCorrect)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.error
                    )

                    Text(
                        text = stringResource(R.string.points_earned, questionResult.pointsEarned, if (questionResult.pointsEarned != 1) stringResource(R.string.points_multiple) else stringResource(R.string.points_single)),
                        style = TextStyle.LabelMedium.value,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

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
                        color = MaterialTheme.colorScheme.primary
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