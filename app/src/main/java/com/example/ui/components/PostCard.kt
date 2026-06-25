package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun PostCard(
    post: Post,
    isLiked: Boolean,
    viewModel: MainViewModel,
    onCommentClick: () -> Unit,
    onAnimeTagClick: (Int) -> Unit
) {
    val myId = viewModel.sessionManager.userId ?: ""
    val context = LocalContext.current

    var isExpanded by remember { mutableStateOf(false) }
    val captionText = post.caption ?: ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderDivider, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Avatar, names, and delete icon (if self)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                WibuAvatar(
                    url = post.profiles?.avatarUrl,
                    username = post.profiles?.username ?: "wibu",
                    size = 40.dp
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.profiles?.displayName ?: "Wibu Nusantara",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "@${post.profiles?.username}",
                            fontSize = 11.sp,
                            color = PrimaryAccent,
                            fontFamily = LabelFontFamily
                        )
                    }
                    Text(
                        text = getRelativeTimeString(post.createdAt),
                        fontSize = 10.sp,
                        fontFamily = LabelFontFamily,
                        color = TextMuted
                    )
                }

                // Delete trash icon (only if owner)
                if (post.userId == myId) {
                    IconButton(
                        onClick = {
                            viewModel.deletePost(post.id, post.imagePublicId)
                            Toast.makeText(context, "Postingan berhasil dihapus!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus Post",
                            tint = ErrorRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Post Image / Layout (if image exists)
            if (!post.imageUrl.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 5f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, BorderDivider, RoundedCornerShape(12.dp))
                ) {
                    ShimmerImage(
                        url = post.imageUrl,
                        contentDescription = "Post image from @${post.profiles?.username}",
                        modifier = Modifier.fillMaxSize()
                    )

                    // Dark gradient bottom overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.3f)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                )
                            )
                    )

                    // Anime tag chips overlay (bottom-left)
                    val tags = post.postAnimeTags ?: emptyList()
                    if (tags.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomStart)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(tags) { tag ->
                                val anime = tag.animes
                                if (anime != null && anime.malId != null) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.Black.copy(alpha = 0.6f))
                                            .border(1.dp, BorderDivider.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                            .clickable { onAnimeTagClick(anime.malId) }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "# ${anime.title}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = LabelFontFamily,
                                            color = PrimaryAccent
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Caption Text
            if (captionText.isNotBlank()) {
                val formattedCaption = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = TextPrimary)) {
                        append("@${post.profiles?.username} ")
                    }
                    append(captionText)
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                ) {
                    Text(
                        text = formattedCaption,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        lineHeight = 18.sp,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (captionText.length > 120) {
                        Text(
                            text = if (isExpanded) "sembunyikan" else "selengkapnya",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryAccent,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clickable { isExpanded = !isExpanded }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Divider(color = BorderDivider, thickness = 1.dp)

            // Action Row: Likes, Comments, Share
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Likes Section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { viewModel.likePostToggle(post) }
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) ErrorRed else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = post.likesCount.toString(),
                        fontSize = 12.sp,
                        fontFamily = LabelFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }

                // Comments Section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onCommentClick() }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Komentar",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = post.commentsCount.toString(),
                        fontSize = 12.sp,
                        fontFamily = LabelFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }

                // Share Section (copy post link to clipboard)
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("AnikuKomu Post", "https://anikukomu.vercel.app/post/${post.id}")
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Link postingan berhasil disalin!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Bagikan",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
