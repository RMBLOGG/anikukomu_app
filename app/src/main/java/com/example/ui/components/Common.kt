package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.*

@Composable
fun AnikuGradient(): Brush {
    return Brush.linearGradient(
        colors = listOf(PrimaryAccent, SecondaryAccent)
    )
}

@Composable
fun AnikuButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(if (enabled && !isLoading) AnikuGradient() else Brush.linearGradient(listOf(BorderDivider, BorderDivider)))
            .clickable(enabled = enabled && !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = TextPrimary,
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                color = if (enabled) TextPrimary else TextMuted,
                fontSize = 15.sp,
                fontFamily = AppFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AnikuSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(
            color = TextSecondary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        ),
        modifier = modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun WibuAvatar(
    url: String?,
    username: String,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val clickedModifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier

    Box(
        modifier = clickedModifier
            .size(size)
            .clip(CircleShape)
            .background(SurfaceElevated)
            .border(1.dp, BorderDivider, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (!url.isNullOrBlank()) {
            AsyncImage(
                model = url,
                contentDescription = "Avatar $username",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Initial letter placeholder
            val letter = username.firstOrNull()?.uppercase() ?: "?"
            Text(
                text = letter,
                color = PrimaryAccent,
                fontSize = (size.value * 0.45).sp,
                fontFamily = LabelFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ShimmerImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val shimmerColors = listOf(
        SurfaceCard,
        SurfaceElevated,
        SurfaceCard,
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = androidx.compose.ui.geometry.Offset.Zero,
        end = androidx.compose.ui.geometry.Offset(x = translateAnim, y = translateAnim)
    )

    Box(modifier = modifier) {
        if (!url.isNullOrBlank()) {
            AsyncImage(
                model = url,
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = Modifier
                    .fillMaxSize()
                    .background(brush)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(brush)
            )
        }
    }
}

@Composable
fun GradientLoading(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = PrimaryAccent,
            modifier = Modifier.size(32.dp),
            strokeWidth = 3.dp
        )
    }
}
