package com.diploma.work.data.grpc

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
import com.diploma.work.grpc.AuthServiceGrpc
import io.grpc.ManagedChannel
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthGrpcClient @Inject constructor(
    private val channel: ManagedChannel
) {
    private val stub = AuthServiceGrpc.newBlockingStub(channel)

    suspend fun register(request: RegisterRequest): Result<RegisterResponse> = withContext(Dispatchers.IO) {
        try {
            val grpcRequest = com.diploma.work.grpc.RegisterRequest.newBuilder()
                .setPassword(request.password)
                .setEmail(request.email)
                .build()
            val grpcResponse = stub.register(grpcRequest)
            Result.success(RegisterResponse(userId = grpcResponse.userId))
        } catch (e: StatusRuntimeException) {
            Result.failure(Exception("Ошибка gRPC: ${e.status.code} - ${e.status.description}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(request: LoginRequest): Result<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            val grpcRequest = com.diploma.work.grpc.LoginRequest.newBuilder()
                .setPassword(request.password)
                .setEmail(request.email)
                .setAppId(request.appId)
                .build()
            val grpcResponse = stub.login(grpcRequest)
            Result.success(
                LoginResponse(
                    accessToken = grpcResponse.accessToken,
                    refreshToken = grpcResponse.refreshToken
                )
            )
        } catch (e: StatusRuntimeException) {
            Result.failure(Exception("Ошибка gRPC: ${e.status.code} - ${e.status.description}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(request: LogoutRequest): Result<LogoutResponse> = withContext(Dispatchers.IO) {
        try {
            val grpcRequest = com.diploma.work.grpc.LogoutRequest.newBuilder()
                .setAccessToken(request.accessToken)
                .build()
            val grpcResponse = stub.logout(grpcRequest)
            Result.success(LogoutResponse(success = grpcResponse.success))
        } catch (e: StatusRuntimeException) {
            Result.failure(Exception("Ошибка gRPC: ${e.status.code} - ${e.status.description}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isAdmin(request: IsAdminRequest): Result<IsAdminResponse> = withContext(Dispatchers.IO) {
        try {
            val grpcRequest = com.diploma.work.grpc.IsAdminRequest.newBuilder()
                .setUserId(request.userId)
                .build()
            val grpcResponse = stub.isAdmin(grpcRequest)
            Result.success(IsAdminResponse(isAdmin = grpcResponse.isAdmin))
        } catch (e: StatusRuntimeException) {
            Result.failure(Exception("Ошибка gRPC: ${e.status.code} - ${e.status.description}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun confirmEmail(request: ConfirmEmailRequest): Result<ConfirmEmailResponse> = withContext(Dispatchers.IO) {
        try {
            val grpcRequest = com.diploma.work.grpc.ConfirmEmailRequest.newBuilder()
                .setConfirmationToken(request.confirmationToken)
                .setEmail(request.email)
                .build()
            val grpcResponse = stub.confirmEmail(grpcRequest)
            Result.success(ConfirmEmailResponse(confirmed = grpcResponse.confirmed))
        } catch (e: StatusRuntimeException) {
            Result.failure(Exception("Ошибка gRPC: ${e.status.code} - ${e.status.description}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendConfirmationCode(request: SendConfirmationCodeRequest): Result<SendConfirmationCodeResponse> = withContext(Dispatchers.IO) {
        try {
            val grpcRequest = com.diploma.work.grpc.SendConfirmationCodeRequest.newBuilder()
                .setEmail(request.email)
                .build()
            val grpcResponse = stub.sendConfirmationCode(grpcRequest)
            Result.success(SendConfirmationCodeResponse(success = grpcResponse.success))
        } catch (e: StatusRuntimeException) {
            Result.failure(Exception("Ошибка gRPC: ${e.status.code} - ${e.status.description}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}