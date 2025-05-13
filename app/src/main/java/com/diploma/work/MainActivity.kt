package com.diploma.work

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.diploma.work.data.AppSession
import com.diploma.work.ui.navigation.AppNavigation
import com.diploma.work.ui.theme.DiplomaWorkTheme
import com.diploma.work.ui.theme.ThemeManager
import com.orhanobut.logger.Logger
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val tag = "MainActivity"
    
    @Inject
    lateinit var session: AppSession

    @Inject
    lateinit var themeManager: ThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d("$tag: onCreate")
        
        val isLoggedIn = session.getToken() != null
        Logger.d("$tag: User login status: ${if (isLoggedIn) "Logged in" else "Not logged in"}")
        
        setContent {
            Logger.d("$tag: Setting up Compose content")
            DiplomaWorkTheme(
                themeManager = themeManager,
                content = {
                    AppNavigation(session = session, themeManager)
                }
            )
        }
    }
    
    override fun onStart() {
        super.onStart()
        Logger.d("$tag: onStart")
    }
    
    override fun onResume() {
        super.onResume()
        Logger.d("$tag: onResume")
    }
    
    override fun onPause() {
        super.onPause()
        Logger.d("$tag: onPause")
    }
    
    override fun onStop() {
        super.onStop()
        Logger.d("$tag: onStop")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Logger.d("$tag: onDestroy")
    }
    
    override fun onRestart() {
        super.onRestart()
        Logger.d("$tag: onRestart")
    }
}