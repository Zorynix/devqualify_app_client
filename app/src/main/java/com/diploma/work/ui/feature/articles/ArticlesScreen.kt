package com.diploma.work.ui.feature.articles

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diploma.work.R
import com.diploma.work.data.models.Article
import com.diploma.work.data.models.ArticleDirection
import com.diploma.work.ui.components.ErrorCard
import com.diploma.work.ui.components.LoadingCard
import com.orhanobut.logger.Logger
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticlesScreen(
    modifier: Modifier = Modifier,
    onOpenDrawer: () -> Unit = {},
    viewModel: ArticlesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showFilters by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.refreshUserPreferences()
    }
    
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.articles)) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.open_menu))
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = stringResource(R.string.filter)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::setSearchQuery,
                onSearch = { viewModel.loadArticles() },
                placeholder = stringResource(R.string.search_articles)
            )
            
            if (showFilters) {
                FiltersPanel(
                    uiState = uiState,
                    onTimePeriodChange = viewModel::setTimePeriod,
                    onSortByChange = viewModel::setSortBy,
                    onSourceChange = viewModel::toggleSource,
                    onClearFilters = viewModel::clearFilters,
                    onApplyUserPreferences = viewModel::applyUserPreferencesToFilters
                )
            }
            
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.refreshArticles() },
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    uiState.isLoading && uiState.articles.isEmpty() -> {
                        LoadingCard(
                            message = stringResource(R.string.loading_articles),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    uiState.error != null -> {
                        ErrorCard(
                            error = uiState.error!!,
                            onRetry = { viewModel.loadArticles() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    uiState.articles.isEmpty() -> {
                        EmptyStateCard()
                    }
                    else -> {
                        ArticlesList(
                            articles = if (uiState.searchQuery.isNotBlank()) uiState.filteredArticles else uiState.articles,
                            onArticleClick = { article -> 
                                viewModel.markArticleAsViewed(article.id)
                            },
                            onArticleLike = { article -> viewModel.likeArticle(article.id) },
                            onArticleDislike = { article -> viewModel.dislikeArticle(article.id) },
                            onRemoveLikeDislike = { article -> viewModel.removeLikeDislike(article.id) },
                            onLoadMore = { viewModel.loadMoreArticles() },
                            isLoading = uiState.isLoading,
                            hasMore = uiState.hasMore
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    placeholder: String
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search_desc))
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.clear_desc))
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        singleLine = true
    )
}

@Composable
private fun FiltersPanel(
    uiState: ArticlesUiState,
    onTimePeriodChange: (TimePeriod) -> Unit,
    onSortByChange: (SortType) -> Unit,
    onSourceChange: (String) -> Unit,
    onClearFilters: () -> Unit,
    onApplyUserPreferences: () -> Unit
){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.filters),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row {                    TextButton(onClick = onApplyUserPreferences) {
                        Text(stringResource(R.string.my_interests))
                    }
                    TextButton(onClick = onClearFilters) {
                        Text(stringResource(R.string.clear_all))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            FilterSection(title = stringResource(R.string.time_period)) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(TimePeriod.entries) { period ->
                        FilterChip(
                            selected = uiState.selectedTimePeriod == period,
                            onClick = { onTimePeriodChange(period) },
                            label = { Text(period.getDisplayName()) }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            FilterSection(title = stringResource(R.string.sort_by)) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(SortType.entries) { sortType ->
                        FilterChip(
                            selected = uiState.selectedSortType == sortType,
                            onClick = { onSortByChange(sortType) },
                            label = { Text(sortType.getDisplayName()) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun ArticlesList(
    articles: List<Article>,
    onArticleClick: (Article) -> Unit,
    onArticleLike: (Article) -> Unit,
    onArticleDislike: (Article) -> Unit,
    onRemoveLikeDislike: (Article) -> Unit,
    onLoadMore: () -> Unit = {},
    isLoading: Boolean = false,
    hasMore: Boolean = true
) {
    val listState = rememberLazyListState()
    
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .collect { visibleItems ->
                val lastVisibleItem = visibleItems.lastOrNull()
                if (lastVisibleItem != null && 
                    lastVisibleItem.index >= articles.size - 5 &&
                    hasMore && 
                    !isLoading) {
                    onLoadMore()
                }
            }
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(articles) { article ->
            ArticleCard(
                article = article,
                onClick = { onArticleClick(article) },
                onLike = { onArticleLike(article) },
                onDislike = { onArticleDislike(article) },
                onRemoveLikeDislike = { onRemoveLikeDislike(article) }
            )
        }
        
        if (isLoading && articles.isNotEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun ArticleCard(
    article: Article,
    onClick: () -> Unit,
    onLike: () -> Unit,
    onDislike: () -> Unit,
    onRemoveLikeDislike: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    var showDislikeConfirm by remember { mutableStateOf(false) }
    
    Logger.d("ArticleCard: title='${article.title}', rssSourceName='${article.rssSourceName}'")

    val accentColor = remember(article.rssSourceName) {
        val hash = article.rssSourceName.hashCode()
        val colors = listOf(
            Color(0xFF4CAF50), // Green
            Color(0xFF2196F3), // Blue
            Color(0xFFFF9800), // Orange
            Color(0xFF9C27B0), // Purple
            Color(0xFFE91E63), // Pink
            Color(0xFF00BCD4), // Cyan
            Color(0xFFFF5722), // Deep Orange
            Color(0xFF3F51B5)  // Indigo
        )
        colors[kotlin.math.abs(hash) % colors.size]
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                onClick()
                if (article.url.isNotEmpty()) {
                    uriHandler.openUri(article.url)
                }
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(IntrinsicSize.Max)
                    .background(accentColor)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (article.rssSourceName.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = accentColor.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = article.rssSourceName,
                                style = MaterialTheme.typography.labelMedium,
                                color = accentColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (article.isViewed) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Просмотрено",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = article.publishedAt
                                .atZone(ZoneId.systemDefault())
                                .format(DateTimeFormatter.ofPattern("dd MMM")),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))


                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )


                if (article.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = article.description,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (article.readTimeMinutes > 0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "${article.readTimeMinutes} мин",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }


                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Surface(
                            onClick = {
                                if (article.isLiked == true) {
                                    onRemoveLikeDislike()
                                } else {
                                    onLike()
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (article.isLiked == true)
                                Color(0xFF4CAF50).copy(alpha = 0.15f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ThumbUp,
                                contentDescription = "Лайк",
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(18.dp),
                                tint = if (article.isLiked == true)
                                    Color(0xFF2E7D32)
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }


                        Surface(
                            onClick = {
                                if (article.isLiked == false) {
                                    onRemoveLikeDislike()
                                } else {
                                    showDislikeConfirm = true
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (article.isLiked == false)
                                Color(0xFFF44336).copy(alpha = 0.15f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ThumbDown,
                                contentDescription = "Дизлайк",
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(18.dp),
                                tint = if (article.isLiked == false)
                                    Color(0xFFC62828)
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
    
    if (showDislikeConfirm) {
        AlertDialog(
            onDismissRequest = { showDislikeConfirm = false },
            title = { Text("Скрыть статью?") },
            text = { Text("Эта статья будет скрыта из списка и больше не будет показываться. Вы уверены?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDislikeConfirm = false
                        onDislike()
                    }
                ) {
                    Text("Да, скрыть")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDislikeConfirm = false }
                ) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
private fun EmptyStateCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Article,
                contentDescription = stringResource(R.string.no_articles_desc),
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.no_articles_found),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.no_articles_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TimePeriod.getDisplayName(): String = when (this) {
    TimePeriod.DAY -> stringResource(R.string.time_24_hours)
    TimePeriod.WEEK -> stringResource(R.string.time_week)
    TimePeriod.MONTH -> stringResource(R.string.time_month)
    TimePeriod.QUARTER -> stringResource(R.string.time_3_months)
    TimePeriod.YEAR -> stringResource(R.string.time_year)
    TimePeriod.ALL -> stringResource(R.string.time_all)
}

@Composable
private fun SortType.getDisplayName(): String = when (this) {
    SortType.RELEVANCE -> stringResource(R.string.sort_relevance)
    SortType.DATE_DESC -> stringResource(R.string.sort_newest)
    SortType.DATE_ASC -> stringResource(R.string.sort_oldest)
    SortType.RATING_DESC -> stringResource(R.string.sort_highest_rated)
    SortType.RATING_ASC -> stringResource(R.string.sort_lowest_rated)
}

private val ArticleDirection.displayName: String
    get() = when (this) {
        ArticleDirection.BACKEND -> "Backend"
        ArticleDirection.FRONTEND -> "Frontend"
        ArticleDirection.DEVOPS -> "DevOps"
        ArticleDirection.DATA_SCIENCE -> "Data Science"
        ArticleDirection.UNSPECIFIED -> "All"
    }
