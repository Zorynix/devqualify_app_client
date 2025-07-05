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
        errorHandler = mockk()
        
        every { savedStateHandle.get<String>("email") } returns testEmail
        
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
        assertTrue(viewModel.confirmEnabled.value)
    }
    
    @Test
    fun `onCodeChanged ignores non-digits`() = runTest {
        viewModel.onCodeChanged("12a34b")
        assertEquals("", viewModel.code.value)
        
        viewModel.onCodeChanged("123456")
        viewModel.onCodeChanged("123abc")
        assertEquals("123456", viewModel.code.value)
    }
    
    @Test
    fun `onCodeChanged ignores codes longer than 6 digits`() = runTest {
        viewModel.onCodeChanged("1234567890")
        assertEquals("", viewModel.code.value)
        
        viewModel.onCodeChanged("123456")
        viewModel.onCodeChanged("12345678")
        assertEquals("123456", viewModel.code.value)
    }
    
    @Test
    fun `confirmEnabled is true only when code is 6 digits`() = runTest {
        assertFalse(viewModel.confirmEnabled.value)
        
        viewModel.onCodeChanged("12345")
        assertFalse(viewModel.confirmEnabled.value)
        
        viewModel.onCodeChanged("123456")
        assertTrue(viewModel.confirmEnabled.value)
        
        viewModel.onCodeChanged("1234567")
        assertTrue(viewModel.confirmEnabled.value)
    }
    
    @Test
    fun `onConfirmClicked success navigates to login`() = runTest {
        val confirmResponse = ConfirmEmailResponse(confirmed = true)
        
        coEvery { 
            authRepository.confirmEmail(ConfirmEmailRequest(email = testEmail, confirmationToken = "123456"))
        } returns Result.success(confirmResponse)
        
        viewModel.onCodeChanged("123456")
        viewModel.onConfirmClicked()
        
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.errorMessage.value)
        
        coVerify { 
            authRepository.confirmEmail(ConfirmEmailRequest(email = testEmail, confirmationToken = "123456"))
        }
    }
    
    @Test
    fun `onConfirmClicked failure updates error state`() = runTest {
        val confirmResponse = ConfirmEmailResponse(confirmed = false)
        
        coEvery { 
            authRepository.confirmEmail(any())
        } returns Result.success(confirmResponse)
        
        viewModel.onCodeChanged("123456")
        viewModel.onConfirmClicked()
        
        assertFalse(viewModel.isLoading.value)
        assertEquals("Подтверждение не удалось", viewModel.errorMessage.value)
    }
    
    @Test
    fun `onConfirmClicked repository failure updates error state`() = runTest {
        val errorMessage = "Network error"
        
        coEvery { 
            authRepository.confirmEmail(any())
        } returns Result.failure(Exception(errorMessage))
        
        viewModel.onCodeChanged("123456")
        viewModel.onConfirmClicked()
        
        assertFalse(viewModel.isLoading.value)
        assertEquals(errorMessage, viewModel.errorMessage.value)
    }
    
    @Test
    fun `onSendCodeClicked sends confirmation code`() = runTest {
        val sendResponse = SendConfirmationCodeResponse(success = true)
        
        coEvery { 
            authRepository.sendConfirmationCode(SendConfirmationCodeRequest(email = testEmail))
        } returns Result.success(sendResponse)
        
        viewModel.onSendCodeClicked()
        
        assertFalse(viewModel.isLoading.value)
        assertEquals("Код отправлен", viewModel.successMessage.value)
        assertTrue(viewModel.resendCooldownSeconds.value > 0)
        
        coVerify { 
            authRepository.sendConfirmationCode(SendConfirmationCodeRequest(email = testEmail))
        }
    }
    
    @Test
    fun `onSendCodeClicked failure updates error state`() = runTest {
        val sendResponse = SendConfirmationCodeResponse(success = false)
        
        coEvery { 
            authRepository.sendConfirmationCode(any())
        } returns Result.success(sendResponse)
        
        viewModel.onSendCodeClicked()
        
        assertFalse(viewModel.isLoading.value)
        assertEquals("Ошибка отправки", viewModel.errorMessage.value)
    }
    
    @Test
    fun `onSendCodeClicked repository failure updates error state`() = runTest {
        val errorMessage = "Network error"
        
        coEvery { 
            authRepository.sendConfirmationCode(any())
        } returns Result.failure(Exception(errorMessage))
        
        viewModel.onSendCodeClicked()
        
        assertFalse(viewModel.isLoading.value)
        assertEquals(errorMessage, viewModel.errorMessage.value)
    }
    
    @Test
    fun `onResendCodeClicked sends code when cooldown is zero`() = runTest {
        val sendResponse = SendConfirmationCodeResponse(success = true)
        
        coEvery { 
            authRepository.sendConfirmationCode(any())
        } returns Result.success(sendResponse)
        
        viewModel.onResendCodeClicked()
        
        coVerify { authRepository.sendConfirmationCode(any()) }
    }
    
    @Test
    fun `resendEnabled is false during loading`() = runTest {
        coEvery { 
            authRepository.sendConfirmationCode(any())
        } coAnswers {
            kotlinx.coroutines.delay(100)
            Result.success(SendConfirmationCodeResponse(success = true))
        }
        
        viewModel.onSendCodeClicked()
        
        assertTrue(viewModel.isLoading.value)
    }
    
    @Test
    fun `loading state is managed correctly during confirmation`() = runTest {
        coEvery { 
            authRepository.confirmEmail(any())
        } coAnswers {
            kotlinx.coroutines.delay(100)
            Result.success(ConfirmEmailResponse(confirmed = true))
        }
        
        viewModel.onCodeChanged("123456")
        
        viewModel.onConfirmClicked()
        
        assertTrue(viewModel.isLoading.value)
    }
    
    @Test
    fun `error and success messages are cleared on new actions`() = runTest {
        viewModel.onCodeChanged("123456")
        
        coEvery { 
            authRepository.confirmEmail(any())
        } returns Result.failure(Exception("Error"))
        
        viewModel.onConfirmClicked()
        
        assertEquals("Error", viewModel.errorMessage.value)
        
        coEvery { 
            authRepository.confirmEmail(any())
        } returns Result.success(ConfirmEmailResponse(confirmed = true))
        
        viewModel.onConfirmClicked()
        
        assertNull(viewModel.errorMessage.value)
        assertNull(viewModel.successMessage.value)
    }
}






