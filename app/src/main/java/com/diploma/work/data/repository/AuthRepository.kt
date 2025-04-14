package com.diploma.work.data.repository


import com.diploma.work.data.models.IsAdminRequest
import com.diploma.work.data.models.IsAdminResponse
import com.diploma.work.data.models.LoginRequest
import com.diploma.work.data.models.LoginResponse
import com.diploma.work.data.models.LogoutRequest
import com.diploma.work.data.models.LogoutResponse
import com.diploma.work.data.models.RegisterRequest
import com.diploma.work.data.models.RegisterResponse

interface AuthRepository {
    suspend fun register(request: RegisterRequest): Result<RegisterResponse>
    suspend fun login(request: LoginRequest): Result<LoginResponse>
    suspend fun logout(request: LogoutRequest): Result<LogoutResponse>
    suspend fun isAdmin(request: IsAdminRequest): Result<IsAdminResponse>
}