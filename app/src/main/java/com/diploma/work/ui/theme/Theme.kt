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
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.platform.LocalContext

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
    val surfaceContainerHigh: Color = Color.Unspecified,
    val surfaceContainerLow: Color = Color.Unspecified
)

@Immutable
data class ColorScheme(
    val primary: Color,
    val primaryActive: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color = Color.Unspecified,
    val onSecondary: Color = Color.Unspecified,
    val secondaryContainer: Color = Color.Unspecified,
    val onSecondaryContainer: Color = Color.Unspecified,
    val tertiary: Color = Color.Unspecified,
    val onTertiary: Color = Color.Unspecified,
    val tertiaryContainer: Color = Color.Unspecified,
    val onTertiaryContainer: Color = Color.Unspecified,
    val background: Color,
    val onBackground: Color,
    val onBackgroundPositive: Color,
    val onBackgroundHint: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color = Color.Unspecified,
    val onSurfaceVariant: Color = Color.Unspecified,
    val inverseSurface: Color = Color.Unspecified,
    val inverseOnSurface: Color = Color.Unspecified,
    val inversePrimary: Color = Color.Unspecified,
    val surfaceContainerLowest: Color = Color.Unspecified,
    val surfaceContainerLow: Color = Color.Unspecified,
    val surfaceContainer: Color = Color.Unspecified,
    val surfaceContainerHigh: Color = Color.Unspecified,
    val surfaceContainerHighest: Color = Color.Unspecified,
    val error: Color,
    val onError: Color,
    val errorContainer: Color = Color.Unspecified,
    val onErrorContainer: Color = Color.Unspecified,
    val outline: Color,
    val outlineActive: Color,
    val outlineDanger: Color,
    val outlineVariant: Color = Color.Unspecified,
    val scrim: Color = Color.Unspecified,
    val backgroundBox: Color,
    val isDark: Boolean
) {
    val material: androidx.compose.material3.ColorScheme
        get() = if (isDark) {
            darkColorScheme(
                primary = primary,
                onPrimary = onPrimary,
                primaryContainer = primaryContainer,
                onPrimaryContainer = onPrimaryContainer,
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
                primaryContainer = primaryContainer,
                onPrimaryContainer = onPrimaryContainer,
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
        primary = primaryLightHighContrast,
        onPrimaryContainer = onPrimaryContainerLightHighContrast,
        primaryContainer = primaryContainerLightHighContrast,
        primaryActive = primaryContainerLightHighContrast,
        onPrimary = onPrimaryLightHighContrast,
        background = backgroundLightHighContrast,
        onBackground = onBackgroundLightHighContrast,
        onBackgroundPositive = secondaryLightHighContrast,
        onBackgroundHint = outlineLightHighContrast,
        surface = surfaceLightHighContrast,
        onSurface = onSurfaceLightHighContrast,
        error = errorLightHighContrast,
        onError = onErrorLightHighContrast,
        outline = outlineLightHighContrast,
        outlineActive = outlineVariantLightHighContrast,
        outlineDanger = errorContainerLightHighContrast,
        backgroundBox = surfaceContainerLightHighContrast,
        isDark = false
    )

    val Dark = ColorScheme(
        primary = primaryDarkHighContrast,
        primaryContainer = primaryContainerDarkHighContrast,
        onPrimaryContainer = onPrimaryContainerDarkHighContrast,
        primaryActive = primaryContainerDarkHighContrast,
        onPrimary = onPrimaryDarkHighContrast,
        background = backgroundDarkHighContrast,
        onBackground = onBackgroundDarkHighContrast,
        onBackgroundPositive = secondaryDarkHighContrast,
        onBackgroundHint = outlineDarkHighContrast,
        surface = surfaceDarkHighContrast,
        onSurface = onSurfaceDarkHighContrast,
        error = errorDarkHighContrast,
        onError = onErrorDarkHighContrast,
        outline = outlineDarkHighContrast,
        outlineActive = tertiaryContainerDarkHighContrast,
        outlineDanger = errorContainerDarkHighContrast,
        backgroundBox = surfaceContainerDarkHighContrast,
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

@Immutable
data class ColorFamily(
    val color: Color,
    val onColor: Color,
    val colorContainer: Color,
    val onColorContainer: Color
)

val unspecified_scheme = ColorFamily(
    Color.Unspecified, Color.Unspecified, Color.Unspecified, Color.Unspecified
)

@Composable
fun DiplomaWorkTheme(
    themeManager: ThemeManager,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
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