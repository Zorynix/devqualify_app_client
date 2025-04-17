package com.diploma.work.ui.feature.profile

import androidx.lifecycle.ViewModel
import com.diploma.work.data.AppSession
import com.diploma.work.ui.theme.AppThemeType
import com.diploma.work.ui.theme.ThemeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

data class ProfileUiState(
    val username: String = "User",
    val theme: AppThemeType = AppThemeType.Light
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val session: AppSession,
    private val themeManager: ThemeManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        loadProfile()
    }

    fun loadProfile() {
        _uiState.value = ProfileUiState(
            username = "TestUser",
            theme = themeManager.currentTheme.value
        )
    }

    fun toggleTheme() {
        themeManager.toggleTheme()
        _uiState.value = _uiState.value.copy(theme = themeManager.currentTheme.value)
    }

    fun logout() {
        session.clearToken()
    }
}