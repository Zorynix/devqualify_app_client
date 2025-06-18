package com.diploma.work.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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

fun NavController.navigate(
    route: NavRoute,
    builder: (NavOptions.Builder.() -> Unit)? = null
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

    Logger.d("Navigation: Navigating to $routeName")
    android.util.Log.d("NavDebug", "Navigating to route: $routeName (raw: $route)")
        
    if (builder != null) {
        val optionsBuilder = NavOptions.Builder().apply(builder)
        navigate(routeName, optionsBuilder.build())
    } else {
        navigate(routeName)
    }
}

@Composable
fun AppNavigation(
    session: AppSession,
    themeManager: ThemeManager
) {
    val navController = rememberNavController()
    val shouldShowBottomNav = remember { mutableStateOf(session.getToken() != null) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val isLoggedIn = session.getToken() != null
    Logger.d("Navigation: AppNavigation setup, user logged in: $isLoggedIn")
    
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
        drawerState = drawerState,
        gesturesEnabled = isLoggedIn,
        drawerContent = {
            if (isLoggedIn) {                AppDrawerContent(
                    username = username.value ?: "User",
                    avatarUrl = avatarUrl.value ?: "https://ui-avatars.com/api/?name=User&background=random&size=200",
                    theme = theme,
                    onThemeToggle = { 
                        themeManager.toggleTheme()
                    },
                    onInterestsClick = {
                        Logger.d("Navigation: Navigating to UserInterests")
                        scope.launch {
                            drawerState.close()
                            navController.navigate("UserInterests") {
                                launchSingleTop = true
                            }
                        }
                    },
                    onLogout = {
                        Logger.d("Navigation: User logging out")
                        session.clearToken()
                        shouldShowBottomNav.value = false
                        scope.launch {
                            drawerState.close()
                            navController.navigate("Login") {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
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
                                selected = selected,                                onClick = {
                                    val route = when (item.route) {
                                        is Home -> "Home" 
                                        is Profile -> "Profile"
                                        is Achievements -> "Achievements"
                                        is Leaderboard -> "Leaderboard"
                                        is Articles -> "Articles"
                                        else -> ""
                                    }
                                    Logger.d("Navigation: Bottom nav bar click - $route")
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,                                        contentDescription = when (item.route) {
                                            is Home -> "Home"
                                            is Profile -> "Profile"
                                            is Achievements -> "Achievements"
                                            is Leaderboard -> "Leaderboard"
                                            is Articles -> "Articles"
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
                startDestination = if (session.getToken() != null) "Home" else "Login",
                modifier = Modifier.padding(innerPadding),
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
                    shouldShowBottomNav.value = true
                    if (session.getUserId() != null) {
                        session.getUsername()
                        session.getAvatarUrl()
                    }
                    HomeScreen(navController)
                }
                composable("Profile") {
                    shouldShowBottomNav.value = true
                    val profileScreen = ProfileScreen(
                        navController = navController,
                        drawerState = drawerState,
                        themeManager = themeManager
                    )
                    if (session.getUserId() != null) {
                        session.getUsername()
                        session.getAvatarUrl()
                    }
                    profileScreen
                }
                composable("Achievements") {
                    shouldShowBottomNav.value = true
                    if (session.getUserId() != null) {
                        session.getUsername()
                        session.getAvatarUrl()
                    }
                    AchievementsScreen()
                }
                composable("Leaderboard") {
                    shouldShowBottomNav.value = true
                    if (session.getUserId() != null) {
                        session.getUsername()
                        session.getAvatarUrl()
                    }
                    LeaderboardScreen(navController)
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
                    shouldShowBottomNav.value = true
                    if (session.getUserId() != null) {
                        session.getUsername()
                        session.getAvatarUrl()
                    }
                    UserInterestsScreen(
                        onBack = { navController.navigateUp() }
                    )
                }
                composable("Articles") {
                    shouldShowBottomNav.value = true
                    if (session.getUserId() != null) {
                        session.getUsername()
                        session.getAvatarUrl()
                    }
                    ArticlesScreen(
                        onNavigateUp = { navController.navigateUp() }
                    )
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
    object Articles : BottomNavItem(com.diploma.work.ui.navigation.Articles, Icons.Filled.Article)
}