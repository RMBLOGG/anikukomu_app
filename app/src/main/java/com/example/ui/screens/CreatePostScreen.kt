package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.MainViewModel
import com.example.ui.OperationState
import com.example.ui.components.*
import com.example.ui.theme.*
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    viewModel: MainViewModel,
    onSuccess: () -> Unit
) {
    val caption by viewModel.caption.collectAsState()
    val selectedTags by viewModel.selectedAnimeTags.collectAsState()
    val animeResults by viewModel.animeSearchResults.collectAsState()
    val createPostState by viewModel.createPostState.collectAsState()

    val context = LocalContext.current

    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchingFocused by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    selectedBitmap = bitmap
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal memuat gambar.", Toast.LENGTH_SHORT).show()
            }
        }
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
                    Text(
                        text = "POSTINGAN BARU",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Caption text area
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    AnikuSectionHeader(title = "CAPTION POSTINGAN")

                    OutlinedTextField(
                        value = caption,
                        onValueChange = { viewModel.updateCaption(it) },
                        placeholder = { Text("Tulis apa saja wibu! Ceritakan anime favorit kamu...", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryAccent,
                            unfocusedBorderColor = BorderDivider,
                            focusedContainerColor = SurfaceCard,
                            unfocusedContainerColor = SurfaceCard,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        maxLines = 8
                    )

                    // Character counter
                    Text(
                        text = "${caption.length}/2200",
                        fontSize = 10.sp,
                        fontFamily = LabelFontFamily,
                        color = if (caption.length >= 2200) ErrorRed else TextMuted,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 4.dp)
                    )
                }
            }

            // Optional Image Picker Section
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    AnikuSectionHeader(title = "GAMBAR POSTINGAN (OPSIONAL)")

                    if (selectedBitmap != null) {
                        // Image Preview Card with click-to-remove
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, BorderDivider, RoundedCornerShape(12.dp))
                        ) {
                            Image(
                                bitmap = selectedBitmap!!.asImageBitmap(),
                                contentDescription = "Preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Remove button (top-right)
                            IconButton(
                                onClick = { selectedBitmap = null },
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .align(Alignment.TopEnd)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Hapus Gambar",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else {
                        // Empty Image Selector Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceCard)
                                .border(1.dp, BorderDivider, RoundedCornerShape(12.dp))
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "Pilih Gambar",
                                    tint = PrimaryAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Pilih Gambar dari Galeri",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Anime tag search
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    AnikuSectionHeader(title = "TAG ANIME")

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            viewModel.searchAnimeForTag(it)
                            isSearchingFocused = it.isNotBlank()
                        },
                        placeholder = { Text("Cari judul anime...", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryAccent,
                            unfocusedBorderColor = BorderDivider,
                            focusedContainerColor = SurfaceCard,
                            unfocusedContainerColor = SurfaceCard,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        },
                        singleLine = true
                    )

                    // Suggestions drop-down list
                    if (isSearchingFocused && animeResults.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceCard)
                                .border(1.dp, BorderDivider, RoundedCornerShape(12.dp))
                                .heightIn(max = 200.dp)
                        ) {
                            animeResults.forEach { anime ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.addAnimeTag(anime)
                                            searchQuery = ""
                                            isSearchingFocused = false
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = anime.coverUrl,
                                        contentDescription = anime.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(32.dp, 44.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = anime.title ?: "No Title",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                                Divider(color = BorderDivider)
                            }
                        }
                    }
                }
            }

            // Selected tag chips
            if (selectedTags.isNotEmpty()) {
                item {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(selectedTags) { anime ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(PrimaryAccent.copy(alpha = 0.2f))
                                    .border(1.dp, PrimaryAccent, RoundedCornerShape(16.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = anime.title ?: "",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = LabelFontFamily,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Hapus Tag",
                                        tint = PrimaryAccent,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable { viewModel.removeAnimeTag(anime) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // CTA Submit button
            item {
                Spacer(modifier = Modifier.height(16.dp))

                AnikuButton(
                    text = "Bagikan Postingan",
                    onClick = {
                        viewModel.createPost(
                            bitmap = selectedBitmap,
                            onSuccess = {
                                selectedBitmap = null
                                Toast.makeText(context, "Postingan berhasil dibagikan!", Toast.LENGTH_SHORT).show()
                                onSuccess()
                            },
                            onError = { error ->
                                Toast.makeText(context, "Gagal membuat postingan: $error", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = caption.isNotBlank(),
                    isLoading = createPostState is OperationState.Loading
                )
            }
        }
    }
}
