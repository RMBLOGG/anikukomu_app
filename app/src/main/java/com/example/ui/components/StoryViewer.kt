package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun StoryViewer(
    allStories: List<Story>,
    initialUserGroupIndex: Int,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val myId = viewModel.sessionManager.userId ?: ""

    // Group stories by user
    val groupedStories = remember(allStories) {
        allStories.groupBy { it.userId }.map { (userId, userStories) ->
            userStories.sortedBy { it.createdAt }
        }.sortedBy { it.first().userId == myId } // Match the ordering of StoryBar
    }

    if (groupedStories.isEmpty() || initialUserGroupIndex >= groupedStories.size) {
        onDismiss()
        return
    }

    var currentUserGroupIndex by remember { mutableStateOf(initialUserGroupIndex) }
    var currentStoryIndex by remember { mutableStateOf(0) }

    val currentUserStories = groupedStories[currentUserGroupIndex]
    val activeStory = currentUserStories[currentStoryIndex]
    val uploader = activeStory.profiles
    val username = uploader?.username ?: "wibu"
    val dispName = uploader?.displayName ?: username

    var isPaused by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }

    // Reset progress on active story change
    LaunchedEffect(currentUserGroupIndex, currentStoryIndex) {
        progress = 0f
    }

    // Story progress and auto-advancing ticker
    LaunchedEffect(isPaused, currentUserGroupIndex, currentStoryIndex) {
        if (!isPaused) {
            val totalTicks = 100
            val tickDuration = 5000L / totalTicks // 5s total
            for (i in (progress * totalTicks).toInt() until totalTicks) {
                delay(tickDuration)
                progress = (i + 1) / totalTicks.toFloat()
            }

            // Move forward
            if (currentStoryIndex < currentUserStories.size - 1) {
                currentStoryIndex += 1
            } else if (currentUserGroupIndex < groupedStories.size - 1) {
                currentUserGroupIndex += 1
                currentStoryIndex = 0
            } else {
                onDismiss() // Finished all stories
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Main Story Image
        AsyncImage(
            model = activeStory.imageUrl,
            contentDescription = "Story from $username",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPaused = true
                            tryAwaitRelease()
                            isPaused = false
                        },
                        onTap = { offset ->
                            val width = size.width
                            if (offset.x < width / 3) {
                                // Tap Left: Previous story
                                if (currentStoryIndex > 0) {
                                    currentStoryIndex -= 1
                                } else if (currentUserGroupIndex > 0) {
                                    currentUserGroupIndex -= 1
                                    currentStoryIndex = groupedStories[currentUserGroupIndex].size - 1
                                }
                            } else {
                                // Tap Right: Next story
                                if (currentStoryIndex < currentUserStories.size - 1) {
                                    currentStoryIndex += 1
                                } else if (currentUserGroupIndex < groupedStories.size - 1) {
                                    currentUserGroupIndex += 1
                                    currentStoryIndex = 0
                                } else {
                                    onDismiss()
                                }
                            }
                        }
                    )
                }
        )

        // Top Indicators and Header info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.4f))
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Segmented Progress Indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                currentUserStories.forEachIndexed { idx, _ ->
                    val segmentProgress = when {
                        idx < currentStoryIndex -> 1f
                        idx > currentStoryIndex -> 0f
                        else -> progress
                    }

                    LinearProgressIndicator(
                        progress = { segmentProgress },
                        color = PrimaryAccent,
                        trackColor = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                    )
                }
            }

            // User Info header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                WibuAvatar(
                    url = uploader?.avatarUrl,
                    username = username,
                    size = 38.dp
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dispName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = getRelativeTimeString(activeStory.createdAt),
                        fontSize = 11.sp,
                        fontFamily = LabelFontFamily,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                // Delete button (owner only)
                if (activeStory.userId == myId) {
                    IconButton(
                        onClick = {
                            viewModel.deleteStory(activeStory.id)
                            // Remove locally and advance
                            if (currentUserStories.size > 1) {
                                currentStoryIndex = (currentStoryIndex - 1).coerceAtLeast(0)
                            } else {
                                onDismiss()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus Story",
                            tint = ErrorRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Close button
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
