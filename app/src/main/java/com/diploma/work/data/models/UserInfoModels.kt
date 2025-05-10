package com.diploma.work.data.models

import com.diploma.work.grpc.Direction
import com.diploma.work.grpc.Level

data class GetUserRequest(
    val userId: Long
)

data class GetUserResponse(
    val user: User
)

data class User(
    val id: Long,
    val username: String,
    val email: String,
    val direction: Direction,
    val level: Level,
    val totalCorrectAnswers: Int,
    val totalIncorrectAnswers: Int,
    val completedTestsCount: Int,
    val achievementsCount: Int,
    val achievements: List<Achievement>,
    val avatarUrl: String = ""
)

data class Achievement(
    val id: Long,
    val name: String,
    val description: String,
    val iconUrl: String,
    val dateEarned: String
)

data class UpdateUserProfileRequest(
    val userId: Long,
    val username: String,
    val direction: Direction,
    val level: Level
)

data class UpdateUserProfileResponse(
    val user: User
)

data class GetUserTestHistoryRequest(
    val userId: Long,
    val pagination: Pagination
)

data class Pagination(
    val pageSize: Int,
    val pageToken: String
)

data class GetUserTestHistoryResponse(
    val tests: List<TestSummary>,
    val nextPageToken: String
)

data class TestSummary(
    val id: Long,
    val title: String,
    val completionDate: String,
    val score: Int,
    val totalPoints: Int
)

data class GetUserAchievementsRequest(
    val userId: Long
)

data class GetUserAchievementsResponse(
    val achievements: List<Achievement>
)

data class GetLeaderboardRequest(
    val direction: Direction,
    val level: Level,
    val pagination: Pagination
)

data class GetLeaderboardResponse(
    val users: List<User>,
    val nextPageToken: String
)

data class UpdateUserAchievementsRequest(
    val userId: Long,
    val achievementIds: List<Long>
)

data class UpdateUserAchievementsResponse(
    val success: Boolean,
    val achievements: List<Achievement>,
    val message: String
)

data class UploadUserAvatarRequest(
    val userId: Long,
    val avatarData: ByteArray,
    val contentType: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UploadUserAvatarRequest

        if (userId != other.userId) return false
        if (!avatarData.contentEquals(other.avatarData)) return false
        if (contentType != other.contentType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + avatarData.contentHashCode()
        result = 31 * result + contentType.hashCode()
        return result
    }
}

data class UploadUserAvatarResponse(
    val success: Boolean,
    val avatarUrl: String,
    val message: String
)

data class UpdateUserAvatarRequest(
    val userId: Long,
    val avatarData: ByteArray,
    val contentType: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UpdateUserAvatarRequest

        if (userId != other.userId) return false
        if (!avatarData.contentEquals(other.avatarData)) return false
        if (contentType != other.contentType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + avatarData.contentHashCode()
        result = 31 * result + contentType.hashCode()
        return result
    }
}

data class UpdateUserAvatarResponse(
    val success: Boolean,
    val avatarUrl: String,
    val message: String
)

data class GetUserAvatarRequest(
    val userId: Long
)

data class GetUserAvatarResponse(
    val success: Boolean,
    val avatarUrl: String,
    val message: String
)