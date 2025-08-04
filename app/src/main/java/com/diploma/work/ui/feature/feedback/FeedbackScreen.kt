package com.diploma.work.ui.feature.feedback

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.diploma.work.R
import com.diploma.work.ui.DiplomTextField
import com.diploma.work.ui.components.ErrorCard
import com.diploma.work.ui.theme.Text
import com.diploma.work.ui.theme.TextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: FeedbackViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            kotlinx.coroutines.delay(2000)
            try {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                } else {
                    navController.navigate("Home") {
                        popUpTo("Home") { inclusive = false }
                    }
                }
            } catch (e: Exception) {
                navController.navigate("Home") {
                    popUpTo("Home") { inclusive = false }
                }
            }
        }
    }
    
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Обратная связь", style = TextStyle.TitleLarge.value) },
                navigationIcon = {
                    IconButton(onClick = { 
                        try {
                            if (navController.previousBackStackEntry != null) {
                                navController.popBackStack()
                            } else {
                                navController.navigate("Home") {
                                    popUpTo("Home") { inclusive = false }
                                }
                            }
                        } catch (e: Exception) {
                            navController.navigate("Home") {
                                popUpTo("Home") { inclusive = false }
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isSuccess) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Сообщение отправлено!",
                        style = TextStyle.TitleMedium.value,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = uiState.successMessage.ifEmpty { "Спасибо за обратную связь! Мы рассмотрим ваше сообщение." },
                        style = TextStyle.BodyMedium.value,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Опишите проблему или предложение. Мы внимательно рассмотрим ваше сообщение.",
                        style = TextStyle.BodyMedium.value,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "Тема сообщения *",
                        style = TextStyle.LabelMedium.value,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    DiplomTextField(
                        value = uiState.subject,
                        onValueChange = { viewModel.onSubjectChanged(it) },
                        placeholder = { Text("Кратко опишите тему") },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Text(
                        text = "Описание проблемы *",
                        style = TextStyle.LabelMedium.value,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    DiplomTextField(
                        value = uiState.body,
                        onValueChange = { viewModel.onBodyChanged(it) },
                        placeholder = { Text("Подробно опишите проблему или предложение") },
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        singleLine = false,
                        maxLines = 10
                    )
                    
                    if (uiState.error != null) {
                        ErrorCard(
                            error = uiState.error!!,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = { viewModel.sendFeedback() },
                        enabled = !uiState.isLoading && uiState.subject.isNotBlank() && uiState.body.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Отправить",
                                style = TextStyle.LabelLarge.value,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                }
            }
        }
    }
}
