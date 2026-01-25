package com.diploma.work.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.diploma.work.data.AppSession
import com.diploma.work.ui.theme.Theme


@Composable
fun AvatarImage(
    avatarUrl: String,
    session: AppSession,
    modifier: Modifier = Modifier,
    size: Dp = 60.dp,
    borderWidth: Dp = 2.dp
) {
    val context = LocalContext.current
    
    if (avatarUrl == "data:avatar") {
        var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }

        LaunchedEffect(avatarUrl) {
            bitmap = session.getAvatarBitmap()?.asImageBitmap()
        }

        if (bitmap != null) {
            Image(
                bitmap = bitmap!!,
                contentDescription = "User Avatar",
                contentScale = ContentScale.Crop,
                modifier = modifier
                    .size(size)
                    .clip(CircleShape)
                    .then(
                        if (borderWidth > 0.dp) 
                            Modifier.border(borderWidth, Theme.extendedColorScheme.outlineActive, CircleShape)
                        else
                            Modifier
                    )
            )
        } else {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data("https://ui-avatars.com/api/?name=User&background=random&size=200")
                    .crossfade(true)
                    .build(),
                contentDescription = "User Avatar",
                contentScale = ContentScale.Crop,
                modifier = modifier
                    .size(size)
                    .clip(CircleShape)
                    .then(
                        if (borderWidth > 0.dp) 
                            Modifier.border(borderWidth, Theme.extendedColorScheme.outlineActive, CircleShape)
                        else
                            Modifier
                    ),
                error = painterResource(android.R.drawable.ic_menu_gallery)
            )
        }
    } else {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(avatarUrl)
                .crossfade(true)
                .build(),
            contentDescription = "User Avatar",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .then(
                    if (borderWidth > 0.dp) 
                        Modifier.border(borderWidth, Theme.extendedColorScheme.outlineActive, CircleShape)
                    else
                        Modifier
                ),
            error = painterResource(android.R.drawable.ic_menu_gallery)
        )
    }
}