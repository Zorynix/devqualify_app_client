package com.diploma.work.data.api

import com.diploma.work.data.api.dto.GetArticlesDto
import com.diploma.work.data.api.dto.GetArticleTechnologiesDto
import com.diploma.work.data.api.dto.GetRecommendedArticlesDto
import com.diploma.work.data.api.dto.GetUserPreferencesDto
import com.diploma.work.data.api.dto.SearchArticlesDto
import com.diploma.work.data.api.dto.UpdateUserPreferencesDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface ArticlesApi {

    @GET("articles")
    suspend fun getArticles(
        @Query("technology_ids") technologyIds: String? = null,
        @Query("rss_source_id") rssSourceId: Long? = null,
        @Query("status") status: String? = null,
        @Query("from_date") fromDate: String? = null,
        @Query("to_date") toDate: String? = null,
        @Query("page_size") pageSize: Int = 20,
        @Query("page_token") pageToken: String = "",
        @Query("sort_by") sortBy: String? = null,
        @Query("sort_order") sortOrder: String? = null
    ): GetArticlesDto

    @GET("articles/search")
    suspend fun searchArticles(
        @Query("query") query: String,
        @Query("technology_ids") technologyIds: String? = null,
        @Query("from_date") fromDate: String? = null,
        @Query("to_date") toDate: String? = null,
        @Query("page_size") pageSize: Int = 20,
        @Query("page_token") pageToken: String = ""
    ): SearchArticlesDto

    @GET("articles/recommended")
    suspend fun getRecommendedArticles(
        @Query("limit") limit: Int = 10,
        @Query("from_date") fromDate: String? = null
    ): GetRecommendedArticlesDto

    @GET("technologies/articles")
    suspend fun getTechnologies(
        @Query("page_size") pageSize: Int = 50,
        @Query("page_token") pageToken: String = ""
    ): GetArticleTechnologiesDto

    @GET("users/me/article-preferences")
    suspend fun getUserPreferences(
        @Query("user_id") userId: Long
    ): GetUserPreferencesDto

    @PUT("users/me/article-preferences")
    suspend fun updateUserPreferences(@Body request: UpdateUserPreferencesBody): UpdateUserPreferencesDto
}

data class UpdateUserPreferencesBody(
    val userId: Long,
    val technologyIds: List<Long>,
    val directions: List<String>,
    val deliveryFrequency: String,
    val emailNotifications: Boolean,
    val pushNotifications: Boolean,
    val excludedSources: List<String>,
    val articlesPerDay: Int
)
