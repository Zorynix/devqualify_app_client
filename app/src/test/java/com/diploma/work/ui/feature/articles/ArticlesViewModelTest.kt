package com.diploma.work.ui.feature.articles

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.diploma.work.data.AppSession
import com.diploma.work.data.models.*
import com.diploma.work.data.repository.ArticlesRepository
import com.diploma.work.utils.ErrorHandler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class ArticlesViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
    
    private lateinit var articlesRepository: ArticlesRepository
    private lateinit var session: AppSession
    private lateinit var errorHandler: ErrorHandler
    private lateinit var viewModel: ArticlesViewModel
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        articlesRepository = mockk()
        session = mockk()
        errorHandler = mockk()
        
        coEvery { articlesRepository.getTechnologies(any()) } returns Result.success(GetTechnologiesResponse(emptyList(), ""))
        coEvery { articlesRepository.getArticles(any()) } returns Result.success(GetArticlesResponse(emptyList(), "", 0))
        coEvery { session.getUserId() } returns 1L
        coEvery { session.getUserPreferences() } returns null
        coEvery { articlesRepository.getUserPreferences(any()) } returns Result.success(GetUserPreferencesResponse(null))
        
        viewModel = ArticlesViewModel(articlesRepository, session, errorHandler)
        
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    private fun createViewModel(): ArticlesViewModel {
        return ArticlesViewModel(articlesRepository, session, errorHandler)
    }
    
    @Test
    fun `initial state is loading`() = runTest {
        advanceUntilIdle()
        
        val finalState = viewModel.uiState.value
        assertFalse(finalState.isLoading)
        assertTrue(finalState.articles.isEmpty())
        assertNull(finalState.error)
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
        
        viewModel = ArticlesViewModel(articlesRepository, session, errorHandler)
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(technologies, state.technologies)
        assertEquals(articles, state.articles)
        assertNull(state.error)
    }
    
    @Test
    fun `initialization with error shows error message`() = runTest {
        val errorMessage = "Failed to load technologies"
        
        coEvery { articlesRepository.getTechnologies(any()) } throws Exception(errorMessage)
        
        viewModel = ArticlesViewModel(articlesRepository, session, errorHandler)
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertNotNull(state.error)
    }
    
    @Test
    fun `setSearchQuery updates search query`() = runTest {
        val query = "kotlin"
        
        viewModel = ArticlesViewModel(articlesRepository, session, errorHandler)
        advanceUntilIdle()
        
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
        
        viewModel = ArticlesViewModel(articlesRepository, session, errorHandler)
        advanceUntilIdle()
        
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
        
        createViewModel()
        
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
        
        createViewModel()
        
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
        
        viewModel = ArticlesViewModel(articlesRepository, session, errorHandler)
        advanceUntilIdle()
        
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
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
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
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
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
        coEvery { articlesRepository.getTechnologies(any()) } returns Result.success(GetTechnologiesResponse(emptyList(), ""))
        coEvery { session.getUserId() } returns 1L
        coEvery { session.getUserPreferences() } returns null
        coEvery { articlesRepository.getUserPreferences(any()) } returns Result.success(GetUserPreferencesResponse(null))
        coEvery { articlesRepository.getArticles(any()) } returns Result.success(GetArticlesResponse(emptyList(), "", 0))
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertNotNull(state.isFiltersExpanded)
    }
}






