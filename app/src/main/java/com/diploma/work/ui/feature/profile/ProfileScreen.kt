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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            snackbarHostState.showSnackbar(
                message = "Profile updated successfully!",
                duration = SnackbarDuration.Short
            )
            viewModel.resetUpdateStatus()
        }
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(
                message = errorMessage ?: "An error occurred",
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
                title = { Text("Profile", style = TextStyle.HeadlineMedium.value) }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)        ) {
            if (isLoading) {
                LoadingCard(
                    message = "Loading profile...",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (user != null) {
                Column {
                    if (errorMessage != null) {
                        ErrorCard(
                            error = errorMessage!!,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    
                    UserProfileContent(
                    user = user!!,
                    username = state.username,
                    direction = direction,
                    level = level,
                    avatarUrl = avatarUrl,
                    session = viewModel.session,
                    onUsernameChange = { viewModel.onUsernameChanged(it) },
                    onDirectionChange = { viewModel.onDirectionChanged(it) },
                    onLevelChange = { viewModel.onLevelChanged(it) },
                    onAvatarChange = { viewModel.onAvatarChanged(it) },
                    onImagePickerClick = { uri -> 
                        uri?.let { 
                            viewModel.uploadAvatar(it)
                            viewModel.onAvatarChanged(it.toString())
                        }
                    },                        onUpdateClick = { viewModel.updateUserProfile() },
                        isLoading = isLoading
                    )
                }            } else {
                ErrorCard(
                    error = "Failed to load user data. Please try again.",
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
fun AppDrawerContent(
    username: String,
    avatarUrl: String,
    theme: AppThemeType,
    session: AppSession,
    onThemeToggle: () -> Unit,
    onInterestsClick: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    DismissibleDrawerSheet(
        modifier = modifier.width(280.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {                        com.diploma.work.ui.components.AvatarImage(
                            avatarUrl = avatarUrl,
                            session = session,
                            size = 60.dp,
                            borderWidth = 2.dp
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Text(
                            text = username,
                            style = TextStyle.TitleMedium.value,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                HorizontalDivider()
                  Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onThemeToggle() }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(
                            id = if (theme == AppThemeType.Dark) R.drawable.light_mode
                            else R.drawable.dark_mode
                        ),
                        contentDescription = "Toggle theme",
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                    )
                    
                    Text(
                        text = if (theme == AppThemeType.Dark) "Light Mode" else "Dark Mode",
                        style = TextStyle.BodyLarge.value,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onInterestsClick() }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_interests),
                        contentDescription = "Interests",
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                    )
                    
                    Text(
                        text = "Interests",
                        style = TextStyle.BodyLarge.value,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
            
            Column {
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLogout() }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logout),
                        contentDescription = "Logout",
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                    )
                    
                    Text(
                        text = "Logout",
                        style = TextStyle.BodyLarge.value,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserProfileContent(
    user: User,
    username: String,
    direction: Direction,
    level: Level,
    avatarUrl: String,
    session: AppSession,
    onUsernameChange: (String) -> Unit,
    onDirectionChange: (Direction) -> Unit,
    onLevelChange: (Level) -> Unit,
    onAvatarChange: (String) -> Unit,
    onImagePickerClick: (Uri?) -> Unit,
    onUpdateClick: () -> Unit,
    isLoading: Boolean
) {
    var directionMenuExpanded by remember { mutableStateOf(false) }
    var levelMenuExpanded by remember { mutableStateOf(false) }
    var showAvatarDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> 
            if (uri != null) {
                onImagePickerClick(uri)
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                ) {
                    com.diploma.work.ui.components.AvatarImage(
                        avatarUrl = avatarUrl,
                        session = session,
                        size = 120.dp,
                        borderWidth = 2.dp
                    )
                    
                    IconButton(
                        onClick = { showAvatarDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_edit), 
                            contentDescription = "Change Avatar",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        
        Text("Personal Information", style = TextStyle.TitleMedium.value)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Email", style = TextStyle.BodyMedium.value)
        Text(
            text = user.email,
            style = TextStyle.BodyLarge.value,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Username", style = TextStyle.BodyMedium.value)
        DiplomTextField(
            value = username,
            onValueChange = onUsernameChange,
            modifier = Modifier.fillMaxWidth()
        )
          Spacer(modifier = Modifier.height(16.dp))
        
        Text("Direction", style = TextStyle.BodyMedium.value)
        ExposedDropdownMenuBox(
            expanded = directionMenuExpanded,
            onExpandedChange = { directionMenuExpanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            DiplomTextField(
                value = directionToString(direction),
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = directionMenuExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
            )
            
            ExposedDropdownMenu(
                expanded = directionMenuExpanded,
                onDismissRequest = { directionMenuExpanded = false }
            ) {
                Direction.entries.filter { it != Direction.UNRECOGNIZED }.forEach { dir ->
                    DropdownMenuItem(
                        text = { Text(directionToString(dir)) },
                        onClick = {
                            onDirectionChange(dir)
                            directionMenuExpanded = false
                        }
                    )
                }
            }
        }
          Spacer(modifier = Modifier.height(16.dp))
        
        Text("Level", style = TextStyle.BodyMedium.value)
        ExposedDropdownMenuBox(
            expanded = levelMenuExpanded,
            onExpandedChange = { levelMenuExpanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            DiplomTextField(
                value = levelToString(level),
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = levelMenuExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
            )
            
            ExposedDropdownMenu(
                expanded = levelMenuExpanded,
                onDismissRequest = { levelMenuExpanded = false }
            ) {
                Level.entries.filter { it != Level.UNRECOGNIZED }.forEach { lvl ->
                    DropdownMenuItem(
                        text = { Text(levelToString(lvl)) },
                        onClick = {
                            onLevelChange(lvl)
                            levelMenuExpanded = false
                        }
                    )
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        
        Text("Statistics", style = TextStyle.TitleMedium.value)
        Spacer(modifier = Modifier.height(8.dp))
        
        StatisticRow("Completed Tests", "${user.completedTestsCount}")
        StatisticRow("Correct Answers", "${user.totalCorrectAnswers}")
        StatisticRow("Incorrect Answers", "${user.totalIncorrectAnswers}")
        StatisticRow("Achievements", "${user.achievementsCount}")
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onUpdateClick,
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Update Profile", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
        
        if (showAvatarDialog) {
            Dialog(onDismissRequest = { showAvatarDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
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
                            "Change Avatar",
                            style = TextStyle.TitleLarge.value,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                          com.diploma.work.ui.components.AvatarImage(
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
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Text("Choose from Gallery", color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(
                            onClick = { showAvatarDialog = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatisticRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = TextStyle.BodyMedium.value)
        Text(value, style = TextStyle.BodyMedium.value, color = MaterialTheme.colorScheme.primary)
    }
}

private fun directionToString(direction: Direction): String {
    return when (direction) {
        Direction.BACKEND -> "Backend"
        Direction.FRONTEND -> "Frontend"
        Direction.DEVOPS -> "DevOps"
        Direction.DATA_SCIENCE -> "Data Science"
        else -> "Select Direction"
    }
}

private fun levelToString(level: Level): String {
    return when (level) {
        Level.JUNIOR -> "Junior"
        Level.MIDDLE -> "Middle"
        Level.SENIOR -> "Senior"
        else -> "Select Level"
    }
}