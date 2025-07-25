package com.diploma.work.ui.feature.auth.confirmation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.diploma.work.data.AppSession
import com.diploma.work.data.models.ConfirmEmailRequest
import com.diploma.work.data.models.SendConfirmationCodeRequest
import com.diploma.work.data.repository.AuthRepository
import com.diploma.work.ui.base.BaseViewModel
import com.diploma.work.ui.navigation.Login
import com.diploma.work.ui.navigation.NavRoute
import com.diploma.work.utils.Constants
import com.diploma.work.utils.ErrorHandler
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
    savedStateHandle: SavedStateHandle,
    override val errorHandler: ErrorHandler
) : BaseViewModel() {
    val email: String = savedStateHandle.get<String>("email") ?: ""
    private val _code = MutableStateFlow("")
    val code: StateFlow<String> = _code

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage
    
    private val _resendCooldownSeconds = MutableStateFlow(0)
    val resendCooldownSeconds: StateFlow<Int> = _resendCooldownSeconds
    
    val resendEnabled: StateFlow<Boolean> = _resendCooldownSeconds
        .map { it == 0 && !isLoading.value }
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
    }    private fun sendConfirmationCode() = viewModelScope.launch {
        setLoading(true)
        val request = SendConfirmationCodeRequest(email = email)
        authRepository.sendConfirmationCode(request)
            .onSuccess {
                if(it.success) {
                    _successMessage.value = "Код отправлен"
                    startResendCooldown()
                } else {
                    setError("Ошибка отправки")
                }
            }.onFailure { error ->
                setError(errorHandler.getContextualErrorMessage(error, ErrorHandler.ErrorContext.EMAIL_CONFIRMATION))
            }
            .also { setLoading(false) }
    }

    fun onSendCodeClicked() = sendConfirmationCode()

    fun onResendCodeClicked() {
        if (_resendCooldownSeconds.value == 0) {
            sendConfirmationCode()
        }
    }    fun onConfirmClicked() {
        viewModelScope.launch {
            setLoading(true)
            clearGlobalError()
            _successMessage.value = null
            val request = ConfirmEmailRequest(email = email, confirmationToken = code.value)
            val confirmationResult = authRepository.confirmEmail(request)
            setLoading(false)
            confirmationResult.onSuccess { response ->
                if (response.confirmed) {
                    _navigationChannel.send(Login)
                } else {
                    setError("Подтверждение не удалось. Проверьте правильность введенного кода.")
                }
            }.onFailure { error ->
                setError(errorHandler.getContextualErrorMessage(error, ErrorHandler.ErrorContext.EMAIL_CONFIRMATION))
            }
        }
    }
}