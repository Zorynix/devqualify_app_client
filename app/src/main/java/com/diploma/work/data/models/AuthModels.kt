package com.diploma.work.data.models

data class RegisterRequest(
    val password: String,
    val email: String
)

data class RegisterResponse(
    val userId: Long
)

data class LoginRequest(
    val password: String,
    val email: String,
    val appId: Int
)

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String
)

data class LogoutRequest(
    val accessToken: String
)

data class LogoutResponse(
    val success: Boolean
)

data class IsAdminRequest(
    val userId: Long
)

data class IsAdminResponse(
    val isAdmin: Boolean
)

data class ConfirmEmailRequest(
    val confirmationToken: String,
    val email: String
)

data class ConfirmEmailResponse(
    val confirmed: Boolean
)

data class SendConfirmationCodeRequest(
    val email: String
)

data class SendConfirmationCodeResponse(
    val success: Boolean
)