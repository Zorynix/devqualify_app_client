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
    enum class ErrorContext {
        LOGIN,
        REGISTRATION,
        EMAIL_CONFIRMATION,
        FEEDBACK,
        TEST_SESSION,
        PROFILE_UPDATE,
        DATA_LOADING,
        NETWORK,
        GENERIC
    }

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
            else -> throwable.message ?: context.getString(R.string.error_generic)
        }
    }

    fun handleAuthError(throwable: Throwable): String {
        return when {
            throwable.message?.contains("401") == true ||
                throwable.message?.contains("Unauthorized", ignoreCase = true) == true ->
                context.getString(R.string.error_auth_invalid_credentials)
            throwable.message?.contains("403") == true ||
                throwable.message?.contains("Forbidden", ignoreCase = true) == true ->
                context.getString(R.string.error_auth_forbidden)
            throwable.message?.contains("email", ignoreCase = true) == true ->
                context.getString(R.string.error_auth_invalid_email)
            else -> getErrorMessage(throwable)
        }
    }

    fun handleValidationError(field: String, error: String): String {
        return when {
            error.contains("required", ignoreCase = true) ->
                context.getString(R.string.error_field_required, field)
            error.contains("invalid", ignoreCase = true) ->
                context.getString(R.string.error_field_invalid, field)
            error.contains("too_short", ignoreCase = true) ->
                context.getString(R.string.error_field_too_short, field)
            error.contains("too_long", ignoreCase = true) ->
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

    fun handleError(throwable: Throwable, defaultMessage: String = ""): String {
        val errorMessage = getErrorMessage(throwable)
        return if (errorMessage.isEmpty() && defaultMessage.isNotEmpty()) defaultMessage else errorMessage
    }

    fun getContextualErrorMessage(error: Throwable, context: ErrorContext): String {
        return when (context) {
            ErrorContext.LOGIN -> when {
                error.message?.contains("401") == true -> this.context.getString(R.string.error_auth_invalid_credentials)
                error.message?.contains("403") == true -> this.context.getString(R.string.error_auth_forbidden)
                error.message?.contains("email", ignoreCase = true) == true -> this.context.getString(R.string.error_auth_invalid_email)
                else -> handleAuthError(error)
            }
            ErrorContext.REGISTRATION -> when {
                error.message?.contains("email", ignoreCase = true) == true -> this.context.getString(R.string.error_auth_email_exists)
                error.message?.contains("username", ignoreCase = true) == true -> this.context.getString(R.string.error_auth_username_exists)
                else -> handleAuthError(error)
            }
            ErrorContext.EMAIL_CONFIRMATION -> when {
                error.message?.contains("invalid", ignoreCase = true) == true -> this.context.getString(R.string.error_auth_invalid_code)
                error.message?.contains("expired", ignoreCase = true) == true -> this.context.getString(R.string.error_auth_code_expired)
                else -> getErrorMessage(error)
            }
            ErrorContext.FEEDBACK -> when {
                error.message?.contains("401") == true || error.message?.contains("unauthorized", ignoreCase = true) == true ->
                    context.getString(R.string.error_feedback_auth_required)
                error.message?.contains("403") == true || error.message?.contains("forbidden", ignoreCase = true) == true ->
                    context.getString(R.string.error_feedback_forbidden)
                error.message?.contains("validation", ignoreCase = true) == true || error.message?.contains("length", ignoreCase = true) == true ->
                    context.getString(R.string.error_feedback_validation)
                error.message?.contains("too long", ignoreCase = true) == true ->
                    context.getString(R.string.error_feedback_too_long)
                error is ConnectException || error is UnknownHostException ->
                    context.getString(R.string.error_network_connection)
                error is SocketTimeoutException ->
                    context.getString(R.string.error_network_timeout)
                else -> context.getString(R.string.error_feedback_generic)
            }
            ErrorContext.TEST_SESSION -> when {
                error.message?.contains("not found", ignoreCase = true) == true -> this.context.getString(R.string.error_test_not_found)
                error.message?.contains("completed", ignoreCase = true) == true -> this.context.getString(R.string.error_test_already_completed)
                error.message?.contains("expired", ignoreCase = true) == true -> this.context.getString(R.string.error_test_expired)
                else -> getErrorMessage(error)
            }
            ErrorContext.PROFILE_UPDATE -> when {
                error.message?.contains("unauthorized", ignoreCase = true) == true -> this.context.getString(R.string.error_auth_unauthorized)
                error.message?.contains("validation", ignoreCase = true) == true -> this.context.getString(R.string.error_validation_failed)
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
