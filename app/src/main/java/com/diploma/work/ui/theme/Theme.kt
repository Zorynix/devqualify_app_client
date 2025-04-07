package com.diploma.work.ui.theme

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

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
) {
    val material: androidx.compose.material3.ColorScheme
        get() =
            lightColorScheme(
                primary = primary,
                onPrimary = onPrimary,
                background = background,
                onBackground = onBackground,
                surface = surface,
                onSurface = onSurface,
                error = error,
                onError = onError,
                outline = outline)

    val extended: ExtendedColorScheme
        get() =
            ExtendedColorScheme(
                primaryActive = primaryActive,
                onBackgroundPositive = onBackgroundPositive,
                onBackgroundHint = onBackgroundHint,
                outlineActive = outlineActive,
                outlineDanger = outlineDanger,
                backgroundBox = backgroundBox,
            )
}

object AppTheme {
    val Light =
        ColorScheme(
            primary = Color(0xFFFE724C),
            primaryActive = Color(0xFFfe3e0a),
            onPrimary = Color.White,
            background = Color.White,
            onBackground = Color.Black,
            onBackgroundPositive = Color(0xFF578f40),
            onBackgroundHint = Color(0xFF8a8a8a),
            surface = Color.White,
            onSurface = Color.Black,
            error = Color(0xFFad2b2b),
            onError = Color.White,
            outline = Color(0xFFbdbdbd),
            outlineActive = Color(0xFFbdbdbd),
            outlineDanger = Color(0xFFad2b2b),
            backgroundBox = Color(0xFFEDE5E3),
        )
    val Dark =
        ColorScheme(
            primary = Color(0xFFFE724C),
            primaryActive = Color(0xFFFFAB94),
            onPrimary = Color.White,
            background = Color(0xFF191917),
            onBackground = Color.White,
            onBackgroundPositive = Color(0xFF578f40),
            onBackgroundHint = Color(0xFF8a8a8a),
            surface = Color(0xFF121212),
            onSurface = Color.White,
            error = Color(0xFFad2b2b),
            onError = Color.White,
            outline = Color(0xFFbdbdbd),
            outlineActive = Color(0xFFbdbdbd),
            outlineDanger = Color(0xFFad2b2b),
            backgroundBox = Color(0xFF2F2F2F),
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
fun CourseWorkTheme(
    theme: AppThemeType = if (isSystemInDarkTheme()) AppThemeType.Dark else AppThemeType.Light,
    content: @Composable () -> Unit,
) {
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
