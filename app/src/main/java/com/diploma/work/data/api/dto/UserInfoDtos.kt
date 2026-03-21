package com.diploma.work.data.api.dto

import com.diploma.work.data.models.Achievement
import com.diploma.work.data.models.GetLeaderboardResponse
import com.diploma.work.data.models.GetUserAchievementsResponse
import com.diploma.work.data.models.GetUserAvatarResponse
import com.diploma.work.data.models.GetUserResponse
import com.diploma.work.data.models.GetUserTestHistoryResponse
import com.diploma.work.data.models.SendFeedbackResponse
import com.diploma.work.data.models.TestSummary
import com.diploma.work.data.models.UpdateUserAchievementsResponse
import com.diploma.work.data.models.UpdateUserProfileResponse
import com.diploma.work.data.models.UploadUserAvatarResponse
import com.diploma.work.data.models.UpdateUserAvatarResponse
import com.diploma.work.data.models.User
import com.diploma.work.data.models.UserDirection
import com.diploma.work.data.models.UserLevel

data class UserDto(
    val id: Long = 0,
    val username: String = "",
    val email: String = "",
    val direction: String = "",
    val level: String = "",
    val totalCorrectAnswers: Int = 0,
    val totalIncorrectAnswers: Int = 0,
    val completedTestsCount: Int = 0,
    val achievementsCount: Int = 0,
    val achievements: List<AchievementDto> = emptyList(),
    val avatarUrl: String = ""
) {
    fun toModel(): User = User(
        id = id,
        username = username,
        email = email,
        direction = parseUserDirection(direction),
        level = parseUserLevel(level),
        totalCorrectAnswers = totalCorrectAnswers,
        totalIncorrectAnswers = totalIncorrectAnswers,
        completedTestsCount = completedTestsCount,
        achievementsCount = achievementsCount,
        achievements = achievements.map { it.toModel() },
        avatarUrl = avatarUrl
    )
}

data class AchievementDto(
    val id: Long = 0,
    val name: String = "",
    val description: String = "",
    val iconUrl: String = "",
    val dateEarned: String = ""
) {
    fun toModel(): Achievement = Achievement(
        id = id,
        name = name,
        description = description,
        iconUrl = iconUrl,
        dateEarned = dateEarned
    )
}

data class TestSummaryDto(
    val id: String = "",
    val title: String = "",
    val completionDate: String = "",
    val score: Int = 0,
    val totalPoints: Int = 0
) {
    fun toModel(): TestSummary = TestSummary(
        id = id,
        title = title,
        completionDate = completionDate,
        score = score,
        totalPoints = totalPoints
    )
}

data class GetUserDto(
    val user: UserDto = UserDto()
) {
    fun toModel(): GetUserResponse = GetUserResponse(user = user.toModel())
}

data class UpdateUserProfileDto(
    val user: UserDto = UserDto()
) {
    fun toModel(): UpdateUserProfileResponse = UpdateUserProfileResponse(user = user.toModel())
}

data class GetUserTestHistoryDto(
    val tests: List<TestSummaryDto> = emptyList(),
    val nextPageToken: String = ""
) {
    fun toModel(): GetUserTestHistoryResponse = GetUserTestHistoryResponse(
        tests = tests.map { it.toModel() },
        nextPageToken = nextPageToken
    )
}

data class GetUserAchievementsDto(
    val achievements: List<AchievementDto> = emptyList()
) {
    fun toModel(): GetUserAchievementsResponse = GetUserAchievementsResponse(
        achievements = achievements.map { it.toModel() }
    )
}

data class UpdateUserAchievementsDto(
    val success: Boolean = false,
    val achievements: List<AchievementDto> = emptyList(),
    val message: String = ""
) {
    fun toModel(): UpdateUserAchievementsResponse = UpdateUserAchievementsResponse(
        success = success,
        achievements = achievements.map { it.toModel() },
        message = message
    )
}

data class GetLeaderboardDto(
    val users: List<UserDto> = emptyList(),
    val nextPageToken: String = ""
) {
    fun toModel(): GetLeaderboardResponse = GetLeaderboardResponse(
        users = users.map { it.toModel() },
        nextPageToken = nextPageToken
    )
}

private fun parseUserDirection(value: String): UserDirection = when (value) {
    "BACKEND" -> UserDirection.BACKEND
    "FRONTEND" -> UserDirection.FRONTEND
    "DEVOPS" -> UserDirection.DEVOPS
    "DATA_SCIENCE" -> UserDirection.DATA_SCIENCE
    else -> UserDirection.DIRECTION_UNSPECIFIED
}

private fun parseUserLevel(value: String): UserLevel = when (value) {
    "JUNIOR" -> UserLevel.JUNIOR
    "MIDDLE" -> UserLevel.MIDDLE
    "SENIOR" -> UserLevel.SENIOR
    else -> UserLevel.LEVEL_UNSPECIFIED
}
