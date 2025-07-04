package com.diploma.work.ui.feature.profile

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.diploma.work.data.AppSession
import com.diploma.work.data.events.ProfileEventBus
import com.diploma.work.data.models.*
import com.diploma.work.data.repository.UserInfoRepository
import com.diploma.work.grpc.userinfo.Direction
import com.diploma.work.grpc.userinfo.Level
import com.diploma.work.ui.theme.ThemeManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = TestCoroutineDispatcher()
    
    private lateinit var userInfoRepository: UserInfoRepository
    private lateinit var session: AppSession
    private lateinit var profileEventBus: ProfileEventBus
    private lateinit var themeManager: ThemeManager
    private lateinit var viewModel: ProfileViewModel
    
    @Before
    fun setup() {
        userInfoRepository = mockk()
        session = mockk(relaxed = true)
        profileEventBus = mockk(relaxed = true)
        themeManager = mockk(relaxed = true)
        viewModel = ProfileViewModel(userInfoRepository, session, profileEventBus, themeManager)
    }
    
    @Test
    fun `initial state is loading`() {
        assertTrue(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.user)
        assertNull(viewModel.uiState.value.error)
    }
    
    @Test
    fun `loadUserProfile loads user successfully`() = runTest {
        val userId = 1L
        val user = User(
            id = userId,
            username = "testuser",
            email = "test@example.com",
            direction = Direction.BACKEND,
            level = Level.JUNIOR,
            totalCorrectAnswers = 10,
            totalIncorrectAnswers = 5,
            completedTestsCount = 3,
            achievementsCount = 2,
            achievements = emptyList(),
            avatarUrl = ""
        )
        
        coEvery { session.getUserId() } returns userId
        coEvery { userInfoRepository.getUser(GetUserRequest(userId)) } returns Result.success(GetUserResponse(user))
        
        viewModel.loadUserProfile()
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(user, state.user)
        assertNull(state.error)
        coVerify { userInfoRepository.getUser(GetUserRequest(userId)) }
    }
    
    @Test
    fun `loadUserProfile with no user id shows error`() = runTest {
        coEvery { session.getUserId() } returns null
        
        viewModel.loadUserProfile()
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.user)
        assertNotNull(state.error)
        assertTrue(state.error?.contains("User not found") == true)
    }
    
    @Test
    fun `loadUserProfile with server error shows error`() = runTest {
        val userId = 1L
        val errorMessage = "Server error"
        
        coEvery { session.getUserId() } returns userId
        coEvery { userInfoRepository.getUser(GetUserRequest(userId)) } returns Result.failure(Exception(errorMessage))
        
        viewModel.loadUserProfile()
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.user)
        assertEquals(errorMessage, state.error)
    }
    
    @Test
    fun `startEditingProfile enables edit mode`() = runTest {
        val user = User(
            id = 1L,
            username = "testuser",
            email = "test@example.com",
            direction = Direction.BACKEND,
            level = Level.JUNIOR,
            totalCorrectAnswers = 10,
            totalIncorrectAnswers = 5,
            completedTestsCount = 3,
            achievementsCount = 2,
            achievements = emptyList(),
            avatarUrl = ""
        )
        
        coEvery { session.getUserId() } returns 1L
        coEvery { userInfoRepository.getUser(any()) } returns Result.success(GetUserResponse(user))
        
        viewModel.loadUserProfile()
        viewModel.startEditingProfile()
        
        val state = viewModel.uiState.value
        assertTrue(state.isEditingProfile)
        assertEquals(user.username, state.editingUsername)
        assertEquals(user.direction, state.editingDirection)
        assertEquals(user.level, state.editingLevel)
    }
    
    @Test
    fun `cancelEditingProfile disables edit mode`() = runTest {
        viewModel.startEditingProfile()
        viewModel.cancelEditingProfile()
        
        val state = viewModel.uiState.value
        assertFalse(state.isEditingProfile)
        assertTrue(state.editingUsername.isEmpty())
        assertEquals(Direction.DIRECTION_UNSPECIFIED, state.editingDirection)
        assertEquals(Level.LEVEL_UNSPECIFIED, state.editingLevel)
    }
    
    @Test
    fun `setEditingUsername updates editing username`() = runTest {
        val username = "newusername"
        
        viewModel.setEditingUsername(username)
        
        val state = viewModel.uiState.value
        assertEquals(username, state.editingUsername)
    }
    
    @Test
    fun `setEditingDirection updates editing direction`() = runTest {
        val direction = Direction.FRONTEND
        
        viewModel.setEditingDirection(direction)
        
        val state = viewModel.uiState.value
        assertEquals(direction, state.editingDirection)
    }
    
    @Test
    fun `setEditingLevel updates editing level`() = runTest {
        val level = Level.SENIOR
        
        viewModel.setEditingLevel(level)
        
        val state = viewModel.uiState.value
        assertEquals(level, state.editingLevel)
    }
    
    @Test
    fun `saveProfile saves profile successfully`() = runTest {
        val userId = 1L
        val originalUser = User(
            id = userId,
            username = "oldusername",
            email = "test@example.com",
            direction = Direction.BACKEND,
            level = Level.JUNIOR,
            totalCorrectAnswers = 10,
            totalIncorrectAnswers = 5,
            completedTestsCount = 3,
            achievementsCount = 2,
            achievements = emptyList(),
            avatarUrl = ""
        )
        val updatedUser = originalUser.copy(
            username = "newusername",
            direction = Direction.FRONTEND,
            level = Level.SENIOR
        )
        
        coEvery { session.getUserId() } returns userId
        coEvery { userInfoRepository.getUser(any()) } returns Result.success(GetUserResponse(originalUser))
        coEvery { userInfoRepository.updateUserProfile(any()) } returns Result.success(UpdateUserProfileResponse(updatedUser))
        
        viewModel.loadUserProfile()
        viewModel.startEditingProfile()
        viewModel.setEditingUsername("newusername")
        viewModel.setEditingDirection(Direction.FRONTEND)
        viewModel.setEditingLevel(Level.SENIOR)
        viewModel.saveProfile()
        
        val expectedRequest = UpdateUserProfileRequest(
            userId = userId,
            username = "newusername",
            direction = Direction.FRONTEND,
            level = Level.SENIOR
        )
        
        val state = viewModel.uiState.value
        assertFalse(state.isEditingProfile)
        assertFalse(state.isSavingProfile)
        assertEquals(updatedUser, state.user)
        assertNull(state.error)
        
        coVerify { userInfoRepository.updateUserProfile(expectedRequest) }
    }
    
    @Test
    fun `saveProfile with error shows error message`() = runTest {
        val userId = 1L
        val user = User(
            id = userId,
            username = "testuser",
            email = "test@example.com",
            direction = Direction.BACKEND,
            level = Level.JUNIOR,
            totalCorrectAnswers = 10,
            totalIncorrectAnswers = 5,
            completedTestsCount = 3,
            achievementsCount = 2,
            achievements = emptyList(),
            avatarUrl = ""
        )
        val errorMessage = "Failed to save profile"
        
        coEvery { session.getUserId() } returns userId
        coEvery { userInfoRepository.getUser(any()) } returns Result.success(GetUserResponse(user))
        coEvery { userInfoRepository.updateUserProfile(any()) } returns Result.failure(Exception(errorMessage))
        
        viewModel.loadUserProfile()
        viewModel.startEditingProfile()
        viewModel.setEditingUsername("newusername")
        viewModel.saveProfile()
        
        val state = viewModel.uiState.value
        assertFalse(state.isSavingProfile)
        assertEquals(errorMessage, state.error)
    }
    
    @Test
    fun `logout clears token and navigates`() = runTest {
        viewModel.logout()
        
        coVerify { session.clearToken() }
    }
    
    @Test
    fun `clearError clears error message`() = runTest {
        viewModel.clearError()
        
        val state = viewModel.uiState.value
        assertNull(state.error)
    }
    
    @Test
    fun `calculateAccuracy calculates accuracy correctly`() = runTest {
        val user = User(
            id = 1L,
            username = "testuser",
            email = "test@example.com",
            direction = Direction.BACKEND,
            level = Level.JUNIOR,
            totalCorrectAnswers = 80,
            totalIncorrectAnswers = 20,
            completedTestsCount = 3,
            achievementsCount = 2,
            achievements = emptyList(),
            avatarUrl = ""
        )
        
        coEvery { session.getUserId() } returns 1L
        coEvery { userInfoRepository.getUser(any()) } returns Result.success(GetUserResponse(user))
        
        viewModel.loadUserProfile()
        
        val accuracy = viewModel.calculateAccuracy()
        assertEquals(80.0, accuracy, 0.01)
    }
    
    @Test
    fun `calculateAccuracy returns zero for no answers`() = runTest {
        val user = User(
            id = 1L,
            username = "testuser",
            email = "test@example.com",
            direction = Direction.BACKEND,
            level = Level.JUNIOR,
            totalCorrectAnswers = 0,
            totalIncorrectAnswers = 0,
            completedTestsCount = 0,
            achievementsCount = 0,
            achievements = emptyList(),
            avatarUrl = ""
        )
        
        coEvery { session.getUserId() } returns 1L
        coEvery { userInfoRepository.getUser(any()) } returns Result.success(GetUserResponse(user))
        
        viewModel.loadUserProfile()
        
        val accuracy = viewModel.calculateAccuracy()
        assertEquals(0.0, accuracy, 0.01)
    }
}
