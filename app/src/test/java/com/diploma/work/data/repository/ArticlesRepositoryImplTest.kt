package com.diploma.work.data.repository

import com.diploma.work.data.grpc.ArticlesGrpcClient
import com.diploma.work.data.models.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class ArticlesRepositoryImplTest {
    
    private lateinit var articlesGrpcClient: ArticlesGrpcClient
    private lateinit var repository: ArticlesRepositoryImpl
    
    @Before
    fun setup() {
        articlesGrpcClient = mockk()
        repository = ArticlesRepositoryImpl(articlesGrpcClient)
    }
    
    @Test
    fun `getTechnologies calls grpc client and returns result`() = runTest {
        val request = GetTechnologiesRequest(pageSize = 50)
        val technologies = listOf(
            ArticleTechnology(1L, "Kotlin", "Modern programming language", ArticleDirection.BACKEND, "https://example.com/kotlin.png"),
            ArticleTechnology(2L, "Java", "Object-oriented language", ArticleDirection.BACKEND, "https://example.com/java.png"),
            ArticleTechnology(3L, "React", "Frontend library", ArticleDirection.FRONTEND, "https://example.com/react.png")
        )
        val response = GetTechnologiesResponse(technologies, "next_token")
        
        coEvery { articlesGrpcClient.getTechnologies(request) } returns Result.success(response)
        
        val result = repository.getTechnologies(request)
        
        assertTrue(result.isSuccess)
        assertEquals(response, result.getOrNull())
        assertEquals(3, result.getOrNull()?.technologies?.size)
        coVerify { articlesGrpcClient.getTechnologies(request) }
    }
    
    @Test
    fun `getTechnologies handles failure from grpc client`() = runTest {
        val request = GetTechnologiesRequest(pageSize = 50)
        val exception = Exception("Network error")
        
        coEvery { articlesGrpcClient.getTechnologies(request) } returns Result.failure(exception)
        
        val result = repository.getTechnologies(request)
        
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify { articlesGrpcClient.getTechnologies(request) }
    }
    
    @Test
    fun `getUserPreferences calls grpc client and returns result`() = runTest {
        val request = GetUserPreferencesRequest(userId = 123L)
        val preferences = UserPreferences(
            userId = 123L,
            technologyIds = listOf(1L, 2L),
            directions = listOf(ArticleDirection.BACKEND, ArticleDirection.FRONTEND),
            deliveryFrequency = DeliveryFrequency.DAILY,
            emailNotifications = true,
            pushNotifications = false,
            excludedSources = listOf("source1", "source2"),
            articlesPerDay = 25,
            updatedAt = Instant.now()
        )
        val response = GetUserPreferencesResponse(preferences)
        
        coEvery { articlesGrpcClient.getUserPreferences(request) } returns Result.success(response)
        
        val result = repository.getUserPreferences(request)
        
        assertTrue(result.isSuccess)
        assertEquals(response, result.getOrNull())
        assertEquals(preferences, result.getOrNull()?.preferences)
        coVerify { articlesGrpcClient.getUserPreferences(request) }
    }
    
    @Test
    fun `getUserPreferences with null preferences returns success`() = runTest {
        val request = GetUserPreferencesRequest(userId = 123L)
        val response = GetUserPreferencesResponse(null)
        
        coEvery { articlesGrpcClient.getUserPreferences(request) } returns Result.success(response)
        
        val result = repository.getUserPreferences(request)
        
        assertTrue(result.isSuccess)
        assertNull(result.getOrNull()?.preferences)
        coVerify { articlesGrpcClient.getUserPreferences(request) }
    }
    
    @Test
    fun `updateUserPreferences calls grpc client and returns result`() = runTest {
        val preferences = UserPreferences(
            userId = 123L,
            technologyIds = listOf(1L, 3L),
            directions = listOf(ArticleDirection.BACKEND),
            deliveryFrequency = DeliveryFrequency.WEEKLY,
            emailNotifications = false,
            pushNotifications = true,
            excludedSources = emptyList(),
            articlesPerDay = 15,
            updatedAt = Instant.now()
        )
        val request = UpdateUserPreferencesRequest(
            userId = preferences.userId,
            technologyIds = preferences.technologyIds,
            directions = preferences.directions,
            deliveryFrequency = preferences.deliveryFrequency,
            emailNotifications = preferences.emailNotifications,
            pushNotifications = preferences.pushNotifications,
            excludedSources = preferences.excludedSources,
            articlesPerDay = preferences.articlesPerDay
        )
        val response = UpdateUserPreferencesResponse("Successfully updated")
        
        coEvery { articlesGrpcClient.updateUserPreferences(request) } returns Result.success(response)
        
        val result = repository.updateUserPreferences(request)
        
        assertTrue(result.isSuccess)
        assertEquals(response, result.getOrNull())
        assertEquals("Successfully updated", result.getOrNull()?.message)
        coVerify { articlesGrpcClient.updateUserPreferences(request) }
    }
    
    @Test
    fun `getArticles calls grpc client and returns result`() = runTest {
        val request = GetArticlesRequest(
            technologyIds = listOf(1L, 2L),
            directions = listOf(ArticleDirection.BACKEND),
            pageSize = 20,
            pageToken = ""
        )
        val articles = listOf(
            Article(
                id = 1L,
                title = "Introduction to Kotlin",
                description = "Learn Kotlin basics",
                content = "Kotlin is a modern programming language...",
                url = "https://example.com/kotlin-intro",
                author = "John Doe",
                publishedAt = Instant.parse("2023-01-01T00:00:00Z"),
                createdAt = Instant.parse("2023-01-01T00:00:00Z"),
                rssSourceId = 1L,
                rssSourceName = "Tech Blog",
                technologyIds = listOf(1L),
                tags = listOf("kotlin", "programming"),
                status = ArticleStatus.PUBLISHED,
                imageUrl = "https://example.com/kotlin.png",
                readTimeMinutes = 5
            )
        )
        val response = GetArticlesResponse(articles, "next_token", 1)
        
        coEvery { articlesGrpcClient.getArticles(request) } returns Result.success(response)
        
        val result = repository.getArticles(request)
        
        assertTrue(result.isSuccess)
        assertEquals(response, result.getOrNull())
        assertEquals(1, result.getOrNull()?.articles?.size)
        assertEquals("Introduction to Kotlin", result.getOrNull()?.articles?.first()?.title)
        coVerify { articlesGrpcClient.getArticles(request) }
    }
    
    @Test
    fun `getRecommendedArticles calls grpc client and returns result`() = runTest {
        val request = GetRecommendedArticlesRequest(
            userId = 123L,
            limit = 10
        )
        val recommendations = listOf(
            ArticleRecommendation(
                article = Article(
                    id = 2L,
                    title = "Advanced Android Development",
                    description = "Learn advanced techniques",
                    content = "Learn advanced Android development techniques...",
                    url = "https://example.com/android-advanced",
                    author = "Jane Smith",
                    publishedAt = Instant.parse("2023-01-02T00:00:00Z"),
                    createdAt = Instant.parse("2023-01-02T00:00:00Z"),
                    rssSourceId = 2L,
                    rssSourceName = "Android Weekly",
                    technologyIds = listOf(2L),
                    tags = listOf("android", "mobile"),
                    status = ArticleStatus.PUBLISHED,
                    imageUrl = "https://example.com/android.png",
                    readTimeMinutes = 8
                ),
                relevanceScore = 0.95f,
                recommendationReason = "Matches your interests",
                matchedTechnologies = listOf("Android")
            )
        )
        val response = GetRecommendedArticlesResponse(recommendations)
        
        coEvery { articlesGrpcClient.getRecommendedArticles(request) } returns Result.success(response)
        
        val result = repository.getRecommendedArticles(request)
        
        assertTrue(result.isSuccess)
        assertEquals(response, result.getOrNull())
        assertEquals(1, result.getOrNull()?.recommendations?.size)
        assertEquals("Advanced Android Development", result.getOrNull()?.recommendations?.first()?.article?.title)
        coVerify { articlesGrpcClient.getRecommendedArticles(request) }
    }
    
    @Test
    fun `getArticles with empty results returns success`() = runTest {
        val request = GetArticlesRequest(
            technologyIds = listOf(999L),
            directions = listOf(ArticleDirection.DEVOPS),
            pageSize = 20,
            pageToken = ""
        )
        val response = GetArticlesResponse(emptyList(), "", 0)
        
        coEvery { articlesGrpcClient.getArticles(request) } returns Result.success(response)
        
        val result = repository.getArticles(request)
        
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.articles?.isEmpty() == true)
        assertEquals("", result.getOrNull()?.nextPageToken)
        coVerify { articlesGrpcClient.getArticles(request) }
    }
    
    @Test
    fun `all methods handle exceptions properly`() = runTest {
        val exception = RuntimeException("GRPC connection failed")
        
        coEvery { articlesGrpcClient.getTechnologies(any()) } returns Result.failure(exception)
        coEvery { articlesGrpcClient.getUserPreferences(any()) } returns Result.failure(exception)
        coEvery { articlesGrpcClient.updateUserPreferences(any()) } returns Result.failure(exception)
        coEvery { articlesGrpcClient.getArticles(any()) } returns Result.failure(exception)
        coEvery { articlesGrpcClient.getRecommendedArticles(any()) } returns Result.failure(exception)
        
        val getTechnologiesResult = repository.getTechnologies(GetTechnologiesRequest(pageSize = 50))
        val getUserPrefsResult = repository.getUserPreferences(GetUserPreferencesRequest(123L))
        val updatePrefsResult = repository.updateUserPreferences(UpdateUserPreferencesRequest(
            userId = 123L,
            technologyIds = emptyList(),
            directions = emptyList(),
            deliveryFrequency = DeliveryFrequency.WEEKLY,
            emailNotifications = true,
            pushNotifications = true,
            excludedSources = emptyList(),
            articlesPerDay = 5
        ))
        val getArticlesResult = repository.getArticles(GetArticlesRequest(
            technologyIds = emptyList(),
            directions = emptyList(),
            pageSize = 10,
            pageToken = ""
        ))
        val getRecommendedResult = repository.getRecommendedArticles(GetRecommendedArticlesRequest(
            userId = 123L,
            limit = 10
        ))
        
        assertTrue(getTechnologiesResult.isFailure)
        assertTrue(getUserPrefsResult.isFailure)
        assertTrue(updatePrefsResult.isFailure)
        assertTrue(getArticlesResult.isFailure)
        assertTrue(getRecommendedResult.isFailure)
        
        assertEquals(exception, getTechnologiesResult.exceptionOrNull())
        assertEquals(exception, getUserPrefsResult.exceptionOrNull())
        assertEquals(exception, updatePrefsResult.exceptionOrNull())
        assertEquals(exception, getArticlesResult.exceptionOrNull())
        assertEquals(exception, getRecommendedResult.exceptionOrNull())
    }
    
    @Test
    fun `getTechnologies with large page size works correctly`() = runTest {
        val request = GetTechnologiesRequest(pageSize = 1000)
        val technologies = (1..100L).map { id ->
            ArticleTechnology(id, "Technology$id", "Description for tech $id", ArticleDirection.BACKEND, "https://example.com/tech$id.png")
        }
        val response = GetTechnologiesResponse(technologies, "next_token")
        
        coEvery { articlesGrpcClient.getTechnologies(request) } returns Result.success(response)
        
        val result = repository.getTechnologies(request)
        
        assertTrue(result.isSuccess)
        assertEquals(100, result.getOrNull()?.technologies?.size)
        coVerify { articlesGrpcClient.getTechnologies(request) }
    }
    
    @Test
    fun `updateUserPreferences with complex preferences works correctly`() = runTest {
        val complexPreferences = UserPreferences(
            userId = 456L,
            technologyIds = (1..10L).toList(),
            directions = ArticleDirection.values().toList(),
            deliveryFrequency = DeliveryFrequency.MONTHLY,
            emailNotifications = true,
            pushNotifications = true,
            excludedSources = (1..5).map { "source$it" },
            articlesPerDay = 100,
            updatedAt = Instant.now()
        )
        val request = UpdateUserPreferencesRequest(
            userId = complexPreferences.userId,
            technologyIds = complexPreferences.technologyIds,
            directions = complexPreferences.directions,
            deliveryFrequency = complexPreferences.deliveryFrequency,
            emailNotifications = complexPreferences.emailNotifications,
            pushNotifications = complexPreferences.pushNotifications,
            excludedSources = complexPreferences.excludedSources,
            articlesPerDay = complexPreferences.articlesPerDay
        )
        val response = UpdateUserPreferencesResponse("Successfully updated complex preferences")
        
        coEvery { articlesGrpcClient.updateUserPreferences(request) } returns Result.success(response)
        
        val result = repository.updateUserPreferences(request)
        
        assertTrue(result.isSuccess)
        assertEquals("Successfully updated complex preferences", result.getOrNull()?.message)
        coVerify { articlesGrpcClient.updateUserPreferences(request) }
    }
}
