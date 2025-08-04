package com.diploma.work.ui.feature.interests

import androidx.lifecycle.viewModelScope
import com.diploma.work.data.AppSession
import com.diploma.work.data.models.*
import com.diploma.work.data.repository.ArticlesRepository
import com.diploma.work.ui.base.BaseViewModel
import com.diploma.work.utils.Constants
import com.diploma.work.utils.ErrorHandler
import com.orhanobut.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserInterestsUiState(
    val isLoading: Boolean = false,
    val technologies: List<ArticleTechnology> = emptyList(),
    val selectedTechnologyIds: Set<Long> = emptySet(),
    val selectedDirections: Set<ArticleDirection> = emptySet(),
    val deliveryFrequency: DeliveryFrequency = DeliveryFrequency.WEEKLY,
    val emailNotifications: Boolean = true,
    val pushNotifications: Boolean = true,
    val articlesPerDay: Int = 20,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class UserInterestsViewModel @Inject constructor(
    private val articlesRepository: ArticlesRepository,
    private val session: AppSession,
    override val errorHandler: ErrorHandler
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(UserInterestsUiState())
    val uiState: StateFlow<UserInterestsUiState> = _uiState.asStateFlow()

    private val tag = "UserInterestsViewModel"
    init {
        Logger.d("$tag: UserInterestsViewModel initialized")
        loadData()
    }private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val technologiesResult = articlesRepository.getTechnologies(
                    GetTechnologiesRequest(pageSize = 100)
                )
                
                if (technologiesResult.isSuccess) {
                    val technologies = technologiesResult.getOrNull()?.technologies ?: emptyList()
                    
                    val userId = session.getUserId()
                    var preferences: UserPreferences? = null
                    
                    if (userId != null) {
                        Logger.d("$tag: Loading preferences from server for user: $userId")
                        val preferencesResult = articlesRepository.getUserPreferences(
                            GetUserPreferencesRequest(userId)
                        )
                        
                        if (preferencesResult.isSuccess) {
                            preferences = preferencesResult.getOrNull()?.preferences
                            
                            if (preferences != null) {
                                session.storeUserPreferences(preferences)
                                Logger.d("$tag: Loaded preferences from server and cached")
                                Logger.d("$tag: Server technology IDs: ${preferences.technologyIds}")
                                Logger.d("$tag: Server directions: ${preferences.directions}")
                            } else {
                                Logger.d("$tag: Server returned no preferences")
                            }
                        } else {
                            Logger.e("$tag: Failed to load preferences from server, trying cache")
                            preferences = session.getUserPreferences()
                            if (preferences != null) {
                                Logger.d("$tag: Using cached preferences as fallback")
                                Logger.d("$tag: Cached technology IDs: ${preferences.technologyIds}")
                                Logger.d("$tag: Cached directions: ${preferences.directions}")
                            }
                        }
                    } else {
                        Logger.w("$tag: No user ID found, cannot load preferences")
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        technologies = technologies,
                        selectedTechnologyIds = preferences?.technologyIds?.toSet() ?: emptySet(),
                        selectedDirections = preferences?.directions?.toSet() ?: emptySet(),
                        deliveryFrequency = preferences?.deliveryFrequency ?: DeliveryFrequency.WEEKLY,
                        emailNotifications = preferences?.emailNotifications ?: true,
                        pushNotifications = preferences?.pushNotifications ?: true,
                        articlesPerDay = preferences?.articlesPerDay ?: 20
                    )
                } else {
                    val error = technologiesResult.exceptionOrNull()?.message ?: "Failed to load technologies"
                    Logger.e("$tag: $error")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error
                    )
                }
            } catch (e: Exception) {
                Logger.e("$tag: Exception during data loading: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun toggleTechnology(technologyId: Long) {
        val currentSelected = _uiState.value.selectedTechnologyIds.toMutableSet()
        if (currentSelected.contains(technologyId)) {
            currentSelected.remove(technologyId)
        } else {
            currentSelected.add(technologyId)
        }
        _uiState.value = _uiState.value.copy(selectedTechnologyIds = currentSelected)
    }

    fun toggleDirection(direction: ArticleDirection) {
        val currentSelected = _uiState.value.selectedDirections.toMutableSet()
        if (currentSelected.contains(direction)) {
            currentSelected.remove(direction)
        } else {
            currentSelected.add(direction)
        }
        _uiState.value = _uiState.value.copy(selectedDirections = currentSelected)
    }

    fun setDeliveryFrequency(frequency: DeliveryFrequency) {
        _uiState.value = _uiState.value.copy(deliveryFrequency = frequency)
    }

    fun setEmailNotifications(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(emailNotifications = enabled)
    }

    fun setPushNotifications(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(pushNotifications = enabled)
    }

    fun setArticlesPerDay(count: Int) {
        val validCount = count.coerceIn(1, 20)
        _uiState.value = _uiState.value.copy(articlesPerDay = validCount)
    }

    fun savePreferences() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, saveSuccess = false)
            
            try {
                val userId = session.getUserId()
                if (userId == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "User not logged in"
                    )
                    return@launch
                }

                Logger.d("$tag: Saving preferences for user: $userId")
                Logger.d("$tag: Technology IDs: ${_uiState.value.selectedTechnologyIds}")
                Logger.d("$tag: Directions: ${_uiState.value.selectedDirections}")
                Logger.d("$tag: Delivery frequency: ${_uiState.value.deliveryFrequency}")
                Logger.d("$tag: Email notifications: ${_uiState.value.emailNotifications}")
                Logger.d("$tag: Push notifications: ${_uiState.value.pushNotifications}")
                Logger.d("$tag: Articles per day: ${_uiState.value.articlesPerDay}")

                val request = UpdateUserPreferencesRequest(
                    userId = userId,
                    technologyIds = _uiState.value.selectedTechnologyIds.toList(),
                    directions = _uiState.value.selectedDirections.toList(),
                    deliveryFrequency = _uiState.value.deliveryFrequency,
                    emailNotifications = _uiState.value.emailNotifications,
                    pushNotifications = _uiState.value.pushNotifications,
                    excludedSources = emptyList(),
                    articlesPerDay = _uiState.value.articlesPerDay
                )

                val result = articlesRepository.updateUserPreferences(request)
                if (result.isSuccess) {
                    Logger.d("$tag: User preferences saved successfully on server")
                      val savedPreferences = UserPreferences(
                        userId = userId,
                        technologyIds = _uiState.value.selectedTechnologyIds.toList(),
                        directions = _uiState.value.selectedDirections.toList(),
                        deliveryFrequency = _uiState.value.deliveryFrequency,
                        emailNotifications = _uiState.value.emailNotifications,
                        pushNotifications = _uiState.value.pushNotifications,
                        excludedSources = emptyList(),
                        articlesPerDay = _uiState.value.articlesPerDay,
                        updatedAt = java.time.Instant.now()
                    )
                    session.storeUserPreferences(savedPreferences)
                    Logger.d("$tag: User preferences cached locally")
                    
                    kotlinx.coroutines.delay(500)
                    verifyPreferencesSaved(userId)
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        saveSuccess = true
                    )
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Failed to save preferences"
                    Logger.e("$tag: Failed to save preferences on server: $error")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error
                    )
                }
            } catch (e: Exception) {
                Logger.e("$tag: Exception during save: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    private suspend fun verifyPreferencesSaved(userId: Long) {
        try {
            Logger.d("$tag: Verifying preferences saved on server...")
            val verifyResult = articlesRepository.getUserPreferences(
                GetUserPreferencesRequest(userId)
            )
            
            if (verifyResult.isSuccess) {
                val serverPreferences = verifyResult.getOrNull()?.preferences
                if (serverPreferences != null) {
                    Logger.d("$tag: Verification successful - server has preferences")
                    Logger.d("$tag: Verified technology IDs: ${serverPreferences.technologyIds}")
                    Logger.d("$tag: Verified directions: ${serverPreferences.directions}")
                    
                    session.storeUserPreferences(serverPreferences)
                } else {
                    Logger.w("$tag: Verification failed - server returned empty preferences")
                }
            } else {
                Logger.e("$tag: Verification failed - could not get preferences from server")
            }
        } catch (e: Exception) {
            Logger.e("$tag: Exception during verification: ${e.message}")
        }
    }

    fun reloadData() {
        Logger.d("$tag: Manual reload requested")
        loadData()
    }

    override fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }

    fun getTechnologiesByDirection(direction: ArticleDirection): List<ArticleTechnology> {
        return _uiState.value.technologies.filter { it.direction == direction }
    }

    fun getDirections(): List<ArticleDirection> {
        return ArticleDirection.entries.filter { it != ArticleDirection.UNSPECIFIED }
    }

    fun manualReload() {
        Logger.d("Manual reload triggered by user")
        loadData()
    }
}
