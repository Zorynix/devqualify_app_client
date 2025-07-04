package com.diploma.work.ui.feature.achievements

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.diploma.work.data.AppSession
import com.diploma.work.data.models.Achievement
import com.diploma.work.data.models.GetUserAchievementsRequest
import com.diploma.work.data.models.GetUserAchievementsResponse
import com.diploma.work.data.repository.UserInfoRepository
import com.diploma.work.utils.ErrorHandler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AchievementsViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var userInfoRepository: UserInfoRepository
    private lateinit var session: AppSession
    private lateinit var errorHandler: ErrorHandler
    private lateinit var viewModel: AchievementsViewModel
    
    @Before
    fun setup() {
        userInfoRepository = mockk()
        session = mockk(relaxed = true)
        errorHandler = mockk()
        
        every { session.getUserId() } returns 123L
        every { errorHandler.getContextualErrorMessage(any(), any()) } returns "Generic error"
        
        coEvery { 
            userInfoRepository.getUserAchievements(any()) 
        } returns Result.success(GetUserAchievementsResponse(emptyList()))
        
        viewModel = AchievementsViewModel(userInfoRepository, session, errorHandler)
    }
    
    @Test
    fun `initial state is correct`() {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.achievements.isEmpty())
        assertNull(state.errorMessage)
        assertNull(state.selectedAchievement)
        assertFalse(state.showAchievementDetails)
    }
    
    @Test
    fun `loadAchievements success updates state correctly`() = runTest {
        val achievements = listOf(
            Achievement(
                id = 1L,
                name = "First Test",
                description = "Complete your first test",
                iconUrl = "icon1.png",
                dateEarned = "2023-01-01"
            ),
            Achievement(
                id = 2L,
                name = "Speed Runner",
                description = "Complete a test in under 5 minutes",
                iconUrl = "icon2.png",
                dateEarned = "2023-01-15"
            )
        )
        
        val response = GetUserAchievementsResponse(achievements)
        
        coEvery { 
            userInfoRepository.getUserAchievements(GetUserAchievementsRequest(123L)) 
        } returns Result.success(response)
        
        viewModel = AchievementsViewModel(userInfoRepository, session, errorHandler)
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.achievements.size)
        assertEquals("First Test", state.achievements[0].name)
        assertEquals("Speed Runner", state.achievements[1].name)
        assertNull(state.errorMessage)
        
        coVerify { userInfoRepository.getUserAchievements(GetUserAchievementsRequest(123L)) }
    }
    
    @Test
    fun `loadAchievements failure updates error state`() = runTest {
        val errorMessage = "Network error"
        
        coEvery { 
            userInfoRepository.getUserAchievements(any()) 
        } returns Result.failure(Exception(errorMessage))
        
        every { 
            errorHandler.getContextualErrorMessage(any(), ErrorHandler.ErrorContext.DATA_LOADING) 
        } returns errorMessage
        
        viewModel = AchievementsViewModel(userInfoRepository, session, errorHandler)
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.achievements.isEmpty())
        assertEquals(errorMessage, state.errorMessage)
    }
    
    @Test
    fun `loadAchievements with null user ID shows error`() = runTest {
        every { session.getUserId() } returns null
        
        viewModel = AchievementsViewModel(userInfoRepository, session, errorHandler)
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.achievements.isEmpty())
        assertEquals("Не удалось определить пользователя. Попробуйте войти в систему заново.", state.errorMessage)
        
        coVerify(exactly = 0) { userInfoRepository.getUserAchievements(any()) }
    }
    
    @Test
    fun `showAchievementDetails updates state correctly`() {
        val achievement = Achievement(
            id = 1L,
            name = "Test Achievement",
            description = "Test description",
            iconUrl = "test.png",
            dateEarned = "2023-01-01"
        )
        
        viewModel.showAchievementDetails(achievement)
        
        val state = viewModel.uiState.value
        assertEquals(achievement, state.selectedAchievement)
        assertTrue(state.showAchievementDetails)
    }
    
    @Test
    fun `dismissAchievementDetails hides details`() {
        val achievement = Achievement(
            id = 1L,
            name = "Test Achievement",
            description = "Test description",
            iconUrl = "test.png",
            dateEarned = "2023-01-01"
        )
        
        viewModel.showAchievementDetails(achievement)
        assertTrue(viewModel.uiState.value.showAchievementDetails)
        
        viewModel.dismissAchievementDetails()
        
        val state = viewModel.uiState.value
        assertFalse(state.showAchievementDetails)
    }
    
    @Test
    fun `clearError resets error state`() {
        every { session.getUserId() } returns null
        
        viewModel.loadAchievements()
        
        val errorState = viewModel.uiState.value
        assertNotNull(errorState.errorMessage)
        
        viewModel.clearError()
        
        val state = viewModel.uiState.value
        assertNull(state.errorMessage)
    }
    
    @Test
    fun `loadAchievements can be called multiple times`() = runTest {
        val achievements1 = listOf(
            Achievement(
                id = 1L,
                name = "Achievement 1",
                description = "First achievement",
                iconUrl = "icon1.png",
                dateEarned = "2023-01-01"
            )
        )
        
        val achievements2 = listOf(
            Achievement(
                id = 1L,
                name = "Achievement 1",
                description = "First achievement",
                iconUrl = "icon1.png",
                dateEarned = "2023-01-01"
            ),
            Achievement(
                id = 2L,
                name = "Achievement 2",
                description = "Second achievement",
                iconUrl = "icon2.png",
                dateEarned = "2023-01-02"
            )
        )
        
        coEvery { 
            userInfoRepository.getUserAchievements(any()) 
        } returns Result.success(GetUserAchievementsResponse(achievements1))
        
        viewModel.loadAchievements()
        
        assertEquals(1, viewModel.uiState.value.achievements.size)
        
        coEvery { 
            userInfoRepository.getUserAchievements(any()) 
        } returns Result.success(GetUserAchievementsResponse(achievements2))
        
        viewModel.loadAchievements()
        
        assertEquals(2, viewModel.uiState.value.achievements.size)
        
        coVerify(exactly = 3) { userInfoRepository.getUserAchievements(any()) }
    }
    
    @Test
    fun `loadAchievements clears previous error state`() = runTest {
        every { session.getUserId() } returns null
        viewModel.loadAchievements()

        assertNotNull(viewModel.uiState.value.errorMessage)
        
        every { session.getUserId() } returns 123L
        
        val achievements = listOf(
            Achievement(
                id = 1L,
                name = "Test Achievement",
                description = "Test description",
                iconUrl = "test.png",
                dateEarned = "2023-01-01"
            )
        )
        
        coEvery { 
            userInfoRepository.getUserAchievements(any()) 
        } returns Result.success(GetUserAchievementsResponse(achievements))
        
        viewModel.loadAchievements()
        
        val state = viewModel.uiState.value
        assertNull(state.errorMessage)
        assertEquals(1, state.achievements.size)
    }
    
    @Test
    fun `selectedAchievement persists across state changes`() {
        val achievement = Achievement(
            id = 1L,
            name = "Persistent Achievement",
            description = "This should persist",
            iconUrl = "persistent.png",
            dateEarned = "2023-01-01"
        )
        
        viewModel.showAchievementDetails(achievement)
        assertEquals(achievement, viewModel.uiState.value.selectedAchievement)
        
        viewModel.clearError()
        assertEquals(achievement, viewModel.uiState.value.selectedAchievement)
        
        viewModel.dismissAchievementDetails()
        assertEquals(achievement, viewModel.uiState.value.selectedAchievement)
    }
}
