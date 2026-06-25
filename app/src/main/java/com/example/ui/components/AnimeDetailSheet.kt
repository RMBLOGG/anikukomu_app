package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
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
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeDetailSheet(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val anime by viewModel.activeAnimeDetail.collectAsState()
    val episodes by viewModel.activeAnimeEpisodes.collectAsState()
    val characters by viewModel.activeAnimeCharacters.collectAsState()
    val isLoading by viewModel.isAnimeDetailLoading.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BackgroundBase,
        scrimColor = Color.Black.copy(alpha = 0.7f),
        dragHandle = { BottomSheetDefaults.DragHandle(color = BorderDivider) }
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                contentAlignment = Alignment.Center
            ) {
                GradientLoading()
            }
        } else if (anime == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Gagal memuat detail anime.", color = TextSecondary)
            }
        } else {
            val animeData = anime!!
            var selectedTab by remember { mutableStateOf(0) }
            val tabs = listOf("Info", "Episode", "Karakter")

            Column(modifier = Modifier.fillMaxWidth()) {
                // Header section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Poster
                    AsyncImage(
                        model = animeData.images?.webp?.largeImageUrl ?: animeData.images?.jpg?.largeImageUrl,
                        contentDescription = animeData.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(100.dp)
                            .height(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, BorderDivider, RoundedCornerShape(12.dp))
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = animeData.title ?: "No Title",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Score Row
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Score",
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = animeData.score?.toString() ?: "N/A",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontFamily = LabelFontFamily
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Detail micro-pills
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Episodes Count
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceElevated)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "${animeData.episodes ?: "?"} Eps",
                                    fontSize = 10.sp,
                                    fontFamily = LabelFontFamily,
                                    color = PrimaryAccent
                                )
                            }
                            // Year
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceElevated)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = animeData.year?.toString() ?: "?",
                                    fontSize = 10.sp,
                                    fontFamily = LabelFontFamily,
                                    color = SecondaryAccent
                                )
                            }
                            // Status
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceElevated)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = animeData.status ?: "?",
                                    fontSize = 10.sp,
                                    fontFamily = LabelFontFamily,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Tab Row Custom Styled (minimal, borders)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .border(1.dp, BorderDivider, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceCard)
                ) {
                    tabs.forEachIndexed { index, title ->
                        val tabBackgroundModifier = if (selectedTab == index) {
                            Modifier.background(AnikuGradient())
                        } else {
                            Modifier.background(Color.Transparent)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = index }
                                .then(tabBackgroundModifier)
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title.uppercase(),
                                fontSize = 11.sp,
                                fontFamily = LabelFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == index) TextPrimary else TextSecondary
                            )
                        }
                    }
                }

                // Tab Contents
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(max = 350.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    when (selectedTab) {
                        0 -> {
                            // INFO TAB
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                item {
                                    if (!animeData.synopsis.isNullOrBlank()) {
                                        Text(
                                            text = "SINOPSIS",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = LabelFontFamily,
                                            color = TextSecondary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = animeData.synopsis ?: "",
                                            fontSize = 13.sp,
                                            color = TextPrimary,
                                            lineHeight = 18.sp
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                    }
                                }

                                item {
                                    // Grid of other info
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(SurfaceCard)
                                            .border(1.dp, BorderDivider, RoundedCornerShape(12.dp))
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        InfoRow(label = "GENRE", value = animeData.genres?.joinToString { it.name } ?: "-")
                                        InfoRow(label = "STUDIO", value = animeData.studios?.joinToString { it.name } ?: "-")
                                        InfoRow(label = "PRODUSEN", value = animeData.producers?.joinToString { it.name } ?: "-")
                                        InfoRow(label = "SUMBER", value = animeData.source ?: "-")
                                        InfoRow(label = "DURASI", value = animeData.duration ?: "-")
                                        InfoRow(label = "RATING", value = animeData.rating ?: "-")
                                    }
                                }
                            }
                        }
                        1 -> {
                            // EPISODES TAB
                            var showAllEpisodes by remember { mutableStateOf(false) }
                            val visibleEpisodes = if (showAllEpisodes) episodes else episodes.take(8)

                            if (episodes.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Belum ada data episode.", color = TextSecondary)
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(visibleEpisodes) { ep ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(SurfaceCard)
                                                .border(1.dp, BorderDivider, RoundedCornerShape(8.dp))
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(SurfaceElevated),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = ep.malId.toString(),
                                                    fontSize = 11.sp,
                                                    fontFamily = LabelFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PrimaryAccent
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = ep.title ?: "Episode ${ep.malId}",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (!ep.aired.isNullOrBlank()) {
                                                    Text(
                                                        text = ep.aired ?: "",
                                                        fontSize = 11.sp,
                                                        color = TextMuted
                                                    )
                                                }
                                            }

                                            // Badges filler/recap
                                            if (ep.filler == true) {
                                                BadgePill(text = "FILLER", color = ErrorRed)
                                            } else if (ep.recap == true) {
                                                BadgePill(text = "RECAP", color = SecondaryAccent)
                                            }
                                        }
                                    }

                                    if (episodes.size > 8 && !showAllEpisodes) {
                                        item {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { showAllEpisodes = true }
                                                    .padding(vertical = 12.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "LIHAT SEMUA (${episodes.size} EPISODE)",
                                                    color = PrimaryAccent,
                                                    fontSize = 12.sp,
                                                    fontFamily = LabelFontFamily,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        2 -> {
                            // CHARACTERS TAB (Threaded Grid of Character + VA Photo)
                            if (characters.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Belum ada data karakter.", color = TextSecondary)
                                }
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(1),
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(characters) { entry ->
                                        val char = entry.character
                                        val jpnVa = entry.voiceActors?.firstOrNull { it.language?.equals("Japanese", ignoreCase = true) == true }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(SurfaceCard)
                                                .border(1.dp, BorderDivider, RoundedCornerShape(8.dp))
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Character image
                                            AsyncImage(
                                                model = char?.images?.jpg?.imageUrl,
                                                contentDescription = char?.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(CircleShape)
                                                    .border(1.dp, BorderDivider, CircleShape)
                                            )

                                            Spacer(modifier = Modifier.width(10.dp))

                                            // Character Name and Role
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = char?.name ?: "Unknown",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary
                                                )
                                                Text(
                                                    text = if (entry.role?.equals("Main", ignoreCase = true) == true) "Utama" else "Pendukung",
                                                    fontSize = 11.sp,
                                                    color = if (entry.role?.equals("Main", ignoreCase = true) == true) SecondaryAccent else TextMuted
                                                )
                                            }

                                            // Voice Actor Side-by-Side (if present)
                                            if (jpnVa != null) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.End,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Column(
                                                        horizontalAlignment = Alignment.End,
                                                        modifier = Modifier.padding(end = 8.dp)
                                                    ) {
                                                        Text(
                                                            text = jpnVa.person?.name ?: "",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = TextPrimary,
                                                            textAlign = TextAlign.End,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Text(
                                                            text = "Seiyuu",
                                                            fontSize = 9.sp,
                                                            color = PrimaryAccent,
                                                            textAlign = TextAlign.End
                                                        )
                                                    }

                                                    AsyncImage(
                                                        model = jpnVa.person?.images?.jpg?.imageUrl,
                                                        contentDescription = jpnVa.person?.name,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .border(1.dp, BorderDivider, CircleShape)
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
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontFamily = LabelFontFamily,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = TextPrimary,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun BadgePill(text: String, color: Color) {
    Box(
        modifier = Modifier
            .padding(start = 6.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.2f))
            .border(1.dp, color, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = 9.sp,
            fontFamily = LabelFontFamily,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
