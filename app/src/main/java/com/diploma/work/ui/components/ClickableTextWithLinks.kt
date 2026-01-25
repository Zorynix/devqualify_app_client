package com.diploma.work.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import com.diploma.work.ui.theme.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.diploma.work.ui.theme.TextStyle
import java.util.regex.Pattern
import androidx.core.net.toUri


@Composable
fun ClickableTextWithLinks(
    text: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val linkColor = MaterialTheme.colorScheme.primary

    val urlPattern = remember {
        Pattern.compile(
            """(https?://|www\.)[\w\-._~:/?#@!$&'()*+,;=%]+"""
        )
    }
    val annotatedString = remember(text, linkColor) {
        buildAnnotatedString {
            var lastIndex = 0
            val matcher = urlPattern.matcher(text)

            while (matcher.find()) {
                val start = matcher.start()
                val end = matcher.end()
                val url = matcher.group()

                if (start > lastIndex) {
                    append(text.substring(lastIndex, start))
                }

                val displayUrl = if (url.length > 50) {
                    url.take(47) + "..."
                } else {
                    url
                }

                pushStringAnnotation(
                    tag = "URL",
                    annotation = url
                )
                withStyle(
                    style = SpanStyle(
                        color = linkColor,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append(displayUrl)
                }
                pop()

                lastIndex = end
            }

            if (lastIndex < text.length) {
                append(text.substring(lastIndex))
            }
        }
    }

    val parts = remember(text) {
        val matcher = urlPattern.matcher(text)
        val result = mutableListOf<Pair<String, Boolean>>()
        var lastIndex = 0
        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()
            if (start > lastIndex) {
                result.add(text.substring(lastIndex, start) to false)
            }
            result.add(text.substring(start, end) to true)
            lastIndex = end
        }
        if (lastIndex < text.length) {
            result.add(text.substring(lastIndex) to false)
        }
        result
    }

    FlowRow(modifier = modifier) {
        parts.forEach { (part, isLink) ->
            if (isLink) {
                val displayUrl = if (part.length > 50) part.take(47) + "..." else part
                Text(
                    text = displayUrl,
                    style = TextStyle.BodySmall.value.copy(
                        color = linkColor,
                        textDecoration = TextDecoration.Underline
                    ),
                    modifier = Modifier.clickable {
                        var url = part
                        if (!url.startsWith("http://") && !url.startsWith("https://")) {
                            url = "https://$url"
                        }
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                )
            } else {
                Text(
                    text = part,
                    style = TextStyle.BodySmall.value.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}
