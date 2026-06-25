package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Image
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.MainViewModel
import com.example.ui.OperationState
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val activeProfile by viewModel.activeProfile.collectAsState()
    val editProfileState by viewModel.editProfileState.collectAsState()

    // 2FA states from MainViewModel
    val is2faEnabled by viewModel.is2faEnabled.collectAsState()
    val isEnrolling2fa by viewModel.isEnrolling2fa.collectAsState()
    val enrollmentSecret by viewModel.enrollmentSecret.collectAsState()

    val context = LocalContext.current

    var displayName by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var websiteUrl by remember { mutableStateOf("") }
    var twitterUrl by remember { mutableStateOf("") }
    var instagramUrl by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf("") }
    var mfaVerifyCode by remember { mutableStateOf("") }

    var selectedAvatarBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Prefill profile values when available
    LaunchedEffect(activeProfile) {
        activeProfile?.let {
            displayName = it.displayName ?: ""
            bio = it.bio ?: ""
            websiteUrl = it.websiteUrl ?: ""
            twitterUrl = it.twitterUrl ?: ""
            instagramUrl = it.instagramUrl ?: ""
            avatarUrl = it.avatarUrl ?: ""
        }
    }

    // Load active 2FA status
    LaunchedEffect(Unit) {
        viewModel.check2faStatus()
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    selectedAvatarBitmap = bitmap
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal memuat gambar avatar.", Toast.LENGTH_SHORT).show()
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
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "EDIT PROFIL",
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Edit Avatar
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnikuSectionHeader(title = "FOTO PROFIL WIBU")

                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(SurfaceCard)
                            .border(1.dp, BorderDivider, CircleShape)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedAvatarBitmap != null) {
                            androidx.compose.foundation.Image(
                                bitmap = selectedAvatarBitmap!!.asImageBitmap(),
                                contentDescription = "New Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            WibuAvatar(
                                url = avatarUrl,
                                username = activeProfile?.username ?: "w",
                                size = 90.dp
                            )
                        }

                        // Edit Overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Pilih Gambar",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Ketuk untuk mengubah foto dari galeri",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            // Form inputs
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AnikuSectionHeader(title = "BIODATA DIRI")

                    // Display Name
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        placeholder = { Text("Nama Tampilan (Contoh: Wibu Nolep)", color = TextSecondary) },
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
                        singleLine = true
                    )

                    // Bio
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        placeholder = { Text("Tulis bio keren wibu kamu...", color = TextSecondary) },
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
                            .height(80.dp),
                        maxLines = 4
                    )
                }
            }

            // Socials input
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AnikuSectionHeader(title = "TAUTAN SOSIAL")

                    // Website URL
                    OutlinedTextField(
                        value = websiteUrl,
                        onValueChange = { websiteUrl = it },
                        placeholder = { Text("URL Website (Contoh: web-kamu.com)", color = TextSecondary) },
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
                        singleLine = true
                    )

                    // Twitter Handle
                    OutlinedTextField(
                        value = twitterUrl,
                        onValueChange = { twitterUrl = it },
                        placeholder = { Text("Username Twitter/X (Contoh: wibu_no_1)", color = TextSecondary) },
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
                        singleLine = true
                    )

                    // Instagram Handle
                    OutlinedTextField(
                        value = instagramUrl,
                        onValueChange = { instagramUrl = it },
                        placeholder = { Text("Username Instagram (Contoh: wibu_indonesia)", color = TextSecondary) },
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
                        singleLine = true
                    )
                }
            }

            // 2FA Verification Card / Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderDivider, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        AnikuSectionHeader(title = "KEAMANAN AKUN (2FA)")

                        if (is2faEnabled) {
                            // 2FA Active Status
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Active",
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Verifikasi 2 Langkah (2FA) AKTIF",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                            }

                            Text(
                                text = "Akun kamu terproteksi secara penuh menggunakan Google Authenticator atau Authy.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Button(
                                onClick = {
                                    viewModel.disable2fa(
                                        onSuccess = { Toast.makeText(context, "2FA berhasil dinonaktifkan.", Toast.LENGTH_SHORT).show() },
                                        onError = { Toast.makeText(context, "Gagal menonaktifkan 2FA: $it", Toast.LENGTH_SHORT).show() }
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Nonaktifkan 2FA", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            // 2FA Inactive / Enrollment state
                            val secret = enrollmentSecret
                            if (isEnrolling2fa && secret != null) {
                                // Show key, instruction, and verify input
                                Text(
                                    text = "Langkah 1: Hubungkan Aplikasi Authenticator",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Salin kunci rahasia berikut dan masukkan ke aplikasi Google Authenticator kamu.",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )

                                // Key display box
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SurfaceElevated)
                                        .border(1.dp, BorderDivider, RoundedCornerShape(8.dp))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = secret,
                                        fontSize = 13.sp,
                                        fontFamily = LabelFontFamily,
                                        color = PrimaryAccent,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )

                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("AnikuKomu 2FA Key", secret)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "Kunci rahasia disalin!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Salin",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Langkah 2: Verifikasi Kode OTP",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )

                                OutlinedTextField(
                                    value = mfaVerifyCode,
                                    onValueChange = { if (it.length <= 6) mfaVerifyCode = it },
                                    placeholder = { Text("6-digit Kode OTP", color = TextSecondary) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PrimaryAccent,
                                        unfocusedBorderColor = BorderDivider,
                                        focusedContainerColor = SurfaceElevated,
                                        unfocusedContainerColor = SurfaceElevated,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    singleLine = true,
                                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.cancel2faEnrollment() },
                                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Batal", color = TextPrimary)
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.confirm2faEnrollment(
                                                code = mfaVerifyCode,
                                                onSuccess = {
                                                    mfaVerifyCode = ""
                                                    Toast.makeText(context, "2FA Berhasil diaktifkan!", Toast.LENGTH_SHORT).show()
                                                },
                                                onError = { Toast.makeText(context, "Verifikasi Gagal: $it", Toast.LENGTH_SHORT).show() }
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                                        modifier = Modifier.weight(1f),
                                        enabled = mfaVerifyCode.length == 6
                                    ) {
                                        Text("Aktifkan 2FA", color = BackgroundBase, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                // Enable button
                                Text(
                                    text = "Aktifkan Verifikasi 2 Langkah (2FA) untuk mengamankan akun wibu kamu dari pembajakan.",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                Button(
                                    onClick = {
                                        viewModel.start2faEnrollment()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Aktifkan 2FA", color = BackgroundBase, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Save Action Bar
            item {
                Spacer(modifier = Modifier.height(8.dp))

                AnikuButton(
                    text = "Simpan Perubahan",
                    onClick = {
                        viewModel.updateProfile(
                            displayName = displayName,
                            bio = bio,
                            avatarBitmap = selectedAvatarBitmap,
                            website = websiteUrl,
                            twitter = twitterUrl,
                            instagram = instagramUrl,
                            onSuccess = {
                                selectedAvatarBitmap = null
                                Toast.makeText(context, "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                                onBack()
                            },
                            onError = { error ->
                                Toast.makeText(context, "Gagal memperbarui profil: $error", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isLoading = editProfileState is OperationState.Loading
                )
            }
        }
    }
}
