package com.diploma.work.utils

import com.orhanobut.logger.BuildConfig
import com.orhanobut.logger.Logger

object SecureLogger {


    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Logger.d("$tag: $message")
        }
    }


    fun i(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Logger.i("$tag: $message")
        }
    }


    fun w(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Logger.w("$tag: $message")
        }
    }


    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            if (throwable != null) {
                Logger.e(throwable, "$tag: $message")
            } else {
                Logger.e("$tag: $message")
            }
        } else {
            // In release, log only generic error without sensitive details
            Logger.e("$tag: An error occurred")
        }
    }

    fun v(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Logger.v("$tag: $message")
        }
    }


    fun sensitive(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Logger.d("$tag [SENSITIVE]: $message")
        }
    }


    fun network(tag: String, method: String, url: String) {
        if (BuildConfig.DEBUG) {
            Logger.d("$tag: $method $url")
        }
    }


    fun maskToken(token: String?): String {
        if (token == null) return "null"
        if (token.length <= 10) return "***"
        return "${token.take(4)}...${token.takeLast(4)}"
    }


    fun maskEmail(email: String?): String {
        if (email == null) return "null"
        val atIndex = email.indexOf('@')
        if (atIndex <= 1) return "***@***"
        return "${email.first()}***@${email.substringAfter('@')}"
    }


    fun maskUserId(userId: Long?): String {
        if (userId == null) return "null"
        return "user_***${userId.toString().takeLast(2)}"
    }
}
