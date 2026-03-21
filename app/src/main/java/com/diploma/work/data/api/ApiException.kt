package com.diploma.work.data.api

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import retrofit2.HttpException

data class ApiErrorResponse(
    @SerializedName("error") val error: ApiErrorBody?
)

data class ApiErrorBody(
    @SerializedName("code") val code: String?,
    @SerializedName("message") val message: String?
)

class ApiException(
    val httpCode: Int,
    val errorCode: String,
    val errorMessage: String
) : Exception(errorMessage)

object ApiErrorParser {

    private val gson = Gson()

    fun parse(e: HttpException): ApiException {
        val body = try {
            e.response()?.errorBody()?.string()
        } catch (_: Exception) {
            null
        }

        val parsed = body?.let {
            try {
                gson.fromJson(it, ApiErrorResponse::class.java)
            } catch (_: Exception) {
                null
            }
        }

        return ApiException(
            httpCode = e.code(),
            errorCode = parsed?.error?.code ?: httpCodeToGrpcCode(e.code()),
            errorMessage = parsed?.error?.message ?: e.message()
        )
    }

    private fun httpCodeToGrpcCode(httpCode: Int): String = when (httpCode) {
        400 -> "InvalidArgument"
        401 -> "Unauthenticated"
        403 -> "PermissionDenied"
        404 -> "NotFound"
        409 -> "AlreadyExists"
        429 -> "ResourceExhausted"
        501 -> "Unimplemented"
        503 -> "Unavailable"
        504 -> "DeadlineExceeded"
        else -> "Internal"
    }
}
