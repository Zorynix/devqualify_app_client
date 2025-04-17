package com.diploma.work.ui.theme

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.GrayLight
import com.example.ui.theme.GreenLight
import com.example.ui.theme.NeutralLight
import com.example.ui.theme.PurpleDark
import com.example.ui.theme.PurpleLight
import com.example.ui.theme.PurpleMedium
import com.example.ui.theme.RedLight
import com.example.ui.theme.White

sealed class AppThemeType {
    object Light : AppThemeType()
    object Dark : AppThemeType()
}

object Theme {
    val extendedColorScheme: ExtendedColorScheme
        @Composable get() = LocalExtendedColors.current
}

@Immutable
data class ExtendedColorScheme(
    val primaryActive: Color,
    val onBackgroundPositive: Color,
    val onBackgroundHint: Color,
    val outlineActive: Color,
    val outlineDanger: Color,
    val backgroundBox: Color,
)

@Immutable
data class ColorScheme(
    val primary: Color,
    val primaryActive: Color,
    val onPrimary: Color,
    val background: Color,
    val onBackground: Color,
    val onBackgroundPositive: Color,
    val onBackgroundHint: Color,
    val surface: Color,
    val onSurface: Color,
    val error: Color,
    val onError: Color,
    val outline: Color,
    val outlineActive: Color,
    val outlineDanger: Color,
    val backgroundBox: Color,
    val isDark: Boolean
) {
    val material: androidx.compose.material3.ColorScheme
        get() = if (isDark) {
            darkColorScheme(
                primary = primary,
                onPrimary = onPrimary,
                background = background,
                onBackground = onBackground,
                surface = surface,
                onSurface = onSurface,
                error = error,
                onError = onError,
                outline = outline
            )
        } else {
            lightColorScheme(
                primary = primary,
                onPrimary = onPrimary,
                background = background,
                onBackground = onBackground,
                surface = surface,
                onSurface = onSurface,
                error = error,
                onError = onError,
                outline = outline
            )
        }

    val extended: ExtendedColorScheme
        get() = ExtendedColorScheme(
            primaryActive = primaryActive,
            onBackgroundPositive = onBackgroundPositive,
            onBackgroundHint = onBackgroundHint,
            outlineActive = outlineActive,
            outlineDanger = outlineDanger,
            backgroundBox = backgroundBox,
        )
}

object AppTheme {
    val Light = ColorScheme(
        primary = PurpleMedium,
        primaryActive = PurpleDark,
        onPrimary = White,
        background = NeutralLight,
        onBackground = PurpleDark,
        onBackgroundPositive = GreenLight,
        onBackgroundHint = GrayLight,
        surface = White,
        onSurface = PurpleDark,
        error = RedLight,
        onError = White,
        outline = GrayLight,
        outlineActive = PurpleMedium,
        outlineDanger = RedLight,
        backgroundBox = PurpleLight,
        isDark = false
    )

    val Dark = ColorScheme(
        primary = PurpleMedium,
        primaryActive = PurpleLight,
        onPrimary = White,
        background = PurpleDark,
        onBackground = NeutralLight,
        onBackgroundPositive = GreenLight,
        onBackgroundHint = GrayLight,
        surface = PurpleDark,
        onSurface = NeutralLight,
        error = RedLight,
        onError = White,
        outline = PurpleLight,
        outlineActive = PurpleMedium,
        outlineDanger = RedLight,
        backgroundBox = GrayLight,
        isDark = true
    )
}

@Suppress("CompositionLocalAllowlist")
val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColorScheme(
        primaryActive = Color.Unspecified,
        onBackgroundPositive = Color.Unspecified,
        onBackgroundHint = Color.Unspecified,
        outlineActive = Color.Unspecified,
        outlineDanger = Color.Unspecified,
        backgroundBox = Color.Unspecified,
    )
}

@Composable
fun rememberCustomRippleIndicator(): IndicationNodeFactory {
    return ripple(color = MaterialTheme.colorScheme.onBackground, bounded = true)
}

@Composable
fun DiplomaWorkTheme(
    themeManager: ThemeManager,
    content: @Composable () -> Unit,
) {
    val theme by themeManager.currentTheme.collectAsState()
    val colorScheme = when (theme) {
        AppThemeType.Dark -> AppTheme.Dark
        AppThemeType.Light -> AppTheme.Light
    }

    MaterialTheme(
        colorScheme = colorScheme.material,
        typography = Typography,
    ) {
        CompositionLocalProvider(
            LocalExtendedColors provides colorScheme.extended,
            LocalIndication provides rememberCustomRippleIndicator(),
            content = content
        )
    }
}