package com.diploma.work.ui.theme

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
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
import androidx.compose.ui.unit.dp

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
    val surfaceContainerLow: Color = Color.Unspecified,
    val glassBackground: Color = Color.Unspecified,
    val glassBorder: Color = Color.Unspecified,
    val glassHighlight: Color = Color.Unspecified
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
                secondary = secondary,
                onSecondary = onSecondary,
                secondaryContainer = secondaryContainer,
                onSecondaryContainer = onSecondaryContainer,
                tertiary = tertiary,
                onTertiary = onTertiary,
                tertiaryContainer = tertiaryContainer,
                onTertiaryContainer = onTertiaryContainer,
                background = background,
                onBackground = onBackground,
                surface = surface,
                onSurface = onSurface,
                surfaceVariant = surfaceVariant,
                onSurfaceVariant = onSurfaceVariant,
                error = error,
                onError = onError,
                errorContainer = errorContainer,
                onErrorContainer = onErrorContainer,
                outline = outline,
                outlineVariant = outlineVariant,
                surfaceContainerLowest = surfaceContainerLowest,
                surfaceContainerLow = surfaceContainerLow,
                surfaceContainer = surfaceContainer,
                surfaceContainerHigh = surfaceContainerHigh,
                surfaceContainerHighest = surfaceContainerHighest,
            )
        } else {
            lightColorScheme(
                primary = primary,
                onPrimary = onPrimary,
                primaryContainer = primaryContainer,
                onPrimaryContainer = onPrimaryContainer,
                secondary = secondary,
                onSecondary = onSecondary,
                secondaryContainer = secondaryContainer,
                onSecondaryContainer = onSecondaryContainer,
                tertiary = tertiary,
                onTertiary = onTertiary,
                tertiaryContainer = tertiaryContainer,
                onTertiaryContainer = onTertiaryContainer,
                background = background,
                onBackground = onBackground,
                surface = surface,
                onSurface = onSurface,
                surfaceVariant = surfaceVariant,
                onSurfaceVariant = onSurfaceVariant,
                error = error,
                onError = onError,
                errorContainer = errorContainer,
                onErrorContainer = onErrorContainer,
                outline = outline,
                outlineVariant = outlineVariant,
                surfaceContainerLowest = surfaceContainerLowest,
                surfaceContainerLow = surfaceContainerLow,
                surfaceContainer = surfaceContainer,
                surfaceContainerHigh = surfaceContainerHigh,
                surfaceContainerHighest = surfaceContainerHighest,
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
            glassBackground = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.72f),
            glassBorder = if (isDark) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.45f),
            glassHighlight = if (isDark) Color.White.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.35f),
        )
}

object AppTheme {
    val Light = ColorScheme(
        primary = primaryLightHighContrast,
        onPrimaryContainer = onPrimaryContainerLightHighContrast,
        primaryContainer = primaryContainerLightHighContrast,
        primaryActive = primaryContainerLightHighContrast,
        onPrimary = onPrimaryLightHighContrast,
        secondary = secondaryLight,
        onSecondary = onSecondaryLight,
        secondaryContainer = secondaryContainerLight,
        onSecondaryContainer = onSecondaryContainerLight,
        tertiary = tertiaryLight,
        onTertiary = onTertiaryLight,
        tertiaryContainer = tertiaryContainerLight,
        onTertiaryContainer = onTertiaryContainerLight,
        background = backgroundLightHighContrast,
        onBackground = onBackgroundLightHighContrast,
        onBackgroundPositive = secondaryLightHighContrast,
        onBackgroundHint = outlineLightHighContrast,
        surface = surfaceLightHighContrast,
        onSurface = onSurfaceLightHighContrast,
        surfaceVariant = surfaceVariantLight,
        onSurfaceVariant = onSurfaceVariantLight,
        error = errorLightHighContrast,
        onError = onErrorLightHighContrast,
        errorContainer = errorContainerLight,
        onErrorContainer = onErrorContainerLight,
        outline = outlineLightHighContrast,
        outlineActive = outlineVariantLightHighContrast,
        outlineDanger = errorContainerLightHighContrast,
        outlineVariant = outlineVariantLight,
        backgroundBox = surfaceContainerLightHighContrast,
        surfaceContainerLowest = surfaceContainerLowestLight,
        surfaceContainerLow = surfaceContainerLowLight,
        surfaceContainer = surfaceContainerLight,
        surfaceContainerHigh = surfaceContainerHighLight,
        surfaceContainerHighest = surfaceContainerHighestLight,
        isDark = false
    )

    val Dark = ColorScheme(
        primary = primaryDarkHighContrast,
        primaryContainer = primaryContainerDarkHighContrast,
        onPrimaryContainer = onPrimaryContainerDarkHighContrast,
        primaryActive = primaryContainerDarkHighContrast,
        onPrimary = onPrimaryDarkHighContrast,
        secondary = secondaryDark,
        onSecondary = onSecondaryDark,
        secondaryContainer = secondaryContainerDark,
        onSecondaryContainer = onSecondaryContainerDark,
        tertiary = tertiaryDark,
        onTertiary = onTertiaryDark,
        tertiaryContainer = tertiaryContainerDark,
        onTertiaryContainer = onTertiaryContainerDark,
        background = backgroundDarkHighContrast,
        onBackground = onBackgroundDarkHighContrast,
        onBackgroundPositive = secondaryDarkHighContrast,
        onBackgroundHint = outlineDarkHighContrast,
        surface = surfaceDarkHighContrast,
        onSurface = onSurfaceDarkHighContrast,
        surfaceVariant = surfaceVariantDark,
        onSurfaceVariant = onSurfaceVariantDark,
        error = errorDarkHighContrast,
        onError = onErrorDarkHighContrast,
        errorContainer = errorContainerDark,
        onErrorContainer = onErrorContainerDark,
        outline = outlineDarkHighContrast,
        outlineActive = tertiaryContainerDarkHighContrast,
        outlineDanger = errorContainerDarkHighContrast,
        outlineVariant = outlineVariantDark,
        backgroundBox = surfaceContainerDarkHighContrast,
        surfaceContainerLowest = surfaceContainerLowestDark,
        surfaceContainerLow = surfaceContainerLowDark,
        surfaceContainer = surfaceContainerDark,
        surfaceContainerHigh = surfaceContainerHighDark,
        surfaceContainerHighest = surfaceContainerHighestDark,
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

val GlassShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
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
        shapes = GlassShapes,
    ) {
        CompositionLocalProvider(
            LocalExtendedColors provides colorScheme.extended,
            LocalIndication provides rememberCustomRippleIndicator(),
            content = content
        )
    }
}
