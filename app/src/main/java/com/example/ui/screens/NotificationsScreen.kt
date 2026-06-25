package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.MainViewModel
import com.example.ui.components.WibuAvatar
import com.example.ui.components.getRelativeTimeString
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: MainViewModel
) {
    val notifications by viewModel.notifications.collectAsState()

    // Mark notifications read as soon as screen is viewed
    LaunchedEffect(Unit) {
        viewModel.fetchNotifications()
        viewModel.markAllNotificationsAsRead()
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundBase)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifikasi",
                        tint = PrimaryAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "NOTIFIKASI",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            fontSize = 18.sp
                        )
                    )
                }
                Divider(color = BorderDivider, thickness = 1.dp)
            }
        },
        containerColor = BackgroundBase
    ) { innerPadding ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text("🔔", fontSize = 44.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Belum ada notifikasi baru.",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Aktivitas menyukai, mengikuti, atau komentar wibu lain akan muncul di sini.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
            ) {
                items(notifications, key = { it.id }) { notif ->
                    val actor = notif.profiles
                    val username = actor?.username ?: "wibu"
                    val dispName = actor?.displayName ?: username

                    // Determine message copy
                    val notifMessage = when (notif.type) {
                        "like" -> " menyukai postingan Anda."
                        "comment" -> " mengomentari postingan Anda."
                        "follow" -> " mulai mengikuti Anda."
                        else -> " melakukan tindakan."
                    }

                    val annotatedText = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = TextPrimary)) {
                            append(dispName)
                        }
                        withStyle(style = SpanStyle(color = TextSecondary)) {
                            append(notifMessage)
                        }
                    }

                    // Notification Row Card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (!notif.isRead) SurfaceElevated else SurfaceCard)
                            .border(
                                width = 1.dp,
                                color = if (!notif.isRead) PrimaryAccent.copy(alpha = 0.5f) else BorderDivider,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Actor Avatar
                        WibuAvatar(
                            url = actor?.avatarUrl,
                            username = username,
                            size = 40.dp
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = annotatedText,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = getRelativeTimeString(notif.createdAt),
                                fontSize = 10.sp,
                                fontFamily = LabelFontFamily,
                                color = TextMuted
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Status Icon Indicator
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(SurfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            when (notif.type) {
                                "like" -> Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Suka",
                                    tint = ErrorRed,
                                    modifier = Modifier.size(14.dp)
                                )
                                "comment" -> Icon(
                                    imageVector = Icons.Default.Comment,
                                    contentDescription = "Komentar",
                                    tint = PrimaryAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                                "follow" -> Icon(
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = "Ikuti",
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
