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
import com.diploma.work.utils.SecureLogger
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

    companion object {
        private const val TAG = "grpc.auth"
    }

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
            SecureLogger.d(TAG, "Registering new user: ${SecureLogger.maskEmail(request.email)}")
            val grpcRequest = com.diploma.work.grpc.auth.RegisterRequest.newBuilder()
                .setPassword(request.password)
                .setEmail(request.email)
                .build()
            val grpcResponse = stub.register(grpcRequest)
            SecureLogger.d(TAG, "Registration successful")
            Result.success(RegisterResponse(userId = grpcResponse.userId))
        } catch (e: StatusRuntimeException) {
            SecureLogger.e(TAG, "Registration failed: ${e.status.code}", e)
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, ErrorContext.REGISTRATION)))
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Registration failed", e)
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, ErrorContext.REGISTRATION)))
        }
    }

    suspend fun login(request: LoginRequest): Result<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            SecureLogger.d(TAG, "Login attempt: ${SecureLogger.maskEmail(request.email)}")
            val grpcRequest = com.diploma.work.grpc.auth.LoginRequest.newBuilder()
                .setPassword(request.password)
                .setEmail(request.email)
                .build()
            val grpcResponse = stub.login(grpcRequest)
            SecureLogger.d(TAG, "Login successful")
            Result.success(
                LoginResponse(
                    accessToken = grpcResponse.accessToken,
                    refreshToken = grpcResponse.refreshToken
                )
            )
        } catch (e: StatusRuntimeException) {
            SecureLogger.e(TAG, "Login failed: ${e.status.code}", e)
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, ErrorContext.LOGIN)))
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Login failed", e)
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, ErrorContext.LOGIN)))
        }
    }

    suspend fun logout(request: LogoutRequest): Result<LogoutResponse> = withContext(Dispatchers.IO) {
        try {
            SecureLogger.d(TAG, "Logging out user")
            val grpcRequest = com.diploma.work.grpc.auth.LogoutRequest.newBuilder()
                .setAccessToken(request.accessToken)
                .build()
            val grpcResponse = stub.logout(grpcRequest)
            SecureLogger.d(TAG, "Logout ${if (grpcResponse.success) "successful" else "failed"}")
            Result.success(LogoutResponse(success = grpcResponse.success))
        } catch (e: StatusRuntimeException) {
            SecureLogger.e(TAG, "Logout failed: ${e.status.code}", e)
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, getErrorContext(e, ErrorContext.GENERIC))))
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Logout failed", e)
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, getGenericErrorContext(e))))
        }
    }

    suspend fun isAdmin(request: IsAdminRequest): Result<IsAdminResponse> = withContext(Dispatchers.IO) {
        try {
            SecureLogger.d(TAG, "Checking admin status")
            val grpcRequest = com.diploma.work.grpc.auth.IsAdminRequest.newBuilder()
                .setUserId(request.userId)
                .build()
            val grpcResponse = stub.isAdmin(grpcRequest)
            SecureLogger.d(TAG, "Admin check completed")
            Result.success(IsAdminResponse(isAdmin = grpcResponse.isAdmin))
        } catch (e: StatusRuntimeException) {
            SecureLogger.e(TAG, "Admin check failed: ${e.status.code}", e)
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, getErrorContext(e, ErrorContext.GENERIC))))
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Admin check failed", e)
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, getGenericErrorContext(e))))
        }
    }

    suspend fun confirmEmail(request: ConfirmEmailRequest): Result<ConfirmEmailResponse> = withContext(Dispatchers.IO) {
        try {
            SecureLogger.d(TAG, "Confirming email: ${SecureLogger.maskEmail(request.email)}")
            val grpcRequest = com.diploma.work.grpc.auth.ConfirmEmailRequest.newBuilder()
                .setConfirmationToken(request.confirmationToken)
                .setEmail(request.email)
                .build()
            val grpcResponse = stub.confirmEmail(grpcRequest)
            SecureLogger.d(TAG, "Email confirmation ${if (grpcResponse.confirmed) "successful" else "failed"}")
            Result.success(ConfirmEmailResponse(confirmed = grpcResponse.confirmed))
        } catch (e: StatusRuntimeException) {
            SecureLogger.e(TAG, "Email confirmation failed: ${e.status.code}", e)
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, ErrorContext.EMAIL_CONFIRMATION)))
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Email confirmation failed", e)
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, ErrorContext.EMAIL_CONFIRMATION)))
        }
    }

    suspend fun sendConfirmationCode(request: SendConfirmationCodeRequest): Result<SendConfirmationCodeResponse> = withContext(Dispatchers.IO) {
        try {
            SecureLogger.d(TAG, "Sending confirmation code to: ${SecureLogger.maskEmail(request.email)}")
            val grpcRequest = com.diploma.work.grpc.auth.SendConfirmationCodeRequest.newBuilder()
                .setEmail(request.email)
                .build()
            val grpcResponse = stub.sendConfirmationCode(grpcRequest)
            SecureLogger.d(TAG, "Confirmation code ${if (grpcResponse.success) "sent" else "failed"}")
            Result.success(SendConfirmationCodeResponse(success = grpcResponse.success))
        } catch (e: StatusRuntimeException) {
            SecureLogger.e(TAG, "Sending confirmation code failed: ${e.status.code}", e)
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, ErrorContext.EMAIL_CONFIRMATION)))
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Sending confirmation code failed", e)
            Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, ErrorContext.EMAIL_CONFIRMATION)))
        }
    }

}