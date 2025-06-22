package com.diploma.work.utils

object Constants {
    
    object Network {
        const val AUTH_SERVER_HOST = "10.0.2.2"
        const val AUTH_SERVER_PORT = 50051
        const val USER_INFO_SERVER_HOST = "10.0.2.2"
        const val USER_INFO_SERVER_PORT = 50052
        const val TESTS_SERVER_HOST = "10.0.2.2"
        const val TESTS_SERVER_PORT = 50053
        const val ARTICLES_SERVER_HOST = "10.0.2.2"
        const val ARTICLES_SERVER_PORT = 50054
        
        const val CONNECTION_TIMEOUT_SECONDS = 30L
        const val READ_TIMEOUT_SECONDS = 30L
        const val WRITE_TIMEOUT_SECONDS = 30L
    }
    
    object PrefsKeys {
        const val APP_SESSION = "app_session"
        const val ACCESS_TOKEN = "access_token"
        const val USER_ID = "user_id"
        const val USERNAME = "username"
        const val AVATAR_URL = "avatar_url"
        const val AVATAR_DATA = "avatar_data"
        const val TEST_SESSIONS_PREFS = "test_sessions_prefs"
    }
    
    object UI {
        const val DEFAULT_AVATAR_SIZE = 120
        const val AVATAR_BORDER_WIDTH = 2
        const val ANIMATION_DURATION_MS = 300
        const val DEBOUNCE_DELAY_MS = 500L
    }
    
    object Business {
        const val MIN_ARTICLES_PER_DAY = 1
        const val MAX_ARTICLES_PER_DAY = 20
        const val DEFAULT_PAGE_SIZE = 20
        const val AVATAR_COMPRESSION_QUALITY = 80
        const val GENERATED_AVATAR_SIZE = 200
    }
    
    object ErrorMessages {
        const val GENERIC_ERROR = "An error occurred"
        const val NETWORK_ERROR = "Network connection error"
        const val AUTH_ERROR = "Authentication failed"
        const val INVALID_EMAIL = "Invalid email format"
        const val PASSWORD_MISMATCH = "Passwords do not match"
        const val USER_NOT_FOUND = "User not found"
        const val AVATAR_UPLOAD_FAILED = "Failed to upload avatar"
    }
}
