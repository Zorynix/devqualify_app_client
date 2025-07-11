package com.diploma.work.ui.feature.auth.register

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.diploma.work.data.AppSession
import com.diploma.work.data.models.RegisterRequest
import com.diploma.work.data.models.RegisterResponse
import com.diploma.work.data.repository.AuthRepository
import com.diploma.work.utils.ErrorHandler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.every
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
@OptIn(ExperimentalCoroutinesApi::class)
class RegistrationViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
    
    private lateinit var authRepository: AuthRepository
    private lateinit var session: AppSession
    private lateinit var errorHandler: ErrorHandler
    private lateinit var viewModel: RegistrationViewModel
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        authRepository = mockk()
        session = mockk(relaxed = true)
        errorHandler = mockk(relaxed = true)

        every { errorHandler.getErrorMessage(any()) } returns "Test error message"
        
        viewModel = RegistrationViewModel(authRepository, session, errorHandler)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state is correct`() = runTest {
        assertTrue(viewModel.email.value.isEmpty())
        assertTrue(viewModel.password.value.isEmpty())
        assertTrue(viewModel.confirmPassword.value.isEmpty())
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.errorMessage.value)
    }
    
    @Test
    fun `onEmailChanged updates email state`() = runTest {
        val email = "test@example.com"
        
        viewModel.onEmailChanged(email)
        
        assertEquals(email, viewModel.email.value)
    }
    
    @Test
    fun `onPasswordChanged updates password state`() = runTest {
        val password = "password123"
        
        viewModel.onPasswordChanged(password)
        
        assertEquals(password, viewModel.password.value)
    }
    
    @Test
    fun `onConfirmPasswordChanged updates confirm password state`() = runTest {
        val confirmPassword = "password123"
        
        viewModel.onConfirmPasswordChanged(confirmPassword)
        
        assertEquals(confirmPassword, viewModel.confirmPassword.value)
    }
    
    @Test
    fun `clearError clears error message`() = runTest {
        viewModel.clearError()
        
        assertNull(viewModel.errorMessage.value)
    }
    
    @Test
    fun `register with invalid email shows validation error`() = runTest {
        viewModel.onEmailChanged("invalid-email")
        viewModel.onPasswordChanged("password123")
        viewModel.onConfirmPasswordChanged("password123")
        viewModel.onRegisterClicked(session)
        
        assertNotNull(viewModel.emailError.value)
    }
    
    @Test
    fun `register with mismatched passwords shows validation error`() = runTest {
        viewModel.onEmailChanged("test@example.com")
        viewModel.onPasswordChanged("password123")
        viewModel.onConfirmPasswordChanged("differentpassword")
        viewModel.onRegisterClicked(session)
        
        assertNotNull(viewModel.confirmPasswordError.value)
    }
    
    @Test
    fun `register with short password shows validation error`() = runTest {
        viewModel.onEmailChanged("test@example.com")
        viewModel.onPasswordChanged("123")
        viewModel.onConfirmPasswordChanged("123")
        viewModel.onRegisterClicked(session)
        
        assertNotNull(viewModel.passwordError.value)
    }
    
    @Test
    fun `register with empty fields shows validation error`() = runTest {
        viewModel.onEmailChanged("")
        viewModel.onPasswordChanged("")
        viewModel.onConfirmPasswordChanged("")
        viewModel.onRegisterClicked(session)
        
        assertNotNull(viewModel.emailError.value)
    }
    
    @Test
    fun `register handles server errors gracefully`() = runTest {
        val email = "test@example.com"
        val password = "password123"
        
        coEvery { authRepository.register(any()) } returns Result.failure(Exception("Server error"))
        
        viewModel.onEmailChanged(email)
        viewModel.onPasswordChanged(password)
        viewModel.onConfirmPasswordChanged(password)
        viewModel.onRegisterClicked(session)
        advanceUntilIdle()
        
        assertFalse(viewModel.isLoading.value)
    }
    
    @Test
    fun `form fields update correctly`() = runTest {
        viewModel.onEmailChanged("test@example.com")
        viewModel.onPasswordChanged("password123")
        viewModel.onConfirmPasswordChanged("password123")
        advanceUntilIdle()
        
        assertEquals("test@example.com", viewModel.email.value)
        assertEquals("password123", viewModel.password.value)
        assertEquals("password123", viewModel.confirmPassword.value)
    }
    
    @Test
    fun `registerEnabled returns false for invalid email`() = runTest {
        viewModel.onEmailChanged("invalid-email")
        viewModel.onPasswordChanged("password123")
        viewModel.onConfirmPasswordChanged("password123")
        
        assertFalse(viewModel.registerEnabled.value)
    }
    
    @Test
    fun `registerEnabled returns false for mismatched passwords`() = runTest {
        viewModel.onEmailChanged("test@example.com")
        viewModel.onPasswordChanged("password123")
        viewModel.onConfirmPasswordChanged("differentpassword")
        
        assertFalse(viewModel.registerEnabled.value)
    }
    
    @Test
    fun `registerEnabled returns false for short password`() = runTest {
        viewModel.onEmailChanged("test@example.com")
        viewModel.onPasswordChanged("123")
        viewModel.onConfirmPasswordChanged("123")
        
        assertFalse(viewModel.registerEnabled.value)
    }
    
    @Test
    fun `registerEnabled returns false for empty fields`() = runTest {
        viewModel.onEmailChanged("")
        viewModel.onPasswordChanged("")
        viewModel.onConfirmPasswordChanged("")
        
        assertFalse(viewModel.registerEnabled.value)
    }
}






