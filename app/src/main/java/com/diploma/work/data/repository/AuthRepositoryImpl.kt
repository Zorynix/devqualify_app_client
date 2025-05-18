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
import com.orhanobut.logger.Logger
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authGrpcClient: AuthGrpcClient
) : AuthRepository {
    private val tag = "repo.auth"
    
    override suspend fun register(request: RegisterRequest): Result<RegisterResponse> {
        Logger.d("$tag: Registering new user with email: ${request.email}")
        return authGrpcClient.register(request).also { result ->
            result.fold(
                onSuccess = { response -> 
                    Logger.d("$tag: User registered successfully with ID: ${response.userId}") 
                },
                onFailure = { error -> 
                    Logger.e("$tag: Registration failed: ${error.message}") 
                }
            )
        }
    }

    override suspend fun login(request: LoginRequest): Result<LoginResponse> {
        Logger.d("$tag: Attempting login for user: ${request.email}")
        return authGrpcClient.login(request).also { result ->
            result.fold(
                onSuccess = { 
                    Logger.d("$tag: User login successful for: ${request.email}") 
                },
                onFailure = { error -> 
                    Logger.e("$tag: Login failed: ${error.message}") 
                }
            )
        }
    }

    override suspend fun logout(request: LogoutRequest): Result<LogoutResponse> {
        Logger.d("$tag: Logging out user")
        return authGrpcClient.logout(request).also { result ->
            result.fold(
                onSuccess = { response -> 
                    Logger.d("$tag: Logout ${if (response.success) "successful" else "failed"}") 
                },
                onFailure = { error -> 
                    Logger.e("$tag: Logout failed: ${error.message}") 
                }
            )
        }
    }

    override suspend fun isAdmin(request: IsAdminRequest): Result<IsAdminResponse> {
        Logger.d("$tag: Checking if user ${request.userId} is admin")
        return authGrpcClient.isAdmin(request).also { result ->
            result.fold(
                onSuccess = { response -> 
                    Logger.d("$tag: User ${request.userId} is admin check result: ${response.isAdmin}") 
                },
                onFailure = { error -> 
                    Logger.e("$tag: Admin check failed: ${error.message}") 
                }
            )
        }
    }

    override suspend fun confirmEmail(request: ConfirmEmailRequest): Result<ConfirmEmailResponse> {
        Logger.d("$tag: Confirming email for: ${request.email}")
        return authGrpcClient.confirmEmail(request).also { result ->
            result.fold(
                onSuccess = { response -> 
                    Logger.d("$tag: Email confirmation ${if (response.confirmed) "successful" else "failed"} for: ${request.email}") 
                },
                onFailure = { error -> 
                    Logger.e("$tag: Email confirmation failed: ${error.message}") 
                }
            )
        }
    }

    override suspend fun sendConfirmationCode(request: SendConfirmationCodeRequest): Result<SendConfirmationCodeResponse> {
        Logger.d("[36m$tag: Sending confirmation code to email: ${request.email}")
        return authGrpcClient.sendConfirmationCode(request).also { result ->
            result.fold(
                onSuccess = { response -> 
                    Logger.d("[36m$tag: Confirmation code ${if (response.success) "sent successfully" else "failed to send"} to: ${request.email}") 
                },
                onFailure = { error -> 
                    Logger.e("[36m$tag: Sending confirmation code failed: ${error.message}") 
                }
            )
        }
    }
}