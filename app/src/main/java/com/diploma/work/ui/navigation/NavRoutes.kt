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
data class EmailConfirmation(val email: String) : NavRoute