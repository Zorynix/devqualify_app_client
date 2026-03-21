package com.diploma.work.data.api

import com.diploma.work.data.models.ConfirmEmailRequest
import com.diploma.work.data.models.ConfirmEmailResponse
import com.diploma.work.data.models.IsAdminResponse
import com.diploma.work.data.models.LoginRequest
import com.diploma.work.data.models.LoginResponse
import com.diploma.work.data.models.LogoutResponse
import com.diploma.work.data.models.RegisterRequest
import com.diploma.work.data.models.RegisterResponse
import com.diploma.work.data.models.SendConfirmationCodeRequest
import com.diploma.work.data.models.SendConfirmationCodeResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApi {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/logout")
    suspend fun logout(): LogoutResponse

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): LoginResponse

    @POST("auth/confirm-email")
    suspend fun confirmEmail(@Body request: ConfirmEmailRequest): ConfirmEmailResponse

    @POST("auth/send-confirmation")
    suspend fun sendConfirmationCode(@Body request: SendConfirmationCodeRequest): SendConfirmationCodeResponse

    @GET("auth/admin/{user_id}")
    suspend fun isAdmin(@Path("user_id") userId: Long): IsAdminResponse
}

data class RefreshTokenRequest(
    val refreshToken: String
)
