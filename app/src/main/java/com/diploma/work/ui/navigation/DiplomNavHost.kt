package com.diploma.work.ui.navigation


import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.diploma.work.ui.DiplomNavHost
import com.diploma.work.ui.feature.auth.login.LoginScreen
import com.diploma.work.ui.feature.auth.register.RegistrationScreen
import com.diploma.work.ui.feature.home.HomeScreen
import com.diploma.work.ui.navigation.Login
import com.diploma.work.ui.navigation.Register

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    DiplomNavHost(
        navController = navController,
        startDestination = Login,
        modifier = Modifier.fillMaxSize(),
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
            LoginScreen(navController)
        }
        composable<Register> {
            RegistrationScreen(navController)
        }
        composable("home") {
            HomeScreen()
        }
    }
}