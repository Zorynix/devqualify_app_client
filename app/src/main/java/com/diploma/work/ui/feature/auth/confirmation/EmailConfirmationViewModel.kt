package com.diploma.work.ui.feature.auth.confirmation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diploma.work.data.AppSession
import com.diploma.work.data.models.ConfirmEmailRequest
import com.diploma.work.data.models.SendConfirmationCodeRequest
import com.diploma.work.data.repository.AuthRepository
import com.diploma.work.ui.navigation.Login
import com.diploma.work.ui.navigation.NavRoute
import com.diploma.work.utils.ErrorContext
import com.diploma.work.utils.ErrorMessageUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmailConfirmationViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val session: AppSession,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val email: String = savedStateHandle.get<String>("email") ?: ""

    private val _code = MutableStateFlow("")
    val code: StateFlow<String> = _code

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage
    
    private val _resendCooldownSeconds = MutableStateFlow(0)
    val resendCooldownSeconds: StateFlow<Int> = _resendCooldownSeconds
    
    val resendEnabled: StateFlow<Boolean> = _resendCooldownSeconds
        .map { it == 0 && !_isLoading.value }
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    private val _navigationChannel = Channel<NavRoute>(Channel.BUFFERED)
    val navigationChannel: Flow<NavRoute> = _navigationChannel.receiveAsFlow()

    val confirmEnabled: StateFlow<Boolean> = code.map { it.length == 6 }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun onCodeChanged(newCode: String) {
        if (newCode.all { it.isDigit() } && newCode.length <= 6) {
            _code.value = newCode
        }
    }

    private fun startResendCooldown() {
        viewModelScope.launch {
            _resendCooldownSeconds.value = 60
            while (_resendCooldownSeconds.value > 0) {
                delay(1000)
                _resendCooldownSeconds.value -= 1
            }
        }
    }

    private fun sendConfirmationCode() = viewModelScope.launch {
        _isLoading.value = true
        val request = SendConfirmationCodeRequest(email = email)
        authRepository.sendConfirmationCode(request)
            .onSuccess {
                if(it.success) {
                    _successMessage.value = "Код отправлен"
                    startResendCooldown()
                }
                else _errorMessage.value = "Ошибка отправки"
            }            .onFailure { error ->
                _errorMessage.value = ErrorMessageUtils.getContextualErrorMessage(error, ErrorContext.EMAIL_CONFIRMATION)
            }
            .also { _isLoading.value = false }
    }

    fun onSendCodeClicked() = sendConfirmationCode()

    fun onResendCodeClicked() {
        if (_resendCooldownSeconds.value == 0) {
            sendConfirmationCode()
        }
    }

    fun onConfirmClicked() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null
            val request = ConfirmEmailRequest(email = email, confirmationToken = code.value)
            val confirmationResult = authRepository.confirmEmail(request)
            _isLoading.value = false
            confirmationResult.onSuccess { response ->
                if (response.confirmed) {
                    _navigationChannel.send(Login)                } else {
                    _errorMessage.value = "Подтверждение не удалось. Проверьте правильность введенного кода."
                }
            }.onFailure { error ->
                _errorMessage.value = ErrorMessageUtils.getContextualErrorMessage(error, ErrorContext.EMAIL_CONFIRMATION)
            }
        }
    }
}