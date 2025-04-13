package com.diploma.work.ui.navigation


import kotlinx.serialization.Serializable

interface NavRoute

@Serializable
object Login : NavRoute

@Serializable
object Register : NavRoute