package com.diploma.work.ui.feature.profile

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.diploma.work.data.AppSession
import com.diploma.work.data.events.ProfileEvent
import com.diploma.work.data.events.ProfileEventBus
import com.diploma.work.data.models.GetUserAvatarRequest
import com.diploma.work.data.models.GetUserRequest
import com.diploma.work.data.models.UpdateUserAvatarRequest
import com.diploma.work.data.models.UpdateUserAvatarResponse
import com.diploma.work.data.models.UpdateUserProfileRequest
import com.diploma.work.data.models.UploadUserAvatarRequest
import com.diploma.work.data.models.UploadUserAvatarResponse
import com.diploma.work.data.models.User
import com.diploma.work.data.repository.UserInfoRepository
import com.diploma.work.grpc.userinfo.Direction
import com.diploma.work.grpc.userinfo.Level
import com.diploma.work.ui.base.BaseViewModel
import com.diploma.work.ui.theme.AppThemeType
import com.diploma.work.ui.theme.ThemeManager
import com.diploma.work.utils.Constants
import com.diploma.work.utils.ErrorHandler
import com.orhanobut.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class ProfileUiState(
    val username: String = "",
    val theme: AppThemeType = AppThemeType.Light,
    val avatarUrl: String = ""
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userInfoRepository: UserInfoRepository,
    val session: AppSession,
    private val themeManager: ThemeManager,
    private val profileEventBus: ProfileEventBus,
    override val errorHandler: ErrorHandler,
    @ApplicationContext private val context: Context
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

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
        viewModelScope.launch {
            profileEventBus.events.collect { event ->
                when (event) {
                    is ProfileEvent.TestCompleted,
                    is ProfileEvent.ProfileUpdated -> {
                        Logger.d("ProfileViewModel: Received profile update event, refreshing profile")
                        loadProfile()
                    }
                }
            }
        }
    }    fun loadProfile() {
        viewModelScope.launch {
            setLoading(true)
            clearGlobalError()
            
            val userId = session.getUserId()
            if (userId != null) {
                try {
                val request = GetUserRequest(userId = userId)
                val result = userInfoRepository.getUser(request)
                
                result.onSuccess { response ->
                    _user.value = response.user
                    
                    if (response.user.avatarUrl.isNotEmpty()) {
                        _avatarUrl.value = response.user.avatarUrl
                        session.storeAvatarUrl(response.user.avatarUrl)
                    } else {
                        val localAvatar = session.getAvatarUrl()
                        if (localAvatar != null) {
                            _avatarUrl.value = localAvatar                        } else {
                            val generatedAvatarUrl = "https://ui-avatars.com/api/?name=${response.user.username}&background=random&size=${Constants.Business.GENERATED_AVATAR_SIZE}"
                            _avatarUrl.value = generatedAvatarUrl
                            session.storeAvatarUrl(generatedAvatarUrl)
                        }
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        username = response.user.username,
                        theme = themeManager.currentTheme.value,
                        avatarUrl = _avatarUrl.value
                    )
                    
                    session.storeUsername(response.user.username)
                    
                    _direction.value = response.user.direction
                    _level.value = response.user.level
                    
                    updateSessionWithProfileData()
                    
                    Logger.d("Successfully loaded user info: ${response.user.id}")                        }.onFailure { error ->
                            val errorMessage = errorHandler.handleError(error, Constants.ErrorMessages.NETWORK_ERROR)
                            setError(errorMessage)
                            Logger.e("Failed to load user info: ${error.message}")
                        }
                    } catch (e: Exception) {
                        val errorMessage = errorHandler.handleError(e, Constants.ErrorMessages.GENERIC_ERROR)
                        setError(errorMessage)
                        Logger.e("Exception loading user info: ${e.message}")
                    }
                } else {
                    setError("Не удалось определить пользователя. Попробуйте войти в систему заново.")
                    Logger.e("User ID not found in session")                
                val defaultAvatarUrl = "https://ui-avatars.com/api/?name=User&background=random&size=${Constants.Business.GENERATED_AVATAR_SIZE}"
                _avatarUrl.value = session.getAvatarUrl() ?: defaultAvatarUrl
                session.storeAvatarUrl(_avatarUrl.value)
                
                _uiState.value = _uiState.value.copy(
                    username = "User",
                    theme = themeManager.currentTheme.value,                    avatarUrl = _avatarUrl.value
                )
            }
            
            setLoading(false)
        }
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
        setLoading(true)
        clearGlobalError()
        
        try {
            session.storeAvatarImage(uri)
            
            val userId = session.getUserId()
            if (userId != null) {
                val inputStream = try {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)
                    }
                } catch (e: Exception) {
                    Logger.e("Error opening avatar file: ${e.message}")
                    val errorMessage = errorHandler.handleError(e, Constants.ErrorMessages.GENERIC_ERROR)
                    setError(errorMessage)
                    setLoading(false)
                    return
                }
                
                if (inputStream != null) {
                    val avatarBytes = withContext(Dispatchers.IO) {
                        try {
                            inputStream.readBytes().also {
                                inputStream.close()
                            }
                        } catch (e: Exception) {
                            Logger.e("Error reading avatar bytes: ${e.message}")
                            null
                        }
                    }
                    if (avatarBytes == null) {
                        val errorMessage = errorHandler.handleError(
                            Exception("Failed to read avatar data"), 
                            Constants.ErrorMessages.GENERIC_ERROR
                        )
                        setError(errorMessage)
                        setLoading(false)
                        return
                    }
                    
                    val contentType = withContext(Dispatchers.IO) {
                        context.contentResolver.getType(uri) ?: "image/jpeg"
                    }
                    
                    val avatarRequest = GetUserAvatarRequest(userId = userId)
                    val avatarResult = userInfoRepository.getUserAvatar(avatarRequest)
                    
                    val uploadResult = if (avatarResult.isSuccess && 
                                          avatarResult.getOrNull()?.success == true && 
                                          avatarResult.getOrNull()?.avatarUrl?.isNotEmpty() == true) {
                        val updateRequest = UpdateUserAvatarRequest(
                            userId = userId,
                            avatarData = avatarBytes,
                            contentType = contentType
                        )
                        userInfoRepository.updateUserAvatar(updateRequest)
                    } else {
                        val uploadRequest = UploadUserAvatarRequest(
                            userId = userId,
                            avatarData = avatarBytes,
                            contentType = contentType
                        )
                        userInfoRepository.uploadUserAvatar(uploadRequest)
                    }
                    
                    uploadResult.onSuccess { response ->
                        if (response is UploadUserAvatarResponse) {
                            _avatarUrl.value = response.avatarUrl
                            _uiState.value = _uiState.value.copy(avatarUrl = response.avatarUrl)
                            session.storeAvatarUrl(response.avatarUrl)
                            updateSessionWithProfileData()
                            Logger.d("Avatar successfully uploaded to server: ${response.avatarUrl}")
                        } else if (response is UpdateUserAvatarResponse) {
                            _avatarUrl.value = response.avatarUrl
                            _uiState.value = _uiState.value.copy(avatarUrl = response.avatarUrl)
                            session.storeAvatarUrl(response.avatarUrl)
                            updateSessionWithProfileData()
                            Logger.d("Avatar successfully updated on server: ${response.avatarUrl}")
                        }
                    }.onFailure { error ->
                        Logger.e("Failed to upload/update avatar on server: ${error.message}")
                        val localAvatarUrl = "data:avatar"
                        _avatarUrl.value = localAvatarUrl
                        _uiState.value = _uiState.value.copy(avatarUrl = localAvatarUrl)
                        session.storeAvatarUrl(localAvatarUrl)
                    }                } else {
                    Logger.e("Failed to open avatar image stream")
                    val errorMessage = errorHandler.handleError(
                        Exception("Failed to open avatar image"), 
                        Constants.ErrorMessages.GENERIC_ERROR
                    )
                    setError(errorMessage)
                    val localAvatarUrl = "data:avatar"
                    _avatarUrl.value = localAvatarUrl
                    _uiState.value = _uiState.value.copy(avatarUrl = localAvatarUrl)
                    session.storeAvatarUrl(localAvatarUrl)
                }
            } else {
                _avatarUrl.value = "data:avatar"
                _uiState.value = _uiState.value.copy(avatarUrl = "data:avatar")
                session.storeAvatarUrl("data:avatar")
                Logger.d("Avatar image stored locally only (no user ID available)")
            }        } catch (e: Exception) {
            Logger.e("Error saving avatar image: ${e.message}")
            val errorMessage = errorHandler.handleError(e, Constants.ErrorMessages.GENERIC_ERROR)
            setError(errorMessage)
            
            if (session.getAvatarUrl() == "data:avatar") {
                _avatarUrl.value = "data:avatar"
                _uiState.value = _uiState.value.copy(avatarUrl = "data:avatar")
                session.storeAvatarUrl("data:avatar")
            }
        } finally {
            setLoading(false)
        }
    }    fun updateUserProfile() {
        viewModelScope.launch {
            setLoading(true)
            clearGlobalError()
            _updateSuccess.value = false
            
            val userId = session.getUserId()
            if (userId != null) {
                try {
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
                        
                        updateSessionWithProfileData()
                        
                        _updateSuccess.value = true
                        Logger.d("Successfully updated user profile: ${response.user.id}")
                        
                        viewModelScope.launch {
                            kotlinx.coroutines.delay(Constants.UI.ANIMATION_DURATION_MS.toLong())
                            _updateSuccess.value = false
                        }
                    }.onFailure { error ->
                        val errorMessage = errorHandler.handleError(error, Constants.ErrorMessages.GENERIC_ERROR)
                        setError(errorMessage)
                        Logger.e("Failed to update user profile: ${error.message}")
                        
                        viewModelScope.launch {
                            kotlinx.coroutines.delay(5000)
                            clearGlobalError()
                        }
                    }
                } catch (e: Exception) {
                    val errorMessage = errorHandler.handleError(e, Constants.ErrorMessages.GENERIC_ERROR)
                    setError(errorMessage)
                    Logger.e("Exception updating user profile: ${e.message}")
                }
            } else {
                setError("Не удалось определить пользователя. Попробуйте войти в систему заново.")
                Logger.e("User ID not found in session")
            }
            
            setLoading(false)
        }
    }

    fun resetUpdateStatus() {
        _updateSuccess.value = false
        clearGlobalError()
    }

    fun uploadAvatar(uri: android.net.Uri) {
        viewModelScope.launch {
            saveAvatarImage(uri)
        }
    }
    
    fun refreshProfile() {
        loadProfile()
    }
    
    private fun updateSessionWithProfileData() {
        session.refreshUsername()
        session.refreshAvatarUrl()
    }
}