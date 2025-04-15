package com.diploma.work.ui.feature.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diploma.work.data.models.RegisterRequest
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
class RegistrationViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _registerSuccess = MutableStateFlow(false)
    val registerSuccess: StateFlow<Boolean> = _registerSuccess

    val registerEnabled: StateFlow<Boolean> = combine(email, password, confirmPassword) { e, p, cp -> e.isNotEmpty() && p.isNotEmpty() && cp == p
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)


    fun onEmailChanged(newValue: String) {
        _email.value = newValue
        _errorMessage.value = null
        Logger.d("Email changed to: $newValue")
    }

    fun onPasswordChanged(newValue: String) {
        _password.value = newValue
        _errorMessage.value = null
        Logger.d("Password changed")
    }

    fun onConfirmPasswordChanged(newValue: String) {
        _confirmPassword.value = newValue
        _errorMessage.value = null
        Logger.d("Confirm password changed")
    }

    fun onRegisterClicked() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            Logger.d("Registration attempt with email: ${email.value}")

            val request = RegisterRequest(
                email = email.value,
                password = password.value
            )

            val result = authRepository.register(request)
            _isLoading.value = false

            result.onSuccess { response ->
                _registerSuccess.value = true
                Logger.d("Registration successful: User ID = ${response.userId}")
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Ошибка регистрации"
                Logger.e("Registration failed: ${error.message}")
            }
        }
    }
}