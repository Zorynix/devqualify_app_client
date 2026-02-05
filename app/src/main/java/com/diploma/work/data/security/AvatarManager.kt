package com.diploma.work.data.security

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.orhanobut.logger.BuildConfig
import com.orhanobut.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AvatarManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val AVATAR_FILENAME = "user_avatar.jpg"
        private const val AVATAR_QUALITY = 80
        private const val TAG = "AvatarManager"
    }

    private val _avatarUrlFlow = MutableStateFlow<String?>(null)
    val avatarUrlFlow: StateFlow<String?> = _avatarUrlFlow

    private val avatarFile: File
        get() = File(context.filesDir, AVATAR_FILENAME)

    init {
        _avatarUrlFlow.value = if (avatarFile.exists()) avatarFile.absolutePath else null
    }


    suspend fun storeAvatarFromUri(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    storeAvatarBitmap(bitmap)
                    bitmap.recycle()
                    true
                } else {
                    logSecure("Failed to decode bitmap from URI")
                    false
                }
            } ?: run {
                logSecure("Failed to open input stream for URI")
                false
            }
        } catch (e: Exception) {
            logSecure("Error storing avatar from URI: ${e.message}")
            false
        }
    }


    suspend fun storeAvatarBitmap(bitmap: Bitmap): Boolean = withContext(Dispatchers.IO) {
        try {
            FileOutputStream(avatarFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, AVATAR_QUALITY, outputStream)
            }
            _avatarUrlFlow.value = avatarFile.absolutePath
            logSecure("Avatar stored successfully")
            true
        } catch (e: Exception) {
            logSecure("Error storing avatar bitmap: ${e.message}")
            false
        }
    }


    suspend fun getAvatarBitmap(): Bitmap? = withContext(Dispatchers.IO) {
        try {
            if (avatarFile.exists()) {
                BitmapFactory.decodeFile(avatarFile.absolutePath)
            } else {
                null
            }
        } catch (e: Exception) {
            logSecure("Error loading avatar bitmap: ${e.message}")
            null
        }
    }


    fun getAvatarPath(): String? {
        return if (avatarFile.exists()) avatarFile.absolutePath else null
    }


    fun hasAvatar(): Boolean = avatarFile.exists()


    suspend fun deleteAvatar(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (avatarFile.exists()) {
                val deleted = avatarFile.delete()
                if (deleted) {
                    _avatarUrlFlow.value = null
                    logSecure("Avatar deleted successfully")
                }
                deleted
            } else {
                true
            }
        } catch (e: Exception) {
            logSecure("Error deleting avatar: ${e.message}")
            false
        }
    }

    fun refreshAvatarUrl() {
        _avatarUrlFlow.value = getAvatarPath()
    }

    private fun logSecure(message: String) {
        if (BuildConfig.DEBUG) {
            Logger.d("$TAG: $message")
        }
    }
}
