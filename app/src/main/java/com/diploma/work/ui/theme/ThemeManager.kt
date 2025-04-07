package com.diploma.work.ui.theme

import android.content.SharedPreferences
import com.diploma.work.di.AppPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeManager @Inject constructor(
    @AppPrefs private val preferences: SharedPreferences
) {
    private val _currentTheme = MutableStateFlow<AppThemeType>(
        if (preferences.getBoolean("isDarkTheme", false)) AppThemeType.Dark else AppThemeType.Light
    )
    val currentTheme: StateFlow<AppThemeType> = _currentTheme.asStateFlow()

    fun toggleTheme() {
        _currentTheme.value = when (_currentTheme.value) {
            AppThemeType.Light -> AppThemeType.Dark
            AppThemeType.Dark -> AppThemeType.Light
        }
        preferences.edit().putBoolean("isDarkTheme", _currentTheme.value == AppThemeType.Dark).apply()
    }
}
