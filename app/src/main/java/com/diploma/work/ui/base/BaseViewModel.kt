package com.diploma.work.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diploma.work.utils.ErrorHandler
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

abstract class BaseViewModel : ViewModel() {
    
    protected abstract val errorHandler: ErrorHandler
    
    protected val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    protected val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Logger.e("Unhandled exception in ${this::class.simpleName}", throwable)
        _isLoading.value = false
        _errorMessage.value = errorHandler.getErrorMessage(throwable)
        onError(throwable)
    }
    

    protected fun safeLaunch(
        showLoading: Boolean = true,
        onError: ((Throwable) -> Unit)? = null,
        block: suspend CoroutineScope.() -> Unit
    ) {
        viewModelScope.launch(exceptionHandler) {
            try {
                if (showLoading) _isLoading.value = true
                clearError()
                block()
            } catch (e: Exception) {
                Logger.e("Error in ${this@BaseViewModel::class.simpleName}", e)
                _errorMessage.value = errorHandler.getErrorMessage(e)
                onError?.invoke(e)
            } finally {
                if (showLoading) _isLoading.value = false
            }
        }
    }
    open fun clearError() {
        _errorMessage.value = null
    }
    
    protected fun setLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }
    
    protected fun setError(message: String) {
        _errorMessage.value = message
    }
    
    protected fun clearGlobalError() {
        _errorMessage.value = null
    }
    
    protected open fun onError(throwable: Throwable) {
        // Базовая обработка - можно переопределить в наследниках
    }
    
    protected fun showError(message: String) {
        _errorMessage.value = message
    }
    

    protected fun showError(throwable: Throwable) {
        _errorMessage.value = errorHandler.getErrorMessage(throwable)
    }
}


data class BaseUiState(
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val hasError: Boolean get() = error != null
    val isIdle: Boolean get() = !isLoading && error == null
}
