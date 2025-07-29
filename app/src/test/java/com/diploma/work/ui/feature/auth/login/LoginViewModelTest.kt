package com.diploma.work.ui.feature.auth.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.diploma.work.data.AppSession
import com.diploma.work.data.models.LoginRequest
import com.diploma.work.data.models.LoginResponse
import com.diploma.work.data.repository.AuthRepository
import com.diploma.work.utils.ErrorHandler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.every
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.Dispatchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlinx.coroutines.test.resetMain
import org.junit.After

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
    
    private lateinit var authRepository: AuthRepository
    private lateinit var session: AppSession
    private lateinit var errorHandler: ErrorHandler
    private lateinit var viewModel: LoginViewModel
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        authRepository = mockk()
        session = mockk(relaxed = true)
        errorHandler = mockk(relaxed = true)
        
        viewModel = LoginViewModel(authRepository, session, errorHandler)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state is correct`() = runTest {
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
        val password = "Password123@"
        val loginResponse = LoginResponse(
            accessToken = "access_token",
            refreshToken = "refresh_token"
        )
        
        coEvery { authRepository.login(any()) } returns Result.success(loginResponse)
        
        viewModel.onUsernameChanged(email)
        viewModel.onPasswordChanged(password)
        viewModel.onLoginClicked(session)
        
        advanceUntilIdle()
        
        val expectedRequest = LoginRequest(
            email = email,
            password = password
        )
        
        coVerify { authRepository.login(expectedRequest) }
        coVerify { session.storeToken(loginResponse.accessToken) }
    }
    
    @Test
    fun `login with invalid credentials fails`() = runTest {
        val email = "test@example.com"
        val password = "WrongPassword123@"
        val errorMessage = "Invalid credentials"
        
        coEvery { authRepository.login(any()) } returns Result.failure(Exception(errorMessage))
        every { errorHandler.handleAuthError(any()) } returns errorMessage
        
        viewModel.onUsernameChanged(email)
        viewModel.onPasswordChanged(password)
        viewModel.onLoginClicked(session)
        
        advanceUntilIdle()
        assertEquals(errorMessage, viewModel.errorMessage.value)
    }
    
    @Test
    fun `login with empty fields shows validation error`() = runTest {
        viewModel.onUsernameChanged("")
        viewModel.onPasswordChanged("")
        viewModel.onLoginClicked(session)
        
        advanceUntilIdle()
        assertNotNull(viewModel.emailError.value)
    }
    
    @Test
    fun `login with invalid email format shows validation error`() = runTest {
        viewModel.onUsernameChanged("invalid-email")
        viewModel.onPasswordChanged("Password123@")
        viewModel.onLoginClicked(session)
        
        assertNotNull(viewModel.emailError.value)
    }
    
    @Test
    fun `loginEnabled returns true for valid form`() = runTest {
        val initialValue = viewModel.loginEnabled.value
        println("Initial loginEnabled value: $initialValue")
        
        viewModel.onUsernameChanged("test@example.com")
        viewModel.onPasswordChanged("StrongPassword123@")
        
        advanceUntilIdle()
        
        val finalValue = viewModel.loginEnabled.value
        println("Final loginEnabled value: $finalValue")
        
        assertTrue("loginEnabled should be true for valid form", finalValue)
    }
    
    @Test
    fun `loginEnabled returns false for invalid form`() = runTest {
        viewModel.onUsernameChanged("")
        viewModel.onPasswordChanged("")
        
        assertFalse(viewModel.loginEnabled.value)
    }
    
    @Test
    fun `loginEnabled returns false for invalid email`() = runTest {
        viewModel.onUsernameChanged("invalid-email")
        viewModel.onPasswordChanged("Password123@")
        
        assertFalse(viewModel.loginEnabled.value)
    }
    
    @Test
    fun `loginEnabled returns false for short password`() = runTest {
        viewModel.onUsernameChanged("test@example.com")
        viewModel.onPasswordChanged("123")
        
        assertFalse(viewModel.loginEnabled.value)
    }
}






