package com.diploma.work.data.repository

import com.diploma.work.data.api.ArticlesApi
import com.diploma.work.data.api.UpdateUserPreferencesBody
import com.diploma.work.data.api.safeApiCall
import com.diploma.work.data.models.GetArticlesRequest
import com.diploma.work.data.models.GetArticlesResponse
import com.diploma.work.data.models.GetRecommendedArticlesRequest
import com.diploma.work.data.models.GetRecommendedArticlesResponse
import com.diploma.work.data.models.GetTechnologiesRequest
import com.diploma.work.data.models.GetTechnologiesResponse
import com.diploma.work.data.models.GetUserPreferencesRequest
import com.diploma.work.data.models.GetUserPreferencesResponse
import com.diploma.work.data.models.UpdateUserPreferencesRequest
import com.diploma.work.data.models.UpdateUserPreferencesResponse
import com.diploma.work.utils.ErrorContext
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class ArticlesRepositoryImpl @Inject constructor(
    private val api: ArticlesApi
) : ArticlesRepository {

    override suspend fun getTechnologies(request: GetTechnologiesRequest): Result<GetTechnologiesResponse> =
        safeApiCall(ErrorContext.DATA_LOADING) {
            api.getTechnologies(
                pageSize = request.pageSize,
                pageToken = request.pageToken
            ).toModel()
        }

    override suspend fun getUserPreferences(request: GetUserPreferencesRequest): Result<GetUserPreferencesResponse> =
        safeApiCall(ErrorContext.DATA_LOADING) {
            api.getUserPreferences(userId = request.userId).toModel()
        }

    override suspend fun updateUserPreferences(request: UpdateUserPreferencesRequest): Result<UpdateUserPreferencesResponse> =
        safeApiCall(ErrorContext.DATA_LOADING) {
            api.updateUserPreferences(
                UpdateUserPreferencesBody(
                    userId = request.userId,
                    technologyIds = request.technologyIds,
                    directions = request.directions.map { it.name },
                    deliveryFrequency = request.deliveryFrequency.name,
                    emailNotifications = request.emailNotifications,
                    pushNotifications = request.pushNotifications,
                    excludedSources = request.excludedSources,
                    articlesPerDay = request.articlesPerDay
                )
            ).toModel()
        }

    override suspend fun getArticles(request: GetArticlesRequest): Result<GetArticlesResponse> =
        safeApiCall(ErrorContext.DATA_LOADING) {
            api.getArticles(
                technologyIds = request.technologyIds.takeIf { it.isNotEmpty() }?.joinToString(","),
                rssSourceId = request.rssSourceId,
                status = request.status.name,
                fromDate = request.fromDate?.let { RFC3339_FORMATTER.format(it.atOffset(ZoneOffset.UTC)) },
                toDate = request.toDate?.let { RFC3339_FORMATTER.format(it.atOffset(ZoneOffset.UTC)) },
                pageSize = request.pageSize,
                pageToken = request.pageToken,
                sortBy = request.sortBy,
                sortOrder = request.sortOrder
            ).toModel()
        }

    override suspend fun getRecommendedArticles(request: GetRecommendedArticlesRequest): Result<GetRecommendedArticlesResponse> =
        safeApiCall(ErrorContext.DATA_LOADING) {
            api.getRecommendedArticles(
                limit = request.limit,
                fromDate = request.fromDate?.let { RFC3339_FORMATTER.format(it.atOffset(ZoneOffset.UTC)) }
            ).toModel()
        }

    companion object {
        private val RFC3339_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    }
}
