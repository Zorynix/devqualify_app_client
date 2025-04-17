package com.diploma.work.ui.feature.achievements


import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

data class AchievementsUiState(val achievementCount: Int = 0)

@HiltViewModel
class AchievementsViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(AchievementsUiState())
    val uiState: StateFlow<AchievementsUiState> = _uiState

    fun loadAchievements() {
        _uiState.value = AchievementsUiState(achievementCount = 5)
    }
}