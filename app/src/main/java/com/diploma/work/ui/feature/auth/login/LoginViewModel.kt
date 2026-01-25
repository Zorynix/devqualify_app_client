package com.diploma.work.ui.feature.auth.login

import androidx.lifecycle.viewModelScope
import com.diploma.work.data.AppSession
import com.diploma.work.data.models.LoginRequest
import com.diploma.work.data.repository.AuthRepository
import com.diploma.work.ui.base.BaseViewModel
import com.diploma.work.utils.ErrorHandler
import com.diploma.work.utils.SecureLogger
import com.diploma.work.utils.ValidationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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
) : BaseViewModel() {

    companion object {
        private const val TAG = "LoginViewModel"
    }

    private val _username = MutableStateFlow("")
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
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun onUsernameChanged(newValue: String) {
        _username.value = newValue
        
        val emailValidation = ValidationUtils.validateEmail(newValue)
        _emailError.value = if (!emailValidation.isValid && newValue.isNotBlank()) 
            emailValidation.errorMessage else null
        clearError()
        SecureLogger.d(TAG, "Email field changed")
    }

    fun onPasswordChanged(newValue: String) {
        _password.value = newValue
        
        val passwordValidation = ValidationUtils.validateStrongPassword(newValue)
        _passwordError.value = if (!passwordValidation.isValid && newValue.isNotBlank()) 
            passwordValidation.errorMessage else null
        clearError()
        SecureLogger.d(TAG, "Password field changed")
    }

    fun onLoginClicked(session: AppSession) {
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
            
            SecureLogger.d(TAG, "Login attempt for: ${SecureLogger.maskEmail(email)}")

            val request = LoginRequest(
                email = email,
                password = password
            )

            val result = authRepository.login(request)

            result.onSuccess { response ->
                session.storeToken(response.accessToken)
                SecureLogger.sensitive(TAG, "Token received: ${SecureLogger.maskToken(response.accessToken)}")

                val userId = extractUserIdFromToken(response.accessToken)
                if (userId != null) {
                    session.storeUserId(userId)
                    SecureLogger.sensitive(TAG, "User ID extracted: ${SecureLogger.maskUserId(userId)}")
                } else {
                    val tempUserId = email.hashCode().toLong().absoluteValue
                    session.storeUserId(tempUserId)
                    SecureLogger.d(TAG, "Using fallback user ID")
                }
                
                session.refreshUsername()
                session.refreshAvatarUrl()
                
                _loginSuccess.value = true
                SecureLogger.d(TAG, "Login successful")
            }.onFailure { error ->
                SecureLogger.e(TAG, "Login failed", error)
                showError(errorHandler.handleAuthError(error))
                _loginSuccess.value = false
            }
        }
    }
    
    private fun extractUserIdFromToken(token: String): Long? {
        try {
            val parts = token.split(".")
            if (parts.size < 2) {
                SecureLogger.e(TAG, "Invalid token format")
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
                    SecureLogger.e(TAG, "Base64 decoding failed", e2)
                    return null
                }
            }
            
            try {
                val jsonObject = JSONObject(decodedString)

                return when {
                    jsonObject.has("sub") -> jsonObject.getString("sub").toLongOrNull()
                    jsonObject.has("user_id") -> jsonObject.optLong("user_id", -1).takeIf { it != -1L }
                    jsonObject.has("id") -> jsonObject.optLong("id", -1).takeIf { it != -1L }
                    jsonObject.has("userId") -> jsonObject.optLong("userId", -1).takeIf { it != -1L }
                    jsonObject.has("uid") -> jsonObject.optLong("uid", -1).takeIf { it != -1L }
                    else -> {
                        SecureLogger.e(TAG, "Token payload doesn't contain recognized user ID field")
                        null
                    }
                }
            } catch (e: JSONException) {
                SecureLogger.e(TAG, "JSON parsing error", e)
                return null
            }
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Failed to decode token", e)
            return null
        }
    }
}