package com.diploma.work.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.diploma.work.R
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.diploma.work.data.AppSession
import com.diploma.work.ui.feature.achievements.AchievementsScreen
import com.diploma.work.ui.feature.articles.ArticlesScreen
import com.diploma.work.ui.feature.auth.confirmation.EmailConfirmationScreen
import com.diploma.work.ui.feature.auth.login.LoginScreen
import com.diploma.work.ui.feature.auth.register.RegistrationScreen
import com.diploma.work.ui.feature.feedback.FeedbackScreen
import com.diploma.work.ui.feature.home.HomeScreen
import com.diploma.work.ui.feature.interests.UserInterestsScreen
import com.diploma.work.ui.feature.leaderboard.LeaderboardScreen
import com.diploma.work.ui.feature.profile.AppDrawerContent
import com.diploma.work.ui.feature.profile.ProfileScreen
import com.diploma.work.ui.feature.test.TestDetailsScreen
import com.diploma.work.ui.feature.test.TestResultScreen
import com.diploma.work.ui.feature.test.TestSessionScreen
import com.diploma.work.ui.theme.ThemeManager
import com.orhanobut.logger.Logger
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    session: AppSession,
    themeManager: ThemeManager,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val token by session.observeToken().collectAsState(initial = session.getToken())
    val isLoggedIn = token != null

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val username = session.observeUsername().collectAsState(initial = session.getUsername() ?: "User")
    val avatarUrl = session.observeAvatarUrl().collectAsState(
        initial = session.getAvatarUrl() ?: "https://ui-avatars.com/api/?name=User&background=random&size=200"
    )

    val navItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Profile,
        BottomNavItem.Achievements,
        BottomNavItem.Leaderboard,
        BottomNavItem.Articles
    )

    ModalNavigationDrawer(
        modifier = modifier.fillMaxSize(),
        drawerState = drawerState,
        gesturesEnabled = isLoggedIn,
        drawerContent = {
            if (isLoggedIn) {
                AppDrawerContent(
                    username = username.value ?: "User",
                    avatarUrl = avatarUrl.value ?: "https://ui-avatars.com/api/?name=User&background=random&size=200",
                    theme = themeManager.currentTheme.collectAsState().value,
                    session = session,
                    onThemeToggle = { themeManager.toggleTheme() },
                    onInterestsClick = {
                        Logger.d("Navigation: Navigating to UserInterests")
                        scope.launch {
                            drawerState.close()
                            navController.safeNavigate("UserInterests")
                        }
                    },
                    onFeedbackClick = {
                        Logger.d("Navigation: Navigating to Feedback")
                        scope.launch {
                            drawerState.close()
                            navController.safeNavigate("Feedback")
                        }
                    },
                    onLogout = {
                        Logger.d("Navigation: User logging out")
                        session.clearToken()
                        scope.launch {
                            drawerState.close()
                            navController.safeNavigate("Login", clearStack = true)
                        }
                    }
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (isLoggedIn) {
                    NavigationBar {
                        val currentRoute by navController.currentBackStackEntryAsState()
                        navItems.forEach { item ->
                            val selected = currentRoute?.destination?.route == when (item.route) {
                                is Home -> "Home"
                                is Profile -> "Profile"
                                is Achievements -> "Achievements"
                                is Leaderboard -> "Leaderboard"
                                is Articles -> "Articles"
                                else -> ""
                            }
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    val route = when (item.route) {
                                        is Home -> "Home"
                                        is Profile -> "Profile"
                                        is Achievements -> "Achievements"
                                        is Leaderboard -> "Leaderboard"
                                        is Articles -> "Articles"
                                        else -> ""
                                    }
                                    Logger.d("Navigation: Bottom nav bar click - $route")
                                    val current = navController.currentDestination?.route
                                    if (current == "UserInterests") {
                                        navController.safeNavigate(route, popUpToRoute = "UserInterests", inclusive = true)
                                    } else {
                                        navController.safeNavigate(route)
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = when (item.route) {
                                            is Home -> stringResource(R.string.home)
                                            is Profile -> stringResource(R.string.profile)
                                            is Achievements -> stringResource(R.string.achievements)
                                            is Leaderboard -> stringResource(R.string.leaderboard)
                                            is Articles -> stringResource(R.string.articles)
                                            else -> ""
                                        }
                                    )
                                },
                                label = {
                                    androidx.compose.material3.Text(
                                        when (item.route) {
                                            is Home -> stringResource(R.string.home)
                                            is Profile -> stringResource(R.string.profile)
                                            is Achievements -> stringResource(R.string.achievements)
                                            is Leaderboard -> stringResource(R.string.leaderboard)
                                            is Articles -> stringResource(R.string.articles)
                                            else -> ""
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = if (isLoggedIn) "Home" else "Login",
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(innerPadding),
                enterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                },
                popEnterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                popExitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                }
            ) {
                composable("Login") { LoginScreen(navController, session) }
                composable("Register") { RegistrationScreen(navController, session) }
                composable("Home") {
                    if (session.getUserId() != null) {
                        session.refreshUsername()
                        session.refreshAvatarUrl()
                    }
                    HomeScreen(
                        navController = navController,
                        onOpenDrawer = { scope.launch { drawerState.open() } }
                    )
                }
                composable("Profile") {
                    val profileScreen = ProfileScreen(
                        navController = navController,
                        drawerState = drawerState,
                        themeManager = themeManager
                    )
                    if (session.getUserId() != null) {
                        session.refreshUsername()
                        session.refreshAvatarUrl()
                    }
                    profileScreen
                }
                composable("Achievements") {
                    if (session.getUserId() != null) {
                        session.refreshUsername()
                        session.refreshAvatarUrl()
                    }
                    AchievementsScreen(
                        onOpenDrawer = { scope.launch { drawerState.open() } }
                    )
                }
                composable("Leaderboard") {
                    if (session.getUserId() != null) {
                        session.refreshUsername()
                        session.refreshAvatarUrl()
                    }
                    LeaderboardScreen(
                        navController = navController,
                        onOpenDrawer = { scope.launch { drawerState.open() } }
                    )
                }
                composable(
                    route = "TestDetails/{testId}",
                    arguments = listOf(navArgument("testId") { type = androidx.navigation.NavType.LongType })
                ) {
                    val testId = it.arguments?.getLong("testId") ?: 0L
                    TestDetailsScreen(navController, testId)
                }
                composable(
                    route = "TestSession/{sessionId}",
                    arguments = listOf(navArgument("sessionId") { type = androidx.navigation.NavType.StringType })
                ) {
                    val sessionId = it.arguments?.getString("sessionId") ?: ""
                    TestSessionScreen(navController, sessionId)
                }
                composable(
                    route = "TestResult/{sessionId}",
                    arguments = listOf(navArgument("sessionId") { type = androidx.navigation.NavType.StringType })
                ) {
                    val sessionId = it.arguments?.getString("sessionId") ?: ""
                    TestResultScreen(navController, sessionId)
                }
                composable(
                    route = "EmailConfirmation/{email}",
                    arguments = listOf(navArgument("email") { type = androidx.navigation.NavType.StringType })
                ) {
                    val email = it.arguments?.getString("email") ?: ""
                    EmailConfirmationScreen(navController, email)
                }
                composable("UserInterests") {
                    if (session.getUserId() != null) {
                        session.refreshUsername()
                        session.refreshAvatarUrl()
                    }
                    UserInterestsScreen(
                        onBack = {
                            if (!navController.safeNavigateBack()) {
                                navController.safeNavigate("Home", clearStack = true)
                            }
                        }
                    )
                }
                composable("Articles") {
                    if (session.getUserId() != null) {
                        session.refreshUsername()
                        session.refreshAvatarUrl()
                    }
                    ArticlesScreen(
                        onOpenDrawer = { scope.launch { drawerState.open() } }
                    )
                }
                composable("Feedback") { FeedbackScreen(navController) }
            }
        }
    }
}

sealed class BottomNavItem(val route: NavRoute, val icon: ImageVector) {
    object Home : BottomNavItem(com.diploma.work.ui.navigation.Home, Icons.Default.Home)
    object Profile : BottomNavItem(com.diploma.work.ui.navigation.Profile, Icons.Default.Person)
    object Achievements : BottomNavItem(com.diploma.work.ui.navigation.Achievements, Icons.Default.Star)
    object Leaderboard : BottomNavItem(com.diploma.work.ui.navigation.Leaderboard, Icons.Filled.Leaderboard)
    object Articles : BottomNavItem(com.diploma.work.ui.navigation.Articles,
        Icons.AutoMirrored.Filled.Article
    )
}