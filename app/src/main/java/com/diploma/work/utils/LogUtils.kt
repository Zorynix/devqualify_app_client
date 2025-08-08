package com.diploma.work.utils

import com.diploma.work.BuildConfig
import com.orhanobut.logger.Logger

object LogUtils {
    private const val NETWORK_TAG = "network"
    private const val USER_TAG = "user"
    private const val EVENT_TAG = "event"
    private const val LIFECYCLE_TAG = "lifecycle"
    
    private const val MAX_LOG_LENGTH = 500
    

    fun logNetworkRequest(endpoint: String, method: String, params: Map<String, Any?> = emptyMap()) {
        if (shouldLog()) {
            Logger.d("$NETWORK_TAG: Request → $method $endpoint | Params: $params")
        }
    }
    
    fun logNetworkResponse(endpoint: String, method: String, responseCode: Int, responseBody: String? = null) {
        if (shouldLog()) {
            val truncatedBody = responseBody?.let {
                if (it.length > MAX_LOG_LENGTH) {
                    "${it.take(MAX_LOG_LENGTH)}... [truncated, total length: ${it.length}]"
                } else {
                    it
                }
            }
            
            Logger.d("$NETWORK_TAG: Response ← $method $endpoint | Code: $responseCode${truncatedBody?.let { " | Body: $it" } ?: ""}")
        }
    }
    

    fun logNetworkError(endpoint: String, method: String, errorMessage: String, throwable: Throwable? = null) {
        if (shouldLog()) {
            if (throwable != null) {
                Logger.e(throwable, "$NETWORK_TAG: Error ⚠ $method $endpoint | $errorMessage")
            } else {
                Logger.e("$NETWORK_TAG: Error ⚠ $method $endpoint | $errorMessage")
            }
        }
    }
    
    fun logUserEvent(event: String, userId: Long? = null, details: String? = null) {
        if (shouldLog()) {
            Logger.d("$USER_TAG: $event${userId?.let { " | UserID: $it" } ?: ""}${details?.let { " | $it" } ?: ""}")
        }
    }
    
    fun logAppEvent(event: String, details: String? = null) {
        if (shouldLog()) {
            Logger.d("$EVENT_TAG: $event${details?.let { " | $it" } ?: ""}")
        }
    }
    
    fun logLifecycleEvent(component: String, event: String, details: String? = null) {
        if (shouldLog()) {
            Logger.d("$LIFECYCLE_TAG: $component | $event${details?.let { " | $it" } ?: ""}")
        }
    }
    

    private fun shouldLog(): Boolean {
        return BuildConfig.DEBUG
    }
}