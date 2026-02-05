package com.diploma.work.ui.feature.history

import com.diploma.work.data.models.Article

data class HistoryUiState(
    val isLoading: Boolean = false,
    val selectedTab: HistoryTab = HistoryTab.VIEWED,
    val viewedArticles: List<Article> = emptyList(),
    val likedArticles: List<Article> = emptyList(),
    val dislikedArticles: List<Article> = emptyList(),
    val error: String? = null
)

enum class HistoryTab(val displayName: String) {
    VIEWED("Просмотры"),
    LIKED("Лайки"),
    DISLIKED("Дизлайки")
}
