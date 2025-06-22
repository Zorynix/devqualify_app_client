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
    val articlesPerDay: Int = 5,
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
                    
                    val cachedPreferences = session.getUserPreferences()
                    
                    if (cachedPreferences != null) {
                        Logger.d("$tag: Loaded preferences from cache")
                        Logger.d("$tag: Cached technology IDs: ${cachedPreferences.technologyIds}")
                        Logger.d("$tag: Cached directions: ${cachedPreferences.directions}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            technologies = technologies,
                            selectedTechnologyIds = cachedPreferences.technologyIds.toSet(),
                            selectedDirections = cachedPreferences.directions.toSet(),
                            deliveryFrequency = cachedPreferences.deliveryFrequency,
                            emailNotifications = cachedPreferences.emailNotifications,
                            pushNotifications = cachedPreferences.pushNotifications,
                            articlesPerDay = cachedPreferences.articlesPerDay
                        )
                    } else {
                        val userId = session.getUserId()
                        if (userId != null) {
                            val preferencesResult = articlesRepository.getUserPreferences(
                                GetUserPreferencesRequest(userId)
                            )
                            
                            if (preferencesResult.isSuccess) {
                                val preferences = preferencesResult.getOrNull()?.preferences
                                
                                if (preferences != null) {
                                    session.storeUserPreferences(preferences)
                                    Logger.d("$tag: Loaded preferences from server and cached")
                                }
                                
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    technologies = technologies,
                                    selectedTechnologyIds = preferences?.technologyIds?.toSet() ?: emptySet(),
                                    selectedDirections = preferences?.directions?.toSet() ?: emptySet(),
                                    deliveryFrequency = preferences?.deliveryFrequency ?: DeliveryFrequency.WEEKLY,
                                    emailNotifications = preferences?.emailNotifications ?: true,
                                    pushNotifications = preferences?.pushNotifications ?: true,
                                    articlesPerDay = preferences?.articlesPerDay ?: 5
                                )
                            } else {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    technologies = technologies
                                )
                            }
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                technologies = technologies
                            )
                        }
                    }
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
                    Logger.d("$tag: User preferences saved successfully")
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
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        saveSuccess = true
                    )
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Failed to save preferences"
                    Logger.e("$tag: $error")
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
}
