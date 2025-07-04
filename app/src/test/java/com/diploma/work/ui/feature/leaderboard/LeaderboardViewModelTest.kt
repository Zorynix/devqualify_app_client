package com.diploma.work.ui.feature.leaderboard

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.diploma.work.data.AppSession
import com.diploma.work.data.models.*
import com.diploma.work.data.repository.UserInfoRepository
import com.diploma.work.grpc.userinfo.Direction
import com.diploma.work.grpc.userinfo.Level
import com.diploma.work.utils.ErrorHandler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LeaderboardViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = TestCoroutineDispatcher()
    
    private lateinit var userInfoRepository: UserInfoRepository
    private lateinit var session: AppSession
    private lateinit var errorHandler: ErrorHandler
    private lateinit var viewModel: LeaderboardViewModel
    
    @Before
    fun setup() {
        userInfoRepository = mockk()
        session = mockk(relaxed = true)
        errorHandler = mockk()
        
        every { errorHandler.getErrorMessage(any()) } returns "Generic error"
        
        viewModel = LeaderboardViewModel(userInfoRepository, session, errorHandler)
    }
    
    @Test
    fun `initial state is correct`() {
        val state = viewModel.uiState.value
        assertTrue(state.users.isEmpty())
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(Direction.DIRECTION_UNSPECIFIED, state.direction)
        assertEquals(Level.LEVEL_UNSPECIFIED, state.level)
        assertEquals(LeaderboardSortType.ACHIEVEMENTS, state.sortType)
        assertFalse(state.hasMoreData)
    }
    
    @Test
    fun `loadLeaderboard success updates state correctly`() = runTest {
        val users = listOf(
            User(
                id = 1L,
                username = "user1",
                email = "user1@test.com",
                direction = Direction.FRONTEND,
                level = Level.SENIOR,
                totalCorrectAnswers = 100,
                totalIncorrectAnswers = 10,
                completedTestsCount = 50,
                achievementsCount = 25,
                achievements = emptyList()
            ),
            User(
                id = 2L,
                username = "user2",
                email = "user2@test.com",
                direction = Direction.BACKEND,
                level = Level.MIDDLE,
                totalCorrectAnswers = 80,
                totalIncorrectAnswers = 20,
                completedTestsCount = 40,
                achievementsCount = 20,
                achievements = emptyList()
            )
        )
        
        val response = GetLeaderboardResponse(
            users = users,
            nextPageToken = "next_token_123"
        )
        
        coEvery { 
            userInfoRepository.getLeaderboard(any()) 
        } returns Result.success(response)
        
        viewModel.loadLeaderboard()
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.users.size)
        assertEquals("user1", state.users[0].username)
        assertEquals("user2", state.users[1].username)
        assertEquals("next_token_123", state.nextPageToken)
        assertTrue(state.hasMoreData)
        assertNull(state.errorMessage)
        
        coVerify { userInfoRepository.getLeaderboard(any()) }
    }
    
    @Test
    fun `loadLeaderboard failure updates error state`() = runTest {
        val errorMessage = "Failed to load leaderboard"
        
        coEvery { 
            userInfoRepository.getLeaderboard(any()) 
        } returns Result.failure(Exception(errorMessage))
        
        every { errorHandler.getErrorMessage(any()) } returns errorMessage
        
        viewModel.loadLeaderboard()
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.users.isEmpty())
        assertEquals(errorMessage, state.errorMessage)
        assertFalse(state.hasMoreData)
    }
    
    @Test
    fun `loadMoreUsers appends to existing users`() = runTest {
        val existingUsers = listOf(
            User(
                id = 1L,
                username = "user1",
                email = "user1@test.com",
                direction = Direction.FRONTEND,
                level = Level.SENIOR,
                totalCorrectAnswers = 100,
                totalIncorrectAnswers = 10,
                completedTestsCount = 50,
                achievementsCount = 25,
                achievements = emptyList()
            )
        )
        
        viewModel.uiState.value = viewModel.uiState.value.copy(
            users = existingUsers,
            nextPageToken = "token_123",
            hasMoreData = true
        )
        
        val newUsers = listOf(
            User(
                id = 2L,
                username = "user2",
                email = "user2@test.com",
                direction = Direction.BACKEND,
                level = Level.MIDDLE,
                totalCorrectAnswers = 80,
                totalIncorrectAnswers = 20,
                completedTestsCount = 40,
                achievementsCount = 20,
                achievements = emptyList()
            )
        )
        
        val response = GetLeaderboardResponse(
            users = newUsers,
            nextPageToken = ""
        )
        
        coEvery { 
            userInfoRepository.getLeaderboard(any()) 
        } returns Result.success(response)
        
        viewModel.loadMoreUsers()
        
        val state = viewModel.uiState.value
        assertEquals(2, state.users.size)
        assertEquals("user1", state.users[0].username)
        assertEquals("user2", state.users[1].username)
        assertEquals("", state.nextPageToken)
        assertFalse(state.hasMoreData)
    }
    
    @Test
    fun `updateFilters reloads leaderboard with new filters`() = runTest {
        val response = GetLeaderboardResponse(
            users = emptyList(),
            nextPageToken = ""
        )
        
        coEvery { 
            userInfoRepository.getLeaderboard(any()) 
        } returns Result.success(response)
        
        viewModel.updateFilters(Direction.BACKEND, Level.SENIOR)
        
        val state = viewModel.uiState.value
        assertEquals(Direction.BACKEND, state.direction)
        assertEquals(Level.SENIOR, state.level)
        
        coVerify { userInfoRepository.getLeaderboard(any()) }
    }
    
    @Test
    fun `updateSortType changes sort and reloads`() = runTest {
        val response = GetLeaderboardResponse(
            users = emptyList(),
            nextPageToken = ""
        )
        
        coEvery { 
            userInfoRepository.getLeaderboard(any()) 
        } returns Result.success(response)
        
        viewModel.updateSortType(LeaderboardSortType.COMPLETED_TESTS)
        
        val state = viewModel.uiState.value
        assertEquals(LeaderboardSortType.COMPLETED_TESTS, state.sortType)
        
        coVerify { userInfoRepository.getLeaderboard(any()) }
    }
    
    @Test
    fun `showUserDetail opens user detail dialog`() = runTest {
        val user = User(
            id = 1L,
            username = "testuser",
            email = "test@test.com",
            direction = Direction.FRONTEND,
            level = Level.MIDDLE,
            totalCorrectAnswers = 50,
            totalIncorrectAnswers = 5,
            completedTestsCount = 25,
            achievementsCount = 10,
            achievements = emptyList()
        )
        
        coEvery { 
            userInfoRepository.getUser(GetUserRequest(1L)) 
        } returns Result.success(GetUserResponse(user))
        
        viewModel.showUserDetail(1L)
        
        val state = viewModel.uiState.value
        assertTrue(state.isUserDetailDialogVisible)
        assertEquals(user, state.selectedUser)
        
        coVerify { userInfoRepository.getUser(GetUserRequest(1L)) }
    }
    
    @Test
    fun `hideUserDetail closes user detail dialog`() {
        viewModel.hideUserDetail()
        
        val state = viewModel.uiState.value
        assertFalse(state.isUserDetailDialogVisible)
        assertNull(state.selectedUser)
    }
    
    @Test
    fun `refreshLeaderboard clears data and reloads`() = runTest {
        viewModel.uiState.value = viewModel.uiState.value.copy(
            users = listOf(mockk()),
            nextPageToken = "old_token",
            hasMoreData = true
        )
        
        val response = GetLeaderboardResponse(
            users = emptyList(),
            nextPageToken = ""
        )
        
        coEvery { 
            userInfoRepository.getLeaderboard(any()) 
        } returns Result.success(response)
        
        viewModel.refreshLeaderboard()
        
        val state = viewModel.uiState.value
        assertTrue(state.users.isEmpty())
        assertEquals("", state.nextPageToken)
        assertFalse(state.hasMoreData)
        
        coVerify { userInfoRepository.getLeaderboard(any()) }
    }
    
    @Test
    fun `loadMoreUsers does nothing when no more data available`() = runTest {
        viewModel.uiState.value = viewModel.uiState.value.copy(
            hasMoreData = false
        )
        
        viewModel.loadMoreUsers()
        
        coVerify(exactly = 0) { userInfoRepository.getLeaderboard(any()) }
    }
    
    @Test
    fun `loadMoreUsers does nothing when already loading`() = runTest {
        viewModel.uiState.value = viewModel.uiState.value.copy(
            isLoading = true,
            hasMoreData = true
        )
        
        viewModel.loadMoreUsers()
        
        coVerify(exactly = 0) { userInfoRepository.getLeaderboard(any()) }
    }
}
