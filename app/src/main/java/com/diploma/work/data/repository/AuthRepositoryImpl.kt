package com.diploma.work.data.repository


import com.diploma.work.data.grpc.AuthGrpcClient
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
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authGrpcClient: AuthGrpcClient
) : AuthRepository {
    override suspend fun register(request: RegisterRequest): Result<RegisterResponse> {
        return authGrpcClient.register(request)
    }

    override suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return authGrpcClient.login(request)
    }

    override suspend fun logout(request: LogoutRequest): Result<LogoutResponse> {
        return authGrpcClient.logout(request)
    }

    override suspend fun isAdmin(request: IsAdminRequest): Result<IsAdminResponse> {
        return authGrpcClient.isAdmin(request)
    }

    override suspend fun confirmEmail(request: ConfirmEmailRequest): Result<ConfirmEmailResponse> {
        return authGrpcClient.confirmEmail(request)
    }

    override suspend fun sendConfirmationCode(request: SendConfirmationCodeRequest): Result<SendConfirmationCodeResponse> {
        return authGrpcClient.sendConfirmationCode(request)
    }
}