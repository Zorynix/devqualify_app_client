package com.diploma.work

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.diploma.work.data.AppSession
import com.diploma.work.ui.navigation.AppNavigation
import com.diploma.work.ui.theme.DiplomaWorkTheme
import com.diploma.work.ui.theme.ThemeManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var session: AppSession

    @Inject
    lateinit var themeManager: ThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DiplomaWorkTheme(themeManager = themeManager) {
                AppNavigation(session = session)
            }
        }
    }
}