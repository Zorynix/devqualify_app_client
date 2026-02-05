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
object History : NavRoute

@Serializable
object Feedback : NavRoute


object Routes {
    const val LOGIN = "Login"
    const val REGISTER = "Register"
    const val HOME = "Home"
    const val PROFILE = "Profile"
    const val ACHIEVEMENTS = "Achievements"
    const val LEADERBOARD = "Leaderboard"
    const val USER_INTERESTS = "UserInterests"
    const val ARTICLES = "Articles"
    const val HISTORY = "History"
    const val FEEDBACK = "Feedback"
    const val TEST_DETAILS = "TestDetails/{testId}"
    const val TEST_SESSION = "TestSession/{sessionId}"
    const val TEST_RESULT = "TestResult/{sessionId}"
    const val EMAIL_CONFIRMATION = "EmailConfirmation/{email}"

    fun testDetails(testId: Long) = "TestDetails/$testId"
    fun testSession(sessionId: String) = "TestSession/$sessionId"
    fun testResult(sessionId: String) = "TestResult/$sessionId"
    fun emailConfirmation(email: String) = "EmailConfirmation/$email"
}


fun NavRoute.toRouteString(): String = when (this) {
    is Login -> Routes.LOGIN
    is Register -> Routes.REGISTER
    is Home -> Routes.HOME
    is Profile -> Routes.PROFILE
    is Achievements -> Routes.ACHIEVEMENTS
    is Leaderboard -> Routes.LEADERBOARD
    is TestDetails -> Routes.testDetails(testId)
    is TestSession -> Routes.testSession(sessionId)
    is TestResult -> Routes.testResult(sessionId)
    is UserInterests -> Routes.USER_INTERESTS
    is Articles -> Routes.ARTICLES
    is EmailConfirmation -> Routes.emailConfirmation(email)
    is History -> Routes.HISTORY
    is Feedback -> Routes.FEEDBACK
    else -> this.javaClass.simpleName
}
