package com.diploma.work.ui.feature.history

import androidx.lifecycle.viewModelScope
import com.diploma.work.data.AppSession
import com.diploma.work.data.models.*
import com.diploma.work.ui.base.BaseViewModel
import com.diploma.work.utils.ErrorHandler
import com.orhanobut.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val session: AppSession,
    override val errorHandler: ErrorHandler
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val tag = "HistoryViewModel"

    init {
        loadHistoryData()
    }

    fun setSelectedTab(tab: HistoryTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun loadHistoryData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val viewedIds = session.getViewedArticles()
                val likedIds = session.getLikedArticles()
                val dislikedIds = session.getDislikedArticles()

                Logger.d("$tag: Loading history - viewed: ${viewedIds.size}, liked: ${likedIds.size}, disliked: ${dislikedIds.size}")

                val cachedArticles = session.getAllCachedArticles()
                Logger.d("$tag: Found ${cachedArticles.size} articles in cache")
                
                val articlesMap = cachedArticles.associateBy { it.id }
                
                val viewedArticles = viewedIds.mapNotNull { id ->
                    articlesMap[id]?.copy(
                        isViewed = true,
                        isLiked = session.getArticleLikeStatus(id)
                    )
                }.sortedByDescending { it.publishedAt }
                
                val likedArticles = likedIds.mapNotNull { id ->
                    articlesMap[id]?.copy(
                        isViewed = session.isArticleViewed(id),
                        isLiked = true
                    )
                }.sortedByDescending { it.publishedAt }
                
                val dislikedArticles = dislikedIds.mapNotNull { id ->
                    articlesMap[id]?.copy(
                        isViewed = session.isArticleViewed(id),
                        isLiked = false,
                        isHidden = true
                    )
                }.sortedByDescending { it.publishedAt }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    viewedArticles = viewedArticles,
                    likedArticles = likedArticles,
                    dislikedArticles = dislikedArticles
                )
                
                Logger.d("$tag: Loaded ${viewedArticles.size} viewed, ${likedArticles.size} liked, ${dislikedArticles.size} disliked articles from cache")
                
            } catch (e: Exception) {
                Logger.e("$tag: Exception during history load: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun restoreDislikedArticle(articleId: Long) {
        session.removeLikeDislike(articleId)
        loadHistoryData()
        Logger.d("$tag: Restored disliked article $articleId")
    }

    fun removeFromLiked(articleId: Long) {
        session.removeLikeDislike(articleId)
        loadHistoryData()
        Logger.d("$tag: Removed article $articleId from liked")
    }

    fun getCurrentArticles(): List<Article> {
        val currentState = _uiState.value
        return when (currentState.selectedTab) {
            HistoryTab.VIEWED -> currentState.viewedArticles
            HistoryTab.LIKED -> currentState.likedArticles
            HistoryTab.DISLIKED -> currentState.dislikedArticles
        }
    }

    override fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
