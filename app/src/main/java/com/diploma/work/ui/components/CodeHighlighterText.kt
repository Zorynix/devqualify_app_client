package com.diploma.work.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.stringResource
import com.diploma.work.R
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diploma.work.ui.theme.TextStyle

@Composable
fun CodeHighlighterText(
    code: String,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false
) {
    val languageTag = remember(code) {
        detectLanguage(code)
    }
    
    val cleanCode = remember(code) {
        removeLanguageComment(code)
    }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Code,
                contentDescription = stringResource(R.string.code),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Code Sample",
                style = TextStyle.BodyMedium.value,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (languageTag.displayName.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "• ${languageTag.displayName}",
                    style = TextStyle.BodySmall.value,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = cleanCode,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private data class LanguageTag(
    val language: String,
    val displayName: String
)

private fun detectLanguage(code: String): LanguageTag {
    val firstLine = code.trim().lines().firstOrNull() ?: ""
    
    return when {
        firstLine.startsWith("// kotlin") -> LanguageTag("kotlin", "Kotlin")
        firstLine.startsWith("// java") -> LanguageTag("java", "Java")
        firstLine.startsWith("// js") || firstLine.startsWith("// javascript") -> LanguageTag("javascript", "JavaScript")
        firstLine.startsWith("// python") -> LanguageTag("python", "Python")
        firstLine.startsWith("// golang") -> LanguageTag("go", "Go")
        firstLine.startsWith("// c++") || firstLine.startsWith("// cpp") -> LanguageTag("cpp", "C++")
        firstLine.startsWith("// c#") || firstLine.startsWith("// csharp") -> LanguageTag("csharp", "C#")
        else -> LanguageTag("", "")
    }
}

private fun removeLanguageComment(code: String): String {
    val lines = code.lines()
    return if (lines.isNotEmpty() && lines[0].trim().startsWith("//")) {
        lines.drop(1).joinToString("\n")
    } else {
        code
    }
} 