package com.diploma.work.ui.feature.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diploma.work.data.AppSession
import com.diploma.work.data.models.LoginRequest
import com.diploma.work.data.repository.AuthRepository
import com.orhanobut.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val session: AppSession
) : ViewModel() {
    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess

    val loginEnabled: StateFlow<Boolean> = combine(username, password) { u, p ->
        u.isNotEmpty() && p.isNotEmpty()
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun onUsernameChanged(newValue: String) {
        _username.value = newValue
        _errorMessage.value = null
        Logger.d("Username changed to: $newValue")
    }

    fun onPasswordChanged(newValue: String) {
        _password.value = newValue
        _errorMessage.value = null
        Logger.d("Password changed")
    }

    fun onLoginClicked(session: AppSession) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            Logger.d("Login attempt with username: ${username.value}")

            val request = LoginRequest(
                email = username.value,
                password = password.value,
                appId = 1
            )

            val result = authRepository.login(request)
            _isLoading.value = false

            result.onSuccess { response ->
                session.storeToken(response.accessToken)
                _loginSuccess.value = true
                Logger.d("Login successful: Access Token = ${response.accessToken}, Refresh Token = ${response.refreshToken}")
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Ошибка авторизации"
                Logger.e("Login failed: ${error.message}")
            }
        }
    }
}