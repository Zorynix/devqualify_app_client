package com.diploma.work.data.models

import java.time.Instant

data class ArticleTechnology(
    val id: Long,
    val name: String,
    val description: String,
    val direction: ArticleDirection,
    val iconUrl: String
)

data class Article(
    val id: Long,
    val title: String,
    val description: String,
    val content: String,
    val url: String,
    val author: String,
    val publishedAt: Instant,
    val createdAt: Instant,
    val rssSourceId: Long,
    val rssSourceName: String,
    val technologyIds: List<Long>,
    val tags: List<String>,
    val status: ArticleStatus,
    val imageUrl: String,
    val readTimeMinutes: Int
)

data class UserPreferences(
    val userId: Long,
    val technologyIds: List<Long>,
    val directions: List<ArticleDirection>,
    val deliveryFrequency: DeliveryFrequency,
    val emailNotifications: Boolean,
    val pushNotifications: Boolean,
    val excludedSources: List<String>,
    val articlesPerDay: Int,
    val updatedAt: Instant
)

data class ArticleRecommendation(
    val article: Article,
    val relevanceScore: Float,
    val recommendationReason: String,
    val matchedTechnologies: List<String>
)

enum class ArticleDirection {
    UNSPECIFIED,
    BACKEND,
    FRONTEND,
    DEVOPS,
    DATA_SCIENCE;

    val displayName: String
        get() = when (this) {
            UNSPECIFIED -> "All"
            BACKEND -> "Backend"
            FRONTEND -> "Frontend"
            DEVOPS -> "DevOps"
            DATA_SCIENCE -> "Data Science"
        }
}

enum class ArticleStatus {
    UNSPECIFIED,
    PENDING,
    PUBLISHED,
    ARCHIVED
}

enum class DeliveryFrequency {
    UNSPECIFIED,
    DAILY,
    WEEKLY,
    MONTHLY;

    val displayName: String
        get() = when (this) {
            UNSPECIFIED -> "Не указано"
            DAILY -> "Ежедневно"
            WEEKLY -> "Еженедельно"
            MONTHLY -> "Ежемесячно"
        }
}

data class GetTechnologiesRequest(
    val direction: ArticleDirection? = null,
    val pageSize: Int = 50,
    val pageToken: String = ""
)

data class GetTechnologiesResponse(
    val technologies: List<ArticleTechnology>,
    val nextPageToken: String
)

data class GetUserPreferencesRequest(
    val userId: Long
)

data class GetUserPreferencesResponse(
    val preferences: UserPreferences?
)

data class UpdateUserPreferencesRequest(
    val userId: Long,
    val technologyIds: List<Long>,
    val directions: List<ArticleDirection>,
    val deliveryFrequency: DeliveryFrequency,
    val emailNotifications: Boolean,
    val pushNotifications: Boolean,
    val excludedSources: List<String>,
    val articlesPerDay: Int
)

data class UpdateUserPreferencesResponse(
    val message: String
)

data class GetArticlesRequest(
    val technologyIds: List<Long> = emptyList(),
    val directions: List<ArticleDirection> = emptyList(),
    val rssSourceId: Long? = null,
    val status: ArticleStatus = ArticleStatus.PUBLISHED,
    val fromDate: Instant? = null,
    val toDate: Instant? = null,
    val pageSize: Int = 20,
    val pageToken: String = "",
    val sortBy: String = "published_at", // "published_at", "relevance", "created_at"
    val sortOrder: String = "desc" // "asc", "desc"
)

data class GetArticlesResponse(
    val articles: List<Article>,
    val nextPageToken: String,
    val totalCount: Int
)

data class GetRecommendedArticlesRequest(
    val userId: Long,
    val limit: Int = 10,
    val fromDate: Instant? = null
)

data class GetRecommendedArticlesResponse(
    val recommendations: List<ArticleRecommendation>
)

data class SearchArticlesRequest(
    val query: String,
    val technologyIds: List<Long> = emptyList(),
    val directions: List<ArticleDirection> = emptyList(),
    val fromDate: Instant? = null,
    val toDate: Instant? = null,
    val pageSize: Int = 20,
    val pageToken: String = ""
)

data class SearchArticlesResponse(
    val articles: List<Article>,
    val nextPageToken: String,
    val totalCount: Int
)

data class ArticlePagination(
    val pageSize: Int = 20,
    val pageToken: String = ""
)
