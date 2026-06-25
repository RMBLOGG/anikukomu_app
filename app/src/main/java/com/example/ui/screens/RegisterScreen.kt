package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AuthViewModel
import com.example.ui.RegisterUiState
import com.example.ui.components.AnikuButton
import com.example.ui.components.AnikuGradient
import com.example.ui.theme.*

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit
) {
    val registerState by viewModel.registerState.collectAsState()
    val context = LocalContext.current

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(registerState) {
        if (registerState is RegisterUiState.Error) {
            Toast.makeText(context, (registerState as RegisterUiState.Error).message, Toast.LENGTH_LONG).show()
            viewModel.resetStates()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBase)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AnikuGradient()),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🔥",
                    fontSize = 32.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Screen title
            Text(
                text = "Daftar Wibu Baru",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    fontSize = 28.sp
                )
            )

            Text(
                text = "Gabung komunitas anime paling seru di Indonesia!",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp),
                textAlign = TextAlign.Center
            )

            when (val state = registerState) {
                is RegisterUiState.Success -> {
                    // Success state: show verification prompt
                    Text(
                        text = "Pendaftaran Berhasil! 🎉",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = state.message,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(top = 12.dp, bottom = 32.dp)
                    )

                    AnikuButton(
                        text = "Kembali ke Halaman Login",
                        onClick = {
                            viewModel.resetStates()
                            onNavigateToLogin()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                else -> {
                    // Registration Inputs
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it.trim().take(30) },
                        placeholder = { Text("Username Wibu", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryAccent,
                            unfocusedBorderColor = BorderDivider,
                            focusedContainerColor = SurfaceElevated,
                            unfocusedContainerColor = SurfaceElevated,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it.trim() },
                        placeholder = { Text("Alamat Email", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryAccent,
                            unfocusedBorderColor = BorderDivider,
                            focusedContainerColor = SurfaceElevated,
                            unfocusedContainerColor = SurfaceElevated,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Kata Sandi (Min 6 karakter)", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryAccent,
                            unfocusedBorderColor = BorderDivider,
                            focusedContainerColor = SurfaceElevated,
                            unfocusedContainerColor = SurfaceElevated,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    AnikuButton(
                        text = "Daftar Sekarang",
                        onClick = {
                            if (username.isBlank() || email.isBlank() || password.isBlank()) {
                                Toast.makeText(context, "Semua bidang wajib diisi.", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.register(username, email, password)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isLoading = state is RegisterUiState.Loading
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sudah punya akun? ",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Masuk di sini",
                            color = PrimaryAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.clickable { onNavigateToLogin() }
                        )
                    }
                }
            }
        }
    }
}
