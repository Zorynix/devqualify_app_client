package com.diploma.work.data.repository

import com.diploma.work.data.grpc.UserInfoGrpcClient
import com.diploma.work.data.models.*
import com.diploma.work.grpc.userinfo.Direction
import com.diploma.work.grpc.userinfo.Level
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserInfoRepositoryImplTest {
    
    private lateinit var userInfoGrpcClient: UserInfoGrpcClient
    private lateinit var repository: UserInfoRepositoryImpl
    
    @Before
    fun setup() {
        userInfoGrpcClient = mockk()
        repository = UserInfoRepositoryImpl(userInfoGrpcClient)
    }
    
    @Test
    fun `getUser calls grpc client and returns result`() = runTest {
        val request = GetUserRequest(userId = 123L)
        val user = User(
            id = 123L,
            username = "testuser",
            email = "test@example.com",
            direction = Direction.FRONTEND,
            level = Level.MIDDLE,
            totalCorrectAnswers = 50,
            totalIncorrectAnswers = 10,
            completedTestsCount = 25,
            achievementsCount = 5,
            achievements = emptyList()
        )
        val response = GetUserResponse(user)
        
        coEvery { userInfoGrpcClient.getUser(request) } returns Result.success(response)
        
        val result = repository.getUser(request)
        
        assertTrue(result.isSuccess)
        assertEquals(response, result.getOrNull())
        coVerify { userInfoGrpcClient.getUser(request) }
    }
    
    @Test
    fun `getUser handles failure from grpc client`() = runTest {
        val request = GetUserRequest(userId = 123L)
        val exception = Exception("Network error")
        
        coEvery { userInfoGrpcClient.getUser(request) } returns Result.failure(exception)
        
        val result = repository.getUser(request)
        
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify { userInfoGrpcClient.getUser(request) }
    }
    
    @Test
    fun `updateUserProfile calls grpc client and returns result`() = runTest {
        val request = UpdateUserProfileRequest(
            userId = 123L,
            username = "newusername",
            direction = Direction.BACKEND,
            level = Level.SENIOR
        )
        val user = User(
            id = 123L,
            username = "newusername",
            email = "test@example.com",
            direction = Direction.BACKEND,
            level = Level.SENIOR,
            totalCorrectAnswers = 50,
            totalIncorrectAnswers = 10,
            completedTestsCount = 25,
            achievementsCount = 5,
            achievements = emptyList()
        )
        val response = UpdateUserProfileResponse(user)
        
        coEvery { userInfoGrpcClient.updateUserProfile(request) } returns Result.success(response)
        
        val result = repository.updateUserProfile(request)
        
        assertTrue(result.isSuccess)
        assertEquals(response, result.getOrNull())
        coVerify { userInfoGrpcClient.updateUserProfile(request) }
    }
    
    @Test
    fun `getUserTestHistory calls grpc client and returns result`() = runTest {
        val request = GetUserTestHistoryRequest(
            userId = 123L,
            pagination = Pagination(pageSize = 10, pageToken = "")
        )
        val testSummaries = listOf(
            TestSummary(
                id = "1",
                title = "Kotlin Basics",
                completionDate = "2023-01-01",
                score = 85,
                totalPoints = 100
            )
        )
        val response = GetUserTestHistoryResponse(testSummaries, "next_token")
        
        coEvery { userInfoGrpcClient.getUserTestHistory(request) } returns Result.success(response)
        
        val result = repository.getUserTestHistory(request)
        
        assertTrue(result.isSuccess)
        assertEquals(response, result.getOrNull())
        coVerify { userInfoGrpcClient.getUserTestHistory(request) }
    }
    
    @Test
    fun `getUserAchievements calls grpc client and returns result`() = runTest {
        val request = GetUserAchievementsRequest(userId = 123L)
        val achievements = listOf(
            Achievement(
                id = 1L,
                name = "First Test",
                description = "Complete your first test",
                iconUrl = "icon.png",
                dateEarned = "2023-01-01"
            )
        )
        val response = GetUserAchievementsResponse(achievements)
        
        coEvery { userInfoGrpcClient.getUserAchievements(request) } returns Result.success(response)
        
        val result = repository.getUserAchievements(request)
        
        assertTrue(result.isSuccess)
        assertEquals(response, result.getOrNull())
        coVerify { userInfoGrpcClient.getUserAchievements(request) }
    }
    
    @Test
    fun `getLeaderboard calls grpc client and returns result`() = runTest {
        val request = GetLeaderboardRequest(
            direction = Direction.FRONTEND,
            level = Level.MIDDLE,
            pagination = Pagination(pageSize = 20, pageToken = "")
        )
        val users = listOf(
            User(
                id = 1L,
                username = "leader1",
                email = "leader1@example.com",
                direction = Direction.FRONTEND,
                level = Level.MIDDLE,
                totalCorrectAnswers = 100,
                totalIncorrectAnswers = 5,
                completedTestsCount = 50,
                achievementsCount = 10,
                achievements = emptyList()
            )
        )
        val response = GetLeaderboardResponse(users, "next_token")
        
        coEvery { userInfoGrpcClient.getLeaderboard(request) } returns Result.success(response)
        
        val result = repository.getLeaderboard(request)
        
        assertTrue(result.isSuccess)
        assertEquals(response, result.getOrNull())
        coVerify { userInfoGrpcClient.getLeaderboard(request) }
    }
    
    @Test
    fun `updateUserAchievements calls grpc client and returns result`() = runTest {
        val request = UpdateUserAchievementsRequest(
            userId = 123L,
            achievementIds = listOf(1L, 2L, 3L)
        )
        val response = UpdateUserAchievementsResponse(
            success = true,
            achievements = emptyList(),
            message = "Achievements updated successfully"
        )
        
        coEvery { userInfoGrpcClient.updateUserAchievements(request) } returns Result.success(response)
        
        val result = repository.updateUserAchievements(request)
        
        assertTrue(result.isSuccess)
        assertEquals(response, result.getOrNull())
        coVerify { userInfoGrpcClient.updateUserAchievements(request) }
    }
    
    @Test
    fun `uploadUserAvatar calls grpc client and returns result`() = runTest {
        val avatarData = byteArrayOf(1, 2, 3, 4, 5)
        val request = UploadUserAvatarRequest(
            userId = 123L,
            avatarData = avatarData,
            contentType = "image/png"
        )
        val response = UploadUserAvatarResponse(
            success = true,
            avatarUrl = "https://example.com/avatar.png",
            message = "Avatar uploaded successfully"
        )
        
        coEvery { userInfoGrpcClient.uploadUserAvatar(request) } returns Result.success(response)
        
        val result = repository.uploadUserAvatar(request)
        
        assertTrue(result.isSuccess)
        assertEquals(response, result.getOrNull())
        coVerify { userInfoGrpcClient.uploadUserAvatar(request) }
    }
    
    @Test
    fun `updateUserAvatar calls grpc client and returns result`() = runTest {
        val avatarData = byteArrayOf(1, 2, 3, 4, 5)
        val request = UpdateUserAvatarRequest(
            userId = 123L,
            avatarData = avatarData,
            contentType = "image/jpeg"
        )
        val response = UpdateUserAvatarResponse(
            success = true,
            avatarUrl = "https://example.com/new_avatar.jpg",
            message = "Avatar updated successfully"
        )
        
        coEvery { userInfoGrpcClient.updateUserAvatar(request) } returns Result.success(response)
        
        val result = repository.updateUserAvatar(request)
        
        assertTrue(result.isSuccess)
        assertEquals(response, result.getOrNull())
        coVerify { userInfoGrpcClient.updateUserAvatar(request) }
    }
    
    @Test
    fun `getUserAvatar calls grpc client and returns result`() = runTest {
        val request = GetUserAvatarRequest(userId = 123L)
        val response = GetUserAvatarResponse(
            success = true,
            avatarUrl = "https://example.com/avatar.png",
            message = "Avatar retrieved successfully"
        )
        
        coEvery { userInfoGrpcClient.getUserAvatar(request) } returns Result.success(response)
        
        val result = repository.getUserAvatar(request)
        
        assertTrue(result.isSuccess)
        assertEquals(response, result.getOrNull())
        coVerify { userInfoGrpcClient.getUserAvatar(request) }
    }
    
    @Test
    fun `all methods handle exceptions properly`() = runTest {
        val exception = RuntimeException("GRPC connection failed")
        
        coEvery { userInfoGrpcClient.getUser(any()) } returns Result.failure(exception)
        coEvery { userInfoGrpcClient.updateUserProfile(any()) } returns Result.failure(exception)
        coEvery { userInfoGrpcClient.getUserTestHistory(any()) } returns Result.failure(exception)
        coEvery { userInfoGrpcClient.getUserAchievements(any()) } returns Result.failure(exception)
        coEvery { userInfoGrpcClient.getLeaderboard(any()) } returns Result.failure(exception)
        coEvery { userInfoGrpcClient.updateUserAchievements(any()) } returns Result.failure(exception)
        coEvery { userInfoGrpcClient.uploadUserAvatar(any()) } returns Result.failure(exception)
        coEvery { userInfoGrpcClient.updateUserAvatar(any()) } returns Result.failure(exception)
        coEvery { userInfoGrpcClient.getUserAvatar(any()) } returns Result.failure(exception)
        
        val getUserResult = repository.getUser(GetUserRequest(123L))
        val updateProfileResult = repository.updateUserProfile(UpdateUserProfileRequest(123L, "test", Direction.FRONTEND, Level.MIDDLE))
        val getHistoryResult = repository.getUserTestHistory(GetUserTestHistoryRequest(123L, Pagination(10, "")))
        val getAchievementsResult = repository.getUserAchievements(GetUserAchievementsRequest(123L))
        val getLeaderboardResult = repository.getLeaderboard(GetLeaderboardRequest(Direction.FRONTEND, Level.MIDDLE, Pagination(10, "")))
        val updateAchievementsResult = repository.updateUserAchievements(UpdateUserAchievementsRequest(123L, emptyList()))
        val uploadAvatarResult = repository.uploadUserAvatar(UploadUserAvatarRequest(123L, byteArrayOf(), "image/png"))
        val updateAvatarResult = repository.updateUserAvatar(UpdateUserAvatarRequest(123L, byteArrayOf(), "image/png"))
        val getAvatarResult = repository.getUserAvatar(GetUserAvatarRequest(123L))
        
        assertTrue(getUserResult.isFailure)
        assertTrue(updateProfileResult.isFailure)
        assertTrue(getHistoryResult.isFailure)
        assertTrue(getAchievementsResult.isFailure)
        assertTrue(getLeaderboardResult.isFailure)
        assertTrue(updateAchievementsResult.isFailure)
        assertTrue(uploadAvatarResult.isFailure)
        assertTrue(updateAvatarResult.isFailure)
        assertTrue(getAvatarResult.isFailure)
        
        assertEquals(exception, getUserResult.exceptionOrNull())
        assertEquals(exception, updateProfileResult.exceptionOrNull())
        assertEquals(exception, getHistoryResult.exceptionOrNull())
        assertEquals(exception, getAchievementsResult.exceptionOrNull())
        assertEquals(exception, getLeaderboardResult.exceptionOrNull())
        assertEquals(exception, updateAchievementsResult.exceptionOrNull())
        assertEquals(exception, uploadAvatarResult.exceptionOrNull())
        assertEquals(exception, updateAvatarResult.exceptionOrNull())
        assertEquals(exception, getAvatarResult.exceptionOrNull())
    }
}
