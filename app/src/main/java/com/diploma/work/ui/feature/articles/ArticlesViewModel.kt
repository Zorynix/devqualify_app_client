package com.diploma.work.ui.feature.articles

import androidx.lifecycle.viewModelScope
import com.diploma.work.data.AppSession
import com.diploma.work.data.models.*
import com.diploma.work.data.repository.ArticlesRepository
import com.diploma.work.ui.base.BaseViewModel
import com.diploma.work.utils.Constants
import com.diploma.work.utils.ErrorHandler
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
    private val session: AppSession,
    override val errorHandler: ErrorHandler
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(ArticlesUiState())
    val uiState: StateFlow<ArticlesUiState> = _uiState.asStateFlow()

    private val tag = "ArticlesViewModel"
    private val pageSize = 50

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
                    selectedTechnologyIds = userPreferences?.technologyIds?.toSet() ?: emptySet(),
                    selectedDirections = userPreferences?.directions?.toSet() ?: emptySet()
                )
                
                Logger.d("$tag: Initial load - User preferences: ${userPreferences != null}")
                Logger.d("$tag: Initial load - Selected technology IDs: ${userPreferences?.technologyIds}")
                Logger.d("$tag: Initial load - Selected directions: ${userPreferences?.directions}")
                
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
                
                Logger.d("$tag: Loading articles with ${request.technologyIds.size} technologies and ${request.directions.size} directions")
                Logger.d("$tag: Technology IDs: ${request.technologyIds}")
                Logger.d("$tag: Directions: ${request.directions}")
                
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
      fun refreshUserPreferences() {
        viewModelScope.launch {
            Logger.d("$tag: ===== REFRESH USER PREFERENCES START =====")
            try {
                val userId = session.getUserId()
                Logger.d("$tag: User ID: $userId")
                
                if (userId != null) {
                    val cachedPreferences = session.getUserPreferences()
                    Logger.d("$tag: Cached preferences: ${cachedPreferences?.technologyIds}")
                    Logger.d("$tag: Cached directions: ${cachedPreferences?.directions}")
                    
                    val preferencesResult = articlesRepository.getUserPreferences(
                        GetUserPreferencesRequest(userId)
                    )
                    val serverPreferences = preferencesResult.getOrNull()?.preferences
                    
                    Logger.d("$tag: Server preferences: ${serverPreferences?.technologyIds}")
                    Logger.d("$tag: Server directions: ${serverPreferences?.directions}")
                    
                    if (serverPreferences != null) {
                        session.storeUserPreferences(serverPreferences)
                        Logger.d("$tag: Refreshed user preferences from server")
                        
                        _uiState.value = _uiState.value.copy(
                            userPreferences = serverPreferences,
                            selectedTechnologyIds = serverPreferences.technologyIds.toSet(),
                            selectedDirections = serverPreferences.directions.toSet()
                        )
                        
                        Logger.d("$tag: Applied server preferences to UI state")
                        loadArticles(reset = true)
                    } else {
                        if (cachedPreferences != null) {
                            _uiState.value = _uiState.value.copy(
                                userPreferences = cachedPreferences,
                                selectedTechnologyIds = cachedPreferences.technologyIds.toSet(),
                                selectedDirections = cachedPreferences.directions.toSet()
                            )
                            
                            Logger.d("$tag: Applied cached preferences to UI state")
                            loadArticles(reset = true)
                        } else {
                            Logger.w("$tag: No preferences found - neither server nor cache")
                        }
                    }
                } else {
                    Logger.w("$tag: No user ID found")
                }
            } catch (e: Exception) {
                Logger.e("$tag: Exception during preferences refresh: ${e.message}")
                val cachedPreferences = session.getUserPreferences()
                if (cachedPreferences != null) {
                    _uiState.value = _uiState.value.copy(
                        userPreferences = cachedPreferences,
                        selectedTechnologyIds = cachedPreferences.technologyIds.toSet(),
                        selectedDirections = cachedPreferences.directions.toSet()
                    )
                    Logger.d("$tag: Used cached preferences after exception")
                    loadArticles(reset = true)
                }
            }
            Logger.d("$tag: ===== REFRESH USER PREFERENCES END =====")
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
    }    fun setSortType(sortType: SortType) {
        _uiState.value = _uiState.value.copy(selectedSortType = sortType)
        loadArticles(reset = true)
    }
    
    fun setSortBy(sortType: SortType) {
        _uiState.value = _uiState.value.copy(selectedSortType = sortType)
        loadArticles(reset = true)
    }
    
    fun toggleSource(source: String) {
        loadArticles(reset = true)
    }    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            selectedDirection = null,
            searchQuery = "",
            selectedTimePeriod = TimePeriod.WEEK,
            selectedSortType = SortType.RELEVANCE
        )
        loadArticles(reset = true)
    }    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        if (query.isBlank()) {
            loadArticles(reset = true)
        }
    }

    fun applyUserPreferencesToFilters() {
        val preferences = _uiState.value.userPreferences
        if (preferences != null) {
            _uiState.value = _uiState.value.copy(
                selectedTechnologyIds = preferences.technologyIds.toSet(),
                selectedDirections = preferences.directions.toSet()
            )
            loadArticles(reset = true)
            Logger.d("$tag: Applied user preferences to filters")
            Logger.d("$tag: Applied technology IDs: ${preferences.technologyIds}")
            Logger.d("$tag: Applied directions: ${preferences.directions}")
        } else {
            Logger.w("$tag: No user preferences found to apply")
        }    }

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
