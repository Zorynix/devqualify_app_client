package com.diploma.work.ui.theme

import com.diploma.work.data.AppSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ThemeManager @Inject constructor(
    private val session: AppSession
) {
    private val _currentTheme = MutableStateFlow<AppThemeType>(
        if (session.getTheme()) AppThemeType.Dark else AppThemeType.Light
    )
    val currentTheme: StateFlow<AppThemeType> = _currentTheme.asStateFlow()

    fun toggleTheme() {
        _currentTheme.value = when (_currentTheme.value) {
            AppThemeType.Light -> AppThemeType.Dark
            AppThemeType.Dark -> AppThemeType.Light
        }
        session.setTheme(_currentTheme.value == AppThemeType.Dark)
    }
}