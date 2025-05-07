package com.diploma.work.data.repository

import com.diploma.work.data.models.GetUserRequest
import com.diploma.work.data.models.GetUserResponse
import com.diploma.work.data.models.UpdateUserProfileRequest
import com.diploma.work.data.models.UpdateUserProfileResponse
import com.diploma.work.data.models.GetUserTestHistoryRequest
import com.diploma.work.data.models.GetUserTestHistoryResponse
import com.diploma.work.data.models.GetUserAchievementsRequest
import com.diploma.work.data.models.GetUserAchievementsResponse
import com.diploma.work.data.models.GetLeaderboardRequest
import com.diploma.work.data.models.GetLeaderboardResponse
import com.diploma.work.data.models.UpdateUserAchievementsRequest
import com.diploma.work.data.models.UpdateUserAchievementsResponse

interface UserInfoRepository {
    suspend fun getUser(request: GetUserRequest): Result<GetUserResponse>
    
    suspend fun updateUserProfile(request: UpdateUserProfileRequest): Result<UpdateUserProfileResponse>
    
    suspend fun getUserTestHistory(request: GetUserTestHistoryRequest): Result<GetUserTestHistoryResponse>
    
    suspend fun getUserAchievements(request: GetUserAchievementsRequest): Result<GetUserAchievementsResponse>
    
    suspend fun getLeaderboard(request: GetLeaderboardRequest): Result<GetLeaderboardResponse>
    
    suspend fun updateUserAchievements(request: UpdateUserAchievementsRequest): Result<UpdateUserAchievementsResponse>
}