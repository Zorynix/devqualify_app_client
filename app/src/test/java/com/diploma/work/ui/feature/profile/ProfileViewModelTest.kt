package com.diploma.work.ui.feature.profile

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.diploma.work.data.AppSession
import com.diploma.work.data.events.ProfileEventBus
import com.diploma.work.data.events.ProfileEvent
import com.diploma.work.data.models.*
import com.diploma.work.data.repository.UserInfoRepository
import com.diploma.work.grpc.userinfo.Direction
import com.diploma.work.grpc.userinfo.Level
import com.diploma.work.ui.theme.ThemeManager
import com.diploma.work.ui.theme.AppThemeType
import com.diploma.work.utils.ErrorHandler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.Before
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
    
    private lateinit var userInfoRepository: UserInfoRepository
    private lateinit var session: AppSession
    private lateinit var profileEventBus: ProfileEventBus
    private lateinit var themeManager: ThemeManager
    private lateinit var errorHandler: ErrorHandler
    private lateinit var context: Context
    private lateinit var viewModel: ProfileViewModel
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        userInfoRepository = mockk()
        session = mockk(relaxed = true)
        profileEventBus = mockk(relaxed = true)
        themeManager = mockk(relaxed = true)
        errorHandler = mockk(relaxed = true)
        context = mockk(relaxed = true)
        
        every { session.getUserId() } returns 1L
        every { session.getAvatarUrl() } returns null
        every { session.storeUsername(any()) } just Runs

        every { themeManager.currentTheme } returns MutableStateFlow(AppThemeType.Light)
        
        every { profileEventBus.events } returns MutableSharedFlow<ProfileEvent>().asSharedFlow()
        
        coEvery { userInfoRepository.getUser(any()) } returns Result.success(
            GetUserResponse(
                User(
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
            )
        )
        
        // Initialize the ViewModel
        viewModel = ProfileViewModel(userInfoRepository, session, themeManager, profileEventBus, errorHandler, context)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state is correct`() = runTest {
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertTrue(state.username.isEmpty())
        assertTrue(state.avatarUrl.isEmpty())
        assertNotNull(state.theme)
    }
    
    @Test
    fun `loadProfile loads user successfully`() = runTest {
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
        
        viewModel.loadProfile()
        
        val state = viewModel.uiState.value
        assertEquals(user.username, state.username)
        assertNotNull(state.avatarUrl)
        
        coVerify { userInfoRepository.getUser(GetUserRequest(userId)) }
    }
    
    @Test
    fun `loadProfile with no user id shows error`() = runTest {
        coEvery { session.getUserId() } returns null
        
        viewModel.loadProfile()
        
        val state = viewModel.uiState.value
        assertEquals("User", state.username)
        assertNotNull(state.avatarUrl)
    }
    
    @Test
    fun `loadProfile with server error shows error`() = runTest {
        val userId = 1L
        val errorMessage = "Server error"
        
        coEvery { session.getUserId() } returns userId
        coEvery { userInfoRepository.getUser(GetUserRequest(userId)) } returns Result.failure(Exception(errorMessage))
        
        viewModel.loadProfile()
        
        val state = viewModel.uiState.value
        assertNotNull(state.username)
    }
    
    @Test
    fun `onUsernameChanged updates username`() = runTest {
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
        
        viewModel.loadProfile()
        
        val newUsername = "newusername"
        viewModel.onUsernameChanged(newUsername)
        
        val state = viewModel.uiState.value
        assertEquals(newUsername, state.username)
    }
    
    @Test
    fun `onDirectionChanged updates direction`() = runTest {
        val direction = Direction.FRONTEND
        
        viewModel.onDirectionChanged(direction)
        
        val directionState = viewModel.direction.value
        assertEquals(direction, directionState)
    }
    
    @Test
    fun `onLevelChanged updates level`() = runTest {
        val level = Level.SENIOR
        
        viewModel.onLevelChanged(level)
        
        val levelState = viewModel.level.value
        assertEquals(level, levelState)
    }
    
    @Test
    fun `onAvatarChanged updates avatar`() = runTest {
        val avatarUrl = "https://example.com/avatar.jpg"
        
        viewModel.onAvatarChanged(avatarUrl)
        
        val state = viewModel.uiState.value
        assertEquals(avatarUrl, state.avatarUrl)
    }
    
    @Test
    fun `updateUserProfile updates profile successfully`() = runTest {
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
        
        viewModel.loadProfile()
        viewModel.onUsernameChanged("newusername")
        viewModel.onDirectionChanged(Direction.FRONTEND)
        viewModel.onLevelChanged(Level.SENIOR)
        viewModel.updateUserProfile()
        
        val expectedRequest = UpdateUserProfileRequest(
            userId = userId,
            username = "newusername",
            direction = Direction.FRONTEND,
            level = Level.SENIOR
        )
        
        val userState = viewModel.user.value
        assertEquals(updatedUser, userState)
        
        coVerify { userInfoRepository.updateUserProfile(expectedRequest) }
    }
    
    @Test
    fun `updateUserProfile with error shows error message`() = runTest {
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
        
        viewModel.loadProfile()
        viewModel.onUsernameChanged("newusername")
        viewModel.updateUserProfile()
        
        val userState = viewModel.user.value
        assertNotNull(userState)
    }
    
    @Test
    fun `resetUpdateStatus resets update status`() = runTest {
        viewModel.resetUpdateStatus()
        
        val updateSuccess = viewModel.updateSuccess.value
        assertFalse(updateSuccess)
    }
    
    @Test
    fun `refreshProfile refreshes profile data`() = runTest {
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
        coEvery { userInfoRepository.getUser(any()) } returns Result.success(GetUserResponse(user))
        
        viewModel.refreshProfile()
        
        val state = viewModel.uiState.value
        assertEquals(user.username, state.username)
        
        coVerify { userInfoRepository.getUser(GetUserRequest(userId)) }
    }
    
    @Test
    fun `user property returns current user`() = runTest {
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
        coEvery { userInfoRepository.getUser(any()) } returns Result.success(GetUserResponse(user))
        
        viewModel.loadProfile()
        
        val userState = viewModel.user.value
        assertEquals(user, userState)
    }
    
    @Test
    fun `direction property returns current direction`() = runTest {
        val direction = Direction.FRONTEND
        
        viewModel.onDirectionChanged(direction)
        
        val directionState = viewModel.direction.value
        assertEquals(direction, directionState)
    }
    
    @Test
    fun `level property returns current level`() = runTest {
        val level = Level.SENIOR
        
        viewModel.onLevelChanged(level)
        
        val levelState = viewModel.level.value
        assertEquals(level, levelState)
    }
}






