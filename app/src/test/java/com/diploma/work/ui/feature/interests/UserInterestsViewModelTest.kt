package com.diploma.work.ui.feature.interests

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.diploma.work.data.AppSession
import com.diploma.work.data.models.*
import com.diploma.work.data.repository.ArticlesRepository
import com.diploma.work.utils.ErrorHandler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.Assert.*
import org.junit.Before
import org.junit.After
import org.junit.Rule
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class UserInterestsViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
    
    private lateinit var articlesRepository: ArticlesRepository
    private lateinit var session: AppSession
    private lateinit var errorHandler: ErrorHandler
    private lateinit var viewModel: UserInterestsViewModel
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        articlesRepository = mockk()
        session = mockk(relaxed = true)
        errorHandler = mockk(relaxed = true)
        
        every { session.getUserId() } returns 123L
        every { session.getUserPreferences() } returns null
        
        coEvery { 
            articlesRepository.getTechnologies(any()) 
        } returns Result.success(GetTechnologiesResponse(emptyList(), ""))
        
        coEvery { 
            articlesRepository.getUserPreferences(any()) 
        } returns Result.success(GetUserPreferencesResponse(null))
        
        // Initialize the ViewModel
        viewModel = UserInterestsViewModel(articlesRepository, session, errorHandler)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state is correct`() = runTest {
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertTrue(state.technologies.isEmpty())
        assertTrue(state.selectedTechnologyIds.isEmpty())
        assertTrue(state.selectedDirections.isEmpty())
        assertEquals(DeliveryFrequency.WEEKLY, state.deliveryFrequency)
        assertTrue(state.emailNotifications)
        assertTrue(state.pushNotifications)
        assertEquals(20, state.articlesPerDay)
        assertNull(state.error)
        assertFalse(state.saveSuccess)
    }
    
    @Test
    fun `loadData success with cached preferences`() = runTest {
        val technologies = listOf(
            ArticleTechnology(1L, "Kotlin", "kotlin", ArticleDirection.BACKEND, ""),
            ArticleTechnology(2L, "Java", "java", ArticleDirection.BACKEND, "")
        )
        
        val cachedPreferences = UserPreferences(
            userId = 123L,
            technologyIds = listOf(1L),
            directions = listOf(ArticleDirection.BACKEND),
            deliveryFrequency = DeliveryFrequency.DAILY,
            emailNotifications = false,
            pushNotifications = true,
            excludedSources = emptyList(),
            articlesPerDay = 30,
            updatedAt = Instant.now()
        )
        
        every { session.getUserPreferences() } returns cachedPreferences
        coEvery { 
            articlesRepository.getTechnologies(any()) 
        } returns Result.success(GetTechnologiesResponse(technologies, ""))
        
        val viewModel = UserInterestsViewModel(articlesRepository, session, errorHandler)
        
        val state = viewModel.uiState.value
        assertEquals(2, state.technologies.size)
        assertEquals(setOf(1L), state.selectedTechnologyIds)
        assertEquals(setOf(ArticleDirection.BACKEND), state.selectedDirections)
        assertEquals(DeliveryFrequency.DAILY, state.deliveryFrequency)
        assertFalse(state.emailNotifications)
        assertTrue(state.pushNotifications)
        assertEquals(30, state.articlesPerDay)
        
        coVerify { articlesRepository.getTechnologies(any()) }
        coVerify(exactly = 0) { articlesRepository.getUserPreferences(any()) }
    }
    
    @Test
    fun `loadData success with server preferences`() = runTest {
        val technologies = listOf(
            ArticleTechnology(1L, "React", "react", ArticleDirection.FRONTEND, ""),
            ArticleTechnology(2L, "Vue", "vue", ArticleDirection.FRONTEND, "")
        )
        
        val serverPreferences = UserPreferences(
            userId = 123L,
            technologyIds = listOf(2L),
            directions = listOf(ArticleDirection.FRONTEND, ArticleDirection.BACKEND),
            deliveryFrequency = DeliveryFrequency.WEEKLY,
            emailNotifications = true,
            pushNotifications = false,
            excludedSources = emptyList(),
            articlesPerDay = 15,
            updatedAt = Instant.now()
        )
        
        coEvery { 
            articlesRepository.getTechnologies(any()) 
        } returns Result.success(GetTechnologiesResponse(technologies, ""))
        
        coEvery { 
            articlesRepository.getUserPreferences(any()) 
        } returns Result.success(GetUserPreferencesResponse(serverPreferences))
        
        viewModel = UserInterestsViewModel(articlesRepository, session, errorHandler)
        
        val state = viewModel.uiState.value
        assertEquals(2, state.technologies.size)
        assertEquals(setOf(2L), state.selectedTechnologyIds)
        assertEquals(setOf(ArticleDirection.FRONTEND, ArticleDirection.BACKEND), state.selectedDirections)
        assertEquals(DeliveryFrequency.WEEKLY, state.deliveryFrequency)
        assertTrue(state.emailNotifications)
        assertFalse(state.pushNotifications)
        assertEquals(15, state.articlesPerDay)
        
        verify { session.storeUserPreferences(serverPreferences) }
    }
    
    @Test
    fun `loadData failure updates error state`() = runTest {
        coEvery { 
            articlesRepository.getTechnologies(any()) 
        } returns Result.failure(Exception("Network error"))
        
        every { errorHandler.getErrorMessage(any()) } returns "Network error"
        
        viewModel = UserInterestsViewModel(articlesRepository, session, errorHandler)
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Network error", state.error)
    }
    
    @Test
    fun `toggleTechnology adds technology when not selected`() = runTest {
        viewModel.toggleTechnology(1L)
        
        val state = viewModel.uiState.value
        assertTrue(state.selectedTechnologyIds.contains(1L))
    }
    
    @Test
    fun `toggleTechnology removes technology when already selected`() = runTest {
        val technologies = listOf(
            ArticleTechnology(1L, "Kotlin", "kotlin", ArticleDirection.BACKEND, ""),
            ArticleTechnology(2L, "Java", "java", ArticleDirection.BACKEND, "")
        )
        
        coEvery { 
            articlesRepository.getTechnologies(any()) 
        } returns Result.success(GetTechnologiesResponse(technologies, ""))
        
        val viewModel = UserInterestsViewModel(articlesRepository, session, errorHandler)
        
        viewModel.toggleTechnology(1L)
        viewModel.toggleTechnology(2L)
        
        viewModel.toggleTechnology(1L)
        
        val state = viewModel.uiState.value
        assertFalse(state.selectedTechnologyIds.contains(1L))
        assertTrue(state.selectedTechnologyIds.contains(2L))
    }
    
    @Test
    fun `toggleDirection adds direction when not selected`() = runTest {
        viewModel.toggleDirection(ArticleDirection.BACKEND)
        
        val state = viewModel.uiState.value
        assertTrue(state.selectedDirections.contains(ArticleDirection.BACKEND))
    }
    
    @Test
    fun `toggleDirection removes direction when already selected`() = runTest {
        coEvery { 
            articlesRepository.getTechnologies(any()) 
        } returns Result.success(GetTechnologiesResponse(emptyList(), ""))
        
        val viewModel = UserInterestsViewModel(articlesRepository, session, errorHandler)
        
        viewModel.toggleDirection(ArticleDirection.BACKEND)
        viewModel.toggleDirection(ArticleDirection.FRONTEND)
        
        viewModel.toggleDirection(ArticleDirection.BACKEND)
        
        val state = viewModel.uiState.value
        assertFalse(state.selectedDirections.contains(ArticleDirection.BACKEND))
        assertTrue(state.selectedDirections.contains(ArticleDirection.FRONTEND))
    }
    
    @Test
    fun `setDeliveryFrequency changes frequency`() = runTest {
        viewModel.setDeliveryFrequency(DeliveryFrequency.DAILY)
        
        val state = viewModel.uiState.value
        assertEquals(DeliveryFrequency.DAILY, state.deliveryFrequency)
    }
    
    @Test
    fun `setEmailNotifications changes email setting`() = runTest {
        viewModel.setEmailNotifications(false)
        
        val state = viewModel.uiState.value
        assertFalse(state.emailNotifications)
    }
    
    @Test
    fun `setPushNotifications changes push setting`() = runTest {
        viewModel.setPushNotifications(false)
        
        val state = viewModel.uiState.value
        assertFalse(state.pushNotifications)
    }
    
    @Test
    fun `setArticlesPerDay changes articles count`() = runTest {
        viewModel.setArticlesPerDay(50)
        
        val state = viewModel.uiState.value
        assertEquals(50, state.articlesPerDay)
    }
    
    @Test
    fun `savePreferences success updates state`() = runTest {
        val userId = 123L
        val preferences = UserPreferences(
            userId = userId,
            technologyIds = listOf(1L, 2L),
            directions = listOf(ArticleDirection.BACKEND),
            deliveryFrequency = DeliveryFrequency.DAILY,
            emailNotifications = true,
            pushNotifications = false,
            excludedSources = emptyList(),
            articlesPerDay = 25,
            updatedAt = Instant.now()
        )
        
        viewModel.toggleTechnology(1L)
        viewModel.toggleTechnology(2L)
        viewModel.toggleDirection(ArticleDirection.BACKEND)
        viewModel.setDeliveryFrequency(DeliveryFrequency.DAILY)
        viewModel.setEmailNotifications(true)
        viewModel.setPushNotifications(false)
        viewModel.setArticlesPerDay(25)
        
        coEvery { 
            articlesRepository.updateUserPreferences(any()) 
        } returns Result.success(UpdateUserPreferencesResponse("Success"))
        
        viewModel.savePreferences()
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.saveSuccess)
        assertNull(state.error)
        
        verify { session.storeUserPreferences(preferences) }
        coVerify { articlesRepository.updateUserPreferences(any()) }
    }
    
    @Test
    fun `savePreferences failure updates error state`() = runTest {
        val errorMessage = "Failed to save preferences"
        
        coEvery { 
            articlesRepository.updateUserPreferences(any()) 
        } returns Result.failure(Exception(errorMessage))
        
        every { errorHandler.getErrorMessage(any()) } returns errorMessage
        
        viewModel.savePreferences()
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.saveSuccess)
        assertEquals(errorMessage, state.error)
    }
    
    @Test
    fun `clearError resets error state`() = runTest {
        coEvery { 
            articlesRepository.updateUserPreferences(any()) 
        } returns Result.failure(Exception("Test error"))
        
        viewModel.savePreferences()
        
        assertNotNull(viewModel.uiState.value.error)
        
        viewModel.clearError()
        
        val state = viewModel.uiState.value
        assertNull(state.error)
    }
    
    @Test
    fun `clearSaveSuccess resets save success state`() = runTest {
        coEvery { 
            articlesRepository.updateUserPreferences(any()) 
        } returns Result.success(UpdateUserPreferencesResponse("Success"))
        
        viewModel.savePreferences()
        
        assertTrue(viewModel.uiState.value.saveSuccess)
        
        viewModel.clearSaveSuccess()
        
        val state = viewModel.uiState.value
        assertFalse(state.saveSuccess)
    }
}






