package com.diploma.work.ui.navigation


import kotlinx.serialization.Serializable

interface NavRoute

@Serializable
object Login : NavRoute

@Serializable
object Register : NavRoute

@Serializable
object Home : NavRoute

@Serializable
object Profile : NavRoute

@Serializable
object Achievements : NavRoute

@Serializable
object Leaderboard : NavRoute

@Serializable
object UserInfo : NavRoute

@Serializable
data class EmailConfirmation(val email: String) : NavRoute

@Serializable
data class TestDetails(val testId: Long) : NavRoute

@Serializable
data class TestSession(val sessionId: String) : NavRoute

@Serializable
data class TestResult(val sessionId: String) : NavRoute

@Serializable
object UserInterests : NavRoute

@Serializable
object Articles : NavRoute

@Serializable
object Feedback : NavRoute