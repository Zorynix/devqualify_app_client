package com.diploma.work.ui.feature.auth.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.diploma.work.AppSession
import com.diploma.work.data.models.LoginRequest
import com.diploma.work.data.models.LoginResponse
import com.diploma.work.data.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var authRepository: AuthRepository
    private lateinit var session: AppSession
    private lateinit var viewModel: LoginViewModel
    
    @Before
    fun setup() {
        authRepository = mockk()
        session = mockk(relaxed = true)
        viewModel = LoginViewModel(authRepository, session)
    }
    
    @Test
    fun `initial state is correct`() {
        assertTrue(viewModel.username.value.isEmpty())
        assertTrue(viewModel.password.value.isEmpty())
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.errorMessage.value)
    }
    
    @Test
    fun `onUsernameChanged updates username state`() = runTest {
        val username = "testuser"
        
        viewModel.onUsernameChanged(username)
        
        assertEquals(username, viewModel.username.value)
    }
    
    @Test
    fun `onPasswordChanged updates password state`() = runTest {
        val password = "testpass"
        
        viewModel.onPasswordChanged(password)
        
        assertEquals(password, viewModel.password.value)
    }
    
    @Test
    fun `clearError clears error message`() = runTest {
        viewModel.clearError()
        
        assertNull(viewModel.errorMessage.value)
    }
    
    @Test
    fun `login with valid credentials succeeds`() = runTest {
        val email = "test@example.com"
        val password = "password123"
        val loginResponse = LoginResponse(
            accessToken = "access_token",
            refreshToken = "refresh_token"
        )
        
        coEvery { authRepository.login(any()) } returns Result.success(loginResponse)
        
        viewModel.onUsernameChanged(email)
        viewModel.onPasswordChanged(password)
        viewModel.onLoginClicked(session)
        
        val expectedRequest = LoginRequest(
            email = email,
            password = password,
            appId = 1
        )
        
        coVerify { authRepository.login(expectedRequest) }
        coVerify { session.storeToken(loginResponse.accessToken) }
    }
    
    @Test
    fun `login with invalid credentials fails`() = runTest {
        val email = "test@example.com"
        val password = "wrongpassword"
        val errorMessage = "Invalid credentials"
        
        coEvery { authRepository.login(any()) } returns Result.failure(Exception(errorMessage))
        
        viewModel.onUsernameChanged(email)
        viewModel.onPasswordChanged(password)
        viewModel.onLoginClicked(session)
        
        assertEquals(errorMessage, viewModel.errorMessage.value)
    }
    
    @Test
    fun `login with empty fields shows validation error`() = runTest {
        viewModel.onUsernameChanged("")
        viewModel.onPasswordChanged("")
        viewModel.onLoginClicked(session)
        
        assertNotNull(viewModel.errorMessage.value)
        assertTrue(viewModel.errorMessage.value?.contains("empty") == true)
    }
    
    @Test
    fun `login with invalid email format shows validation error`() = runTest {
        viewModel.setUsername("invalid-email")
        viewModel.setPassword("password123")
        viewModel.login()
        
        assertNotNull(viewModel.errorMessage.value)
        assertTrue(viewModel.errorMessage.value?.contains("valid email") == true)
    }
    
    @Test
    fun `loginEnabled returns true for valid form`() = runTest {
        viewModel.onUsernameChanged("test@example.com")
        viewModel.onPasswordChanged("password123")
        
        assertTrue(viewModel.loginEnabled.value)
    }
    
    @Test
    fun `isFormValid returns false for invalid form`() = runTest {
        viewModel.setUsername("")
        viewModel.setPassword("")
        
        assertFalse(viewModel.isFormValid.value)
    }
    
    @Test
    fun `isFormValid returns false for invalid email`() = runTest {
        viewModel.setUsername("invalid-email")
        viewModel.setPassword("password123")
        
        assertFalse(viewModel.isFormValid.value)
    }
    
    @Test
    fun `isFormValid returns false for short password`() = runTest {
        viewModel.setUsername("test@example.com")
        viewModel.setPassword("123")
        
        assertFalse(viewModel.isFormValid.value)
    }
}
