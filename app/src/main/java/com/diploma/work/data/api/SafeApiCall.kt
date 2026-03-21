package com.diploma.work.data.api

import com.diploma.work.utils.ErrorContext
import com.diploma.work.utils.ErrorMessageUtils
import retrofit2.HttpException
import java.io.IOException

suspend fun <T> safeApiCall(
    context: ErrorContext = ErrorContext.GENERIC,
    block: suspend () -> T
): Result<T> {
    return try {
        Result.success(block())
    } catch (e: HttpException) {
        val apiException = ApiErrorParser.parse(e)
        Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(apiException, context)))
    } catch (e: IOException) {
        Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, ErrorContext.NETWORK)))
    } catch (e: Exception) {
        Result.failure(Exception(ErrorMessageUtils.getContextualErrorMessage(e, context)))
    }
}
