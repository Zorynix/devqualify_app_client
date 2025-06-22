package com.diploma.work.utils

import android.content.Context
import com.diploma.work.R
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


@RunWith(MockitoJUnitRunner::class)
class ErrorHandlerTest {
    
    @Mock
    private lateinit var mockContext: Context
    
    private lateinit var errorHandler: ErrorHandler
    
    @Before
    fun setup() {
        whenever(mockContext.getString(R.string.error_network_connection))
            .thenReturn("Ошибка подключения к сети")
        whenever(mockContext.getString(R.string.error_network_timeout))
            .thenReturn("Время ожидания истекло")
        whenever(mockContext.getString(R.string.error_security))
            .thenReturn("Ошибка безопасности")
        whenever(mockContext.getString(R.string.error_invalid_input))
            .thenReturn("Неверные данные")
        whenever(mockContext.getString(R.string.error_generic))
            .thenReturn("Произошла ошибка")
        whenever(mockContext.getString(R.string.error_auth_invalid_credentials))
            .thenReturn("Неверные учетные данные")
        
        errorHandler = ErrorHandler(mockContext)
    }
    
    @Test
    fun getErrorMessage_connectException_returnsNetworkError() {
        val exception = ConnectException("Connection failed")
        val result = errorHandler.getErrorMessage(exception)
        assertEquals("Ошибка подключения к сети", result)
    }
    
    @Test
    fun getErrorMessage_unknownHostException_returnsNetworkError() {
        val exception = UnknownHostException("Host not found")
        val result = errorHandler.getErrorMessage(exception)
        assertEquals("Ошибка подключения к сети", result)
    }
    
    @Test
    fun getErrorMessage_socketTimeoutException_returnsTimeoutError() {
        val exception = SocketTimeoutException("Timeout")
        val result = errorHandler.getErrorMessage(exception)
        assertEquals("Время ожидания истекло", result)
    }
    
    @Test
    fun getErrorMessage_securityException_returnsSecurityError() {
        val exception = SecurityException("Security violation")
        val result = errorHandler.getErrorMessage(exception)
        assertEquals("Ошибка безопасности", result)
    }
    
    @Test
    fun getErrorMessage_illegalArgumentException_returnsInvalidInputError() {
        val exception = IllegalArgumentException("Invalid argument")
        val result = errorHandler.getErrorMessage(exception)
        assertEquals("Неверные данные", result)
    }
    
    @Test
    fun getErrorMessage_genericException_returnsExceptionMessage() {
        val exception = RuntimeException("Custom error message")
        val result = errorHandler.getErrorMessage(exception)
        assertEquals("Custom error message", result)
    }
    
    @Test
    fun getErrorMessage_exceptionWithNullMessage_returnsGenericError() {
        val exception = RuntimeException(null as String?)
        val result = errorHandler.getErrorMessage(exception)
        assertEquals("Произошла ошибка", result)
    }
    
    @Test
    fun handleAuthError_unauthorizedException_returnsInvalidCredentials() {
        val exception = RuntimeException("401 Unauthorized")
        val result = errorHandler.handleAuthError(exception)
        assertEquals("Неверные учетные данные", result)
    }
    
    @Test
    fun handleAuthError_emailError_returnsEmailError() {
        whenever(mockContext.getString(R.string.error_auth_invalid_email))
            .thenReturn("Неверный формат email")
        
        val exception = RuntimeException("Invalid email format")
        val result = errorHandler.handleAuthError(exception)
        assertEquals("Неверный формат email", result)
    }
    
    @Test
    fun isCriticalError_outOfMemoryError_returnsTrue() {
        val error = OutOfMemoryError("Out of memory")
        assertTrue(errorHandler.isCriticalError(error))
    }
    
    @Test
    fun isCriticalError_stackOverflowError_returnsTrue() {
        val error = StackOverflowError("Stack overflow")
        assertTrue(errorHandler.isCriticalError(error))
    }
    
    @Test
    fun isCriticalError_securityException_returnsTrue() {
        val exception = SecurityException("Security violation")
        assertTrue(errorHandler.isCriticalError(exception))
    }
    
    @Test
    fun isCriticalError_regularException_returnsFalse() {
        val exception = RuntimeException("Regular exception")
        assertFalse(errorHandler.isCriticalError(exception))
    }
}
