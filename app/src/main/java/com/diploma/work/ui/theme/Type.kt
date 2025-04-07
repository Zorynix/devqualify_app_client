package com.diploma.work.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.Font
import com.diploma.work.R

val displayFontFamily =
    FontFamily(
        Font(R.font.poppins_regular, FontWeight.Normal),
        Font(R.font.poppins_medium, FontWeight.Medium),
        Font(R.font.poppins_semibold, FontWeight.SemiBold),
        Font(R.font.poppins_bold, FontWeight.Bold),
        Font(R.font.poppins_thin, FontWeight.Thin),
        Font(R.font.poppins_light, FontWeight.Light),
        Font(R.font.poppins_extralight, FontWeight.ExtraLight),
        Font(R.font.poppins_black, FontWeight.Black),
    )
val baseline = Typography()

// Set of Material typography styles to start with
val Typography =
    Typography(
        displayLarge = baseline.displayLarge.copy(fontFamily = displayFontFamily),
        displayMedium = baseline.displayMedium.copy(fontFamily = displayFontFamily),
        displaySmall = baseline.displaySmall.copy(fontFamily = displayFontFamily),
        headlineLarge = baseline.headlineLarge.copy(fontFamily = displayFontFamily),
        headlineMedium = baseline.headlineMedium.copy(fontFamily = displayFontFamily),
        headlineSmall = baseline.headlineSmall.copy(fontFamily = displayFontFamily),
        titleLarge = baseline.titleLarge.copy(fontFamily = displayFontFamily),
        titleMedium = baseline.titleMedium.copy(fontFamily = displayFontFamily),
        titleSmall = baseline.titleSmall.copy(fontFamily = displayFontFamily),
        bodyLarge = baseline.bodyLarge.copy(fontFamily = displayFontFamily),
        bodyMedium = baseline.bodyMedium.copy(fontFamily = displayFontFamily),
        bodySmall = baseline.bodySmall.copy(fontFamily = displayFontFamily),
        labelLarge = baseline.labelLarge.copy(fontFamily = displayFontFamily),
        labelMedium = baseline.labelMedium.copy(fontFamily = displayFontFamily),
        labelSmall = baseline.labelSmall.copy(fontFamily = displayFontFamily),
    )
