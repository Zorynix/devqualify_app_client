package com.diploma.work.ui.feature.userinfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diploma.work.data.AppSession
import com.diploma.work.data.models.GetUserRequest
import com.diploma.work.data.models.UpdateUserProfileRequest
import com.diploma.work.data.models.User
import com.diploma.work.data.repository.UserInfoRepository
import com.diploma.work.grpc.Direction
import com.diploma.work.grpc.Level
import com.orhanobut.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserInfoViewModel @Inject constructor(
    private val userInfoRepository: UserInfoRepository,
    private val session: AppSession
) : ViewModel() {
    private val tag = "UserInfoViewModel"

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _direction = MutableStateFlow(Direction.DIRECTION_UNSPECIFIED)
    val direction: StateFlow<Direction> = _direction

    private val _level = MutableStateFlow(Level.LEVEL_UNSPECIFIED)
    val level: StateFlow<Level> = _level

    init {
        loadUserInfo()
    }

    fun loadUserInfo() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val userId = session.getUserId()
            
            if (userId != null) {
                Logger.d("Loading user info for userId: $userId")
                val request = GetUserRequest(userId = userId)
                
                val result = userInfoRepository.getUser(request)
                result.onSuccess { response ->
                    _user.value = response.user
                    _username.value = response.user.username
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
            }
            
            _isLoading.value = false
        }
    }

    fun onUsernameChanged(newValue: String) {
        _username.value = newValue
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
                    username = username.value,
                    direction = direction.value,
                    level = level.value
                )
                
                val result = userInfoRepository.updateUserProfile(request)
                result.onSuccess { response ->
                    _user.value = response.user
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