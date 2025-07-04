package com.diploma.work.ui.feature.auth.register

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.diploma.work.data.AppSession
import com.diploma.work.data.models.RegisterRequest
import com.diploma.work.data.models.RegisterResponse
import com.diploma.work.data.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class RegistrationViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = TestCoroutineDispatcher()
    
    private lateinit var authRepository: AuthRepository
    private lateinit var session: AppSession
    private lateinit var viewModel: RegistrationViewModel
    
    @Before
    fun setup() {
        authRepository = mockk()
        session = mockk(relaxed = true)
        viewModel = RegistrationViewModel(authRepository, session)
    }
    
    @Test
    fun `initial state is correct`() {
        assertTrue(viewModel.email.value.isEmpty())
        assertTrue(viewModel.password.value.isEmpty())
        assertTrue(viewModel.confirmPassword.value.isEmpty())
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.errorMessage.value)
    }
    
    @Test
    fun `setEmail updates email state`() = runTest {
        val email = "test@example.com"
        
        viewModel.setEmail(email)
        
        assertEquals(email, viewModel.email.value)
    }
    
    @Test
    fun `setPassword updates password state`() = runTest {
        val password = "password123"
        
        viewModel.setPassword(password)
        
        assertEquals(password, viewModel.password.value)
    }
    
    @Test
    fun `setConfirmPassword updates confirm password state`() = runTest {
        val confirmPassword = "password123"
        
        viewModel.setConfirmPassword(confirmPassword)
        
        assertEquals(confirmPassword, viewModel.confirmPassword.value)
    }
    
    @Test
    fun `clearError clears error message`() = runTest {
        viewModel.clearError()
        
        assertNull(viewModel.errorMessage.value)
    }
    
    @Test
    fun `register with valid data succeeds`() = runTest {
        val email = "test@example.com"
        val password = "password123"
        val registerResponse = RegisterResponse(userId = 1L)
        
        coEvery { authRepository.register(any()) } returns Result.success(registerResponse)
        
        viewModel.setEmail(email)
        viewModel.setPassword(password)
        viewModel.setConfirmPassword(password)
        viewModel.register()
        
        val expectedRequest = RegisterRequest(
            email = email,
            password = password
        )
        
        coVerify { authRepository.register(expectedRequest) }
    }
    
    @Test
    fun `register with invalid email shows validation error`() = runTest {
        viewModel.setEmail("invalid-email")
        viewModel.setPassword("password123")
        viewModel.setConfirmPassword("password123")
        viewModel.register()
        
        assertNotNull(viewModel.errorMessage.value)
        assertTrue(viewModel.errorMessage.value?.contains("valid email") == true)
    }
    
    @Test
    fun `register with mismatched passwords shows validation error`() = runTest {
        viewModel.setEmail("test@example.com")
        viewModel.setPassword("password123")
        viewModel.setConfirmPassword("differentpassword")
        viewModel.register()
        
        assertNotNull(viewModel.errorMessage.value)
        assertTrue(viewModel.errorMessage.value?.contains("match") == true)
    }
    
    @Test
    fun `register with short password shows validation error`() = runTest {
        viewModel.setEmail("test@example.com")
        viewModel.setPassword("123")
        viewModel.setConfirmPassword("123")
        viewModel.register()
        
        assertNotNull(viewModel.errorMessage.value)
        assertTrue(viewModel.errorMessage.value?.contains("6 characters") == true)
    }
    
    @Test
    fun `register with empty fields shows validation error`() = runTest {
        viewModel.setEmail("")
        viewModel.setPassword("")
        viewModel.setConfirmPassword("")
        viewModel.register()
        
        assertNotNull(viewModel.errorMessage.value)
        assertTrue(viewModel.errorMessage.value?.contains("empty") == true)
    }
    
    @Test
    fun `register with server error shows error message`() = runTest {
        val email = "test@example.com"
        val password = "password123"
        val errorMessage = "Email already exists"
        
        coEvery { authRepository.register(any()) } returns Result.failure(Exception(errorMessage))
        
        viewModel.setEmail(email)
        viewModel.setPassword(password)
        viewModel.setConfirmPassword(password)
        viewModel.register()
        
        assertEquals(errorMessage, viewModel.errorMessage.value)
    }
    
    @Test
    fun `isFormValid returns true for valid form`() = runTest {
        viewModel.setEmail("test@example.com")
        viewModel.setPassword("password123")
        viewModel.setConfirmPassword("password123")
        
        assertTrue(viewModel.isFormValid.value)
    }
    
    @Test
    fun `isFormValid returns false for invalid email`() = runTest {
        viewModel.setEmail("invalid-email")
        viewModel.setPassword("password123")
        viewModel.setConfirmPassword("password123")
        
        assertFalse(viewModel.isFormValid.value)
    }
    
    @Test
    fun `isFormValid returns false for mismatched passwords`() = runTest {
        viewModel.setEmail("test@example.com")
        viewModel.setPassword("password123")
        viewModel.setConfirmPassword("differentpassword")
        
        assertFalse(viewModel.isFormValid.value)
    }
    
    @Test
    fun `isFormValid returns false for short password`() = runTest {
        viewModel.setEmail("test@example.com")
        viewModel.setPassword("123")
        viewModel.setConfirmPassword("123")
        
        assertFalse(viewModel.isFormValid.value)
    }
    
    @Test
    fun `isFormValid returns false for empty fields`() = runTest {
        viewModel.setEmail("")
        viewModel.setPassword("")
        viewModel.setConfirmPassword("")
        
        assertFalse(viewModel.isFormValid.value)
    }
}
