package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LoginUiState {
    object Idle : LoginUiState
    object Loading : LoginUiState
    data class Success(val profile: Profile) : LoginUiState
    data class MfaRequired(val factorId: String, val challengeId: String, val authResponse: SupabaseAuthResponse) : LoginUiState
    data class Error(val message: String) : LoginUiState
}

sealed interface RegisterUiState {
    object Idle : RegisterUiState
    object Loading : RegisterUiState
    data class Success(val message: String) : RegisterUiState
    data class Error(val message: String) : RegisterUiState
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val client = SupabaseClient.getInstance(application)
    private val sessionManager = client.sessionManager

    private val _isCheckingSession = MutableStateFlow(true)
    val isCheckingSession: StateFlow<Boolean> = _isCheckingSession.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val registerState: StateFlow<RegisterUiState> = _registerState.asStateFlow()

    init {
        checkSession()
    }

    fun checkSession() {
        viewModelScope.launch {
            _isCheckingSession.value = true
            val hasToken = sessionManager.isLoggedIn
            if (hasToken) {
                try {
                    val profileId = sessionManager.userId ?: ""
                    val profiles = client.dbApi.getProfile("id=eq.$profileId")
                    if (profiles.isNotEmpty()) {
                        _loginState.value = LoginUiState.Success(profiles.first())
                        _isLoggedIn.value = true
                    } else {
                        // Profile row missing but logged in? Ensure we try to create it or logout
                        sessionManager.clearSession()
                        _loginState.value = LoginUiState.Idle
                        _isLoggedIn.value = false
                    }
                } catch (e: Exception) {
                    // Token expired or network issue, clear and prompt login
                    sessionManager.clearSession()
                    _loginState.value = LoginUiState.Idle
                    _isLoggedIn.value = false
                }
            } else {
                _loginState.value = LoginUiState.Idle
                _isLoggedIn.value = false
            }
            _isCheckingSession.value = false
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginUiState.Loading
            try {
                val response = client.authApi.signIn(body = SupabaseAuthRequest(email, password))
                val user = response.user
                val token = response.accessToken

                if (token != null && user != null) {
                    // Check if TOTP factor is verified and enrolled
                    val totpFactor = user.factors?.firstOrNull { it.factorType == "totp" && it.status == "verified" }
                    if (totpFactor != null) {
                        // MFA Challenge required! Temporarily save access token to client for calling challenge
                        sessionManager.accessToken = token
                        try {
                            val challenge = client.authApi.challengeFactor(totpFactor.id)
                            _loginState.value = LoginUiState.MfaRequired(
                                factorId = totpFactor.id,
                                challengeId = challenge.id,
                                authResponse = response
                            )
                        } catch (e: Exception) {
                            sessionManager.clearSession()
                            _loginState.value = LoginUiState.Error("Gagal memicu verifikasi keamanan (2FA): ${e.localizedMessage}")
                        }
                    } else {
                        // No MFA, complete regular login
                        completeLogin(response)
                    }
                } else {
                    _loginState.value = LoginUiState.Error("Token atau User data tidak valid dari server.")
                }
            } catch (e: Exception) {
                _loginState.value = LoginUiState.Error(e.localizedMessage ?: "Gagal masuk. Periksa kembali email dan password kamu.")
            }
        }
    }

    fun verifyMfa(factorId: String, challengeId: String, code: String, pendingResponse: SupabaseAuthResponse) {
        viewModelScope.launch {
            _loginState.value = LoginUiState.Loading
            try {
                // Ensure we use the temporary token in interceptor
                sessionManager.accessToken = pendingResponse.accessToken
                val response = client.authApi.verifyChallenge(
                    factorId = factorId,
                    body = VerifyFactorRequest(challengeId = challengeId, code = code)
                )
                if (response.accessToken != null) {
                    // Merged session
                    val finalResponse = pendingResponse.copy(
                        accessToken = response.accessToken,
                        refreshToken = response.refreshToken ?: pendingResponse.refreshToken
                    )
                    completeLogin(finalResponse)
                } else {
                    _loginState.value = LoginUiState.Error("Kode OTP salah atau telah kedaluwarsa.")
                }
            } catch (e: Exception) {
                _loginState.value = LoginUiState.Error("Verifikasi OTP gagal: ${e.localizedMessage}")
            }
        }
    }

    private suspend fun completeLogin(authResponse: SupabaseAuthResponse) {
        val user = authResponse.user ?: return
        val token = authResponse.accessToken ?: return
        sessionManager.accessToken = token

        try {
            // Check if profile exists
            val profiles = client.dbApi.getProfile("id=eq.${user.id}")
            val profile = if (profiles.isNotEmpty()) {
                profiles.first()
            } else {
                // Create profile row using metadata
                val rawMeta = user.userMetadata
                val metaUsername = rawMeta?.get("username")?.toString()
                val metaDisplayName = rawMeta?.get("display_name")?.toString()

                val fallbackUsername = metaUsername ?: user.email?.substringBefore("@") ?: "wibu_${user.id.take(6)}"
                val fallbackDisplayName = metaDisplayName ?: fallbackUsername

                val newProfile = Profile(
                    id = user.id,
                    username = fallbackUsername,
                    displayName = fallbackDisplayName,
                    avatarUrl = null,
                    bio = "Penggemar Anime Nusantara 🇮🇩",
                    websiteUrl = null,
                    twitterUrl = null,
                    instagramUrl = null,
                    followersCount = 0,
                    followingCount = 0,
                    postsCount = 0
                )
                val created = client.dbApi.createProfile(newProfile)
                created.firstOrNull() ?: newProfile
            }

            sessionManager.saveSession(
                token = token,
                refresh = authResponse.refreshToken,
                uId = profile.id,
                uName = profile.username,
                dName = profile.displayName,
                avUrl = profile.avatarUrl
            )
            _loginState.value = LoginUiState.Success(profile)
            _isLoggedIn.value = true
        } catch (e: Exception) {
            sessionManager.clearSession()
            _isLoggedIn.value = false
            _loginState.value = LoginUiState.Error("Gagal memuat profil pengguna: ${e.localizedMessage}")
        }
    }

    fun register(username: String, email: String, password: String) {
        if (username.length < 3) {
            _registerState.value = RegisterUiState.Error("Username minimal harus 3 karakter.")
            return
        }
        if (password.length < 6) {
            _registerState.value = RegisterUiState.Error("Password minimal harus 6 karakter.")
            return
        }

        viewModelScope.launch {
            _registerState.value = RegisterUiState.Loading
            try {
                val metadata = mapOf(
                    "username" to username,
                    "display_name" to username
                )
                client.authApi.signUp(
                    body = SupabaseAuthRequest(
                        email = email,
                        password = password,
                        data = metadata
                    )
                )
                _registerState.value = RegisterUiState.Success(
                    "Cek email kamu untuk konfirmasi sebelum login."
                )
            } catch (e: Exception) {
                _registerState.value = RegisterUiState.Error(
                    e.localizedMessage ?: "Pendaftaran gagal. Pastikan email belum terdaftar."
                )
            }
        }
    }

    fun logout() {
        sessionManager.clearSession()
        _loginState.value = LoginUiState.Idle
        _registerState.value = RegisterUiState.Idle
        _isLoggedIn.value = false
    }

    fun resetStates() {
        _loginState.value = LoginUiState.Idle
        _registerState.value = RegisterUiState.Idle
    }
}
