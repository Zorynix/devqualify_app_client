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
import com.diploma.work.grpc.auth.AuthServiceGrpc
import com.diploma.work.utils.ErrorContext
import com.diploma.work.utils.ErrorMessageUtils
import com.orhanobut.logger.Logger
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthGrpcClient @Inject constructor(
    private val channel: ManagedChannel
) {
    private val stub = AuthServiceGrpc.newBlockingStub(channel)
    private val tag = "grpc.auth"

    private fun getErrorContext(e: StatusRuntimeException, defaultContext: ErrorContext): ErrorContext {
        return when (e.status.code) {
            Status.Code.UNAVAILABLE, Status.Code.DEADLINE_EXCEEDED -> ErrorContext.NETWORK
            else -> defaultContext
        }
    }

    private fun getGenericErrorContext(e: Exception): ErrorContext {
        return if (e is StatusRuntimeException) {
            getErrorContext(e, ErrorContext.GENERIC)
        } else {
            ErrorContext.GENERIC
        }
    }

    suspend fun register(request: RegisterRequest): Result<RegisterResponse> = withContext(Dispatchers.IO) {
        try {
            Logger.d("$tag: Registering new user with email: ${request.email}")
            val grpcRequest = com.diploma.work.grpc.auth.RegisterRequest.newBuilder()
                .setPassword(request.password)
                .setEmail(request.email)
                .build()
            val grpcResponse = stub.register(grpcRequest)
            Logger.d("$tag: Registration successful for email: ${request.email}, userId: ${grpcResponse.userId}")
            Result.success(RegisterResponse(userId = grpcResponse.userId))        } catch (e: StatusRuntimeException) {
            Logger.e("$tag: Registration failed with gRPC error: ${e.status.code} - ${e.status.description}")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, ErrorContext.REGISTRATION)))
        } catch (e: Exception) {
            Logger.e("$tag: Registration failed with exception: ${e.message}")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, ErrorContext.REGISTRATION)))
        }
    }

    suspend fun login(request: LoginRequest): Result<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            Logger.d("$tag: User login attempt with email: ${request.email}, appId: ${request.appId}")
            val grpcRequest = com.diploma.work.grpc.auth.LoginRequest.newBuilder()
                .setPassword(request.password)
                .setEmail(request.email)
                .setAppId(request.appId)
                .build()
            val grpcResponse = stub.login(grpcRequest)
            Logger.d("$tag: Login successful for email: ${request.email}")
            Logger.v("$tag: Token received: ${grpcResponse.accessToken.take(10)}...")
            Result.success(
                LoginResponse(
                    accessToken = grpcResponse.accessToken,
                    refreshToken = grpcResponse.refreshToken
                )
            )        } catch (e: StatusRuntimeException) {
            Logger.e("$tag: Login failed with gRPC error: ${e.status.code} - ${e.status.description}")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, ErrorContext.LOGIN)))
        } catch (e: Exception) {
            Logger.e("$tag: Login failed with exception: ${e.message}")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, ErrorContext.LOGIN)))
        }
    }

    suspend fun logout(request: LogoutRequest): Result<LogoutResponse> = withContext(Dispatchers.IO) {
        try {
            Logger.d("$tag: Logging out user")
            val grpcRequest = com.diploma.work.grpc.auth.LogoutRequest.newBuilder()
                .setAccessToken(request.accessToken)
                .build()
            val grpcResponse = stub.logout(grpcRequest)
            Logger.d("$tag: Logout ${if (grpcResponse.success) "successful" else "failed"}")
            Result.success(LogoutResponse(success = grpcResponse.success))        } catch (e: StatusRuntimeException) {
            Logger.e("$tag: Logout failed with gRPC error: ${e.status.code} - ${e.status.description}")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, getErrorContext(e, ErrorContext.GENERIC))))
        } catch (e: Exception) {
            Logger.e("$tag: Logout failed with exception: ${e.message}")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, getGenericErrorContext(e))))
        }
    }

    suspend fun isAdmin(request: IsAdminRequest): Result<IsAdminResponse> = withContext(Dispatchers.IO) {
        try {
            Logger.d("$tag: Checking if user ${request.userId} is admin")
            val grpcRequest = com.diploma.work.grpc.auth.IsAdminRequest.newBuilder()
                .setUserId(request.userId)
                .build()
            val grpcResponse = stub.isAdmin(grpcRequest)
            Logger.d("$tag: User ${request.userId} is admin: ${grpcResponse.isAdmin}")
            Result.success(IsAdminResponse(isAdmin = grpcResponse.isAdmin))        } catch (e: StatusRuntimeException) {
            Logger.e("$tag: Admin check failed with gRPC error: ${e.status.code} - ${e.status.description}")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, getErrorContext(e, ErrorContext.GENERIC))))
        } catch (e: Exception) {
            Logger.e("$tag: Admin check failed with exception: ${e.message}")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, getGenericErrorContext(e))))
        }
    }

    suspend fun confirmEmail(request: ConfirmEmailRequest): Result<ConfirmEmailResponse> = withContext(Dispatchers.IO) {
        try {
            Logger.d("$tag: Confirming email for: ${request.email}")
            val grpcRequest = com.diploma.work.grpc.auth.ConfirmEmailRequest.newBuilder()
                .setConfirmationToken(request.confirmationToken)
                .setEmail(request.email)
                .build()
            val grpcResponse = stub.confirmEmail(grpcRequest)
            Logger.d("$tag: Email confirmation ${if (grpcResponse.confirmed) "successful" else "failed"} for: ${request.email}")
            Result.success(ConfirmEmailResponse(confirmed = grpcResponse.confirmed))        } catch (e: StatusRuntimeException) {
            Logger.e("$tag: Email confirmation failed with gRPC error: ${e.status.code} - ${e.status.description}")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, ErrorContext.EMAIL_CONFIRMATION)))
        } catch (e: Exception) {
            Logger.e("$tag: Email confirmation failed with exception: ${e.message}")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, ErrorContext.EMAIL_CONFIRMATION)))
        }
    }

    suspend fun sendConfirmationCode(request: SendConfirmationCodeRequest): Result<SendConfirmationCodeResponse> = withContext(Dispatchers.IO) {
        try {
            Logger.d("$tag: Sending confirmation code to email: ${request.email}")
            val grpcRequest = com.diploma.work.grpc.auth.SendConfirmationCodeRequest.newBuilder()
                .setEmail(request.email)
                .build()
            val grpcResponse = stub.sendConfirmationCode(grpcRequest)
            Logger.d("$tag: Confirmation code ${if (grpcResponse.success) "sent successfully" else "failed to send"} to: ${request.email}")
            Result.success(SendConfirmationCodeResponse(success = grpcResponse.success))        } catch (e: StatusRuntimeException) {
            Logger.e("$tag: Sending confirmation code failed with gRPC error: ${e.status.code} - ${e.status.description}")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, ErrorContext.EMAIL_CONFIRMATION)))
        } catch (e: Exception) {
            Logger.e("$tag: Sending confirmation code failed with exception: ${e.message}")
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, ErrorContext.EMAIL_CONFIRMATION)))
        }
    }

}