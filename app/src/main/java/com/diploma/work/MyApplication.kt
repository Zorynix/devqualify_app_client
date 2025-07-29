package com.diploma.work

import android.app.Application
import android.os.Build
import android.util.Log
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.FormatStrategy
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        val formatStrategy: FormatStrategy = PrettyFormatStrategy.newBuilder()
            .showThreadInfo(true)
            .methodCount(2)
            .methodOffset(5)
            .tag("DiplomaWork")
            .build()
            
        Logger.addLogAdapter(object : AndroidLogAdapter(formatStrategy) {
            override fun isLoggable(priority: Int, tag: String?): Boolean {
                return true
            }
        })
        
        Log.d("DiplomaWork", "Application started (Android Log)")
        Logger.d("Application started (Logger)")
        logDeviceInfo()
        logAppVersion()
    }
    
    private fun logDeviceInfo() {
        Logger.i("Device Info: " +
                "Model: ${Build.MODEL}, " +
                "Brand: ${Build.BRAND}, " +
                "Manufacturer: ${Build.MANUFACTURER}, " +
                "Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
    }
    
    private fun logAppVersion() {
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            val versionName = packageInfo.versionName
            val versionCode =
                packageInfo.longVersionCode

            Logger.i("App Version: $versionName ($versionCode)")
        } catch (e: Exception) {
            Logger.e("Failed to get app version: ${e.message}")
        }
    }
    
    override fun onLowMemory() {
        super.onLowMemory()
        Logger.w("System is running low on memory")
    }
    
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        
        when (level) {
            TRIM_MEMORY_COMPLETE -> Logger.w("Memory Trim: TRIM_MEMORY_COMPLETE")
            TRIM_MEMORY_MODERATE -> Logger.w("Memory Trim: TRIM_MEMORY_MODERATE")
            TRIM_MEMORY_BACKGROUND -> Logger.w("Memory Trim: TRIM_MEMORY_BACKGROUND")
            TRIM_MEMORY_UI_HIDDEN -> Logger.d("Memory Trim: TRIM_MEMORY_UI_HIDDEN")
            TRIM_MEMORY_RUNNING_CRITICAL -> Logger.w("Memory Trim: TRIM_MEMORY_RUNNING_CRITICAL")
            TRIM_MEMORY_RUNNING_LOW -> Logger.w("Memory Trim: TRIM_MEMORY_RUNNING_LOW")
            TRIM_MEMORY_RUNNING_MODERATE -> Logger.d("Memory Trim: TRIM_MEMORY_RUNNING_MODERATE")
        }
    }
}
