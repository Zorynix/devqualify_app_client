package com.diploma.work.ui.feature.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diploma.work.data.AppSession
import com.diploma.work.data.models.Achievement
import com.diploma.work.data.models.GetUserAchievementsRequest
import com.diploma.work.data.repository.UserInfoRepository
import com.orhanobut.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AchievementsUiState(
    val isLoading: Boolean = false,
    val achievements: List<Achievement> = emptyList(),
    val errorMessage: String? = null,
    val selectedAchievement: Achievement? = null,
    val showAchievementDetails: Boolean = false
)

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val userInfoRepository: UserInfoRepository,
    private val session: AppSession
) : ViewModel() {
    private val _uiState = MutableStateFlow(AchievementsUiState())
    val uiState: StateFlow<AchievementsUiState> = _uiState.asStateFlow()

    init {
        loadAchievements()
    }

    fun loadAchievements() {
        viewModelScope.launch {
            Logger.d("Loading achievements")
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            val userId = session.getUserId()
            if (userId != null) {
                Logger.d("Fetching achievements for user: $userId")
                val request = GetUserAchievementsRequest(userId = userId)
                
                val result = userInfoRepository.getUserAchievements(request)
                result.onSuccess { response ->
                    Logger.d("Successfully loaded ${response.achievements.size} achievements")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        achievements = response.achievements
                    )
                }.onFailure { error ->
                    Logger.e("Failed to load achievements: ${error.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to load achievements"
                    )
                }
            } else {
                Logger.e("User ID not found in session")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "User ID not found in session"
                )
            }
        }
    }

    fun showAchievementDetails(achievement: Achievement) {
        Logger.d("Showing details for achievement: ${achievement.name}")
        _uiState.value = _uiState.value.copy(
            selectedAchievement = achievement,
            showAchievementDetails = true
        )
    }

    fun dismissAchievementDetails() {
        Logger.d("Dismissing achievement details")
        _uiState.value = _uiState.value.copy(
            showAchievementDetails = false
        )
    }
    
    fun clearError() {
        Logger.d("Clearing error message")
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}