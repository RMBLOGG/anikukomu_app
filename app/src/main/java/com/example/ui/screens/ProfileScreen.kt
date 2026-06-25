package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.MainViewModel
import com.example.ui.components.*
import com.example.ui.theme.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String,
    viewModel: MainViewModel,
    onEditProfileClick: () -> Unit,
    onPostClick: (Post) -> Unit
) {
    val myId = viewModel.sessionManager.userId ?: ""
    val activeProfile by viewModel.activeProfile.collectAsState()
    val posts by viewModel.profilePosts.collectAsState()
    val likedPosts by viewModel.profileLikedPosts.collectAsState()
    val profileTab by viewModel.profileTab.collectAsState()
    val isRefreshing by viewModel.isRefreshingProfile.collectAsState()
    val followedUserIds by viewModel.followedUserIds.collectAsState()

    val isSelf = userId == myId
    val isFollowing = followedUserIds.contains(userId)

    val tabs = listOf("Postingan", "Disukai")

    LaunchedEffect(userId) {
        viewModel.fetchProfileDetails(userId)
    }

    Scaffold(
        containerColor = BackgroundBase
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.fetchProfileDetails(userId) },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (activeProfile == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        GradientLoading()
                    }
                } else {
                    val profile = activeProfile!!

                    // Scrollable profile info header
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        // Avatar + Edit/Follow row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            WibuAvatar(
                                url = profile.avatarUrl,
                                username = profile.username,
                                size = 76.dp
                            )

                            // Follow/Edit button
                            if (isSelf) {
                                Button(
                                    onClick = onEditProfileClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.border(1.dp, BorderDivider, RoundedCornerShape(20.dp))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = TextPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Edit Profil", fontSize = 13.sp, color = TextPrimary)
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.followUserToggle(userId) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isFollowing) SurfaceCard else PrimaryAccent
                                    ),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = if (isFollowing) Modifier.border(1.dp, BorderDivider, RoundedCornerShape(20.dp)) else Modifier
                                ) {
                                    Icon(
                                        imageVector = if (isFollowing) Icons.Default.PersonRemove else Icons.Default.PersonAdd,
                                        contentDescription = null,
                                        tint = if (isFollowing) TextPrimary else BackgroundBase,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isFollowing) "Batal Ikuti" else "Ikuti",
                                        fontSize = 13.sp,
                                        color = if (isFollowing) TextPrimary else BackgroundBase,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Name, Username
                        Text(
                            text = profile.displayName ?: "Wibu Nusantara",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "@${profile.username}",
                            fontSize = 13.sp,
                            color = PrimaryAccent,
                            fontFamily = LabelFontFamily,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Bio
                        Text(
                            text = profile.bio ?: "Penggemar Anime Nusantara 🇮🇩",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Website and Social icons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!profile.websiteUrl.isNullOrBlank()) {
                                SocialBadge(iconText = "WEB", detail = profile.websiteUrl)
                            }
                            if (!profile.twitterUrl.isNullOrBlank()) {
                                SocialBadge(iconText = "X", detail = "@" + profile.twitterUrl)
                            }
                            if (!profile.instagramUrl.isNullOrBlank()) {
                                SocialBadge(iconText = "IG", detail = "@" + profile.instagramUrl)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats counters: Postingan, Pengikut, Mengikuti
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceCard)
                                .border(1.dp, BorderDivider, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            StatItem(count = profile.postsCount, label = "POST")
                            StatItem(count = profile.followersCount, label = "PENGIKUT")
                            StatItem(count = profile.followingCount, label = "MENGIKUTI")
                        }
                    }

                    // Profile Tabs selection
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .border(1.dp, BorderDivider, RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceCard)
                    ) {
                        tabs.forEach { tabName ->
                            val isSelected = profileTab == tabName
                            val tabBg = if (isSelected) {
                                Modifier.background(AnikuGradient())
                            } else {
                                Modifier.background(Color.Transparent)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.setProfileTab(tabName) }
                                    .then(tabBg)
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tabName.uppercase(),
                                    fontSize = 11.sp,
                                    fontFamily = LabelFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) TextPrimary else TextSecondary
                                )
                            }
                        }
                    }

                    // Post/Likes Grids
                    val gridItems = if (profileTab == "Postingan") posts else likedPosts

                    if (gridItems.isEmpty() && !isRefreshing) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Belum ada postingan di sini.",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                fontFamily = AppFontFamily
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(gridItems, key = { it.id }) { post ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, BorderDivider, RoundedCornerShape(8.dp))
                                        .clickable { onPostClick(post) }
                                ) {
                                    if (!post.imageUrl.isNullOrBlank()) {
                                        ShimmerImage(
                                            url = post.imageUrl,
                                            contentDescription = post.caption,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        // Text-only placeholder card
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(AnikuGradient())
                                                .padding(6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = post.caption ?: "",
                                                color = TextPrimary,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 3,
                                                overflow = TextOverflow.Ellipsis,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SocialBadge(iconText: String, detail: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderDivider, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = iconText,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = LabelFontFamily,
                color = SecondaryAccent
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = detail,
                fontSize = 11.sp,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 120.dp)
            )
        }
    }
}

@Composable
fun StatItem(count: Int, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = count.toString(),
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary,
            fontFamily = LabelFontFamily
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontFamily = LabelFontFamily,
            fontWeight = FontWeight.Medium,
            color = TextSecondary,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
