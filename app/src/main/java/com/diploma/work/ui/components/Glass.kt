package com.diploma.work.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object GlassTokens {
    val CardShape = RoundedCornerShape(24.dp)
    val DialogShape = RoundedCornerShape(28.dp)
    val ChipShape = RoundedCornerShape(20.dp)
    val ButtonShape = RoundedCornerShape(16.dp)
    val SmallShape = RoundedCornerShape(14.dp)
    val TagShape = RoundedCornerShape(10.dp)

    val CardElevation = 0.dp
    val BorderWidth = 0.75.dp
    val ThinBorderWidth = 0.5.dp
}

@Composable
private fun isDarkTheme(): Boolean {
    return MaterialTheme.colorScheme.background.luminance() < 0.5f
}

@Composable
fun glassBackground(): Color {
    return if (isDarkTheme()) Color.White.copy(alpha = 0.06f)
    else Color.White.copy(alpha = 0.72f)
}

@Composable
fun glassDialogBackground(): Color {
    return if (isDarkTheme()) Color(0xFF1E1A2E).copy(alpha = 0.92f)
    else Color.White.copy(alpha = 0.95f)
}

@Composable
fun glassBorder(): Color {
    return if (isDarkTheme()) Color.White.copy(alpha = 0.10f)
    else Color.White.copy(alpha = 0.45f)
}

@Composable
fun glassHighlight(): Color {
    return if (isDarkTheme()) Color.White.copy(alpha = 0.04f)
    else Color.White.copy(alpha = 0.35f)
}

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = GlassTokens.CardShape,
    backgroundColor: Color = glassBackground(),
    borderColor: Color = glassBorder(),
    borderWidth: Dp = GlassTokens.BorderWidth,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = backgroundColor,
        border = BorderStroke(borderWidth, borderColor),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        content = content
    )
}

@Composable
fun GlassDialogSurface(
    modifier: Modifier = Modifier,
    shape: Shape = GlassTokens.DialogShape,
    content: @Composable () -> Unit
) {
    GlassSurface(
        modifier = modifier,
        shape = shape,
        backgroundColor = glassDialogBackground(),
        content = content
    )
}

@Composable
fun GlassCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = GlassTokens.CardShape,
    backgroundColor: Color = glassBackground(),
    borderColor: Color = glassBorder(),
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = shape,
        color = backgroundColor,
        border = BorderStroke(GlassTokens.BorderWidth, borderColor),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        content = content
    )
}

@Composable
fun GradientScaffoldBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isDarkTheme()

    val gradientColors = if (isDark) {
        listOf(
            Color(0xFF1A1025),
            Color(0xFF12101A),
            Color(0xFF0F1520)
        )
    } else {
        listOf(
            Color(0xFFF5EEFF),
            Color(0xFFFFF7FD),
            Color(0xFFEEF4FF)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = gradientColors,
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            ),
        content = content
    )
}

@Composable
fun GlassNavigationBarBackground(): Color {
    return if (isDarkTheme()) Color(0xFF1A1025).copy(alpha = 0.85f)
    else Color.White.copy(alpha = 0.78f)
}

@Composable
fun AccentGlassSurface(
    accentColors: List<Color>,
    modifier: Modifier = Modifier,
    shape: Shape = GlassTokens.CardShape,
    content: @Composable () -> Unit
) {
    val isDark = isDarkTheme()
    val overlayAlpha = if (isDark) 0.25f else 0.85f

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = accentColors.map { it.copy(alpha = overlayAlpha) }
                ),
                shape = shape
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (isDark) 0.08f else 0.25f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = size.height * 0.5f
                        )
                    )
                }
        ) {
            content()
        }
    }
}
