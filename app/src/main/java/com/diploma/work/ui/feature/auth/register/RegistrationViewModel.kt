package com.diploma.work.ui.feature.auth.register

import androidx.lifecycle.viewModelScope
import com.diploma.work.data.AppSession
import com.diploma.work.data.models.RegisterRequest
import com.diploma.work.data.repository.AuthRepository
import com.diploma.work.ui.base.BaseViewModel
import com.diploma.work.utils.Constants
import com.diploma.work.utils.ErrorHandler
import com.diploma.work.utils.ValidationUtils
import com.orhanobut.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val session: AppSession,
    override val errorHandler: ErrorHandler
) : BaseViewModel() {private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError

    private val _confirmPasswordError = MutableStateFlow<String?>(null)
    val confirmPasswordError: StateFlow<String?> = _confirmPasswordError

    private val _navigationChannel = Channel<String>(Channel.BUFFERED)
    val navigationChannel: Flow<String> = _navigationChannel.receiveAsFlow()
    private val fieldsValidation = combine(
        email, password, confirmPassword
    ) { email, password, confirmPassword ->
        email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty() && password == confirmPassword
    }
    
    private val errorsValidation = combine(
        emailError, passwordError, confirmPasswordError
    ) { emailError, passwordError, confirmPasswordError ->
        emailError == null && passwordError == null && confirmPasswordError == null
    }
    
    val registerEnabled: StateFlow<Boolean> = combine(
        fieldsValidation, errorsValidation
    ) { fieldsValid, errorsValid ->
        fieldsValid && errorsValid
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)
    fun onEmailChanged(newValue: String) {
        _email.value = newValue
        
        val emailValidation = ValidationUtils.validateEmail(newValue)
        _emailError.value = if (emailValidation.isValid) null else emailValidation.errorMessage
        
        clearGlobalError()
        Logger.d("Email changed to: $newValue")
    }

    fun onPasswordChanged(newValue: String) {
        _password.value = newValue
        
        val passwordValidation = ValidationUtils.validateStrongPassword(newValue)
        _passwordError.value = if (passwordValidation.isValid) null else passwordValidation.errorMessage
        
        if (_confirmPassword.value.isNotEmpty()) {
            val confirmValidation = ValidationUtils.validatePasswordConfirmation(newValue, _confirmPassword.value)
            _confirmPasswordError.value = if (confirmValidation.isValid) null else confirmValidation.errorMessage
        }
        
        clearGlobalError()
        Logger.d("Password changed")
    }

    fun onConfirmPasswordChanged(newValue: String) {
        _confirmPassword.value = newValue
        
        val confirmValidation = ValidationUtils.validatePasswordConfirmation(_password.value, newValue)
        _confirmPasswordError.value = if (confirmValidation.isValid) null else confirmValidation.errorMessage
        
        clearGlobalError()
        Logger.d("Confirm password changed")
    }    fun onRegisterClicked(session: AppSession) {
        viewModelScope.launch {
            setLoading(true)
            clearGlobalError()
            val emailValidation = ValidationUtils.validateEmail(email.value)
            val passwordValidation = ValidationUtils.validateStrongPassword(password.value)
            val confirmValidation = ValidationUtils.validatePasswordConfirmation(password.value, confirmPassword.value)

            if (!emailValidation.isValid) {
                _emailError.value = emailValidation.errorMessage
                setLoading(false)
                return@launch
            }

            if (!passwordValidation.isValid) {
                _passwordError.value = passwordValidation.errorMessage
                setLoading(false)
                return@launch
            }

            if (!confirmValidation.isValid) {
                _confirmPasswordError.value = confirmValidation.errorMessage
                setLoading(false)
                return@launch
            }

            Logger.d("Registration attempt with email: ${email.value}")

            try {
                val registerRequest = RegisterRequest(
                    email = email.value,
                    password = password.value
                )

                val registerResult = authRepository.register(registerRequest)
                setLoading(false)

                registerResult.onSuccess {
                    Logger.d("Registration successful")
                    _navigationChannel.send("emailConfirmation/${email.value}")
                }.onFailure { error ->
                    val errorMessage = errorHandler.handleError(error, Constants.ErrorMessages.AUTH_ERROR)
                    setError(errorMessage)
                    Logger.e("Registration failed: ${error.message}")
                }
            } catch (e: Exception) {
                setLoading(false)
                val errorMessage = errorHandler.handleError(e, Constants.ErrorMessages.AUTH_ERROR)
                setError(errorMessage)
                Logger.e("Registration exception: ${e.message}")
            }
        }
    }
}