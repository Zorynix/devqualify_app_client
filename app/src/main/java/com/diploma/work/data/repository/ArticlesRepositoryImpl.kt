package com.diploma.work.data.repository

import com.diploma.work.data.grpc.ArticlesGrpcClient
import com.diploma.work.data.models.*
import javax.inject.Inject

class ArticlesRepositoryImpl @Inject constructor(
    private val articlesGrpcClient: ArticlesGrpcClient
) : ArticlesRepository {

    override suspend fun getTechnologies(request: GetTechnologiesRequest): Result<GetTechnologiesResponse> {
        return articlesGrpcClient.getTechnologies(request)
    }

    override suspend fun getUserPreferences(request: GetUserPreferencesRequest): Result<GetUserPreferencesResponse> {
        return articlesGrpcClient.getUserPreferences(request)
    }

    override suspend fun updateUserPreferences(request: UpdateUserPreferencesRequest): Result<UpdateUserPreferencesResponse> {
        return articlesGrpcClient.updateUserPreferences(request)
    }

    override suspend fun getArticles(request: GetArticlesRequest): Result<GetArticlesResponse> {
        return articlesGrpcClient.getArticles(request)
    }    override suspend fun getRecommendedArticles(request: GetRecommendedArticlesRequest): Result<GetRecommendedArticlesResponse> {
        return articlesGrpcClient.getRecommendedArticles(request)
    }
}
