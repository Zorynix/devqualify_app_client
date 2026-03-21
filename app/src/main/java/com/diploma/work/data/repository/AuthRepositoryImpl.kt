package com.diploma.work.data.repository

import com.diploma.work.data.api.AuthApi
import com.diploma.work.data.api.safeApiCall
import com.diploma.work.data.models.ConfirmEmailRequest
import com.diploma.work.data.models.ConfirmEmailResponse
import com.diploma.work.data.models.IsAdminRequest
import com.diploma.work.data.models.IsAdminResponse
import com.diploma.work.data.models.LoginRequest
import com.diploma.work.data.models.LoginResponse
import com.diploma.work.data.models.LogoutRequest
import com.diploma.work.data.models.LogoutResponse
import com.diploma.work.data.models.RegisterRequest
import com.diploma.work.data.models.RegisterResponse
import com.diploma.work.data.models.SendConfirmationCodeRequest
import com.diploma.work.data.models.SendConfirmationCodeResponse
import com.diploma.work.utils.ErrorContext
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi
) : AuthRepository {

    override suspend fun register(request: RegisterRequest): Result<RegisterResponse> =
        safeApiCall(ErrorContext.REGISTRATION) { api.register(request) }

    override suspend fun login(request: LoginRequest): Result<LoginResponse> =
        safeApiCall(ErrorContext.LOGIN) { api.login(request) }

    override suspend fun logout(request: LogoutRequest): Result<LogoutResponse> =
        safeApiCall { api.logout() }

    override suspend fun isAdmin(request: IsAdminRequest): Result<IsAdminResponse> =
        safeApiCall { api.isAdmin(request.userId) }

    override suspend fun confirmEmail(request: ConfirmEmailRequest): Result<ConfirmEmailResponse> =
        safeApiCall(ErrorContext.EMAIL_CONFIRMATION) { api.confirmEmail(request) }

    override suspend fun sendConfirmationCode(request: SendConfirmationCodeRequest): Result<SendConfirmationCodeResponse> =
        safeApiCall(ErrorContext.EMAIL_CONFIRMATION) { api.sendConfirmationCode(request) }
}
