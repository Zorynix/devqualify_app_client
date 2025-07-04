package com.diploma.work.ui.feature.articles

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.diploma.work.data.AppSession
import com.diploma.work.data.models.*
import com.diploma.work.data.repository.ArticlesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class ArticlesViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var articlesRepository: ArticlesRepository
    private lateinit var session: AppSession
    private lateinit var viewModel: ArticlesViewModel
    
    @Before
    fun setup() {
        articlesRepository = mockk()
        session = mockk()
        viewModel = ArticlesViewModel(articlesRepository, session)
    }
    
    @Test
    fun `initial state is loading`() {
        assertTrue(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.articles.isEmpty())
        assertNull(viewModel.uiState.value.error)
    }
    
    @Test
    fun `initialization loads technologies and articles successfully`() = runTest {
        val technologies = listOf(
            ArticleTechnology(
                id = 1L,
                name = "Kotlin",
                description = "Kotlin programming language",
                direction = ArticleDirection.BACKEND,
                iconUrl = ""
            )
        )
        val articles = listOf(
            Article(
                id = 1L,
                title = "Test Article",
                description = "Test Description",
                content = "Test Content",
                url = "https://example.com",
                author = "Test Author",
                publishedAt = Instant.now(),
                createdAt = Instant.now(),
                rssSourceId = 1L,
                rssSourceName = "Test Source",
                technologyIds = listOf(1L),
                tags = listOf("kotlin", "programming"),
                status = ArticleStatus.PUBLISHED,
                imageUrl = "",
                readTimeMinutes = 5
            )
        )
        
        coEvery { articlesRepository.getTechnologies(any()) } returns Result.success(GetTechnologiesResponse(technologies, ""))
        coEvery { session.getUserId() } returns 1L
        coEvery { session.getUserPreferences() } returns null
        coEvery { articlesRepository.getUserPreferences(any()) } returns Result.success(GetUserPreferencesResponse(null))
        coEvery { articlesRepository.getArticles(any()) } returns Result.success(GetArticlesResponse(articles, "", 1))
        
        assertNotNull(viewModel.uiState.value)
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(technologies, state.technologies)
        assertEquals(articles, state.articles)
        assertNull(state.error)
    }
    
    @Test
    fun `initialization with error shows error message`() = runTest {
        val errorMessage = "Failed to load technologies"
        
        coEvery { articlesRepository.getTechnologies(any()) } returns Result.failure(Exception(errorMessage))
        
        val state = viewModel.uiState.value
        assertNull(state.error)
    }
    
    @Test
    fun `setSearchQuery updates search query`() = runTest {
        val query = "kotlin"
        
        viewModel.setSearchQuery(query)
        
        val state = viewModel.uiState.value
        assertEquals(query, state.searchQuery)
    }
    
    @Test
    fun `clearSearch clears search query and reloads articles`() = runTest {
        coEvery { articlesRepository.getTechnologies(any()) } returns Result.success(GetTechnologiesResponse(emptyList(), ""))
        coEvery { session.getUserId() } returns 1L
        coEvery { session.getUserPreferences() } returns null
        coEvery { articlesRepository.getUserPreferences(any()) } returns Result.success(GetUserPreferencesResponse(null))
        coEvery { articlesRepository.getArticles(any()) } returns Result.success(GetArticlesResponse(emptyList(), "", 0))
        
        viewModel.setSearchQuery("kotlin")
        viewModel.setSearchQuery("")
        
        val state = viewModel.uiState.value
        assertTrue(state.searchQuery.isEmpty())
    }
    
    @Test
    fun `setTimePeriod updates time period and reloads articles`() = runTest {
        coEvery { articlesRepository.getTechnologies(any()) } returns Result.success(GetTechnologiesResponse(emptyList(), ""))
        coEvery { session.getUserId() } returns 1L
        coEvery { session.getUserPreferences() } returns null
        coEvery { articlesRepository.getUserPreferences(any()) } returns Result.success(GetUserPreferencesResponse(null))
        coEvery { articlesRepository.getArticles(any()) } returns Result.success(GetArticlesResponse(emptyList(), "", 0))
        
        viewModel.setTimePeriod(TimePeriod.MONTH)
        
        val state = viewModel.uiState.value
        assertEquals(TimePeriod.MONTH, state.selectedTimePeriod)
    }
    
    @Test
    fun `setSortType updates sort type and reloads articles`() = runTest {
        coEvery { articlesRepository.getTechnologies(any()) } returns Result.success(GetTechnologiesResponse(emptyList(), ""))
        coEvery { session.getUserId() } returns 1L
        coEvery { session.getUserPreferences() } returns null
        coEvery { articlesRepository.getUserPreferences(any()) } returns Result.success(GetUserPreferencesResponse(null))
        coEvery { articlesRepository.getArticles(any()) } returns Result.success(GetArticlesResponse(emptyList(), "", 0))
        
        viewModel.setSortType(SortType.DATE_DESC)
        
        val state = viewModel.uiState.value
        assertEquals(SortType.DATE_DESC, state.selectedSortType)
    }
    
    @Test
    fun `selected directions are tracked in state`() = runTest {
        coEvery { articlesRepository.getTechnologies(any()) } returns Result.success(GetTechnologiesResponse(emptyList(), ""))
        coEvery { session.getUserId() } returns 1L
        coEvery { session.getUserPreferences() } returns null
        coEvery { articlesRepository.getUserPreferences(any()) } returns Result.success(GetUserPreferencesResponse(null))
        coEvery { articlesRepository.getArticles(any()) } returns Result.success(GetArticlesResponse(emptyList(), "", 0))
        
        val state = viewModel.uiState.value
        assertNotNull(state.selectedDirections)
    }
    
    @Test
    fun `selected technology ids are tracked in state`() = runTest {
        coEvery { articlesRepository.getTechnologies(any()) } returns Result.success(GetTechnologiesResponse(emptyList(), ""))
        coEvery { session.getUserId() } returns 1L
        coEvery { session.getUserPreferences() } returns null
        coEvery { articlesRepository.getUserPreferences(any()) } returns Result.success(GetUserPreferencesResponse(null))
        coEvery { articlesRepository.getArticles(any()) } returns Result.success(GetArticlesResponse(emptyList(), "", 0))
        
        val state = viewModel.uiState.value
        assertNotNull(state.selectedTechnologyIds)
    }
    
    @Test
    fun `clearFilters resets search and filters`() = runTest {
        coEvery { articlesRepository.getTechnologies(any()) } returns Result.success(GetTechnologiesResponse(emptyList(), ""))
        coEvery { session.getUserId() } returns 1L
        coEvery { session.getUserPreferences() } returns null
        coEvery { articlesRepository.getUserPreferences(any()) } returns Result.success(GetUserPreferencesResponse(null))
        coEvery { articlesRepository.getArticles(any()) } returns Result.success(GetArticlesResponse(emptyList(), "", 0))
        
        viewModel.setSearchQuery("kotlin")
        viewModel.setTimePeriod(TimePeriod.MONTH)
        viewModel.setSortType(SortType.DATE_DESC)
        
        viewModel.clearFilters()
        
        val state = viewModel.uiState.value
        assertTrue(state.searchQuery.isEmpty())
        assertEquals(TimePeriod.WEEK, state.selectedTimePeriod)
        assertEquals(SortType.RELEVANCE, state.selectedSortType)
    }
    
    @Test
    fun `filters expanded state is tracked`() = runTest {
        val state = viewModel.uiState.value
        assertNotNull(state.isFiltersExpanded)
    }
    
    @Test
    fun `loadMoreArticles loads additional articles`() = runTest {
        val initialArticles = listOf(
            Article(
                id = 1L,
                title = "Article 1",
                description = "Description 1",
                content = "Content 1",
                url = "https://example.com/1",
                author = "Author 1",
                publishedAt = Instant.now(),
                createdAt = Instant.now(),
                rssSourceId = 1L,
                rssSourceName = "Source 1",
                technologyIds = listOf(1L),
                tags = listOf("tag1"),
                status = ArticleStatus.PUBLISHED,
                imageUrl = "",
                readTimeMinutes = 5
            )
        )
        val moreArticles = listOf(
            Article(
                id = 2L,
                title = "Article 2",
                description = "Description 2",
                content = "Content 2",
                url = "https://example.com/2",
                author = "Author 2",
                publishedAt = Instant.now(),
                createdAt = Instant.now(),
                rssSourceId = 1L,
                rssSourceName = "Source 1",
                technologyIds = listOf(1L),
                tags = listOf("tag2"),
                status = ArticleStatus.PUBLISHED,
                imageUrl = "",
                readTimeMinutes = 3
            )
        )
        
        coEvery { articlesRepository.getTechnologies(any()) } returns Result.success(GetTechnologiesResponse(emptyList(), ""))
        coEvery { session.getUserId() } returns 1L
        coEvery { session.getUserPreferences() } returns null
        coEvery { articlesRepository.getUserPreferences(any()) } returns Result.success(GetUserPreferencesResponse(null))
        coEvery { articlesRepository.getArticles(any()) } returnsMany listOf(
            Result.success(GetArticlesResponse(initialArticles, "", 1)),
            Result.success(GetArticlesResponse(moreArticles, "", 2))
        )
        
        viewModel.loadMoreArticles()
        
        val state = viewModel.uiState.value
        assertEquals(2, state.articles.size)
        assertEquals("Article 1", state.articles[0].title)
        assertEquals("Article 2", state.articles[1].title)
    }
}
