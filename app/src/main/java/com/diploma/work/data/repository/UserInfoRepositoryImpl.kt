package com.diploma.work.data.repository

import com.diploma.work.data.api.SendFeedbackBody
import com.diploma.work.data.api.UpdateUserAchievementsBody
import com.diploma.work.data.api.UpdateUserProfileBody
import com.diploma.work.data.api.UserInfoApi
import com.diploma.work.data.api.safeApiCall
import com.diploma.work.data.models.GetLeaderboardRequest
import com.diploma.work.data.models.GetLeaderboardResponse
import com.diploma.work.data.models.GetUserAchievementsRequest
import com.diploma.work.data.models.GetUserAchievementsResponse
import com.diploma.work.data.models.GetUserAvatarRequest
import com.diploma.work.data.models.GetUserAvatarResponse
import com.diploma.work.data.models.GetUserRequest
import com.diploma.work.data.models.GetUserResponse
import com.diploma.work.data.models.GetUserTestHistoryRequest
import com.diploma.work.data.models.GetUserTestHistoryResponse
import com.diploma.work.data.models.SendFeedbackRequest
import com.diploma.work.data.models.SendFeedbackResponse
import com.diploma.work.data.models.UpdateUserAchievementsRequest
import com.diploma.work.data.models.UpdateUserAchievementsResponse
import com.diploma.work.data.models.UpdateUserAvatarRequest
import com.diploma.work.data.models.UpdateUserAvatarResponse
import com.diploma.work.data.models.UpdateUserProfileRequest
import com.diploma.work.data.models.UpdateUserProfileResponse
import com.diploma.work.data.models.UploadUserAvatarRequest
import com.diploma.work.data.models.UploadUserAvatarResponse
import com.diploma.work.utils.ErrorContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class UserInfoRepositoryImpl @Inject constructor(
    private val api: UserInfoApi
) : UserInfoRepository {

    override suspend fun getUser(request: GetUserRequest): Result<GetUserResponse> =
        safeApiCall(ErrorContext.DATA_LOADING) { api.getUser().toModel() }

    override suspend fun updateUserProfile(request: UpdateUserProfileRequest): Result<UpdateUserProfileResponse> =
        safeApiCall(ErrorContext.PROFILE_UPDATE) {
            api.updateUserProfile(
                UpdateUserProfileBody(
                    username = request.username,
                    direction = request.direction.name,
                    level = request.level.name
                )
            ).toModel()
        }

    override suspend fun getUserTestHistory(request: GetUserTestHistoryRequest): Result<GetUserTestHistoryResponse> =
        safeApiCall(ErrorContext.DATA_LOADING) {
            api.getUserTestHistory(
                pageSize = request.pagination.pageSize,
                pageToken = request.pagination.pageToken
            ).toModel()
        }

    override suspend fun getUserAchievements(request: GetUserAchievementsRequest): Result<GetUserAchievementsResponse> =
        safeApiCall(ErrorContext.DATA_LOADING) { api.getUserAchievements().toModel() }

    override suspend fun getLeaderboard(request: GetLeaderboardRequest): Result<GetLeaderboardResponse> =
        safeApiCall(ErrorContext.DATA_LOADING) {
            api.getLeaderboard(
                direction = request.direction.queryValue,
                level = request.level.queryValue,
                pageSize = request.pagination.pageSize,
                pageToken = request.pagination.pageToken
            ).toModel()
        }

    override suspend fun updateUserAchievements(request: UpdateUserAchievementsRequest): Result<UpdateUserAchievementsResponse> =
        safeApiCall(ErrorContext.DATA_LOADING) {
            api.updateUserAchievements(
                UpdateUserAchievementsBody(achievementIds = request.achievementIds)
            ).toModel()
        }

    override suspend fun uploadUserAvatar(request: UploadUserAvatarRequest): Result<UploadUserAvatarResponse> =
        safeApiCall(ErrorContext.PROFILE_UPDATE) {
            val body = request.avatarData.toRequestBody(request.contentType.toMediaTypeOrNull())
            api.uploadUserAvatar(body)
        }

    override suspend fun updateUserAvatar(request: UpdateUserAvatarRequest): Result<UpdateUserAvatarResponse> =
        safeApiCall(ErrorContext.PROFILE_UPDATE) {
            val body = request.avatarData.toRequestBody(request.contentType.toMediaTypeOrNull())
            api.updateUserAvatar(body)
        }

    override suspend fun getUserAvatar(request: GetUserAvatarRequest): Result<GetUserAvatarResponse> =
        safeApiCall(ErrorContext.DATA_LOADING) { api.getUserAvatar() }

    override suspend fun sendFeedback(request: SendFeedbackRequest): Result<SendFeedbackResponse> =
        safeApiCall(ErrorContext.FEEDBACK) {
            api.sendFeedback(SendFeedbackBody(subject = request.subject, body = request.body))
        }
}
