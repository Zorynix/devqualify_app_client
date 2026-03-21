package com.diploma.work.data.api.dto

import com.diploma.work.data.models.Article
import com.diploma.work.data.models.ArticleDirection
import com.diploma.work.data.models.ArticleRecommendation
import com.diploma.work.data.models.ArticleStatus
import com.diploma.work.data.models.ArticleTechnology
import com.diploma.work.data.models.DeliveryFrequency
import com.diploma.work.data.models.GetArticlesResponse
import com.diploma.work.data.models.GetRecommendedArticlesResponse
import com.diploma.work.data.models.GetTechnologiesResponse
import com.diploma.work.data.models.GetUserPreferencesResponse
import com.diploma.work.data.models.SearchArticlesResponse
import com.diploma.work.data.models.UpdateUserPreferencesResponse
import com.diploma.work.data.models.UserPreferences
import java.time.Instant

data class GetArticleTechnologiesDto(
    val technologies: List<ArticleTechnologyDto> = emptyList(),
    val nextPageToken: String = ""
) {
    fun toModel(): GetTechnologiesResponse = GetTechnologiesResponse(
        technologies = technologies.map { it.toModel() },
        nextPageToken = nextPageToken
    )
}

data class ArticleTechnologyDto(
    val id: Long = 0,
    val name: String = "",
    val description: String = "",
    val direction: String = "",
    val iconUrl: String = ""
) {
    fun toModel(): ArticleTechnology = ArticleTechnology(
        id = id,
        name = name,
        description = description,
        direction = parseArticleDirection(direction),
        iconUrl = iconUrl
    )
}

data class ArticleDto(
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val content: String = "",
    val url: String = "",
    val author: String = "",
    val publishedAt: Instant? = null,
    val createdAt: Instant? = null,
    val rssSourceId: Long = 0,
    val rssSourceName: String = "",
    val technologyIds: List<Long> = emptyList(),
    val tags: List<String> = emptyList(),
    val status: String = "",
    val imageUrl: String = "",
    val readTimeMinutes: Int = 0
) {
    fun toModel(): Article = Article(
        id = id,
        title = title,
        description = description,
        content = content,
        url = url,
        author = author,
        publishedAt = publishedAt ?: Instant.EPOCH,
        createdAt = createdAt ?: Instant.EPOCH,
        rssSourceId = rssSourceId,
        rssSourceName = rssSourceName,
        technologyIds = technologyIds,
        tags = tags,
        status = parseArticleStatus(status),
        imageUrl = imageUrl,
        readTimeMinutes = readTimeMinutes
    )
}

data class GetArticlesDto(
    val articles: List<ArticleDto> = emptyList(),
    val nextPageToken: String = "",
    val totalCount: Int = 0
) {
    fun toModel(): GetArticlesResponse = GetArticlesResponse(
        articles = articles.map { it.toModel() },
        nextPageToken = nextPageToken,
        totalCount = totalCount
    )
}

data class SearchArticlesDto(
    val articles: List<ArticleDto> = emptyList(),
    val nextPageToken: String = "",
    val totalCount: Int = 0
) {
    fun toModel(): SearchArticlesResponse = SearchArticlesResponse(
        articles = articles.map { it.toModel() },
        nextPageToken = nextPageToken,
        totalCount = totalCount
    )
}

data class ArticleRecommendationDto(
    val article: ArticleDto = ArticleDto(),
    val relevanceScore: Float = 0f,
    val recommendationReason: String = "",
    val matchedTechnologies: List<String> = emptyList()
) {
    fun toModel(): ArticleRecommendation = ArticleRecommendation(
        article = article.toModel(),
        relevanceScore = relevanceScore,
        recommendationReason = recommendationReason,
        matchedTechnologies = matchedTechnologies
    )
}

data class GetRecommendedArticlesDto(
    val recommendations: List<ArticleRecommendationDto> = emptyList()
) {
    fun toModel(): GetRecommendedArticlesResponse = GetRecommendedArticlesResponse(
        recommendations = recommendations.map { it.toModel() }
    )
}

data class UserPreferencesDto(
    val userId: Long = 0,
    val technologyIds: List<Long> = emptyList(),
    val directions: List<String> = emptyList(),
    val deliveryFrequency: String = "",
    val emailNotifications: Boolean = false,
    val pushNotifications: Boolean = false,
    val excludedSources: List<String> = emptyList(),
    val articlesPerDay: Int = 0,
    val updatedAt: Instant? = null
) {
    fun toModel(): UserPreferences = UserPreferences(
        userId = userId,
        technologyIds = technologyIds,
        directions = directions.map { parseArticleDirection(it) },
        deliveryFrequency = parseDeliveryFrequency(deliveryFrequency),
        emailNotifications = emailNotifications,
        pushNotifications = pushNotifications,
        excludedSources = excludedSources,
        articlesPerDay = articlesPerDay,
        updatedAt = updatedAt ?: Instant.EPOCH
    )
}

data class GetUserPreferencesDto(
    val preferences: UserPreferencesDto? = null
) {
    fun toModel(): GetUserPreferencesResponse = GetUserPreferencesResponse(
        preferences = preferences?.toModel()
    )
}

data class UpdateUserPreferencesDto(
    val message: String = ""
) {
    fun toModel(): UpdateUserPreferencesResponse = UpdateUserPreferencesResponse(
        message = message
    )
}

private fun parseArticleDirection(value: String): ArticleDirection = when (value) {
    "BACKEND" -> ArticleDirection.BACKEND
    "FRONTEND" -> ArticleDirection.FRONTEND
    "DEVOPS" -> ArticleDirection.DEVOPS
    "DATA_SCIENCE" -> ArticleDirection.DATA_SCIENCE
    else -> ArticleDirection.UNSPECIFIED
}

private fun parseArticleStatus(value: String): ArticleStatus = when (value) {
    "PENDING" -> ArticleStatus.PENDING
    "PUBLISHED" -> ArticleStatus.PUBLISHED
    "ARCHIVED" -> ArticleStatus.ARCHIVED
    else -> ArticleStatus.UNSPECIFIED
}

private fun parseDeliveryFrequency(value: String): DeliveryFrequency = when (value) {
    "DAILY" -> DeliveryFrequency.DAILY
    "WEEKLY" -> DeliveryFrequency.WEEKLY
    "MONTHLY" -> DeliveryFrequency.MONTHLY
    else -> DeliveryFrequency.UNSPECIFIED
}
