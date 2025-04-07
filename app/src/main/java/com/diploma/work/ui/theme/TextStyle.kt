package com.diploma.work.ui.theme

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

sealed interface TextStyle {

    val value: androidx.compose.ui.text.TextStyle

    data object titleMedium: TextStyle {
        override val value: androidx.compose.ui.text.TextStyle
            get() = androidx.compose.ui.text.TextStyle(
                fontFamily = displayFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight(500),
                lineHeight = 22.sp)
    }

    data object bodyMedium: TextStyle {
        override val value: androidx.compose.ui.text.TextStyle
            get() = androidx.compose.ui.text.TextStyle(
                fontFamily = displayFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight(400),
                lineHeight = 20.sp)
    }

    data object bodySmall: TextStyle {
        override val value: androidx.compose.ui.text.TextStyle
            get() = androidx.compose.ui.text.TextStyle(
                fontFamily = displayFontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight(400),
                lineHeight = 20.sp)
    }

    data object categoryName: TextStyle {
        override val value: androidx.compose.ui.text.TextStyle
            get() = androidx.compose.ui.text.TextStyle(
                fontFamily = displayFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight(400),
                lineHeight = 16.sp)
    }

    data object titleLarge: TextStyle {
        override val value: androidx.compose.ui.text.TextStyle
            get() = androidx.compose.ui.text.TextStyle(
                fontFamily = displayFontFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight(500),
                lineHeight = 25.sp)
    }


    data object Paragraph : TextStyle {
        override val value: androidx.compose.ui.text.TextStyle
            get() =
                androidx.compose.ui.text.TextStyle(
                    fontFamily = displayFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight(400),
                    lineHeight = 20.sp)
    }

    data object titleSmall : TextStyle {
        override val value: androidx.compose.ui.text.TextStyle
            get() =
                androidx.compose.ui.text.TextStyle(
                    fontFamily = displayFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight(400),
                    lineHeight = 16.sp)
    }

    data object headlineLarge : TextStyle {
        override val value: androidx.compose.ui.text.TextStyle
            get() =
                androidx.compose.ui.text.TextStyle(
                    fontFamily = displayFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight(400),
                    lineHeight = 16.sp)
    }

}
