package com.diploma.work.ui.feature.auth.login

import androidx.lifecycle.viewModelScope
import com.diploma.work.data.AppSession
import com.diploma.work.data.models.LoginRequest
import com.diploma.work.data.repository.AuthRepository
import com.diploma.work.ui.base.BaseViewModel
import com.diploma.work.utils.ErrorHandler
import com.diploma.work.utils.ValidationUtils
import com.orhanobut.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import android.util.Base64
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import kotlin.math.absoluteValue

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val session: AppSession,
    override val errorHandler: ErrorHandler
) : BaseViewModel() {private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess
    
    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError
    
    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError

    val loginEnabled: StateFlow<Boolean> = combine(username, password) { u, p ->
        ValidationUtils.validateEmail(u).isValid && ValidationUtils.validateStrongPassword(p).isValid
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)
    fun onUsernameChanged(newValue: String) {
        _username.value = newValue
        
        val emailValidation = ValidationUtils.validateEmail(newValue)
        _emailError.value = if (!emailValidation.isValid && newValue.isNotBlank()) 
            emailValidation.errorMessage else null
        clearError()
        Logger.d("Username changed to: $newValue")
    }

    fun onPasswordChanged(newValue: String) {
        _password.value = newValue
        
        val passwordValidation = ValidationUtils.validateStrongPassword(newValue)
        _passwordError.value = if (!passwordValidation.isValid && newValue.isNotBlank()) 
            passwordValidation.errorMessage else null
        clearError()
        Logger.d("Password changed")
    }    fun onLoginClicked(session: AppSession) {
        safeLaunch(showLoading = true) {
            val email = username.value
            val password = password.value
            
            val emailValidation = ValidationUtils.validateEmail(email)
            val passwordValidation = ValidationUtils.validateStrongPassword(password)
            
            if (!emailValidation.isValid) {
                _emailError.value = emailValidation.errorMessage
                return@safeLaunch
            }
            
            if (!passwordValidation.isValid) {
                _passwordError.value = passwordValidation.errorMessage
                return@safeLaunch
            }
            
            Logger.d("Login attempt with email: $email")

            val request = LoginRequest(
                email = email,
                password = password,
                appId = 1
            )

            val result = authRepository.login(request)

            result.onSuccess { response ->
                session.storeToken(response.accessToken)

                val userId = extractUserIdFromToken(response.accessToken)
                if (userId != null) {
                    session.storeUserId(userId)
                    Logger.d("User ID extracted and stored: $userId")
                } else {
                    val tempUserId = username.value.hashCode().toLong().absoluteValue
                    session.storeUserId(tempUserId)
                    Logger.d("Using temporary user ID: $tempUserId")
                }
                
                _loginSuccess.value = true
                Logger.d("Login successful: Access Token = ${response.accessToken}")
            }.onFailure { error ->
                Logger.e("Login failed: ${error.message}")
                showError(errorHandler.handleAuthError(error))
                _loginSuccess.value = false
            }
        }
    }
    
    private fun extractUserIdFromToken(token: String): Long? {
        try {
            val parts = token.split(".")
            if (parts.size < 2) {
                Logger.e("Invalid token format")
                return null
            }
            var payload = parts[1]

            while (payload.length % 4 != 0) {
                payload += "="
            }

            val decodedString = try {
                val bytes = Base64.decode(payload, Base64.URL_SAFE)
                String(bytes, StandardCharsets.UTF_8)
            } catch (e: Exception) {
                try {
                    val bytes = Base64.decode(payload, Base64.DEFAULT)
                    String(bytes, StandardCharsets.UTF_8)
                } catch (e2: Exception) {
                    Logger.e("Base64 decoding failed: ${e2.message}")
                    return null
                }
            }
            
            try {
                val jsonObject = JSONObject(decodedString)
                Logger.d("Token payload fields: ${jsonObject.keys().asSequence().toList()}")

                return when {
                    jsonObject.has("sub") -> jsonObject.getString("sub").toLong()
                    jsonObject.has("user_id") -> jsonObject.getLong("user_id")
                    jsonObject.has("id") -> jsonObject.getLong("id")
                    jsonObject.has("userId") -> jsonObject.getLong("userId")
                    jsonObject.has("uid") -> jsonObject.getLong("uid")
                    else -> {
                        Logger.e("Token payload doesn't contain recognized user ID field")
                        null
                    }
                }
            } catch (e: JSONException) {
                Logger.e("JSON parsing error: ${e.message}")
                return null
            }
        } catch (e: Exception) {
            Logger.e("Failed to decode token: ${e.message}")
            return null
        }
    }
}