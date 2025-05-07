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
    val achievements: List<Achievement>
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