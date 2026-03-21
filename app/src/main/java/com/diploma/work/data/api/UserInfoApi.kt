package com.diploma.work.data.api

import com.diploma.work.data.api.dto.GetLeaderboardDto
import com.diploma.work.data.api.dto.GetUserAchievementsDto
import com.diploma.work.data.api.dto.GetUserDto
import com.diploma.work.data.api.dto.GetUserTestHistoryDto
import com.diploma.work.data.api.dto.UpdateUserAchievementsDto
import com.diploma.work.data.api.dto.UpdateUserProfileDto
import com.diploma.work.data.models.GetUserAvatarResponse
import com.diploma.work.data.models.SendFeedbackResponse
import com.diploma.work.data.models.UploadUserAvatarResponse
import com.diploma.work.data.models.UpdateUserAvatarResponse
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface UserInfoApi {

    @GET("users/me")
    suspend fun getUser(): GetUserDto

    @PUT("users/me/profile")
    suspend fun updateUserProfile(@Body request: UpdateUserProfileBody): UpdateUserProfileDto

    @GET("users/me/test-history")
    suspend fun getUserTestHistory(
        @Query("page_size") pageSize: Int = 20,
        @Query("page_token") pageToken: String = ""
    ): GetUserTestHistoryDto

    @GET("users/me/achievements")
    suspend fun getUserAchievements(): GetUserAchievementsDto

    @PUT("users/me/achievements")
    suspend fun updateUserAchievements(@Body request: UpdateUserAchievementsBody): UpdateUserAchievementsDto

    @POST("users/me/avatar")
    suspend fun uploadUserAvatar(@Body avatarData: RequestBody): UploadUserAvatarResponse

    @PUT("users/me/avatar")
    suspend fun updateUserAvatar(@Body avatarData: RequestBody): UpdateUserAvatarResponse

    @GET("users/me/avatar")
    suspend fun getUserAvatar(): GetUserAvatarResponse

    @POST("feedback")
    suspend fun sendFeedback(@Body request: SendFeedbackBody): SendFeedbackResponse

    @GET("leaderboard")
    suspend fun getLeaderboard(
        @Query("direction") direction: Int = 0,
        @Query("level") level: Int = 0,
        @Query("page_size") pageSize: Int = 20,
        @Query("page_token") pageToken: String = ""
    ): GetLeaderboardDto
}

data class UpdateUserProfileBody(
    val username: String,
    val direction: String,
    val level: String
)

data class UpdateUserAchievementsBody(
    val achievementIds: List<Long>
)

data class SendFeedbackBody(
    val subject: String,
    val body: String
)
