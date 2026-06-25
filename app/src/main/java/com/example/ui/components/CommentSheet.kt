package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentSheet(
    postId: String,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    var textInput by remember { mutableStateOf("") }
    val comments by viewModel.comments.collectAsState()
    val likedCommentIds by viewModel.likedCommentIds.collectAsState()
    val replyTarget by viewModel.replyTargetComment.collectAsState()
    val isSubmitting by viewModel.isSubmittingComment.collectAsState()

    LaunchedEffect(postId) {
        viewModel.fetchComments(postId)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BackgroundBase,
        scrimColor = Color.Black.copy(alpha = 0.7f),
        dragHandle = { BottomSheetDefaults.DragHandle(color = BorderDivider) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "KOMENTAR WIBU",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )

                Text(
                    text = "${comments.size} Komentar",
                    fontSize = 12.sp,
                    fontFamily = LabelFontFamily,
                    color = TextSecondary
                )
            }

            Divider(color = BorderDivider, thickness = 1.dp)

            // Threaded Comments List
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (comments.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada komentar. Jadilah wibu pertama!",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            fontFamily = AppFontFamily
                        )
                    }
                } else {
                    // Structure: separate top-level comments and replies
                    val topLevelComments = comments.filter { it.parentId == null }
                    val repliesMap = comments.filter { it.parentId != null }.groupBy { it.parentId }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
                    ) {
                        topLevelComments.forEach { parent ->
                            item(key = parent.id) {
                                CommentItem(
                                    comment = parent,
                                    isLiked = likedCommentIds.contains(parent.id),
                                    onLikeToggle = { viewModel.likeCommentToggle(parent) },
                                    onReplyClick = { viewModel.setReplyTarget(parent) }
                                )
                            }

                            // Show nested replies underneath
                            val replies = repliesMap[parent.id] ?: emptyList()
                            if (replies.isNotEmpty()) {
                                items(replies, key = { it.id }) { reply ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 32.dp, top = 8.dp)
                                    ) {
                                        // Reply Visual Indent Line
                                        Box(
                                            modifier = Modifier
                                                .width(2.dp)
                                                .height(36.dp)
                                                .background(BorderDivider)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))

                                        Box(modifier = Modifier.weight(1f)) {
                                            CommentItem(
                                                comment = reply,
                                                isLiked = likedCommentIds.contains(reply.id),
                                                onLikeToggle = { viewModel.likeCommentToggle(reply) },
                                                onReplyClick = { viewModel.setReplyTarget(parent) } // Replying to thread
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Divider(color = BorderDivider, thickness = 1.dp)

            // Reply target Indicator / Input panel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard)
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(12.dp)
            ) {
                if (replyTarget != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceElevated)
                            .border(1.dp, BorderDivider, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Membalas @${replyTarget!!.profiles?.username}",
                            fontSize = 12.sp,
                            color = PrimaryAccent,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel Reply",
                            tint = TextSecondary,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { viewModel.setReplyTarget(null) }
                        )
                    }
                }

                // Input row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it.take(1000) },
                        placeholder = { Text("Tulis komentar kamu...", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryAccent,
                            unfocusedBorderColor = BorderDivider,
                            focusedContainerColor = SurfaceElevated,
                            unfocusedContainerColor = SurfaceElevated,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.weight(1f),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    val sendBtnBg = if (textInput.isNotBlank() && !isSubmitting) {
                        Modifier.background(AnikuGradient())
                    } else {
                        Modifier.background(BorderDivider)
                    }

                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank() && !isSubmitting) {
                                viewModel.addComment(postId, textInput) {
                                    textInput = ""
                                }
                            }
                        },
                        enabled = textInput.isNotBlank() && !isSubmitting,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .then(sendBtnBg)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(color = TextPrimary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: Comment,
    isLiked: Boolean,
    onLikeToggle: () -> Unit,
    onReplyClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        WibuAvatar(
            url = comment.profiles?.avatarUrl,
            username = comment.profiles?.username ?: "wibu",
            size = 36.dp
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.profiles?.displayName ?: "Wibu Nusantara",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "@${comment.profiles?.username}",
                    fontSize = 11.sp,
                    color = PrimaryAccent,
                    fontFamily = LabelFontFamily
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = comment.content,
                fontSize = 13.sp,
                color = TextPrimary,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Action labels
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = getRelativeTimeString(comment.createdAt),
                    fontSize = 10.sp,
                    fontFamily = LabelFontFamily,
                    color = TextMuted
                )

                Text(
                    text = "BALAS",
                    fontSize = 10.sp,
                    fontFamily = LabelFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryAccent,
                    modifier = Modifier.clickable { onReplyClick() }
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Comment Like button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = onLikeToggle,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like Comment",
                    tint = if (isLiked) ErrorRed else TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
            if (comment.likesCount > 0) {
                Text(
                    text = comment.likesCount.toString(),
                    fontSize = 10.sp,
                    fontFamily = LabelFontFamily,
                    color = TextSecondary
                )
            }
        }
    }
}

fun getRelativeTimeString(isoString: String): String {
    return try {
        val instant = Instant.parse(isoString)
        val now = Instant.now()
        val seconds = ChronoUnit.SECONDS.between(instant, now)
        val minutes = ChronoUnit.MINUTES.between(instant, now)
        val hours = ChronoUnit.HOURS.between(instant, now)
        val days = ChronoUnit.DAYS.between(instant, now)

        when {
            seconds < 60 -> "baru saja"
            minutes < 60 -> "$minutes m"
            hours < 24 -> "$hours j"
            days < 7 -> "$days h"
            else -> {
                // Show date formatted
                ZonedDateTime.parse(isoString).format(DateTimeFormatter.ofPattern("dd/MM/yy"))
            }
        }
    } catch (e: Exception) {
        "baru saja"
    }
}
