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
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.diploma.work.data.AppSession
import com.diploma.work.ui.DiplomNavHost
import com.diploma.work.ui.feature.achievements.AchievementsScreen
import com.diploma.work.ui.feature.auth.login.LoginScreen
import com.diploma.work.ui.feature.auth.register.RegistrationScreen
import com.diploma.work.ui.feature.home.HomeScreen
import com.diploma.work.ui.feature.profile.ProfileScreen

@Composable
fun AppNavigation(session: AppSession) {
    val navController = rememberNavController()
    val shouldShowBottomNav = remember { mutableStateOf(session.getToken() != null) }

    val navItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Profile,
        BottomNavItem.Achievements
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (shouldShowBottomNav.value) {
                NavigationBar {
                    val currentRoute by navController.currentBackStackEntryAsState()
                    navItems.forEach { item ->
                        val selected = currentRoute?.destination?.hierarchy?.any {
                            it.route == item.route::class.qualifiedName
                        } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.route::class.simpleName
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        DiplomNavHost(
            navController = navController,
            startDestination = if (session.getToken() != null) Home else Login,
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
            composable<Login> {
                shouldShowBottomNav.value = false
                LoginScreen(navController, session)
            }
            composable<Register> {
                shouldShowBottomNav.value = false
                RegistrationScreen(navController, session)
            }
            composable<Home> {
                shouldShowBottomNav.value = true
                HomeScreen()
            }
            composable<Profile> {
                shouldShowBottomNav.value = true
                ProfileScreen(navController)
            }
            composable<Achievements> {
                shouldShowBottomNav.value = true
                AchievementsScreen()
            }
        }
    }
}

sealed class BottomNavItem(val route: NavRoute, val icon: ImageVector) {
    object Home : BottomNavItem(com.diploma.work.ui.navigation.Home, Icons.Default.Home)
    object Profile : BottomNavItem(com.diploma.work.ui.navigation.Profile, Icons.Default.Person)
    object Achievements : BottomNavItem(com.diploma.work.ui.navigation.Achievements, Icons.Default.Star)
}