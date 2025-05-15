package dev.vku.livesnap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun Avatar(
    size: Int,
    borderColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    defaultAvatarBackgroundColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    avatarUrl: String? = null,
    initials: String? = null,
    isGold: Boolean = false,
    borderWidth: Dp = 4.dp,
    fontSize: Int = 32
) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .then(
                if (isGold) {
                    Modifier.background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFFD700), // Gold
                                Color(0xFFFFA500)  // Orange
                            )
                        )
                    )
                } else {
                    Modifier.border(borderWidth, borderColor, CircleShape)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size((size - borderWidth.value * 2).dp)
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size((size - borderWidth.value * 4).dp)
                    .clip(CircleShape)
                    .background(defaultAvatarBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                if (avatarUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context = LocalContext.current)
                            .crossfade(false)
                            .data(avatarUrl)
                            .build(),
                        contentDescription = "User avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (initials != null) {
                    Text(
                        text = initials,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontSize = fontSize.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
} 