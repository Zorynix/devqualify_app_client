package com.diploma.work

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.diploma.work.data.AppSession
import com.diploma.work.ui.navigation.AppNavigation
import com.diploma.work.ui.theme.DiplomaWorkTheme
import com.diploma.work.ui.theme.ThemeManager
import com.diploma.work.utils.SecureLogger
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    @Inject
    lateinit var session: AppSession

    @Inject
    lateinit var themeManager: ThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        
        SecureLogger.d(TAG, "onCreate")
        
        val isLoggedIn = session.getToken() != null
        SecureLogger.d(TAG, "User logged in: $isLoggedIn")
        
        setContent {
            DiplomaWorkTheme(
                themeManager = themeManager,
                content = {
                    AppNavigation(session = session, themeManager)
                }
            )
        }
    }
}