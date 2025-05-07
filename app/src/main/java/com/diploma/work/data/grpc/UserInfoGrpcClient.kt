package com.diploma.work.data.grpc

import com.diploma.work.data.models.*
import com.diploma.work.grpc.UserServiceGrpc
import com.diploma.work.grpc.Pagination as GrpcPagination
import com.orhanobut.logger.Logger
import io.grpc.ManagedChannel
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserInfoGrpcClient @Inject constructor(
    private val channel: ManagedChannel
) {
    private val stub = UserServiceGrpc.newBlockingStub(channel)

    suspend fun getUser(request: GetUserRequest): Result<GetUserResponse> = withContext(Dispatchers.IO) {
        try {
            Logger.d("Getting user info for userId: ${request.userId}")
            
            val grpcRequest = com.diploma.work.grpc.GetUserRequest.newBuilder()
                .setUserId(request.userId)
                .build()
                
            val grpcResponse = stub.getUser(grpcRequest)
            val user = grpcResponse.user.toModel()
            
            Logger.d("Successfully got user info: ${user.id}")
            Result.success(GetUserResponse(user = user))
        } catch (e: StatusRuntimeException) {
            Logger.e("gRPC error while getting user info: ${e.status.code} - ${e.status.description}")
            Result.failure(Exception("Ошибка gRPC: ${e.status.code} - ${e.status.description}"))
        } catch (e: Exception) {
            Logger.e("Error while getting user info")
            Result.failure(e)
        }
    }
    
    suspend fun updateUserProfile(request: UpdateUserProfileRequest): Result<UpdateUserProfileResponse> = withContext(Dispatchers.IO) {
        try {
            Logger.d("Updating user profile for userId: ${request.userId}")
            
            val grpcRequest = com.diploma.work.grpc.UpdateUserProfileRequest.newBuilder()
                .setUserId(request.userId)
                .setUsername(request.username)
                .setDirection(request.direction)
                .setLevel(request.level)
                .build()
                
            val grpcResponse = stub.updateUserProfile(grpcRequest)
            val user = grpcResponse.user.toModel()
            
            Logger.d("Successfully updated user profile: ${user.id}")
            Result.success(UpdateUserProfileResponse(user = user))
        } catch (e: StatusRuntimeException) {
            Logger.e("gRPC error while updating user profile: ${e.status.code} - ${e.status.description}")
            Result.failure(Exception("Ошибка gRPC: ${e.status.code} - ${e.status.description}"))
        } catch (e: Exception) {
            Logger.e("Error while updating user profile")
            Result.failure(e)
        }
    }
    
    suspend fun getUserTestHistory(request: GetUserTestHistoryRequest): Result<GetUserTestHistoryResponse> = withContext(Dispatchers.IO) {
        try {
            Logger.d("Getting test history for userId: ${request.userId}")
            
            val pagination = GrpcPagination.newBuilder()
                .setPageSize(request.pagination.pageSize)
                .setPageToken(request.pagination.pageToken)
                .build()
                
            val grpcRequest = com.diploma.work.grpc.GetUserTestHistoryRequest.newBuilder()
                .setUserId(request.userId)
                .setPagination(pagination)
                .build()
                
            val grpcResponse = stub.getUserTestHistory(grpcRequest)
            val tests = grpcResponse.testsList.map { it.toModel() }
            
            Logger.d("Successfully got test history: ${tests.size} tests")
            Result.success(
                GetUserTestHistoryResponse(
                    tests = tests,
                    nextPageToken = grpcResponse.nextPageToken
                )
            )
        } catch (e: StatusRuntimeException) {
            Logger.e("gRPC error while getting test history: ${e.status.code} - ${e.status.description}")
            Result.failure(Exception("Ошибка gRPC: ${e.status.code} - ${e.status.description}"))
        } catch (e: Exception) {
            Logger.e("Error while getting test history")
            Result.failure(e)
        }
    }
    
    suspend fun getUserAchievements(request: GetUserAchievementsRequest): Result<GetUserAchievementsResponse> = withContext(Dispatchers.IO) {
        try {
            Logger.d("Getting achievements for userId: ${request.userId}")
            
            val grpcRequest = com.diploma.work.grpc.GetUserAchievementsRequest.newBuilder()
                .setUserId(request.userId)
                .build()
                
            val grpcResponse = stub.getUserAchievements(grpcRequest)
            val achievements = grpcResponse.achievementsList.map { it.toModel() }
            
            Logger.d("Successfully got achievements: ${achievements.size} achievements")
            Result.success(
                GetUserAchievementsResponse(
                    achievements = achievements
                )
            )
        } catch (e: StatusRuntimeException) {
            Logger.e("gRPC error while getting achievements: ${e.status.code} - ${e.status.description}")
            Result.failure(Exception("Ошибка gRPC: ${e.status.code} - ${e.status.description}"))
        } catch (e: Exception) {
            Logger.e("Error while getting achievements")
            Result.failure(e)
        }
    }
    
    suspend fun getLeaderboard(request: GetLeaderboardRequest): Result<GetLeaderboardResponse> = withContext(Dispatchers.IO) {
        try {
            Logger.d("Getting leaderboard for direction: ${request.direction}, level: ${request.level}")
            
            val pagination = GrpcPagination.newBuilder()
                .setPageSize(request.pagination.pageSize)
                .setPageToken(request.pagination.pageToken)
                .build()
                
            val grpcRequest = com.diploma.work.grpc.GetLeaderboardRequest.newBuilder()
                .setDirection(request.direction)
                .setLevel(request.level)
                .setPagination(pagination)
                .build()
                
            val grpcResponse = stub.getLeaderboard(grpcRequest)
            val users = grpcResponse.usersList.map { it.toModel() }
            
            Logger.d("Successfully got leaderboard: ${users.size} users")
            Result.success(
                GetLeaderboardResponse(
                    users = users,
                    nextPageToken = grpcResponse.nextPageToken
                )
            )
        } catch (e: StatusRuntimeException) {
            Logger.e("gRPC error while getting leaderboard: ${e.status.code} - ${e.status.description}")
            Result.failure(Exception("Ошибка gRPC: ${e.status.code} - ${e.status.description}"))
        } catch (e: Exception) {
            Logger.e("Error while getting leaderboard")
            Result.failure(e)
        }
    }
    
    suspend fun updateUserAchievements(request: UpdateUserAchievementsRequest): Result<UpdateUserAchievementsResponse> = withContext(Dispatchers.IO) {
        try {
            Logger.d("Updating achievements for userId: ${request.userId}")
            
            val grpcRequest = com.diploma.work.grpc.UpdateUserAchievementsRequest.newBuilder()
                .setUserId(request.userId)
                .addAllAchievementIds(request.achievementIds)
                .build()
                
            val grpcResponse = stub.updateUserAchievements(grpcRequest)
            val achievements = grpcResponse.achievementsList.map { it.toModel() }
            
            Logger.d("Successfully updated achievements: ${achievements.size} achievements")
            Result.success(
                UpdateUserAchievementsResponse(
                    success = grpcResponse.success,
                    achievements = achievements,
                    message = grpcResponse.message
                )
            )
        } catch (e: StatusRuntimeException) {
            Logger.e("gRPC error while updating achievements: ${e.status.code} - ${e.status.description}")
            Result.failure(Exception("Ошибка gRPC: ${e.status.code} - ${e.status.description}"))
        } catch (e: Exception) {
            Logger.e("Error while updating achievements")
            Result.failure(e)
        }
    }

    private fun com.diploma.work.grpc.User.toModel(): User {
        return User(
            id = id,
            username = username,
            email = email,
            direction = direction,
            level = level,
            totalCorrectAnswers = totalCorrectAnswers,
            totalIncorrectAnswers = totalIncorrectAnswers,
            completedTestsCount = completedTestsCount,
            achievementsCount = achievementsCount9,
            achievements = achievements10List.map { it.toModel() }
        )
    }
    
    private fun com.diploma.work.grpc.Achievement.toModel(): Achievement {
        return Achievement(
            id = id,
            name = name,
            description = description,
            iconUrl = iconUrl,
            dateEarned = dateEarned
        )
    }
    
    private fun com.diploma.work.grpc.TestSummary.toModel(): TestSummary {
        return TestSummary(
            id = id,
            title = title,
            completionDate = completionDate,
            score = score,
            totalPoints = totalPoints
        )
    }
}