package com.diploma.work.ui.feature.interests

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.diploma.work.R
import com.diploma.work.data.models.ArticleDirection
import com.diploma.work.data.models.ArticleTechnology
import com.diploma.work.data.models.DeliveryFrequency
import com.diploma.work.ui.components.LoadingCard
import com.diploma.work.ui.theme.TextStyle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun UserInterestsScreen(
    modifier: Modifier = Modifier,
    viewModel: UserInterestsViewModel = hiltViewModel(),
    onBack: (() -> Unit)? = null
) {
    val state by viewModel.uiState.collectAsState()
    val expandedCategories = remember { mutableStateMapOf<ArticleDirection, Boolean>() }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.interests),
                        style = TextStyle.TitleLarge.value
                    )
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.manualReload() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.refresh)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = {
            if (state.error != null) {
                Snackbar(
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text(stringResource(R.string.ok))
                        }
                    },
                    modifier = Modifier.padding(8.dp)
                ) { Text(state.error ?: stringResource(R.string.error)) }
            }
        }
    ) { padding ->
        if (state.isLoading && state.technologies.isEmpty()) {
            LoadingCard(
                message = stringResource(R.string.loading_user_interests),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                item {
                    Text(
                        text = "Выберите интересующие технологии",
                        style = TextStyle.TitleMedium.value,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Мы будем подбирать статьи и тесты на основе ваших предпочтений",
                        style = TextStyle.BodyMedium.value,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }


                viewModel.getDirections().forEach { direction ->
                    val techs = viewModel.getTechnologiesByDirection(direction)
                    if (techs.isNotEmpty()) {
                        item {
                            TechnologyCategoryCard(
                                direction = direction,
                                technologies = techs,
                                selectedTechIds = state.selectedTechnologyIds,
                                isExpanded = expandedCategories[direction] ?: false,
                                onExpandToggle = {
                                    expandedCategories[direction] = !(expandedCategories[direction] ?: false)
                                },
                                onTechnologyToggle = { viewModel.toggleTechnology(it) }
                            )
                        }
                    }
                }


                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingsCard(
                        state = state,
                        onDeliveryFrequencyChange = { viewModel.setDeliveryFrequency(it) },
                        onEmailNotificationsChange = { viewModel.setEmailNotifications(it) },
                        onPushNotificationsChange = { viewModel.setPushNotifications(it) },
                        onArticlesPerDayChange = { viewModel.setArticlesPerDay(it) }
                    )
                }


                item {
                    Button(
                        onClick = { viewModel.savePreferences() },
                        enabled = !state.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.save),
                                style = TextStyle.ButtonText.value
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = state.saveSuccess,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Настройки сохранены!",
                                    style = TextStyle.BodyMedium.value,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TechnologyCategoryCard(
    direction: ArticleDirection,
    technologies: List<ArticleTechnology>,
    selectedTechIds: Set<Long>,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onTechnologyToggle: (Long) -> Unit
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "rotation"
    )

    val selectedCount = technologies.count { selectedTechIds.contains(it.id) }

    val (categoryColor, categoryIcon) = getCategoryStyle(direction)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = categoryColor.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {

            Surface(
                onClick = onExpandToggle,
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = categoryColor.copy(alpha = 0.2f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = categoryIcon,
                                    contentDescription = null,
                                    tint = categoryColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = direction.displayName,
                                style = TextStyle.TitleMedium.value,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (selectedCount > 0) {
                                    "Выбрано: $selectedCount из ${technologies.size}"
                                } else {
                                    "${technologies.size} технологий"
                                },
                                style = TextStyle.BodySmall.value,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Свернуть" else "Развернуть",
                        modifier = Modifier.rotate(rotationAngle),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }


            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    HorizontalDivider(
                        modifier = Modifier.padding(bottom = 12.dp),
                        color = categoryColor.copy(alpha = 0.3f)
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        technologies.forEach { tech ->
                            val isSelected = selectedTechIds.contains(tech.id)
                            FilterChip(
                                selected = isSelected,
                                onClick = { onTechnologyToggle(tech.id) },
                                label = {
                                    Text(
                                        text = tech.name,
                                        style = TextStyle.BodyMedium.value
                                    )
                                },
                                leadingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                                        )
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = categoryColor.copy(alpha = 0.3f),
                                    selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                                    selectedLeadingIconColor = categoryColor
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(
    state: UserInterestsUiState,
    onDeliveryFrequencyChange: (DeliveryFrequency) -> Unit,
    onEmailNotificationsChange: (Boolean) -> Unit,
    onPushNotificationsChange: (Boolean) -> Unit,
    onArticlesPerDayChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Настройки рассылки",
                style = TextStyle.TitleMedium.value,
                color = MaterialTheme.colorScheme.onSurface
            )


            Column {
                Text(
                    text = "Частота получения статей",
                    style = TextStyle.BodyMedium.value,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DeliveryFrequency.entries
                        .filter { it != DeliveryFrequency.UNSPECIFIED }
                        .forEach { freq ->
                            FilterChip(
                                selected = state.deliveryFrequency == freq,
                                onClick = { onDeliveryFrequencyChange(freq) },
                                label = { Text(freq.displayName) }
                            )
                        }
                }
            }

            HorizontalDivider()

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Статей в день",
                        style = TextStyle.BodyMedium.value,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${state.articlesPerDay}",
                        style = TextStyle.TitleMedium.value,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = state.articlesPerDay.toFloat(),
                    onValueChange = { onArticlesPerDayChange(it.toInt()) },
                    valueRange = 1f..20f,
                    steps = 18
                )
            }

            HorizontalDivider()


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Email-уведомления",
                        style = TextStyle.BodyMedium.value,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Получать дайджест на почту",
                        style = TextStyle.BodySmall.value,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = state.emailNotifications,
                    onCheckedChange = onEmailNotificationsChange
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Push-уведомления",
                        style = TextStyle.BodyMedium.value,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Уведомления о новых статьях",
                        style = TextStyle.BodySmall.value,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = state.pushNotifications,
                    onCheckedChange = onPushNotificationsChange
                )
            }
        }
    }
}

@Composable
private fun getCategoryStyle(direction: ArticleDirection): Pair<Color, ImageVector> {
    return when (direction) {
        ArticleDirection.BACKEND -> Color(0xFF4CAF50) to Icons.Default.Code
        ArticleDirection.FRONTEND -> Color(0xFF2196F3) to Icons.Default.Web
        ArticleDirection.DEVOPS -> Color(0xFFFF9800) to Icons.Default.Cloud
        ArticleDirection.DATA_SCIENCE -> Color(0xFF9C27B0) to Icons.Default.Science
        else -> MaterialTheme.colorScheme.primary to Icons.Default.Code
    }
}
