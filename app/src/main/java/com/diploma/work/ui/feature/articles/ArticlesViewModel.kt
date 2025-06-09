package com.diploma.work.ui.feature.articles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diploma.work.data.AppSession
import com.diploma.work.data.models.*
import com.diploma.work.data.repository.ArticlesRepository
import com.orhanobut.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class ArticlesUiState(
    val isLoading: Boolean = false,
    val articles: List<Article> = emptyList(),
    val filteredArticles: List<Article> = emptyList(),
    val isRefreshing: Boolean = false,
    val hasMore: Boolean = true,
    val currentPage: Int = 0,
    val error: String? = null,
    
    val selectedTimePeriod: TimePeriod = TimePeriod.WEEK,
    val selectedSortType: SortType = SortType.RELEVANCE,
    val selectedDirection: ArticleDirection? = null,
    val selectedDirections: Set<ArticleDirection> = emptySet(),
    val selectedTechnologyIds: Set<Long> = emptySet(),
    val searchQuery: String = "",    val isFiltersExpanded: Boolean = false,
    
    val technologies: List<ArticleTechnology> = emptyList(),
    val userPreferences: UserPreferences? = null
)

enum class TimePeriod(val displayName: String, val days: Int) {
    DAY("Today", 1),
    WEEK("This Week", 7),
    MONTH("This Month", 30),
    QUARTER("3 Months", 90),
    YEAR("This Year", 365),
    ALL("All Time", -1)
}

enum class SortType(val displayName: String) {
    RELEVANCE("Relevance"),
    DATE_DESC("Newest First"),
    DATE_ASC("Oldest First"),
    RATING_DESC("Highest Rated"),
    RATING_ASC("Lowest Rated")
}

@HiltViewModel
class ArticlesViewModel @Inject constructor(
    private val articlesRepository: ArticlesRepository,
    private val session: AppSession
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArticlesUiState())
    val uiState: StateFlow<ArticlesUiState> = _uiState.asStateFlow()

    private val tag = "ArticlesViewModel"
    private val pageSize = 20

    init {
        loadInitialData()
    }    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val technologiesResult = articlesRepository.getTechnologies(
                    GetTechnologiesRequest(pageSize = 100)
                )
                
                val userId = session.getUserId()
                var userPreferences: UserPreferences? = null
                
                val cachedPreferences = session.getUserPreferences()
                
                if (cachedPreferences != null) {
                    userPreferences = cachedPreferences
                    Logger.d("$tag: Loaded user preferences from cache")
                } else if (userId != null) {
                    val preferencesResult = articlesRepository.getUserPreferences(
                        GetUserPreferencesRequest(userId)
                    )
                    userPreferences = preferencesResult.getOrNull()?.preferences
                    
                    if (userPreferences != null) {
                        session.storeUserPreferences(userPreferences)
                        Logger.d("$tag: Loaded user preferences from server and cached")
                    }
                }
                
                val technologies = technologiesResult.getOrNull()?.technologies ?: emptyList()
                
                _uiState.value = _uiState.value.copy(
                    technologies = technologies,
                    userPreferences = userPreferences,
                    selectedTechnologyIds = userPreferences?.technologyIds?.toSet() ?: emptySet()
                )
                
                loadArticles(reset = true)
                
            } catch (e: Exception) {
                Logger.e("$tag: Exception during initial load: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load data"
                )
            }
        }
    }

    fun loadArticles(reset: Boolean = false) {
        viewModelScope.launch {
            if (reset) {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    currentPage = 0,
                    hasMore = true,
                    error = null
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            }
            
            try {                val currentState = _uiState.value
                val page = if (reset) 0 else currentState.currentPage + 1
                
                val (fromDate, toDate) = calculateDateRange(currentState.selectedTimePeriod)
                
                val request = GetArticlesRequest(
                    technologyIds = currentState.selectedTechnologyIds.toList(),
                    directions = currentState.selectedDirections.toList(),
                    fromDate = fromDate,
                    toDate = toDate,
                    sortBy = mapSortTypeToProto(currentState.selectedSortType),
                    sortOrder = getSortOrder(currentState.selectedSortType),
                    pageSize = pageSize,
                    pageToken = if (reset) "" else generatePageToken(page)
                )
                
                val result = articlesRepository.getArticles(request)
                if (result.isSuccess) {
                    val response = result.getOrNull()
                    val newArticles = response?.articles ?: emptyList()
                    
                    val updatedArticles = if (reset) {
                        newArticles
                    } else {
                        currentState.articles + newArticles
                    }
                    
                    val filteredArticles = if (currentState.searchQuery.isNotBlank()) {
                        applySearchFilter(updatedArticles, currentState.searchQuery)
                    } else {
                        updatedArticles
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        articles = updatedArticles,
                        filteredArticles = filteredArticles,
                        currentPage = page,
                        hasMore = newArticles.size == pageSize
                    )
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Failed to load articles"
                    Logger.e("$tag: $error")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error
                    )
                }
            } catch (e: Exception) {
                Logger.e("$tag: Exception during articles load: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun refreshArticles() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            loadArticles(reset = true)
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }

    fun loadMoreArticles() {
        if (!_uiState.value.isLoading && _uiState.value.hasMore) {
            loadArticles(reset = false)
        }
    }

    fun setTimePeriod(timePeriod: TimePeriod) {
        _uiState.value = _uiState.value.copy(selectedTimePeriod = timePeriod)
        loadArticles(reset = true)
    }

    fun setSortType(sortType: SortType) {
        _uiState.value = _uiState.value.copy(selectedSortType = sortType)
        loadArticles(reset = true)
    }    fun setDirection(direction: ArticleDirection?) {
        _uiState.value = _uiState.value.copy(selectedDirection = direction)
        loadArticles(reset = true)
    }
    
    fun toggleDirection(direction: ArticleDirection) {
        val currentDirections = _uiState.value.selectedDirections.toMutableSet()
        if (currentDirections.contains(direction)) {
            currentDirections.remove(direction)
        } else {
            currentDirections.add(direction)
        }
        _uiState.value = _uiState.value.copy(selectedDirections = currentDirections)
        loadArticles(reset = true)
    }
    
    fun setSortBy(sortType: SortType) {
        _uiState.value = _uiState.value.copy(selectedSortType = sortType)
        loadArticles(reset = true)
    }
    
    fun toggleSource(source: String) {
        // Implementation for source filtering if needed
        loadArticles(reset = true)
    }
    
    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            selectedDirection = null,
            selectedDirections = emptySet(),
            selectedTechnologyIds = emptySet(),
            searchQuery = "",
            selectedTimePeriod = TimePeriod.WEEK,
            selectedSortType = SortType.RELEVANCE
        )
        loadArticles(reset = true)
    }
    
    fun hasActiveFilters(): Boolean {
        val state = _uiState.value
        return state.selectedDirection != null ||
                state.selectedDirections.isNotEmpty() ||
                state.selectedTechnologyIds.isNotEmpty() ||
                state.searchQuery.isNotBlank() ||
                state.selectedTimePeriod != TimePeriod.WEEK ||
                state.selectedSortType != SortType.RELEVANCE
    }
      fun toggleTechnology(technologyId: Long) {
        val currentSelected = _uiState.value.selectedTechnologyIds.toMutableSet()
        if (currentSelected.contains(technologyId)) {
            currentSelected.remove(technologyId)
        } else {
            currentSelected.add(technologyId)
        }
        _uiState.value = _uiState.value.copy(selectedTechnologyIds = currentSelected)
        loadArticles(reset = true)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        if (query.isBlank()) {
            loadArticles(reset = true)
        }
    }

    fun performSearch() {
        loadArticles(reset = true)
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(searchQuery = "")
        loadArticles(reset = true)
    }

    fun toggleFiltersExpanded() {
        _uiState.value = _uiState.value.copy(
            isFiltersExpanded = !_uiState.value.isFiltersExpanded
        )
    }

    fun clearAllFilters() {
        _uiState.value = _uiState.value.copy(
            selectedDirection = null,
            selectedTechnologyIds = emptySet(),
            searchQuery = "",
            selectedTimePeriod = TimePeriod.WEEK,
            selectedSortType = SortType.RELEVANCE
        )
        loadArticles(reset = true)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun getTechnologiesByDirection(direction: ArticleDirection): List<ArticleTechnology> {
        return _uiState.value.technologies.filter { it.direction == direction }
    }    fun getDirections(): List<ArticleDirection> {
        return ArticleDirection.entries.filter { it != ArticleDirection.UNSPECIFIED }
    }

    private fun mapTimePeriodToProto(timePeriod: TimePeriod): Int {
        return when (timePeriod) {
            TimePeriod.DAY -> 1
            TimePeriod.WEEK -> 7
            TimePeriod.MONTH -> 30
            TimePeriod.QUARTER -> 90
            TimePeriod.YEAR -> 365
            TimePeriod.ALL -> -1
        }
    }

    private fun calculateDateRange(timePeriod: TimePeriod): Pair<Instant?, Instant?> {
        val now = Instant.now()
        return when (timePeriod) {
            TimePeriod.DAY -> {
                val oneDayAgo = now.minusSeconds(24 * 60 * 60)
                Pair(oneDayAgo, now)
            }
            TimePeriod.WEEK -> {
                val oneWeekAgo = now.minusSeconds(7 * 24 * 60 * 60)
                Pair(oneWeekAgo, now)
            }
            TimePeriod.MONTH -> {
                val oneMonthAgo = now.minusSeconds(30L * 24 * 60 * 60)
                Pair(oneMonthAgo, now)
            }
            TimePeriod.QUARTER -> {
                val threeMonthsAgo = now.minusSeconds(90L * 24 * 60 * 60)
                Pair(threeMonthsAgo, now)
            }
            TimePeriod.YEAR -> {
                val oneYearAgo = now.minusSeconds(365L * 24 * 60 * 60)
                Pair(oneYearAgo, now)
            }
            TimePeriod.ALL -> Pair(null, null)
        }
    }

    private fun generatePageToken(page: Int): String {
        return if (page == 0) "" else page.toString()
    }

    private fun getSortOrder(sortType: SortType): String {
        return when (sortType) {
            SortType.DATE_DESC, SortType.RATING_DESC -> "desc"
            SortType.DATE_ASC, SortType.RATING_ASC -> "asc"
            SortType.RELEVANCE -> "desc"
        }
    }

    private fun mapSortTypeToProto(sortType: SortType): String {
        return when (sortType) {
            SortType.RELEVANCE -> "relevance"
            SortType.DATE_DESC, SortType.DATE_ASC -> "published_at"
            SortType.RATING_DESC, SortType.RATING_ASC -> "relevance"
        }
    }
    
    private fun applySearchFilter(articles: List<Article>, query: String): List<Article> {
        if (query.isBlank()) return articles
        
        val lowercaseQuery = query.lowercase()
        return articles.filter { article ->
            article.title.lowercase().contains(lowercaseQuery) ||
            article.description.lowercase().contains(lowercaseQuery) ||
            article.content.lowercase().contains(lowercaseQuery) ||
            article.author.lowercase().contains(lowercaseQuery) ||
            article.tags.any { it.lowercase().contains(lowercaseQuery) }
        }
    }
}
