package com.example.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun StoryBar(
    viewModel: MainViewModel,
    onStoryClick: (List<Story>, Int) -> Unit,
    onImageSelect: (android.graphics.Bitmap) -> Unit
) {
    val stories by viewModel.stories.collectAsState()
    val myId = viewModel.sessionManager.userId ?: ""
    val myUsername = viewModel.sessionManager.username ?: "wibu"
    val myAvatar = viewModel.sessionManager.avatarUrl

    val context = LocalContext.current

    // Group stories by user
    val groupedStories = remember(stories) {
        stories.groupBy { it.userId }.map { (userId, userStories) ->
            userStories.sortedBy { it.createdAt }
        }.sortedBy { it.first().userId == myId } // Show my stories or newest first
    }

    // Story picker activity launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    onImageSelect(bitmap)
                }
            } catch (e: Exception) {
                // Squelch
            }
        }
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        // Kisahmu (Your Story) circle
        item {
            val myGroup = groupedStories.find { it.first().userId == myId }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.width(68.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clickable {
                            if (myGroup != null) {
                                // If I have active stories, open the viewer!
                                val idx = groupedStories.indexOf(myGroup)
                                onStoryClick(stories, idx)
                            } else {
                                // Launch image picker
                                imagePickerLauncher.launch("image/*")
                            }
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(
                                width = if (myGroup != null) 2.dp else 1.dp,
                                brush = if (myGroup != null) AnikuGradient() else androidx.compose.ui.graphics.SolidColor(BorderDivider),
                                shape = CircleShape
                            )
                            .padding(3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        WibuAvatar(
                            url = myAvatar,
                            username = myUsername,
                            size = 52.dp
                        )
                    }

                    // Add story button badge (only if no active story, or show as secondary)
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(AnikuGradient())
                            .border(1.5.dp, BackgroundBase, CircleShape)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Tambah Story",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Kisahmu",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Active stories circles
        val otherGroups = groupedStories.filter { it.first().userId != myId }
        items(otherGroups) { group ->
            val firstStory = group.first()
            val uploader = firstStory.profiles
            val username = uploader?.username ?: "wibu"
            val dispName = uploader?.displayName ?: username

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.width(68.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clickable {
                            val idx = groupedStories.indexOf(group)
                            onStoryClick(stories, idx)
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(2.dp, AnikuGradient(), CircleShape)
                            .padding(3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        WibuAvatar(
                            url = uploader?.avatarUrl,
                            username = username,
                            size = 52.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = dispName,
                    fontSize = 11.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
