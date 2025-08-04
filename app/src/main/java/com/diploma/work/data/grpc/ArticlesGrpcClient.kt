package com.diploma.work.data.grpc

import com.diploma.work.data.AppSession
import com.diploma.work.data.models.*
import com.diploma.work.grpc.articles.ArticlesServiceGrpc
import com.diploma.work.utils.ErrorContext
import com.diploma.work.utils.ErrorMessageUtils
import com.orhanobut.logger.Logger
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject
import com.diploma.work.grpc.articles.Direction as ProtoDirection
import com.diploma.work.grpc.articles.ArticleStatus as ProtoArticleStatus
import com.diploma.work.grpc.articles.DeliveryFrequency as ProtoDeliveryFrequency

class ArticlesGrpcClient @Inject constructor(
    private val channel: ManagedChannel,
    private val session: AppSession
) {
    private val stub = ArticlesServiceGrpc.newBlockingStub(channel)
    private val tag = "grpc.articles"

    private fun getErrorContext(e: StatusRuntimeException, defaultContext: ErrorContext): ErrorContext {
        return when (e.status.code) {
            Status.Code.UNAVAILABLE, Status.Code.DEADLINE_EXCEEDED -> ErrorContext.NETWORK
            else -> defaultContext
        }
    }

    private fun getGenericErrorContext(e: Exception): ErrorContext {
        return if (e is StatusRuntimeException) {
            getErrorContext(e, ErrorContext.GENERIC)
        } else {
            ErrorContext.GENERIC
        }
    }

    suspend fun getTechnologies(request: GetTechnologiesRequest): Result<GetTechnologiesResponse> = withContext(Dispatchers.IO) {
        try {
            Logger.d("$tag: Getting technologies with direction: ${request.direction}")
            
            val grpcRequest = com.diploma.work.grpc.articles.GetTechnologiesRequest.newBuilder()
                .apply {
                    request.direction?.let { direction = it.toProtoDirection() }
                    paginationBuilder
                        .setPageSize(request.pageSize)
                        .setPageToken(request.pageToken)
                }
                .build()
                
            val grpcResponse = stub.getTechnologies(grpcRequest)
            
            val technologies = grpcResponse.technologiesList.map { tech ->
                ArticleTechnology(
                    id = tech.id,
                    name = tech.name,
                    description = tech.description,
                    direction = tech.direction.toModelDirection(),
                    iconUrl = tech.iconUrl
                )
            }
            
            Logger.d("$tag: Retrieved ${technologies.size} technologies")
            Result.success(GetTechnologiesResponse(
                technologies = technologies,
                nextPageToken = grpcResponse.nextPageToken
            ))        } catch (e: StatusRuntimeException) {
            Logger.e("$tag: Get technologies failed with gRPC error: ${e.status.code} - ${e.status.description}")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, getErrorContext(e, ErrorContext.DATA_LOADING))))
        } catch (e: Exception) {
            Logger.e("$tag: Get technologies failed with exception: ${e.message}")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, getGenericErrorContext(e))))
        }
    }

    suspend fun getUserPreferences(request: GetUserPreferencesRequest): Result<GetUserPreferencesResponse> = withContext(Dispatchers.IO) {
        try {
            Logger.d("$tag: Getting user preferences for user: ${request.userId}")
            
            val grpcRequest = com.diploma.work.grpc.articles.GetUserPreferencesRequest.newBuilder()
                .setUserId(request.userId)
                .build()
                
            val grpcResponse = stub.getUserPreferences(grpcRequest)
            
            val preferences = if (grpcResponse.hasPreferences()) {
                val pref = grpcResponse.preferences
                Logger.d("$tag: Server returned preferences - Technology IDs: ${pref.technologyIdsList}")
                Logger.d("$tag: Server returned preferences - Directions: ${pref.directionsList}")
                Logger.d("$tag: Server returned preferences - Delivery frequency: ${pref.deliveryFrequency}")
                Logger.d("$tag: Server returned preferences - Email notifications: ${pref.emailNotifications}")
                Logger.d("$tag: Server returned preferences - Push notifications: ${pref.pushNotifications}")
                Logger.d("$tag: Server returned preferences - Articles per day: ${pref.articlesPerDay}")
                
                UserPreferences(
                    userId = pref.userId,
                    technologyIds = pref.technologyIdsList,
                    directions = pref.directionsList.map { it.toModelDirection() },
                    deliveryFrequency = pref.deliveryFrequency.toModelDeliveryFrequency(),
                    emailNotifications = pref.emailNotifications,
                    pushNotifications = pref.pushNotifications,
                    excludedSources = pref.excludedSourcesList,
                    articlesPerDay = pref.articlesPerDay,
                    updatedAt = Instant.ofEpochSecond(pref.updatedAt.seconds, pref.updatedAt.nanos.toLong())
                )
            } else {
                Logger.d("$tag: Server returned no preferences")
                null
            }
            
            Logger.d("$tag: Retrieved user preferences: ${preferences != null}")
            Result.success(GetUserPreferencesResponse(preferences = preferences))        } catch (e: StatusRuntimeException) {
            Logger.e("$tag: Get user preferences failed with gRPC error: ${e.status.code} - ${e.status.description}")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, getErrorContext(e, ErrorContext.DATA_LOADING))))
        } catch (e: Exception) {
            Logger.e("$tag: Get user preferences failed with exception: ${e.message}")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, getGenericErrorContext(e))))
        }
    }

    suspend fun updateUserPreferences(request: UpdateUserPreferencesRequest): Result<UpdateUserPreferencesResponse> = withContext(Dispatchers.IO) {
        try {
            Logger.d("$tag: Updating user preferences for user: ${request.userId}")
            Logger.d("$tag: Technology IDs to save: ${request.technologyIds}")
            Logger.d("$tag: Directions to save: ${request.directions}")
            Logger.d("$tag: Delivery frequency: ${request.deliveryFrequency}")
            Logger.d("$tag: Email notifications: ${request.emailNotifications}")
            Logger.d("$tag: Push notifications: ${request.pushNotifications}")
            Logger.d("$tag: Articles per day: ${request.articlesPerDay}")
            
            val grpcRequest = com.diploma.work.grpc.articles.UpdateUserPreferencesRequest.newBuilder()
                .setUserId(request.userId)
                .addAllTechnologyIds(request.technologyIds)
                .addAllDirections(request.directions.map { it.toProtoDirection() })
                .setDeliveryFrequency(request.deliveryFrequency.toProtoDeliveryFrequency())
                .setEmailNotifications(request.emailNotifications)
                .setPushNotifications(request.pushNotifications)
                .addAllExcludedSources(request.excludedSources)
                .setArticlesPerDay(request.articlesPerDay)
                .build()
                
            Logger.d("$tag: Sending gRPC request to server...")
            val grpcResponse = stub.updateUserPreferences(grpcRequest)
            
            Logger.d("$tag: User preferences updated successfully on server")
            Logger.d("$tag: Server response message: ${grpcResponse.message}")
            Result.success(UpdateUserPreferencesResponse(message = grpcResponse.message))        } catch (e: StatusRuntimeException) {
            Logger.e("$tag: Update user preferences failed with gRPC error: ${e.status.code} - ${e.status.description}")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, ErrorContext.PROFILE_UPDATE)))        } catch (e: Exception) {
            Logger.e("$tag: Update user preferences failed with exception: ${e.message}")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, getGenericErrorContext(e))))
        }
    }

    suspend fun getArticles(request: GetArticlesRequest): Result<GetArticlesResponse> = withContext(Dispatchers.IO) {
        try {
            Logger.d("$tag: Getting articles with ${request.technologyIds.size} technology filters")
            
            val grpcRequest = com.diploma.work.grpc.articles.GetArticlesRequest.newBuilder()
                .addAllTechnologyIds(request.technologyIds)
                .addAllDirections(request.directions.map { it.toProtoDirection() })
                .apply {
                    request.rssSourceId?.let { rssSourceId = it }
                    status = request.status.toProtoArticleStatus()
                    request.fromDate?.let { 
                        fromDateBuilder.setSeconds(it.epochSecond).setNanos(it.nano)
                    }
                    request.toDate?.let { 
                        toDateBuilder.setSeconds(it.epochSecond).setNanos(it.nano)
                    }
                    paginationBuilder
                        .setPageSize(request.pageSize)
                        .setPageToken(request.pageToken)
                    sortBy = request.sortBy
                    sortOrder = request.sortOrder
                }
                .build()
                
            val grpcResponse = stub.getArticles(grpcRequest)
            
            Logger.d("$tag: getArticles response - ${grpcResponse.articlesList.size} articles")
            if (grpcResponse.articlesList.isNotEmpty()) {
                val firstArticle = grpcResponse.articlesList.first()
                Logger.d("$tag: First article - title: '${firstArticle.title}', rssSourceName: '${firstArticle.rssSourceName}'")
            }
            
            val articles = grpcResponse.articlesList.map { article ->
                Article(
                    id = article.id,
                    title = article.title,
                    description = article.description,
                    content = article.content,
                    url = article.url,
                    author = article.author,
                    publishedAt = Instant.ofEpochSecond(article.publishedAt.seconds, article.publishedAt.nanos.toLong()),
                    createdAt = Instant.ofEpochSecond(article.createdAt.seconds, article.createdAt.nanos.toLong()),
                    rssSourceId = article.rssSourceId,
                    rssSourceName = article.rssSourceName,
                    technologyIds = article.technologyIdsList,
                    tags = article.tagsList,
                    status = article.status.toModelArticleStatus(),
                    imageUrl = article.imageUrl,
                    readTimeMinutes = article.readTimeMinutes
                )
            }
            
            Logger.d("$tag: Retrieved ${articles.size} articles")
            Result.success(GetArticlesResponse(
                articles = articles,
                nextPageToken = grpcResponse.nextPageToken,
                totalCount = grpcResponse.totalCount
            ))        } catch (e: StatusRuntimeException) {
            Logger.e("$tag: Get articles failed with gRPC error: ${e.status.code} - ${e.status.description}")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, getErrorContext(e, ErrorContext.DATA_LOADING))))
        } catch (e: Exception) {
            Logger.e("$tag: Get articles failed with exception: ${e.message}")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, getGenericErrorContext(e))))
        }
    }

    suspend fun getRecommendedArticles(request: GetRecommendedArticlesRequest): Result<GetRecommendedArticlesResponse> = withContext(Dispatchers.IO) {
        try {
            Logger.d("$tag: Getting recommended articles for user: ${request.userId}")
            
            val grpcRequest = com.diploma.work.grpc.articles.GetRecommendedArticlesRequest.newBuilder()
                .setUserId(request.userId)
                .setLimit(request.limit)
                .apply {
                    request.fromDate?.let { 
                        fromDateBuilder.setSeconds(it.epochSecond).setNanos(it.nano)
                    }
                }
                .build()
                
            val grpcResponse = stub.getRecommendedArticles(grpcRequest)
            
            val recommendations = grpcResponse.recommendationsList.map { rec ->
                val article = rec.article
                ArticleRecommendation(
                    article = Article(
                        id = article.id,
                        title = article.title,
                        description = article.description,
                        content = article.content,
                        url = article.url,
                        author = article.author,
                        publishedAt = Instant.ofEpochSecond(article.publishedAt.seconds, article.publishedAt.nanos.toLong()),
                        createdAt = Instant.ofEpochSecond(article.createdAt.seconds, article.createdAt.nanos.toLong()),
                        rssSourceId = article.rssSourceId,
                        rssSourceName = article.rssSourceName,
                        technologyIds = article.technologyIdsList,
                        tags = article.tagsList,
                        status = article.status.toModelArticleStatus(),
                        imageUrl = article.imageUrl,
                        readTimeMinutes = article.readTimeMinutes
                    ),
                    relevanceScore = rec.relevanceScore,
                    recommendationReason = rec.recommendationReason,
                    matchedTechnologies = rec.matchedTechnologiesList
                )
            }
            
            Logger.d("$tag: Retrieved ${recommendations.size} recommended articles")
            Result.success(GetRecommendedArticlesResponse(recommendations = recommendations))        } catch (e: StatusRuntimeException) {
            Logger.e("$tag: Get recommended articles failed with gRPC error: ${e.status.code} - ${e.status.description}")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, getErrorContext(e, ErrorContext.DATA_LOADING))))
        } catch (e: Exception) {
            Logger.e("$tag: Get recommended articles failed with exception: ${e.message}")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, getGenericErrorContext(e))))
        }
    }

    suspend fun searchArticles(request: SearchArticlesRequest): Result<SearchArticlesResponse> = withContext(Dispatchers.IO) {
        try {
            Logger.d("$tag: Searching articles with query: ${request.query}")
            
            val grpcRequest = com.diploma.work.grpc.articles.SearchArticlesRequest.newBuilder()
                .setQuery(request.query)
                .addAllTechnologyIds(request.technologyIds)
                .addAllDirections(request.directions.map { it.toProtoDirection() })
                .apply {
                    request.fromDate?.let { 
                        fromDateBuilder.setSeconds(it.epochSecond).setNanos(it.nano)
                    }
                    request.toDate?.let { 
                        toDateBuilder.setSeconds(it.epochSecond).setNanos(it.nano)
                    }
                    paginationBuilder
                        .setPageSize(request.pageSize)
                        .setPageToken(request.pageToken)
                }
                .build()
                
            val grpcResponse = stub.searchArticles(grpcRequest)
            
            val articles = grpcResponse.articlesList.map { article ->
                Article(
                    id = article.id,
                    title = article.title,
                    description = article.description,
                    content = article.content,
                    url = article.url,
                    author = article.author,
                    publishedAt = Instant.ofEpochSecond(article.publishedAt.seconds, article.publishedAt.nanos.toLong()),
                    createdAt = Instant.ofEpochSecond(article.createdAt.seconds, article.createdAt.nanos.toLong()),
                    rssSourceId = article.rssSourceId,
                    rssSourceName = article.rssSourceName,
                    technologyIds = article.technologyIdsList,
                    tags = article.tagsList,
                    status = article.status.toModelArticleStatus(),
                    imageUrl = article.imageUrl,
                    readTimeMinutes = article.readTimeMinutes
                )
            }
            
            Logger.d("$tag: Found ${articles.size} articles")
            Result.success(SearchArticlesResponse(
                articles = articles,
                nextPageToken = grpcResponse.nextPageToken,
                totalCount = grpcResponse.totalCount
            ))        } catch (e: StatusRuntimeException) {
            Logger.e("$tag: Search articles failed with gRPC error: ${e.status.code} - ${e.status.description}")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, ErrorContext.DATA_LOADING)))
        } catch (e: Exception) {
            Logger.e("$tag: Search articles failed with exception: ${e.message}")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, ErrorContext.DATA_LOADING)))
        }
    }

    private fun ArticleDirection.toProtoDirection(): ProtoDirection = when (this) {
        ArticleDirection.UNSPECIFIED -> ProtoDirection.DIRECTION_UNSPECIFIED
        ArticleDirection.BACKEND -> ProtoDirection.BACKEND
        ArticleDirection.FRONTEND -> ProtoDirection.FRONTEND
        ArticleDirection.DEVOPS -> ProtoDirection.DEVOPS
        ArticleDirection.DATA_SCIENCE -> ProtoDirection.DATA_SCIENCE
    }

    private fun ProtoDirection.toModelDirection(): ArticleDirection = when (this) {
        ProtoDirection.DIRECTION_UNSPECIFIED -> ArticleDirection.UNSPECIFIED
        ProtoDirection.BACKEND -> ArticleDirection.BACKEND
        ProtoDirection.FRONTEND -> ArticleDirection.FRONTEND
        ProtoDirection.DEVOPS -> ArticleDirection.DEVOPS
        ProtoDirection.DATA_SCIENCE -> ArticleDirection.DATA_SCIENCE
        ProtoDirection.UNRECOGNIZED -> ArticleDirection.UNSPECIFIED
    }

    private fun ArticleStatus.toProtoArticleStatus(): ProtoArticleStatus = when (this) {
        ArticleStatus.UNSPECIFIED -> ProtoArticleStatus.ARTICLE_STATUS_UNSPECIFIED
        ArticleStatus.PENDING -> ProtoArticleStatus.PENDING
        ArticleStatus.PUBLISHED -> ProtoArticleStatus.PUBLISHED
        ArticleStatus.ARCHIVED -> ProtoArticleStatus.ARCHIVED
    }

    private fun ProtoArticleStatus.toModelArticleStatus(): ArticleStatus = when (this) {
        ProtoArticleStatus.ARTICLE_STATUS_UNSPECIFIED -> ArticleStatus.UNSPECIFIED
        ProtoArticleStatus.PENDING -> ArticleStatus.PENDING
        ProtoArticleStatus.PUBLISHED -> ArticleStatus.PUBLISHED
        ProtoArticleStatus.ARCHIVED -> ArticleStatus.ARCHIVED
        ProtoArticleStatus.UNRECOGNIZED -> ArticleStatus.UNSPECIFIED
    }

    private fun DeliveryFrequency.toProtoDeliveryFrequency(): ProtoDeliveryFrequency = when (this) {
        DeliveryFrequency.UNSPECIFIED -> ProtoDeliveryFrequency.DELIVERY_FREQUENCY_UNSPECIFIED
        DeliveryFrequency.DAILY -> ProtoDeliveryFrequency.DAILY
        DeliveryFrequency.WEEKLY -> ProtoDeliveryFrequency.WEEKLY
        DeliveryFrequency.MONTHLY -> ProtoDeliveryFrequency.MONTHLY
    }

    private fun ProtoDeliveryFrequency.toModelDeliveryFrequency(): DeliveryFrequency = when (this) {
        ProtoDeliveryFrequency.DELIVERY_FREQUENCY_UNSPECIFIED -> DeliveryFrequency.UNSPECIFIED
        ProtoDeliveryFrequency.DAILY -> DeliveryFrequency.DAILY
        ProtoDeliveryFrequency.WEEKLY -> DeliveryFrequency.WEEKLY
        ProtoDeliveryFrequency.MONTHLY -> DeliveryFrequency.MONTHLY
        ProtoDeliveryFrequency.UNRECOGNIZED -> DeliveryFrequency.UNSPECIFIED
    }
}
