package com.diploma.work.data.grpc

import com.diploma.work.data.models.*
import com.diploma.work.grpc.userinfo.UserServiceGrpc
import com.diploma.work.grpc.userinfo.Pagination as GrpcPagination
import com.diploma.work.utils.ErrorContext
import com.diploma.work.utils.ErrorMessageUtils
import com.google.protobuf.ByteString
import com.orhanobut.logger.Logger
import io.grpc.ManagedChannel
import io.grpc.Metadata
import io.grpc.Status
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import com.diploma.work.data.AppSession
import io.grpc.stub.MetadataUtils

class UserInfoGrpcClient @Inject constructor(
    private val channel: ManagedChannel,
    private val session: AppSession
) {
    private fun withAuthStub(): UserServiceGrpc.UserServiceBlockingStub {
        val token = session.getToken()
        return if (!token.isNullOrEmpty()) {
            val metadata = Metadata()
            metadata.put(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER), "Bearer $token")
            val interceptor = MetadataUtils.newAttachHeadersInterceptor(metadata)
            UserServiceGrpc.newBlockingStub(channel).withInterceptors(interceptor)
        } else {
            UserServiceGrpc.newBlockingStub(channel)
        }
    }

    suspend fun getUser(request: GetUserRequest): Result<GetUserResponse> = withContext(Dispatchers.IO) {
        try {
            Logger.d("Getting user info for userId: ${request.userId}")
            
            val grpcRequest = com.diploma.work.grpc.userinfo.GetUserRequest.newBuilder()
                .setUserId(request.userId)
                .build()
                
            val grpcResponse = withAuthStub().getUser(grpcRequest)
            val user = grpcResponse.user.toModel()
            
            Logger.d("Successfully got user info: ${user.id}")
            Result.success(GetUserResponse(user = user))        } catch (e: StatusRuntimeException) {
            Logger.e("gRPC error while getting user info: ${e.status.code} - ${e.status.description}")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, getErrorContext(e, ErrorContext.DATA_LOADING))))
        } catch (e: Exception) {
            Logger.e("Error while getting user info")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, getGenericErrorContext(e))))
        }
    }
    
    suspend fun updateUserProfile(request: UpdateUserProfileRequest): Result<UpdateUserProfileResponse> = withContext(Dispatchers.IO) {
        try {
            Logger.d("Updating user profile for userId: ${request.userId}")
            
            val grpcRequest = com.diploma.work.grpc.userinfo.UpdateUserProfileRequest.newBuilder()
                .setUserId(request.userId)
                .setUsername(request.username)
                .setDirection(request.direction)
                .setLevel(request.level)
                .build()
                
            val grpcResponse = withAuthStub().updateUserProfile(grpcRequest)
            val user = grpcResponse.user.toModel()
            
            Logger.d("Successfully updated user profile: ${user.id}")
            Result.success(UpdateUserProfileResponse(user = user))        } catch (e: StatusRuntimeException) {
            Logger.e("gRPC error while updating user profile: ${e.status.code} - ${e.status.description}")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, ErrorContext.PROFILE_UPDATE)))        } catch (e: Exception) {
            Logger.e("Error while updating user profile")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, getGenericErrorContext(e))))
        }
    }
    
    suspend fun getUserTestHistory(request: GetUserTestHistoryRequest): Result<GetUserTestHistoryResponse> = withContext(Dispatchers.IO) {
        try {
            Logger.d("Getting test history for userId: ${request.userId}")
            
            val pagination = GrpcPagination.newBuilder()
                .setPageSize(request.pagination.pageSize)
                .setPageToken(request.pagination.pageToken)
                .build()
                
            val grpcRequest = com.diploma.work.grpc.userinfo.GetUserTestHistoryRequest.newBuilder()
                .setUserId(request.userId)
                .setPagination(pagination)
                .build()
                
            val grpcResponse = withAuthStub().getUserTestHistory(grpcRequest)
            val tests = grpcResponse.testsList.map { it.toModel() }
            
            Logger.d("Successfully got test history: ${tests.size} tests")
            Result.success(
                GetUserTestHistoryResponse(
                    tests = tests,
                    nextPageToken = grpcResponse.nextPageToken
                )
            )        } catch (e: StatusRuntimeException) {
            Logger.e("gRPC error while getting test history: ${e.status.code} - ${e.status.description}")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, getErrorContext(e, ErrorContext.DATA_LOADING))))
        } catch (e: Exception) {
            Logger.e("Error while getting test history")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, getGenericErrorContext(e))))
        }
    }
    
    suspend fun getUserAchievements(request: GetUserAchievementsRequest): Result<GetUserAchievementsResponse> = withContext(Dispatchers.IO) {
        try {
            Logger.d("Getting achievements for userId: ${request.userId}")
            
            val grpcRequest = com.diploma.work.grpc.userinfo.GetUserAchievementsRequest.newBuilder()
                .setUserId(request.userId)
                .build()
                
            val grpcResponse = withAuthStub().getUserAchievements(grpcRequest)
            val achievements = grpcResponse.achievementsList.map { it.toModel() }
            
            Logger.d("Successfully got achievements: ${achievements.size} achievements")
            Result.success(
                GetUserAchievementsResponse(
                    achievements = achievements
                )
            )        } catch (e: StatusRuntimeException) {
            Logger.e("gRPC error while getting achievements: ${e.status.code} - ${e.status.description}")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, getErrorContext(e, ErrorContext.DATA_LOADING))))
        } catch (e: Exception) {
            Logger.e("Error while getting achievements")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, getGenericErrorContext(e))))
        }
    }
    
    suspend fun getLeaderboard(request: GetLeaderboardRequest): Result<GetLeaderboardResponse> = withContext(Dispatchers.IO) {
        try {
            Logger.d("Getting leaderboard for direction: ${request.direction}, level: ${request.level}")
            
            val pagination = GrpcPagination.newBuilder()
                .setPageSize(request.pagination.pageSize)
                .setPageToken(request.pagination.pageToken)
                .build()
                
            val grpcRequest = com.diploma.work.grpc.userinfo.GetLeaderboardRequest.newBuilder()
                .setDirection(request.direction)
                .setLevel(request.level)
                .setPagination(pagination)
                .build()
                
            val grpcResponse = withAuthStub().getLeaderboard(grpcRequest)
            val users = grpcResponse.usersList.map { it.toModel() }
            
            Logger.d("Successfully got leaderboard: ${users.size} users")
            Result.success(
                GetLeaderboardResponse(
                    users = users,
                    nextPageToken = grpcResponse.nextPageToken
                )
            )        } catch (e: StatusRuntimeException) {
            Logger.e("gRPC error while getting leaderboard: ${e.status.code} - ${e.status.description}")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, getErrorContext(e, ErrorContext.DATA_LOADING))))
        } catch (e: Exception) {
            Logger.e("Error while getting leaderboard")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, getGenericErrorContext(e))))
        }
    }
    
    suspend fun updateUserAchievements(request: UpdateUserAchievementsRequest): Result<UpdateUserAchievementsResponse> = withContext(Dispatchers.IO) {
        try {
            Logger.d("Updating achievements for userId: ${request.userId}")
            
            val grpcRequest = com.diploma.work.grpc.userinfo.UpdateUserAchievementsRequest.newBuilder()
                .setUserId(request.userId)
                .addAllAchievementIds(request.achievementIds)
                .build()
                
            val grpcResponse = withAuthStub().updateUserAchievements(grpcRequest)
            val achievements = grpcResponse.achievementsList.map { it.toModel() }
            
            Logger.d("Successfully updated achievements: ${achievements.size} achievements")
            Result.success(
                UpdateUserAchievementsResponse(
                    success = grpcResponse.success,
                    achievements = achievements,
                    message = grpcResponse.message
                )
            )        } catch (e: StatusRuntimeException) {
            Logger.e("gRPC error while updating achievements: ${e.status.code} - ${e.status.description}")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, getErrorContext(e, ErrorContext.DATA_LOADING))))
        } catch (e: Exception) {
            Logger.e("Error while updating achievements")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, getGenericErrorContext(e))))
        }
    }
    
    suspend fun uploadUserAvatar(request: UploadUserAvatarRequest): Result<UploadUserAvatarResponse> = withContext(Dispatchers.IO) {
        try {
            Logger.d("Uploading avatar for userId: ${request.userId}")
            
            val grpcRequest = com.diploma.work.grpc.userinfo.UploadUserAvatarRequest.newBuilder()
                .setUserId(request.userId)
                .setAvatarData(ByteString.copyFrom(request.avatarData))
                .setContentType(request.contentType)
                .build()
                
            val grpcResponse = withAuthStub().uploadUserAvatar(grpcRequest)
            
            Logger.d("Successfully uploaded avatar: ${grpcResponse.avatarUrl}")
            Result.success(
                UploadUserAvatarResponse(
                    success = grpcResponse.success,
                    avatarUrl = grpcResponse.avatarUrl,
                    message = grpcResponse.message
                )
            )        } catch (e: StatusRuntimeException) {
            Logger.e("gRPC error while uploading avatar: ${e.status.code} - ${e.status.description}")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, ErrorContext.PROFILE_UPDATE)))        } catch (e: Exception) {
            Logger.e("Error while uploading avatar")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, getGenericErrorContext(e))))
        }
    }
    
    suspend fun updateUserAvatar(request: UpdateUserAvatarRequest): Result<UpdateUserAvatarResponse> = withContext(Dispatchers.IO) {
        try {
            Logger.d("Updating avatar for userId: ${request.userId}")
            
            val grpcRequest = com.diploma.work.grpc.userinfo.UpdateUserAvatarRequest.newBuilder()
                .setUserId(request.userId)
                .setAvatarData(ByteString.copyFrom(request.avatarData))
                .setContentType(request.contentType)
                .build()
                
            val grpcResponse = withAuthStub().updateUserAvatar(grpcRequest)
            
            Logger.d("Successfully updated avatar: ${grpcResponse.avatarUrl}")
            Result.success(
                UpdateUserAvatarResponse(
                    success = grpcResponse.success,
                    avatarUrl = grpcResponse.avatarUrl,
                    message = grpcResponse.message
                )
            )        } catch (e: StatusRuntimeException) {
            Logger.e("gRPC error while updating avatar: ${e.status.code} - ${e.status.description}")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, ErrorContext.PROFILE_UPDATE)))        } catch (e: Exception) {
            Logger.e("Error while updating avatar")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, getGenericErrorContext(e))))
        }
    }
    
    suspend fun getUserAvatar(request: GetUserAvatarRequest): Result<GetUserAvatarResponse> = withContext(Dispatchers.IO) {
        try {
            Logger.d("Getting avatar for userId: ${request.userId}")
            
            val grpcRequest = com.diploma.work.grpc.userinfo.GetUserAvatarRequest.newBuilder()
                .setUserId(request.userId)
                .build()
                
            val grpcResponse = withAuthStub().getUserAvatar(grpcRequest)
            
            Logger.d("Successfully got avatar: ${grpcResponse.avatarUrl}")
            Result.success(
                GetUserAvatarResponse(
                    success = grpcResponse.success,
                    avatarUrl = grpcResponse.avatarUrl,
                    message = grpcResponse.message
                )
            )        } catch (e: StatusRuntimeException) {
            Logger.e("gRPC error while getting avatar: ${e.status.code} - ${e.status.description}")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, getErrorContext(e, ErrorContext.DATA_LOADING))))
        } catch (e: Exception) {
            Logger.e("Error while getting avatar")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, getGenericErrorContext(e))))
        }
    }
    
    suspend fun sendFeedback(request: SendFeedbackRequest): Result<SendFeedbackResponse> = withContext(Dispatchers.IO) {
        try {
            Logger.d("Sending feedback from user: ${request.userId}")
            
            val grpcRequest = com.diploma.work.grpc.userinfo.SendFeedbackRequest.newBuilder()
                .setUserId(request.userId)
                .setSubject(request.subject)
                .setBody(request.body)
                .build()
                
            val grpcResponse = withAuthStub().sendFeedback(grpcRequest)
            
            Logger.d("Feedback sent successfully: ${grpcResponse.success}")
            Result.success(
                SendFeedbackResponse(
                    success = grpcResponse.success,
                    message = grpcResponse.message
                )
            )
        } catch (e: StatusRuntimeException) {
            Logger.e("gRPC error while sending feedback: ${e.status.code} - ${e.status.description}")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, getErrorContext(e, ErrorContext.FEEDBACK))))
        } catch (e: Exception) {
            Logger.e("Error while sending feedback")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, getGenericErrorContext(e))))
        }
    }

    private fun com.diploma.work.grpc.userinfo.User.toModel(): User {
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
            achievements = achievements10List.map { it.toModel() },
            avatarUrl = avatarUrl
        )
    }
    
    private fun com.diploma.work.grpc.userinfo.Achievement.toModel(): Achievement {
        return Achievement(
            id = id,
            name = name,
            description = description,
            iconUrl = iconUrl,
            dateEarned = dateEarned
        )
    }
    
    private fun com.diploma.work.grpc.userinfo.TestSummary.toModel(): TestSummary {
        return TestSummary(
            id = id,
            title = title,
            completionDate = completionDate,
            score = score,
            totalPoints = totalPoints
        )
    }
    
    private fun getErrorContext(e: StatusRuntimeException, defaultContext: ErrorContext): ErrorContext {
        return when (e.status.code) {
            Status.Code.UNAVAILABLE, Status.Code.DEADLINE_EXCEEDED -> ErrorContext.NETWORK
            else -> defaultContext
        }
    }

    private fun getGenericErrorContext(e: Exception): ErrorContext {
        return if (e is StatusRuntimeException) {
            getErrorContext(e, ErrorContext.GENERIC)
        } else {
            ErrorContext.GENERIC
        }
    }
}