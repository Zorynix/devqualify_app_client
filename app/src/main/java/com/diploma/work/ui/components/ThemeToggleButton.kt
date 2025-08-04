package com.diploma.work.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.diploma.work.R
import com.diploma.work.ui.theme.AppThemeType
import com.diploma.work.ui.theme.ThemeManager
import dagger.hilt.android.EntryPointAccessors

@Composable
fun ThemeToggleButton(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val themeManager = EntryPointAccessors.fromApplication(
        context,
        ThemeManagerEntryPoint::class.java
    ).themeManager()
    
    val theme by themeManager.currentTheme.collectAsState()
    
    Surface(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable { themeManager.toggleTheme() },
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
    ) {
        Image(
            painter = painterResource(
                id = if (theme == AppThemeType.Dark) R.drawable.light_mode
                else R.drawable.dark_mode
            ),
            contentDescription = stringResource(R.string.toggle_theme),
            modifier = Modifier
                .padding(12.dp)
                .size(24.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
        )
    }
}

@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface ThemeManagerEntryPoint {
    fun themeManager(): ThemeManager
}
