package com.diploma.work.ui.feature.userinfo

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.diploma.work.data.models.User
import com.diploma.work.grpc.Direction
import com.diploma.work.grpc.Level
import com.diploma.work.ui.DiplomTextField
import com.diploma.work.ui.theme.Text
import com.diploma.work.ui.theme.TextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInfoScreen(
    navController: NavController,
    viewModel: UserInfoViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val updateSuccess by viewModel.updateSuccess.collectAsState()
    val username by viewModel.username.collectAsState()
    val direction by viewModel.direction.collectAsState()
    val level by viewModel.level.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            snackbarHostState.showSnackbar("Profile updated successfully!")
            viewModel.resetUpdateStatus()
        }
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage ?: "An error occurred")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("User Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(50.dp)
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 32.dp)
                )
            } else if (user != null) {
                UserProfileContent(
                    user = user!!,
                    username = username,
                    direction = direction,
                    level = level,
                    onUsernameChanged = { viewModel.onUsernameChanged(it) },
                    onDirectionChanged = { viewModel.onDirectionChanged(it) },
                    onLevelChanged = { viewModel.onLevelChanged(it) },
                    onUpdateClicked = { viewModel.updateUserProfile() },
                    isLoading = isLoading
                )
            } else {
                Text(
                    "Failed to load user data. Please try again.",
                    style = TextStyle.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Button(
                    onClick = { viewModel.loadUserInfo() },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Retry")
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
    onUsernameChanged: (String) -> Unit,
    onDirectionChanged: (Direction) -> Unit,
    onLevelChanged: (Level) -> Unit,
    onUpdateClicked: () -> Unit,
    isLoading: Boolean
) {
    var directionMenuExpanded by remember { mutableStateOf(false) }
    var levelMenuExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Personal Information", style = TextStyle.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Email", style = TextStyle.bodySmall)
        Text(
            text = user.email,
            style = TextStyle.bodyMedium,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Username", style = TextStyle.bodySmall)
        DiplomTextField(
            value = username,
            onValueChange = onUsernameChanged,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Direction", style = TextStyle.bodySmall)
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
                    .menuAnchor()
            )
            
            ExposedDropdownMenu(
                expanded = directionMenuExpanded,
                onDismissRequest = { directionMenuExpanded = false }
            ) {
                Direction.values().filter { it != Direction.UNRECOGNIZED }.forEach { dir ->
                    DropdownMenuItem(
                        text = { Text(directionToString(dir)) },
                        onClick = {
                            onDirectionChanged(dir)
                            directionMenuExpanded = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Level", style = TextStyle.bodySmall)
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
                    .menuAnchor()
            )
            
            ExposedDropdownMenu(
                expanded = levelMenuExpanded,
                onDismissRequest = { levelMenuExpanded = false }
            ) {
                Level.values().filter { it != Level.UNRECOGNIZED }.forEach { lvl ->
                    DropdownMenuItem(
                        text = { Text(levelToString(lvl)) },
                        onClick = {
                            onLevelChanged(lvl)
                            levelMenuExpanded = false
                        }
                    )
                }
            }
        }
        
        Divider(modifier = Modifier.padding(vertical = 16.dp))
        
        Text("Statistics", style = TextStyle.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        
        StatisticRow("Completed Tests", "${user.completedTestsCount}")
        StatisticRow("Correct Answers", "${user.totalCorrectAnswers}")
        StatisticRow("Incorrect Answers", "${user.totalIncorrectAnswers}")
        StatisticRow("Achievements", "${user.achievementsCount}")
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onUpdateClicked,
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
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
        Text(label, style = TextStyle.bodyMedium)
        Text(value, style = TextStyle.bodyMedium, color = MaterialTheme.colorScheme.primary)
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