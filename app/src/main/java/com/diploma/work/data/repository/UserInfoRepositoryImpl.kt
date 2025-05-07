package com.diploma.work.data.repository

import com.diploma.work.data.grpc.UserInfoGrpcClient
import com.diploma.work.data.models.*
import com.orhanobut.logger.Logger
import javax.inject.Inject

class UserInfoRepositoryImpl @Inject constructor(
    private val userInfoGrpcClient: UserInfoGrpcClient
) : UserInfoRepository {
    private val tag = "UserInfoRepositoryImpl"
    
    override suspend fun getUser(request: GetUserRequest): Result<GetUserResponse> {
        Logger.d("Getting user info for userId: ${request.userId}")
        return userInfoGrpcClient.getUser(request)
    }
    
    override suspend fun updateUserProfile(request: UpdateUserProfileRequest): Result<UpdateUserProfileResponse> {
        Logger.d("Updating user profile for userId: ${request.userId}")
        return userInfoGrpcClient.updateUserProfile(request)
    }
    
    override suspend fun getUserTestHistory(request: GetUserTestHistoryRequest): Result<GetUserTestHistoryResponse> {
        Logger.d("Getting test history for userId: ${request.userId}")
        return userInfoGrpcClient.getUserTestHistory(request)
    }
    
    override suspend fun getUserAchievements(request: GetUserAchievementsRequest): Result<GetUserAchievementsResponse> {
        Logger.d("Getting achievements for userId: ${request.userId}")
        return userInfoGrpcClient.getUserAchievements(request)
    }
    
    override suspend fun getLeaderboard(request: GetLeaderboardRequest): Result<GetLeaderboardResponse> {
        Logger.d("Getting leaderboard for direction: ${request.direction}, level: ${request.level}")
        return userInfoGrpcClient.getLeaderboard(request)
    }
    
    override suspend fun updateUserAchievements(request: UpdateUserAchievementsRequest): Result<UpdateUserAchievementsResponse> {
        Logger.d("Updating achievements for userId: ${request.userId}")
        return userInfoGrpcClient.updateUserAchievements(request)
    }
}