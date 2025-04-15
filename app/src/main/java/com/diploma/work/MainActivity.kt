package com.diploma.work

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.diploma.work.ui.navigation.AppNavigation
import com.diploma.work.ui.theme.DiplomaWorkTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DiplomaWorkTheme {
                AppNavigation()
            }
        }
    }
}