package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.MainViewModel
import com.example.ui.components.*
import com.example.ui.theme.*
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: MainViewModel,
    onCommentClick: (Post) -> Unit,
    onAnimeTagClick: (Int) -> Unit,
    onStoryClick: (List<Story>, Int) -> Unit
) {
    val posts by viewModel.posts.collectAsState()
    val feedTab by viewModel.feedTab.collectAsState()
    val isRefreshing by viewModel.isRefreshingFeed.collectAsState()
    val likedPostIds by viewModel.likedPostIds.collectAsState()

    val context = LocalContext.current
    val tabs = listOf("Terbaru", "Diikuti")

    // Trigger initial feed load on screen entry
    LaunchedEffect(Unit) {
        viewModel.fetchFeedPosts()
        viewModel.fetchStories()
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundBase)
                    .statusBarsPadding()
            ) {
                // Header Title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ANIKUKOMU",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = TextPrimary
                        )
                    )

                    // Minimal fire icon badge
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AnikuGradient()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔥", fontSize = 18.sp)
                    }
                }

                // Feed tab pills: Terbaru & Diikuti
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tabs.forEach { tabName ->
                        val isSelected = feedTab == tabName
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) SurfaceCard else Color.Transparent)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) PrimaryAccent else BorderDivider,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable { viewModel.setFeedTab(tabName) }
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tabName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                fontFamily = LabelFontFamily
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Divider(color = BorderDivider, thickness = 1.dp)
            }
        },
        containerColor = BackgroundBase
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                viewModel.fetchFeedPosts()
                viewModel.fetchStories()
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Story Bar
                item {
                    StoryBar(
                        viewModel = viewModel,
                        onStoryClick = onStoryClick,
                        onImageSelect = { bitmap ->
                            viewModel.uploadStory(
                                bitmap = bitmap,
                                onSuccess = {
                                    Toast.makeText(context, "Story berhasil diunggah!", Toast.LENGTH_SHORT).show()
                                },
                                onError = { error ->
                                    Toast.makeText(context, "Gagal mengunggah story: $error", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    )
                    Divider(color = BorderDivider, thickness = 1.dp)
                }

                // Feed Posts / Empty States
                if (posts.isEmpty() && !isRefreshing) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Text(
                                    text = "👾",
                                    fontSize = 44.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (feedTab == "Diikuti") 
                                        "Belum ada postingan dari wibu yang kamu ikuti." 
                                    else 
                                        "Belum ada postingan sama sekali.",
                                    fontSize = 14.sp,
                                    color = TextSecondary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    lineHeight = 20.sp
                                )
                                if (feedTab == "Diikuti") {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Ikuti beberapa wibu di tab Explore atau profil wibu lainnya ya!",
                                        fontSize = 12.sp,
                                        color = TextMuted,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(posts, key = { it.id }) { post ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            PostCard(
                                post = post,
                                isLiked = likedPostIds.contains(post.id),
                                viewModel = viewModel,
                                onCommentClick = { onCommentClick(post) },
                                onAnimeTagClick = onAnimeTagClick
                            )
                        }
                    }
                }
            }
        }
    }
}
