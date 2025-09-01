package com.diploma.work.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.exclude
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Article
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.diploma.work.R
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptions
import androidx.navigation.NavType
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
import com.diploma.work.ui.feature.history.HistoryScreen
import com.diploma.work.ui.feature.home.HomeScreen
import com.diploma.work.ui.feature.interests.UserInterestsScreen
import com.diploma.work.ui.feature.leaderboard.LeaderboardScreen
import com.diploma.work.ui.feature.profile.AppDrawerContent
import com.diploma.work.ui.feature.profile.ProfileScreen
import com.diploma.work.ui.feature.test.TestDetailsScreen
import com.diploma.work.ui.feature.test.TestResultScreen
import com.diploma.work.ui.feature.test.TestSessionScreen
import com.diploma.work.ui.theme.AppThemeType
import com.diploma.work.ui.theme.ThemeManager
import com.orhanobut.logger.Logger
import kotlinx.coroutines.launch
import javax.inject.Inject

fun NavController.safeNavigate(
    route: String,
    singleTop: Boolean = true,
    clearStack: Boolean = false,
    popUpToRoute: String? = null,
    inclusive: Boolean = false
) {
    try {
        val currentRoute = currentDestination?.route

        if (currentRoute == route) {
            Logger.d("Navigation: Already on route $route, skipping navigation")
            return
        }

        Logger.d("Navigation: Navigating from $currentRoute to $route")

        navigate(route) {
            launchSingleTop = singleTop

            if (clearStack || route == "Home") {
                popUpTo(graph.startDestinationId) {
                    this.inclusive = true
                }
            } else if (popUpToRoute != null) {
                popUpTo(popUpToRoute) {
                    this.inclusive = inclusive
                }
            }
        }
    } catch (e: Exception) {
        Logger.e("Navigation error: ${e.message}", e)
    }
}

fun NavController.safeNavigateBack(): Boolean {
    return try {
        if (previousBackStackEntry != null) {
            popBackStack()
            true
        } else {
            Logger.w("Navigation: No previous destination to navigate back to")
            false
        }
    } catch (e: Exception) {
        Logger.e("Navigation back error: ${e.message}", e)
        false
    }
}

fun NavController.safeNavigate(
    route: NavRoute,
    singleTop: Boolean = true,
    clearStack: Boolean = false,
    popUpToRoute: String? = null,
    inclusive: Boolean = false
) {
    val routeName = when (route) {
        is Login -> "Login"
        is Register -> "Register"
        is Home -> "Home"
        is Profile -> "Profile"
        is Achievements -> "Achievements"
        is Leaderboard -> "Leaderboard"
        is TestDetails -> "TestDetails/${route.testId}"
        is TestSession -> "TestSession/${route.sessionId}"
        is TestResult -> "TestResult/${route.sessionId}"
        is UserInterests -> "UserInterests"
        is Articles -> "Articles"
        is EmailConfirmation -> "EmailConfirmation/${route.email}"
        else -> route.javaClass.simpleName
    }

    Logger.d("Navigation: Converting NavRoute $route to string $routeName")

    safeNavigate(
        route = routeName,
        singleTop = singleTop,
        clearStack = clearStack || routeName == "Home" || routeName == "Login",
        popUpToRoute = popUpToRoute,
        inclusive = inclusive
    )
}

@Composable
fun AppNavigation(
    session: AppSession,
    themeManager: ThemeManager,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    
    val token by session.observeToken().collectAsState(initial = session.getToken())
    val shouldShowBottomNav = remember { mutableStateOf(false) }
    val isLoggedIn = token != null
    
    shouldShowBottomNav.value = isLoggedIn
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    Logger.d("Navigation: AppNavigation setup, user logged in: $isLoggedIn, token: ${token != null}")

    val username = session.observeUsername().collectAsState(initial = session.getUsername() ?: "User")
    val avatarUrl = session.observeAvatarUrl().collectAsState(
        initial = session.getAvatarUrl() ?: "https://ui-avatars.com/api/?name=User&background=random&size=200"
    )

    val theme by themeManager.currentTheme.collectAsState()
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
            Logger.d("Navigation: Drawer content rendering, isLoggedIn: $isLoggedIn")
            if (isLoggedIn) {                AppDrawerContent(
                username = username.value ?: "User",
                avatarUrl = avatarUrl.value ?: "https://ui-avatars.com/api/?name=User&background=random&size=200",
                theme = theme,
                session = session,
                onThemeToggle = {
                    themeManager.toggleTheme()
                },                    onInterestsClick = {
                    Logger.d("Navigation: Navigating to UserInterests")
                    scope.launch {
                        drawerState.close()
                        navController.safeNavigate("UserInterests")
                    }
                },
                onHistoryClick = {
                    Logger.d("Navigation: Navigating to History")
                    scope.launch {
                        drawerState.close()
                        navController.safeNavigate("History")
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
                    shouldShowBottomNav.value = false
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
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (shouldShowBottomNav.value) {
                    NavigationBar {
                        val currentRoute by navController.currentBackStackEntryAsState()
                        navItems.forEach { item ->                            val selected = currentRoute?.destination?.hierarchy?.any {
                            it.route == when (item.route) {
                                is Home -> "Home"
                                is Profile -> "Profile"
                                is Achievements -> "Achievements"
                                is Leaderboard -> "Leaderboard"
                                is Articles -> "Articles"
                                else -> ""
                            }
                        } == true

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
                                    Logger.d("Navigation: Current destination: ${navController.currentDestination?.route}")
                                    val currentRoute = navController.currentDestination?.route
                                    if (currentRoute == "UserInterests") {
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
                composable("Login") {
                    shouldShowBottomNav.value = false
                    LoginScreen(navController, session)
                }
                composable("Register") {
                    shouldShowBottomNav.value = false
                    RegistrationScreen(navController, session)
                }
                composable("Home") {
                    shouldShowBottomNav.value = isLoggedIn
                    if (session.getUserId() != null) {
                        session.refreshUsername()
                        session.refreshAvatarUrl()
                    }
                    HomeScreen(
                        navController = navController,
                        onOpenDrawer = {
                            Logger.d("Navigation: Opening drawer from Home")
                            scope.launch {
                                drawerState.open()
                            }
                        }
                    )
                }
                composable("Profile") {
                    shouldShowBottomNav.value = isLoggedIn
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
                    shouldShowBottomNav.value = isLoggedIn
                    if (session.getUserId() != null) {
                        session.refreshUsername()
                        session.refreshAvatarUrl()
                    }
                    AchievementsScreen(
                        onOpenDrawer = {
                            Logger.d("Navigation: Opening drawer from Achievements")
                            scope.launch {
                                drawerState.open()
                            }
                        }
                    )
                }
                composable("Leaderboard") {
                    shouldShowBottomNav.value = isLoggedIn
                    if (session.getUserId() != null) {
                        session.refreshUsername()
                        session.refreshAvatarUrl()
                    }
                    LeaderboardScreen(
                        navController = navController,
                        onOpenDrawer = {
                            Logger.d("Navigation: Opening drawer from Leaderboard")
                            scope.launch {
                                drawerState.open()
                            }
                        }
                    )
                }
                composable(
                    route = "TestDetails/{testId}",
                    arguments = listOf(navArgument("testId") { type = NavType.LongType })
                ) {
                    val testId = it.arguments?.getLong("testId") ?: 0L
                    shouldShowBottomNav.value = false
                    TestDetailsScreen(navController, testId)
                }
                composable(
                    route = "TestSession/{sessionId}",
                    arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
                ) {
                    val sessionId = it.arguments?.getString("sessionId") ?: ""
                    shouldShowBottomNav.value = false
                    TestSessionScreen(navController, sessionId)
                }
                composable(
                    route = "TestResult/{sessionId}",
                    arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
                ) {
                    val sessionId = it.arguments?.getString("sessionId") ?: ""
                    shouldShowBottomNav.value = false
                    TestResultScreen(navController, sessionId)
                }
                composable(
                    route = "EmailConfirmation/{email}",
                    arguments = listOf(navArgument("email") { type = NavType.StringType })
                ) {
                    val email = it.arguments?.getString("email") ?: ""
                    shouldShowBottomNav.value = false
                    EmailConfirmationScreen(navController, email)
                }
                composable("UserInterests") {
                    shouldShowBottomNav.value = session.getToken() != null
                    if (session.getUserId() != null) {
                        session.refreshUsername()
                        session.refreshAvatarUrl()
                    }
                    UserInterestsScreen(
                        onBack = {
                            Logger.d("Navigation: Going back from UserInterests")
                            Logger.d("Navigation: Current destination: ${navController.currentDestination?.route}")
                            if (!navController.safeNavigateBack()) {
                                navController.safeNavigate("Home", clearStack = true)
                            }
                        }
                    )
                }
                composable("Articles") {
                    shouldShowBottomNav.value = session.getToken() != null
                    if (session.getUserId() != null) {
                        session.refreshUsername()
                        session.refreshAvatarUrl()
                    }
                    ArticlesScreen(
                        onOpenDrawer = {
                            Logger.d("Navigation: Opening drawer from Articles")
                            scope.launch {
                                drawerState.open()
                            }
                        }
                    )
                }
                composable("History") {
                    shouldShowBottomNav.value = false
                    HistoryScreen(
                        onBack = {
                            Logger.d("Navigation: Back from History")
                            if (!navController.safeNavigateBack()) {
                                navController.safeNavigate("Home", clearStack = true)
                            }
                        }
                    )
                }
                composable("Feedback") {
                    shouldShowBottomNav.value = false
                    FeedbackScreen(navController)
                }
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