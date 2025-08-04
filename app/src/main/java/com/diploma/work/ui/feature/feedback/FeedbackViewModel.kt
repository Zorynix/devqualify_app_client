package com.diploma.work.ui.feature.feedback

import androidx.lifecycle.viewModelScope
import com.diploma.work.data.AppSession
import com.diploma.work.data.models.SendFeedbackRequest
import com.diploma.work.data.repository.UserInfoRepository
import com.diploma.work.ui.base.BaseViewModel
import com.diploma.work.utils.ErrorHandler
import com.orhanobut.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeedbackUiState(
    val subject: String = "",
    val body: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val successMessage: String = ""
)

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val userInfoRepository: UserInfoRepository,
    private val session: AppSession,
    override val errorHandler: ErrorHandler
) : BaseViewModel() {
    
    private val tag = "FeedbackViewModel"
    
    private val _uiState = MutableStateFlow(FeedbackUiState())
    val uiState: StateFlow<FeedbackUiState> = _uiState.asStateFlow()
    
    fun onSubjectChanged(subject: String) {
        _uiState.value = _uiState.value.copy(
            subject = subject,
            error = null,
            isSuccess = false
        )
    }
    
    fun onBodyChanged(body: String) {
        _uiState.value = _uiState.value.copy(
            body = body,
            error = null,
            isSuccess = false
        )
    }
    
    fun sendFeedback() {
        val currentState = _uiState.value
        
        if (currentState.subject.isBlank()) {
            _uiState.value = currentState.copy(error = "Укажите тему сообщения")
            return
        }
        
        if (currentState.body.isBlank()) {
            _uiState.value = currentState.copy(error = "Опишите проблему")
            return
        }
        
        val userId = session.getUserId()
        if (userId == null) {
            _uiState.value = currentState.copy(error = "Необходимо войти в систему")
            return
        }
        
        viewModelScope.launch {
            try {
                _uiState.value = currentState.copy(isLoading = true, error = null)
                
                val request = SendFeedbackRequest(
                    userId = userId,
                    subject = currentState.subject.trim(),
                    body = currentState.body.trim()
                )
                
                val result = userInfoRepository.sendFeedback(request)
                
                if (result.isSuccess) {
                    val response = result.getOrNull()
                    if (response != null && response.success) {
//                        Logger.d("$tag: Feedback sent successfully: ${response.success}")
                        _uiState.value = FeedbackUiState(
                            isSuccess = true,
                            successMessage = response.message.ifEmpty { "Сообщение успешно отправлено!" }
                        )
                    } else {
                        _uiState.value = currentState.copy(
                            isLoading = false,
                            error = response?.message?.ifEmpty { "Не удалось отправить сообщение" } ?: "Не удалось отправить сообщение"
                        )
                    }
                } else {
                    val error = result.exceptionOrNull()
                    Logger.e("$tag: Failed to send feedback: ${error?.message}")
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        error = errorHandler.getContextualErrorMessage(error ?: Exception("Unknown error"), ErrorHandler.ErrorContext.FEEDBACK)
                    )
                }
            } catch (e: Exception) {
                Logger.e("$tag: Exception sending feedback: ${e.message}")
                _uiState.value = currentState.copy(
                    isLoading = false,
                    error = errorHandler.getErrorMessage(e)
                )
            }
        }
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            isSuccess = false,
            successMessage = ""
        )
    }
}
