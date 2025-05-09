package com.diploma.work.ui.theme

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

sealed interface TextStyle {

    val value: androidx.compose.ui.text.TextStyle

    
    data object DisplayLarge : TextStyle {
        override val value: androidx.compose.ui.text.TextStyle
            get() = androidx.compose.ui.text.TextStyle(
                fontFamily = displayFontFamily,
                fontSize = 57.sp,
                fontWeight = FontWeight(400),
                lineHeight = 64.sp,
                letterSpacing = (-0.25).sp
            )
    }
    
    data object DisplayMedium : TextStyle {
        override val value: androidx.compose.ui.text.TextStyle
            get() = androidx.compose.ui.text.TextStyle(
                fontFamily = displayFontFamily,
                fontSize = 45.sp,
                fontWeight = FontWeight(400),
                lineHeight = 52.sp,
                letterSpacing = 0.sp
            )
    }
    
    data object DisplaySmall : TextStyle {
        override val value: androidx.compose.ui.text.TextStyle
            get() = androidx.compose.ui.text.TextStyle(
                fontFamily = displayFontFamily,
                fontSize = 36.sp,
                fontWeight = FontWeight(400),
                lineHeight = 44.sp,
                letterSpacing = 0.sp
            )
    }
    
    
    data object HeadlineLarge : TextStyle {
        override val value: androidx.compose.ui.text.TextStyle
            get() = androidx.compose.ui.text.TextStyle(
                fontFamily = displayFontFamily,
                fontSize = 32.sp,
                fontWeight = FontWeight(700),
                lineHeight = 40.sp,
                letterSpacing = 0.sp
            )
    }
    
    data object HeadlineMedium : TextStyle {
        override val value: androidx.compose.ui.text.TextStyle
            get() = androidx.compose.ui.text.TextStyle(
                fontFamily = displayFontFamily,
                fontSize = 28.sp,
                fontWeight = FontWeight(600),
                lineHeight = 36.sp,
                letterSpacing = 0.sp
            )
    }
    
    data object HeadlineSmall : TextStyle {
        override val value: androidx.compose.ui.text.TextStyle
            get() = androidx.compose.ui.text.TextStyle(
                fontFamily = displayFontFamily,
                fontSize = 24.sp,
                fontWeight = FontWeight(500),
                lineHeight = 32.sp,
                letterSpacing = 0.sp
            )
    }

    
    data object TitleLarge : TextStyle {
        override val value: androidx.compose.ui.text.TextStyle
            get() = androidx.compose.ui.text.TextStyle(
                fontFamily = displayFontFamily,
                fontSize = 22.sp,
                fontWeight = FontWeight(500),
                lineHeight = 28.sp,
                letterSpacing = 0.sp
            )
    }
    
    data object TitleMedium : TextStyle {
        override val value: androidx.compose.ui.text.TextStyle
            get() = androidx.compose.ui.text.TextStyle(
                fontFamily = displayFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight(500),
                lineHeight = 24.sp,
                letterSpacing = 0.15.sp
            )
    }
    
    data object TitleSmall : TextStyle {
        override val value: androidx.compose.ui.text.TextStyle
            get() = androidx.compose.ui.text.TextStyle(
                fontFamily = displayFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight(500),
                lineHeight = 20.sp,
            )
    }

    
    data object BodyLarge : TextStyle {
        override val value: androidx.compose.ui.text.TextStyle
            get() = androidx.compose.ui.text.TextStyle(
                fontFamily = bodyFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight(400),
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp
            )
    }
    
    data object BodyMedium : TextStyle {
        override val value: androidx.compose.ui.text.TextStyle
            get() = androidx.compose.ui.text.TextStyle(
                fontFamily = bodyFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight(400),
                lineHeight = 20.sp,
                letterSpacing = 0.25.sp
            )
    }
    
    data object BodySmall : TextStyle {
        override val value: androidx.compose.ui.text.TextStyle
            get() = androidx.compose.ui.text.TextStyle(
                fontFamily = bodyFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight(400),
                lineHeight = 16.sp,
                letterSpacing = 0.4.sp
            )
    }

    
    data object LabelLarge : TextStyle {
        override val value: androidx.compose.ui.text.TextStyle
            get() = androidx.compose.ui.text.TextStyle(
                fontFamily = bodyFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight(500),
                lineHeight = 20.sp,
                letterSpacing = 0.1.sp
            )
    }
    
    data object LabelMedium : TextStyle {
        override val value: androidx.compose.ui.text.TextStyle
            get() = androidx.compose.ui.text.TextStyle(
                fontFamily = bodyFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight(500),
                lineHeight = 16.sp,
                letterSpacing = 0.5.sp
            )
    }
    
    data object LabelSmall : TextStyle {
        override val value: androidx.compose.ui.text.TextStyle
            get() = androidx.compose.ui.text.TextStyle(
                fontFamily = bodyFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight(500),
                lineHeight = 16.sp,
                letterSpacing = 0.5.sp
            )
    }

    
    data object Paragraph : TextStyle {
        override val value: androidx.compose.ui.text.TextStyle
            get() = androidx.compose.ui.text.TextStyle(
                fontFamily = bodyFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight(400),
                lineHeight = 22.sp,
                letterSpacing = 0.25.sp
            )
    }
    
    data object CategoryName : TextStyle {
        override val value: androidx.compose.ui.text.TextStyle
            get() = androidx.compose.ui.text.TextStyle(
                fontFamily = displayFontFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight(600),
                lineHeight = 24.sp,
                letterSpacing = 0.15.sp
            )
    }
    
    data object Quote : TextStyle {
        override val value: androidx.compose.ui.text.TextStyle
            get() = androidx.compose.ui.text.TextStyle(
                fontFamily = bodyFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight(400),
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
    }
    
    data object ButtonText : TextStyle {
        override val value: androidx.compose.ui.text.TextStyle
            get() = androidx.compose.ui.text.TextStyle(
                fontFamily = bodyFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight(500),
                lineHeight = 20.sp,
                letterSpacing = 0.1.sp
            )
    }
    
    
    data object CardTitle : TextStyle {
        override val value: androidx.compose.ui.text.TextStyle
            get() = androidx.compose.ui.text.TextStyle(
                fontFamily = displayFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight(600),
                lineHeight = 24.sp,
                letterSpacing = 0.15.sp
            )
    }
    
    data object Caption : TextStyle {
        override val value: androidx.compose.ui.text.TextStyle
            get() = androidx.compose.ui.text.TextStyle(
                fontFamily = bodyFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight(400),
                lineHeight = 16.sp,
                letterSpacing = 0.4.sp
            )
    }
    
    data object Overline : TextStyle {
        override val value: androidx.compose.ui.text.TextStyle
            get() = androidx.compose.ui.text.TextStyle(
                fontFamily = bodyFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight(400),
                lineHeight = 14.sp,
                letterSpacing = 1.5.sp,
                textDecoration = TextDecoration.None
            )
    }

    
    data object Emphasis : TextStyle {
        override val value: androidx.compose.ui.text.TextStyle
            get() = androidx.compose.ui.text.TextStyle(
                fontFamily = bodyFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight(700),
                lineHeight = 20.sp,
                letterSpacing = 0.25.sp
            )
    }
    
    data object Link : TextStyle {
        override val value: androidx.compose.ui.text.TextStyle
            get() = androidx.compose.ui.text.TextStyle(
                fontFamily = bodyFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight(500),
                lineHeight = 20.sp,
                letterSpacing = 0.25.sp,
                textDecoration = TextDecoration.Underline
            )
    }
}
