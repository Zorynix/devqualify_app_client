package com.diploma.work.ui.feature.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DismissibleDrawerSheet
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.diploma.work.R
import com.diploma.work.data.AppSession
import com.diploma.work.data.models.User
import com.diploma.work.grpc.userinfo.Direction
import com.diploma.work.grpc.userinfo.Level
import com.diploma.work.ui.DiplomTextField
import com.diploma.work.ui.components.AvatarImage
import com.diploma.work.ui.components.ErrorCard
import com.diploma.work.ui.components.LoadingCard
import com.diploma.work.ui.theme.AppThemeType
import com.diploma.work.ui.theme.Text
import com.diploma.work.ui.theme.TextStyle
import com.diploma.work.ui.theme.Theme
import com.diploma.work.ui.theme.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    themeManager: ThemeManager,
    modifier: Modifier = Modifier,
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val updateSuccess by viewModel.updateSuccess.collectAsState()
    val direction by viewModel.direction.collectAsState()
    val level by viewModel.level.collectAsState()
    val avatarUrl by viewModel.avatarUrl.collectAsState()
    val theme by themeManager.currentTheme.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val errorOccurredText = stringResource(R.string.error_occurred)
    val profileUpdatedText = stringResource(R.string.profile_updated_successfully)


    var showStatisticsDialog by remember { mutableStateOf(false) }
    var showGradeDialog by remember { mutableStateOf(false) }
    var showDirectionDialog by remember { mutableStateOf(false) }
    var showAchievementsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            snackbarHostState.showSnackbar(
                message = profileUpdatedText,
                duration = SnackbarDuration.Short
            )
            viewModel.resetUpdateStatus()
        }
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(
                message = errorMessage ?: errorOccurredText,
                duration = SnackbarDuration.Short
            )
            viewModel.resetUpdateStatus()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetUpdateStatus()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile), style = TextStyle.TitleLarge.value) }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            if (isLoading && user == null) {
                LoadingCard(
                    message = stringResource(R.string.loading_profile),
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (user != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (errorMessage != null) {
                        ErrorCard(
                            error = errorMessage!!,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                    

                    ProfileHeader(
                        user = user!!,
                        username = state.username,
                        avatarUrl = avatarUrl,
                        session = viewModel.session,
                        onUsernameChange = { viewModel.onUsernameChanged(it) },
                        onAvatarChange = { uri -> uri?.let { viewModel.uploadAvatar(it) } },
                        onUpdateClick = { viewModel.updateUserProfile() },
                        isLoading = isLoading
                    )

                    Spacer(modifier = Modifier.height(24.dp))


                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ProfileActionCard(
                            title = "Статистика",
                            subtitle = "${user!!.completedTestsCount} тестов",
                            icon = Icons.Default.Analytics,
                            gradientColors = listOf(
                                Color(0xFF5C6BC0), // Indigo 400
                                Color(0xFF512DA8)  // Deep Purple 700
                            ),
                            onClick = { showStatisticsDialog = true },
                            modifier = Modifier.weight(1f)
                        )

                        ProfileActionCard(
                            title = "Мой грейд",
                            subtitle = levelToString(level),
                            icon = Icons.Default.WorkspacePremium,
                            gradientColors = listOf(
                                Color(0xFFEC407A), // Pink 400
                                Color(0xFFC2185B)  // Pink 700
                            ),
                            onClick = { showGradeDialog = true },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ProfileActionCard(
                            title = "Достижения",
                            subtitle = "${user!!.achievementsCount} получено",
                            icon = Icons.Default.EmojiEvents,
                            gradientColors = listOf(
                                Color(0xFF29B6F6), // Light Blue 400
                                Color(0xFF0288D1)  // Light Blue 700
                            ),
                            onClick = { showAchievementsDialog = true },
                            modifier = Modifier.weight(1f)
                        )

                        ProfileActionCard(
                            title = "Направление",
                            subtitle = directionToString(direction),
                            icon = Icons.Default.School,
                            gradientColors = listOf(
                                Color(0xFF66BB6A), // Green 400
                                Color(0xFF388E3C)  // Green 700
                            ),
                            onClick = { showDirectionDialog = true },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }


                if (showStatisticsDialog) {
                    StatisticsDialog(
                        user = user!!,
                        onDismiss = { showStatisticsDialog = false }
                    )
                }

                if (showGradeDialog) {
                    LevelSelectionDialog(
                        currentLevel = level,
                        onLevelChange = { viewModel.onLevelChanged(it) },
                        onDismiss = { showGradeDialog = false },
                        onSave = {
                            viewModel.updateUserProfile()
                            showGradeDialog = false
                        }
                    )
                }

                if (showDirectionDialog) {
                    DirectionSelectionDialog(
                        currentDirection = direction,
                        onDirectionChange = { viewModel.onDirectionChanged(it) },
                        onDismiss = { showDirectionDialog = false },
                        onSave = {
                            viewModel.updateUserProfile()
                            showDirectionDialog = false
                        }
                    )
                }

                if (showAchievementsDialog) {
                    AchievementsPreviewDialog(
                        achievementsCount = user!!.achievementsCount,
                        onDismiss = { showAchievementsDialog = false }
                    )
                }
            } else {
                ErrorCard(
                    error = stringResource(R.string.failed_load_user_data),
                    onRetry = { viewModel.loadProfile() },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileHeader(
    user: User,
    username: String,
    avatarUrl: String,
    session: AppSession,
    onUsernameChange: (String) -> Unit,
    onAvatarChange: (Uri?) -> Unit,
    onUpdateClick: () -> Unit,
    isLoading: Boolean
) {
    var showAvatarDialog by remember { mutableStateOf(false) }
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                onAvatarChange(uri)
            }
        }
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                contentAlignment = Alignment.BottomEnd
            ) {
                AvatarImage(
                    avatarUrl = avatarUrl,
                    session = session,
                    size = 100.dp,
                    borderWidth = 3.dp
                )

                IconButton(
                    onClick = { showAvatarDialog = true },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_edit),
                        contentDescription = stringResource(R.string.change_avatar),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))


            Text(
                text = user.email,
                style = TextStyle.BodyMedium.value,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))


            DiplomTextField(
                value = username,
                onValueChange = onUsernameChange,
                label = {
                    Text(
                        stringResource(R.string.username),
                        style = TextStyle.BodyMedium.value,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))


            Button(
                onClick = onUpdateClick,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.save),
                        style = TextStyle.ButtonText.value,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }


    if (showAvatarDialog) {
        Dialog(onDismissRequest = { showAvatarDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        stringResource(R.string.change_avatar_desc),
                        style = TextStyle.TitleLarge.value
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    AvatarImage(
                        avatarUrl = avatarUrl,
                        session = session,
                        size = 120.dp,
                        borderWidth = 2.dp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            imagePicker.launch("image/*")
                            showAvatarDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.choose_from_gallery),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = { showAvatarDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(120.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(gradientColors),
                    shape = MaterialTheme.shapes.large
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(28.dp)
                )

                Column {
                    Text(
                        text = title,
                        style = TextStyle.TitleMedium.value,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        style = TextStyle.BodySmall.value,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatisticsDialog(
    user: User,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.statistics),
                    style = TextStyle.TitleLarge.value,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                StatisticItem(
                    icon = Icons.Default.School,
                    label = stringResource(R.string.completed_tests),
                    value = "${user.completedTestsCount}",
                    color = Color(0xFF667EEA)
                )

                StatisticItem(
                    icon = Icons.Default.TrendingUp,
                    label = stringResource(R.string.correct_answers_stat),
                    value = "${user.totalCorrectAnswers}",
                    color = Color(0xFF43E97B)
                )

                StatisticItem(
                    icon = Icons.Default.Grade,
                    label = stringResource(R.string.incorrect_answers_stat),
                    value = "${user.totalIncorrectAnswers}",
                    color = Color(0xFFF5576C)
                )

                StatisticItem(
                    icon = Icons.Default.EmojiEvents,
                    label = stringResource(R.string.achievements_stat),
                    value = "${user.achievementsCount}",
                    color = Color(0xFF4FACFE)
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.close))
                }
            }
        }
    }
}

@Composable
private fun StatisticItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = color.copy(alpha = 0.1f),
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = TextStyle.BodyMedium.value,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = value,
            style = TextStyle.TitleMedium.value,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LevelSelectionDialog(
    currentLevel: Level,
    onLevelChange: (Level) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var levelExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.select_level),
                    style = TextStyle.TitleLarge.value,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.level_description),
                    style = TextStyle.BodyMedium.value,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.level),
                    style = TextStyle.BodyMedium.value,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = levelExpanded,
                    onExpandedChange = { levelExpanded = it }
                ) {
                    DiplomTextField(
                        value = levelToString(currentLevel),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = levelExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                    )

                    ExposedDropdownMenu(
                        expanded = levelExpanded,
                        onDismissRequest = { levelExpanded = false }
                    ) {
                        Level.entries.filter { it != Level.UNRECOGNIZED }.forEach { lvl ->
                            DropdownMenuItem(
                                text = { Text(levelToString(lvl)) },
                                onClick = {
                                    onLevelChange(lvl)
                                    levelExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onSave,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.save),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DirectionSelectionDialog(
    currentDirection: Direction,
    onDirectionChange: (Direction) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var directionExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.select_direction),
                    style = TextStyle.TitleLarge.value,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.direction_description),
                    style = TextStyle.BodyMedium.value,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.direction),
                    style = TextStyle.BodyMedium.value,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = directionExpanded,
                    onExpandedChange = { directionExpanded = it }
                ) {
                    DiplomTextField(
                        value = directionToString(currentDirection),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = directionExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                    )

                    ExposedDropdownMenu(
                        expanded = directionExpanded,
                        onDismissRequest = { directionExpanded = false }
                    ) {
                        Direction.entries.filter { it != Direction.UNRECOGNIZED }.forEach { dir ->
                            DropdownMenuItem(
                                text = { Text(directionToString(dir)) },
                                onClick = {
                                    onDirectionChange(dir)
                                    directionExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onSave,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.save),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementsPreviewDialog(
    achievementsCount: Int,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.achievements),
                    style = TextStyle.TitleLarge.value,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Получено: $achievementsCount",
                    style = TextStyle.BodyLarge.value,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Продолжайте проходить тесты, чтобы получить больше достижений!",
                    style = TextStyle.BodyMedium.value,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.close))
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawerContent(
    username: String,
    avatarUrl: String,
    theme: AppThemeType,
    session: AppSession,
    onThemeToggle: () -> Unit,
    onInterestsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val headerGradient = if (theme == AppThemeType.Dark) {
        listOf(
            Color(0xFF1A237E), // Dark Indigo
            Color(0xFF4A148C)  // Dark Purple
        )
    } else {
        listOf(
            Color(0xFF5C6BC0), // Indigo 400
            Color(0xFF7E57C2)  // Deep Purple 400
        )
    }

    DismissibleDrawerSheet(
        modifier = modifier.width(300.dp),
        drawerContainerColor = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surface)
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(colors = headerGradient),
                        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                    )
                    .padding(24.dp)
                    .padding(top = 16.dp)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .border(3.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        AvatarImage(
                            avatarUrl = avatarUrl,
                            session = session,
                            size = 68.dp,
                            borderWidth = 0.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Greeting + Username
                    Text(
                        text = "Привет,",
                        style = TextStyle.BodyMedium.value,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = username,
                        style = TextStyle.TitleMedium.value,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))



                    Surface(
                        onClick = onThemeToggle,
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.25f),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(
                                    id = if (theme == AppThemeType.Dark) R.drawable.light_mode
                                    else R.drawable.dark_mode
                                ),
                                contentDescription = stringResource(R.string.toggle_theme),
                                modifier = Modifier.size(18.dp),
                                colorFilter = ColorFilter.tint(Color.White)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (theme == AppThemeType.Dark) "Светлая тема" else "Тёмная тема",
                                style = TextStyle.LabelMedium.value,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            DrawerMenuItem(
                icon = {
                    Image(
                        painter = painterResource(id = R.drawable.ic_interests),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                },
                label = stringResource(R.string.interests),
                onClick = onInterestsClick
            )

            DrawerMenuItem(
                icon = {
                    Image(
                        painter = painterResource(id = R.drawable.timer),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                },
                label = "История",
                onClick = onHistoryClick
            )

            DrawerMenuItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Feedback,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                label = "Обратная связь",
                onClick = onFeedbackClick
            )

            Spacer(modifier = Modifier.weight(1f))


            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            DrawerMenuItem(
                icon = {
                    Image(
                        painter = painterResource(id = R.drawable.logout),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.error)
                    )
                },
                label = stringResource(R.string.logout),
                labelColor = MaterialTheme.colorScheme.error,
                onClick = onLogout
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DrawerMenuItem(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit,
    labelColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                style = TextStyle.BodyLarge.value,
                color = labelColor
            )
        }
    }
}


@Composable
private fun directionToString(direction: Direction): String {
    return when (direction) {
        Direction.BACKEND -> stringResource(R.string.backend)
        Direction.FRONTEND -> stringResource(R.string.frontend)
        Direction.DEVOPS -> stringResource(R.string.devops)
        Direction.DATA_SCIENCE -> stringResource(R.string.data_science)
        else -> stringResource(R.string.select_direction_placeholder)
    }
}

@Composable
private fun levelToString(level: Level): String {
    return when (level) {
        Level.JUNIOR -> stringResource(R.string.junior)
        Level.MIDDLE -> stringResource(R.string.middle)
        Level.SENIOR -> stringResource(R.string.senior)
        else -> stringResource(R.string.select_level_placeholder)
    }
}