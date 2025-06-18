package com.diploma.work.ui.feature.interests

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.diploma.work.data.models.ArticleDirection
import com.diploma.work.data.models.DeliveryFrequency
import com.diploma.work.ui.theme.TextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInterestsScreen(
    viewModel: UserInterestsViewModel = hiltViewModel(),
    onBack: (() -> Unit)? = null
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Интересы", style = TextStyle.TitleLarge.value) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                        }
                    }
                }
            )
        },
        snackbarHost = {
            if (state.error != null) {
                Snackbar(
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("ОК")
                        }
                    },
                    modifier = Modifier.padding(8.dp)
                ) { Text(state.error ?: "Ошибка") }
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("Выберите интересующие технологии:", style = TextStyle.BodyLarge.value)
                }
                viewModel.getDirections().forEach { direction ->
                    val techs = viewModel.getTechnologiesByDirection(direction)
                    if (techs.isNotEmpty()) {
                        item {
                            Text(direction.name, style = TextStyle.TitleMedium.value, modifier = Modifier.padding(top = 8.dp))
                        }
                        items(techs) { tech ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.toggleTechnology(tech.id) }
                                    .background(
                                        if (state.selectedTechnologyIds.contains(tech.id)) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        else Color.Transparent
                                    )
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = state.selectedTechnologyIds.contains(tech.id),
                                    onCheckedChange = { viewModel.toggleTechnology(tech.id) }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(tech.name, style = TextStyle.BodyLarge.value)
                            }
                        }
                    }
                }
                item {
                    Spacer(Modifier.height(16.dp))
                    Text("Частота получения статей:", style = TextStyle.BodyLarge.value)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DeliveryFrequency.entries.filter { it != DeliveryFrequency.UNSPECIFIED }.forEach { freq ->
                            FilterChip(
                                selected = state.deliveryFrequency == freq,
                                onClick = { viewModel.setDeliveryFrequency(freq) },
                                label = { Text(freq.name.lowercase().replaceFirstChar { it.uppercase() }) }
                            )
                        }
                    }
                }
                item {
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = state.emailNotifications,
                            onCheckedChange = { viewModel.setEmailNotifications(it) }
                        )
                        Text("Email-уведомления", style = TextStyle.BodyLarge.value)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = state.pushNotifications,
                            onCheckedChange = { viewModel.setPushNotifications(it) }
                        )
                        Text("Push-уведомления", style = TextStyle.BodyLarge.value)
                    }
                }
                item {
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Статей в день:", style = TextStyle.BodyLarge.value)
                        Spacer(Modifier.width(8.dp))
                        OutlinedTextField(
                            value = state.articlesPerDay.toString(),
                            onValueChange = { value ->
                                value.toIntOrNull()?.let { viewModel.setArticlesPerDay(it) }
                            },
                            singleLine = true,
                            modifier = Modifier.width(80.dp)
                        )
                    }
                }
                item {
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.savePreferences() },
                        enabled = !state.isLoading
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(Modifier.size(20.dp))
                        } else {
                            Text("Сохранить")
                        }
                    }
                    if (state.saveSuccess) {
                        Text("Сохранено!", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }
    }
}
