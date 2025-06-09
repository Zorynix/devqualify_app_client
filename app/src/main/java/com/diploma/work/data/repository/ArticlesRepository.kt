package com.diploma.work.data.repository

import com.diploma.work.data.models.*

interface ArticlesRepository {
    suspend fun getTechnologies(request: GetTechnologiesRequest): Result<GetTechnologiesResponse>
    suspend fun getUserPreferences(request: GetUserPreferencesRequest): Result<GetUserPreferencesResponse>
    suspend fun updateUserPreferences(request: UpdateUserPreferencesRequest): Result<UpdateUserPreferencesResponse>
    suspend fun getArticles(request: GetArticlesRequest): Result<GetArticlesResponse>
    suspend fun getRecommendedArticles(request: GetRecommendedArticlesRequest): Result<GetRecommendedArticlesResponse>
}
