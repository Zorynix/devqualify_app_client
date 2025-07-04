package com.diploma.work.data.repository

import com.diploma.work.data.grpc.AuthGrpcClient
import com.diploma.work.data.models.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class AuthRepositoryImplTest {
    
    private lateinit var authGrpcClient: AuthGrpcClient
    private lateinit var authRepository: AuthRepository
    
    @Before
    fun setup() {
        authGrpcClient = mockk()
        authRepository = AuthRepositoryImpl(authGrpcClient)
    }
    
    @Test
    fun `register with valid data succeeds`() = runTest {
        val request = RegisterRequest(
            email = "test@example.com",
            password = "password123"
        )
        val response = RegisterResponse(userId = 1L)
        
        coEvery { authGrpcClient.register(request) } returns Result.success(response)
        
        val result = authRepository.register(request)
        
        assertTrue(result.isSuccess)
        assertEquals(response, result.getOrNull())
        coVerify { authGrpcClient.register(request) }
    }
    
    @Test
    fun `register with invalid data fails`() = runTest {
        val request = RegisterRequest(
            email = "invalid-email",
            password = "123"
        )
        val errorMessage = "Invalid email or password"
        
        coEvery { authGrpcClient.register(request) } returns Result.failure(Exception(errorMessage))
        
        val result = authRepository.register(request)
        
        assertTrue(result.isFailure)
        assertEquals(errorMessage, result.exceptionOrNull()?.message)
        coVerify { authGrpcClient.register(request) }
    }
    
    @Test
    fun `login with valid credentials succeeds`() = runTest {
        val request = LoginRequest(
            email = "test@example.com",
            password = "password123"
        )
        val response = LoginResponse(
            accessToken = "access_token",
            refreshToken = "refresh_token"
        )
        
        coEvery { authGrpcClient.login(request) } returns Result.success(response)
        
        val result = authRepository.login(request)
        
        assertTrue(result.isSuccess)
        assertEquals(response, result.getOrNull())
        coVerify { authGrpcClient.login(request) }
    }
    
    @Test
    fun `login with invalid credentials fails`() = runTest {
        val request = LoginRequest(
            email = "test@example.com",
            password = "wrongpassword"
        )
        val errorMessage = "Invalid credentials"
        
        coEvery { authGrpcClient.login(request) } returns Result.failure(Exception(errorMessage))
        
        val result = authRepository.login(request)
        
        assertTrue(result.isFailure)
        assertEquals(errorMessage, result.exceptionOrNull()?.message)
        coVerify { authGrpcClient.login(request) }
    }
    
    @Test
    fun `logout succeeds`() = runTest {
        val request = LogoutRequest(accessToken = "access_token")
        val response = LogoutResponse(success = true)
        
        coEvery { authGrpcClient.logout(request) } returns Result.success(response)
        
        val result = authRepository.logout(request)
        
        assertTrue(result.isSuccess)
        assertEquals(response, result.getOrNull())
        coVerify { authGrpcClient.logout(request) }
    }
    
    @Test
    fun `logout fails`() = runTest {
        val request = LogoutRequest(accessToken = "invalid_token")
        val errorMessage = "Invalid token"
        
        coEvery { authGrpcClient.logout(request) } returns Result.failure(Exception(errorMessage))
        
        val result = authRepository.logout(request)
        
        assertTrue(result.isFailure)
        assertEquals(errorMessage, result.exceptionOrNull()?.message)
        coVerify { authGrpcClient.logout(request) }
    }
    
    @Test
    fun `isAdmin returns true for admin user`() = runTest {
        val request = IsAdminRequest(userId = 1L)
        val response = IsAdminResponse(isAdmin = true)
        
        coEvery { authGrpcClient.isAdmin(request) } returns Result.success(response)
        
        val result = authRepository.isAdmin(request)
        
        assertTrue(result.isSuccess)
        assertEquals(response, result.getOrNull())
        assertTrue(result.getOrNull()?.isAdmin == true)
        coVerify { authGrpcClient.isAdmin(request) }
    }
    
    @Test
    fun `isAdmin returns false for regular user`() = runTest {
        val request = IsAdminRequest(userId = 2L)
        val response = IsAdminResponse(isAdmin = false)
        
        coEvery { authGrpcClient.isAdmin(request) } returns Result.success(response)
        
        val result = authRepository.isAdmin(request)
        
        assertTrue(result.isSuccess)
        assertEquals(response, result.getOrNull())
        assertFalse(result.getOrNull()?.isAdmin == true)
        coVerify { authGrpcClient.isAdmin(request) }
    }
    
    @Test
    fun `confirmEmail with valid token succeeds`() = runTest {
        val request = ConfirmEmailRequest(
            email = "test@example.com",
            confirmationToken = "valid_token"
        )
        val response = ConfirmEmailResponse(confirmed = true)
        
        coEvery { authGrpcClient.confirmEmail(request) } returns Result.success(response)
        
        val result = authRepository.confirmEmail(request)
        
        assertTrue(result.isSuccess)
        assertEquals(response, result.getOrNull())
        assertTrue(result.getOrNull()?.confirmed == true)
        coVerify { authGrpcClient.confirmEmail(request) }
    }
    
    @Test
    fun `confirmEmail with invalid token fails`() = runTest {
        val request = ConfirmEmailRequest(
            email = "test@example.com",
            confirmationToken = "invalid_token"
        )
        val response = ConfirmEmailResponse(confirmed = false)
        
        coEvery { authGrpcClient.confirmEmail(request) } returns Result.success(response)
        
        val result = authRepository.confirmEmail(request)
        
        assertTrue(result.isSuccess)
        assertEquals(response, result.getOrNull())
        assertFalse(result.getOrNull()?.confirmed == true)
        coVerify { authGrpcClient.confirmEmail(request) }
    }
    
    @Test
    fun `sendConfirmationCode succeeds`() = runTest {
        val request = SendConfirmationCodeRequest(email = "test@example.com")
        val response = SendConfirmationCodeResponse(success = true)
        
        coEvery { authGrpcClient.sendConfirmationCode(request) } returns Result.success(response)
        
        val result = authRepository.sendConfirmationCode(request)
        
        assertTrue(result.isSuccess)
        assertEquals(response, result.getOrNull())
        assertTrue(result.getOrNull()?.success == true)
        coVerify { authGrpcClient.sendConfirmationCode(request) }
    }
    
    @Test
    fun `sendConfirmationCode fails`() = runTest {
        val request = SendConfirmationCodeRequest(email = "invalid@example.com")
        val errorMessage = "Email not found"
        
        coEvery { authGrpcClient.sendConfirmationCode(request) } returns Result.failure(Exception(errorMessage))
        
        val result = authRepository.sendConfirmationCode(request)
        
        assertTrue(result.isFailure)
        assertEquals(errorMessage, result.exceptionOrNull()?.message)
        coVerify { authGrpcClient.sendConfirmationCode(request) }
    }
}
