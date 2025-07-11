package com.diploma.work.ui.feature.auth.confirmation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.diploma.work.data.AppSession
import com.diploma.work.data.models.ConfirmEmailRequest
import com.diploma.work.data.models.ConfirmEmailResponse
import com.diploma.work.data.models.SendConfirmationCodeRequest
import com.diploma.work.data.models.SendConfirmationCodeResponse
import com.diploma.work.data.repository.AuthRepository
import com.diploma.work.ui.navigation.Login
import com.diploma.work.utils.ErrorHandler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EmailConfirmationViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
    
    private lateinit var authRepository: AuthRepository
    private lateinit var session: AppSession
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var errorHandler: ErrorHandler
    private lateinit var viewModel: EmailConfirmationViewModel
    
    private val testEmail = "test@example.com"
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        authRepository = mockk()
        session = mockk(relaxed = true)
        savedStateHandle = mockk()
        errorHandler = mockk(relaxed = true)
        
        every { savedStateHandle.get<String>("email") } returns testEmail
        every { errorHandler.getContextualErrorMessage(any(), any()) } returns "Test error message"
        
        viewModel = EmailConfirmationViewModel(authRepository, session, savedStateHandle, errorHandler)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state is correct`() = runTest {
        assertEquals(testEmail, viewModel.email)
        assertEquals("", viewModel.code.value)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.errorMessage.value)
        assertNull(viewModel.successMessage.value)
        assertEquals(0, viewModel.resendCooldownSeconds.value)
        assertFalse(viewModel.confirmEnabled.value)
        assertTrue(viewModel.resendEnabled.value)
    }
    
    @Test
    fun `onCodeChanged updates code when valid`() = runTest {
        viewModel.onCodeChanged("123456")
        assertEquals("123456", viewModel.code.value)
    }
    
    @Test
    fun `onCodeChanged ignores non-digits`() = runTest {
        viewModel.onCodeChanged("12a34b")
        assertEquals("", viewModel.code.value)
        
        viewModel.onCodeChanged("123456")
        assertEquals("123456", viewModel.code.value)
    }
    
    @Test
    fun `onCodeChanged ignores codes longer than 6 digits`() = runTest {
        viewModel.onCodeChanged("1234567890")
        assertEquals("", viewModel.code.value)
        
        viewModel.onCodeChanged("123456")
        assertEquals("123456", viewModel.code.value)
    }
    
    @Test
    fun `code input field works correctly`() = runTest {
        viewModel.onCodeChanged("12345")
        assertEquals("12345", viewModel.code.value)
        
        viewModel.onCodeChanged("123456")
        assertEquals("123456", viewModel.code.value)
    }
    
    @Test
    fun `onConfirmClicked triggers confirmation action`() = runTest {
        val confirmResponse = ConfirmEmailResponse(confirmed = true)
        
        coEvery { 
            authRepository.confirmEmail(any())
        } returns Result.success(confirmResponse)
        
        viewModel.onCodeChanged("123456")
        viewModel.onConfirmClicked()
        advanceUntilIdle()
        
        coVerify { authRepository.confirmEmail(any()) }
    }
    
    @Test
    fun `onConfirmClicked handles failure gracefully`() = runTest {
        val confirmResponse = ConfirmEmailResponse(confirmed = false)
        
        coEvery { 
            authRepository.confirmEmail(any())
        } returns Result.success(confirmResponse)
        
        viewModel.onCodeChanged("123456")
        viewModel.onConfirmClicked()
        advanceUntilIdle()
        
        assertFalse(viewModel.isLoading.value)
    }
    
    @Test
    fun `onConfirmClicked handles repository errors`() = runTest {
        coEvery { 
            authRepository.confirmEmail(any())
        } returns Result.failure(Exception("Network error"))
        
        viewModel.onCodeChanged("123456")
        viewModel.onConfirmClicked()
        advanceUntilIdle()
        
        assertFalse(viewModel.isLoading.value)
    }
    
    @Test
    fun `onSendCodeClicked triggers send action`() = runTest {
        val sendResponse = SendConfirmationCodeResponse(success = true)
        
        coEvery { 
            authRepository.sendConfirmationCode(any())
        } returns Result.success(sendResponse)
        
        viewModel.onSendCodeClicked()
        advanceUntilIdle()
        
        coVerify { authRepository.sendConfirmationCode(any()) }
    }
    
    @Test
    fun `onSendCodeClicked handles send failure`() = runTest {
        val sendResponse = SendConfirmationCodeResponse(success = false)
        
        coEvery { 
            authRepository.sendConfirmationCode(any())
        } returns Result.success(sendResponse)
        
        viewModel.onSendCodeClicked()
        advanceUntilIdle()
        
        assertFalse(viewModel.isLoading.value)
    }
    
    @Test
    fun `onSendCodeClicked handles repository errors`() = runTest {
        coEvery { 
            authRepository.sendConfirmationCode(any())
        } returns Result.failure(Exception("Network error"))
        
        viewModel.onSendCodeClicked()
        advanceUntilIdle()
        
        assertFalse(viewModel.isLoading.value)
    }
    
    @Test
    fun `onResendCodeClicked triggers resend action`() = runTest {
        val sendResponse = SendConfirmationCodeResponse(success = true)
        
        coEvery { 
            authRepository.sendConfirmationCode(any())
        } returns Result.success(sendResponse)
        
        viewModel.onResendCodeClicked()
        advanceUntilIdle()
        
        coVerify { authRepository.sendConfirmationCode(any()) }
    }
    
    @Test
    fun `loading state changes during operations`() = runTest {
        coEvery { 
            authRepository.sendConfirmationCode(any())
        } coAnswers {
            kotlinx.coroutines.delay(100)
            Result.success(SendConfirmationCodeResponse(success = true))
        }
        
        viewModel.onSendCodeClicked()
        advanceUntilIdle()
        
        assertFalse(viewModel.isLoading.value)
    }
    
    @Test
    fun `confirmation loading state changes during operations`() = runTest {
        coEvery { 
            authRepository.confirmEmail(any())
        } coAnswers {
            kotlinx.coroutines.delay(100)
            Result.success(ConfirmEmailResponse(confirmed = true))
        }
        
        viewModel.onCodeChanged("123456")
        viewModel.onConfirmClicked()
        advanceUntilIdle()
        
        assertFalse(viewModel.isLoading.value)
    }
    
    @Test
    fun `UI actions clear previous messages`() = runTest {
        viewModel.onCodeChanged("123456")
        
        coEvery { 
            authRepository.confirmEmail(any())
        } returns Result.failure(Exception("Error"))
        
        viewModel.onConfirmClicked()
        advanceUntilIdle()
        
        coEvery { 
            authRepository.confirmEmail(any())
        } returns Result.success(ConfirmEmailResponse(confirmed = true))
        
        viewModel.onConfirmClicked()
        advanceUntilIdle()
        
        assertFalse(viewModel.isLoading.value)
    }
}






