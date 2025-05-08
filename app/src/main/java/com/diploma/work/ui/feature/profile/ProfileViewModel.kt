package com.diploma.work.ui.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diploma.work.data.AppSession
import com.diploma.work.data.models.GetUserRequest
import com.diploma.work.data.models.UpdateUserProfileRequest
import com.diploma.work.data.models.User
import com.diploma.work.data.repository.UserInfoRepository
import com.diploma.work.grpc.Direction
import com.diploma.work.grpc.Level
import com.diploma.work.ui.theme.AppThemeType
import com.diploma.work.ui.theme.ThemeManager
import com.orhanobut.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val username: String = "",
    val theme: AppThemeType = AppThemeType.Light,
    val avatarUrl: String = ""
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    val session: AppSession,
    private val themeManager: ThemeManager,
    private val userInfoRepository: UserInfoRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess

    private val _direction = MutableStateFlow(Direction.DIRECTION_UNSPECIFIED)
    val direction: StateFlow<Direction> = _direction

    private val _level = MutableStateFlow(Level.LEVEL_UNSPECIFIED)
    val level: StateFlow<Level> = _level
    
    private val _avatarUrl = MutableStateFlow("")
    val avatarUrl: StateFlow<String> = _avatarUrl

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            val userId = session.getUserId()
            if (userId != null) {
                val request = GetUserRequest(userId = userId)
                val result = userInfoRepository.getUser(request)
                
                result.onSuccess { response ->
                    _user.value = response.user
                    
                    val generatedAvatarUrl = "https://ui-avatars.com/api/?name=${response.user.username}&background=random&size=200"
                    
                    _avatarUrl.value = session.getAvatarUrl() ?: generatedAvatarUrl
                    
                    _uiState.value = _uiState.value.copy(
                        username = response.user.username,
                        theme = themeManager.currentTheme.value,
                        avatarUrl = _avatarUrl.value
                    )
                    
                    session.storeUsername(response.user.username)
                    
                    _direction.value = response.user.direction
                    _level.value = response.user.level
                    Logger.d("Successfully loaded user info: ${response.user.id}")
                }.onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to load user information"
                    Logger.e("Failed to load user info: ${error.message}")
                }
            } else {
                _errorMessage.value = "User ID not found in session"
                Logger.e("User ID not found in session")
                
                val defaultAvatarUrl = "https://ui-avatars.com/api/?name=User&background=random&size=200"
                _avatarUrl.value = session.getAvatarUrl() ?: defaultAvatarUrl
                
                _uiState.value = _uiState.value.copy(
                    username = "User",
                    theme = themeManager.currentTheme.value,
                    avatarUrl = _avatarUrl.value
                )
            }
            
            _isLoading.value = false
        }
    }

    fun toggleTheme() {
        themeManager.toggleTheme()
        _uiState.value = _uiState.value.copy(theme = themeManager.currentTheme.value)
    }

    fun logout() {
        session.clearToken()
    }
    
    fun onUsernameChanged(newValue: String) {
        _uiState.value = _uiState.value.copy(username = newValue)
        session.storeUsername(newValue)
        Logger.d("Username changed to: $newValue")
    }

    fun onDirectionChanged(newValue: Direction) {
        _direction.value = newValue
        Logger.d("Direction changed to: $newValue")
    }

    fun onLevelChanged(newValue: Level) {
        _level.value = newValue
        Logger.d("Level changed to: $newValue")
    }
    
    fun onAvatarChanged(newAvatarUrl: String) {
        _avatarUrl.value = newAvatarUrl
        _uiState.value = _uiState.value.copy(avatarUrl = newAvatarUrl)
        session.storeAvatarUrl(newAvatarUrl)
        Logger.d("Avatar changed to: $newAvatarUrl")
    }
    
    suspend fun saveAvatarImage(uri: android.net.Uri) {
        _isLoading.value = true
        try {
            session.storeAvatarImage(uri)
            val newAvatarUrl = "data:avatar"
            _avatarUrl.value = newAvatarUrl
            _uiState.value = _uiState.value.copy(avatarUrl = newAvatarUrl)
            Logger.d("Avatar image stored successfully")
        } catch (e: Exception) {
            Logger.e("Error saving avatar image: ${e.message}")
            _errorMessage.value = "Failed to save avatar image"
        } finally {
            _isLoading.value = false
        }
    }

    fun updateUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _updateSuccess.value = false
            
            val userId = session.getUserId()
            if (userId != null) {
                Logger.d("Updating user profile for userId: $userId")
                
                val request = UpdateUserProfileRequest(
                    userId = userId,
                    username = _uiState.value.username,
                    direction = direction.value,
                    level = level.value
                )
                
                val result = userInfoRepository.updateUserProfile(request)
                result.onSuccess { response ->
                    _user.value = response.user
                    
                    session.storeAvatarUrl(_avatarUrl.value)
                    session.storeUsername(_uiState.value.username)
                    
                    _updateSuccess.value = true
                    Logger.d("Successfully updated user profile: ${response.user.id}")
                }.onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to update user profile"
                    Logger.e("Failed to update user profile: ${error.message}")
                }
            } else {
                _errorMessage.value = "User ID not found in session"
                Logger.e("User ID not found in session")
            }
            
            _isLoading.value = false
        }
    }

    fun resetUpdateStatus() {
        _updateSuccess.value = false
        _errorMessage.value = null
    }
}