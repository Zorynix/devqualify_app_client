package com.diploma.work.utils

import android.content.Context
import com.diploma.work.R
import com.orhanobut.logger.Logger
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ErrorHandler @Inject constructor(
    private val context: Context
) {
    

    fun getErrorMessage(throwable: Throwable): String {
        Logger.e("Error occurred: ${throwable.message}", throwable)
        
        return when (throwable) {
            is ConnectException, is UnknownHostException -> 
                context.getString(R.string.error_network_connection)
            
            is SocketTimeoutException -> 
                context.getString(R.string.error_network_timeout)
            
            is SecurityException -> 
                context.getString(R.string.error_security)
            
            is IllegalArgumentException -> 
                context.getString(R.string.error_invalid_input)
            
            is IllegalStateException -> 
                context.getString(R.string.error_invalid_state)
            
            else -> {
                Logger.e("Unhandled error: ${throwable.javaClass.simpleName} - ${throwable.message}")
                throwable.message ?: context.getString(R.string.error_generic)
            }
        }
    }
    
    fun handleAuthError(throwable: Throwable): String {
        return when {
            throwable.message?.contains("401") == true || 
            throwable.message?.contains("Unauthorized") == true -> 
                context.getString(R.string.error_auth_invalid_credentials)
            
            throwable.message?.contains("403") == true ||
            throwable.message?.contains("Forbidden") == true -> 
                context.getString(R.string.error_auth_forbidden)
            
            throwable.message?.contains("email") == true -> 
                context.getString(R.string.error_auth_invalid_email)
            
            else -> getErrorMessage(throwable)
        }
    }
    

    fun handleValidationError(field: String, error: String): String {
        return when {
            error.contains("required") -> 
                context.getString(R.string.error_field_required, field)
            
            error.contains("invalid") -> 
                context.getString(R.string.error_field_invalid, field)
            
            error.contains("too_short") -> 
                context.getString(R.string.error_field_too_short, field)
            
            error.contains("too_long") -> 
                context.getString(R.string.error_field_too_long, field)
            
            else -> error
        }
    }
    
    fun isCriticalError(throwable: Throwable): Boolean {
        return when (throwable) {
            is OutOfMemoryError,
            is StackOverflowError,
            is SecurityException -> true
            else -> false
        }
    }
    
    enum class ErrorContext {
        LOGIN,
        REGISTRATION,
        EMAIL_CONFIRMATION,
        TEST_SESSION,
        PROFILE_UPDATE,
        DATA_LOADING,
        NETWORK,
        GENERIC
    }
    
    fun handleError(throwable: Throwable, defaultMessage: String = ""): String {
        val errorMessage = getErrorMessage(throwable)
        return if (errorMessage.isEmpty() && defaultMessage.isNotEmpty()) {
            defaultMessage
        } else {
            errorMessage
        }
    }
      fun getContextualErrorMessage(error: Throwable, context: ErrorContext): String {
        return when (context) {
            ErrorContext.LOGIN -> when {
                error.message?.contains("401") == true -> this.context.getString(R.string.error_auth_invalid_credentials)
                error.message?.contains("403") == true -> this.context.getString(R.string.error_auth_forbidden)
                error.message?.contains("email") == true -> this.context.getString(R.string.error_auth_invalid_email)
                else -> handleAuthError(error)
            }
            ErrorContext.REGISTRATION -> when {
                error.message?.contains("email") == true -> this.context.getString(R.string.error_auth_email_exists)
                error.message?.contains("username") == true -> this.context.getString(R.string.error_auth_username_exists)
                else -> handleAuthError(error)
            }
            ErrorContext.EMAIL_CONFIRMATION -> when {
                error.message?.contains("invalid") == true -> this.context.getString(R.string.error_auth_invalid_code)
                error.message?.contains("expired") == true -> this.context.getString(R.string.error_auth_code_expired)
                else -> getErrorMessage(error)
            }
            ErrorContext.TEST_SESSION -> when {
                error.message?.contains("not found") == true -> this.context.getString(R.string.error_test_not_found)
                error.message?.contains("completed") == true -> this.context.getString(R.string.error_test_already_completed)
                error.message?.contains("expired") == true -> this.context.getString(R.string.error_test_expired)
                else -> getErrorMessage(error)
            }
            ErrorContext.PROFILE_UPDATE -> when {
                error.message?.contains("unauthorized") == true -> this.context.getString(R.string.error_auth_unauthorized)
                error.message?.contains("validation") == true -> this.context.getString(R.string.error_validation_failed)
                else -> getErrorMessage(error)
            }
            ErrorContext.DATA_LOADING -> when {
                error is ConnectException || error is UnknownHostException -> this.context.getString(R.string.error_network_connection)
                error is SocketTimeoutException -> this.context.getString(R.string.error_network_timeout)
                else -> getErrorMessage(error)
            }
            ErrorContext.NETWORK -> when {
                error is ConnectException || error is UnknownHostException -> this.context.getString(R.string.error_network_connection)
                error is SocketTimeoutException -> this.context.getString(R.string.error_network_timeout)
                else -> this.context.getString(R.string.error_network_generic)
            }
            ErrorContext.GENERIC -> when {
                error.message?.isNotEmpty() == true -> error.message!!
                else -> this.context.getString(R.string.error_generic)
            }
        }
    }
}
