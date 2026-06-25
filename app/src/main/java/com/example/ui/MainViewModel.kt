package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

sealed interface OperationState {
    object Idle : OperationState
    object Loading : OperationState
    object Success : OperationState
    data class Error(val message: String) : OperationState
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val client = SupabaseClient.getInstance(application)
    val sessionManager = client.sessionManager

    // --- HOME FEED STATE ---
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _feedTab = MutableStateFlow("Terbaru") // "Terbaru" or "Diikuti"
    val feedTab: StateFlow<String> = _feedTab.asStateFlow()

    private val _isRefreshingFeed = MutableStateFlow(false)
    val isRefreshingFeed: StateFlow<Boolean> = _isRefreshingFeed.asStateFlow()

    private val _likedPostIds = MutableStateFlow<Set<String>>(emptySet())
    val likedPostIds: StateFlow<Set<String>> = _likedPostIds.asStateFlow()

    private val _followedUserIds = MutableStateFlow<Set<String>>(emptySet())
    val followedUserIds: StateFlow<Set<String>> = _followedUserIds.asStateFlow()

    // --- STORIES STATE ---
    private val _stories = MutableStateFlow<List<Story>>(emptyList())
    val stories: StateFlow<List<Story>> = _stories.asStateFlow()

    private val _storyUploadState = MutableStateFlow<OperationState>(OperationState.Idle)
    val storyUploadState: StateFlow<OperationState> = _storyUploadState.asStateFlow()

    // --- EXPLORE STATE ---
    private val _explorePosts = MutableStateFlow<List<Post>>(emptyList())
    val explorePosts: StateFlow<List<Post>> = _explorePosts.asStateFlow()

    private val _genreFilter = MutableStateFlow("Semua")
    val genreFilter: StateFlow<String> = _genreFilter.asStateFlow()

    private val _isRefreshingExplore = MutableStateFlow(false)
    val isRefreshingExplore: StateFlow<Boolean> = _isRefreshingExplore.asStateFlow()

    // --- CREATE POST STATE ---
    private val _caption = MutableStateFlow("")
    val caption: StateFlow<String> = _caption.asStateFlow()

    private val _selectedAnimeTags = MutableStateFlow<List<Anime>>(emptyList())
    val selectedAnimeTags: StateFlow<List<Anime>> = _selectedAnimeTags.asStateFlow()

    private val _animeSearchResults = MutableStateFlow<List<Anime>>(emptyList())
    val animeSearchResults: StateFlow<List<Anime>> = _animeSearchResults.asStateFlow()

    private val _createPostState = MutableStateFlow<OperationState>(OperationState.Idle)
    val createPostState: StateFlow<OperationState> = _createPostState.asStateFlow()

    // --- PROFILE STATE ---
    private val _activeProfile = MutableStateFlow<Profile?>(null)
    val activeProfile: StateFlow<Profile?> = _activeProfile.asStateFlow()

    private val _profilePosts = MutableStateFlow<List<Post>>(emptyList())
    val profilePosts: StateFlow<List<Post>> = _profilePosts.asStateFlow()

    private val _profileLikedPosts = MutableStateFlow<List<Post>>(emptyList())
    val profileLikedPosts: StateFlow<List<Post>> = _profileLikedPosts.asStateFlow()

    private val _profileTab = MutableStateFlow("Postingan") // "Postingan" or "Disukai"
    val profileTab: StateFlow<String> = _profileTab.asStateFlow()

    private val _isRefreshingProfile = MutableStateFlow(false)
    val isRefreshingProfile: StateFlow<Boolean> = _isRefreshingProfile.asStateFlow()

    // --- EDIT PROFILE STATE ---
    private val _editProfileState = MutableStateFlow<OperationState>(OperationState.Idle)
    val editProfileState: StateFlow<OperationState> = _editProfileState.asStateFlow()

    // --- 2FA STATE ---
    private val _is2faEnabled = MutableStateFlow(false)
    val is2faEnabled: StateFlow<Boolean> = _is2faEnabled.asStateFlow()

    private val _isEnrolling2fa = MutableStateFlow(false)
    val isEnrolling2fa: StateFlow<Boolean> = _isEnrolling2fa.asStateFlow()

    private val _enrollmentFactorId = MutableStateFlow<String?>(null)
    val enrollmentFactorId: StateFlow<String?> = _enrollmentFactorId.asStateFlow()

    private val _enrollmentSecret = MutableStateFlow<String?>(null)
    val enrollmentSecret: StateFlow<String?> = _enrollmentSecret.asStateFlow()

    private val _enrollmentQrCode = MutableStateFlow<String?>(null)
    val enrollmentQrCode: StateFlow<String?> = _enrollmentQrCode.asStateFlow()

    private val _enrollmentError = MutableStateFlow<String?>(null)
    val enrollmentError: StateFlow<String?> = _enrollmentError.asStateFlow()

    // --- COMMENT SHEET STATE ---
    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    private val _likedCommentIds = MutableStateFlow<Set<String>>(emptySet())
    val likedCommentIds: StateFlow<Set<String>> = _likedCommentIds.asStateFlow()

    private val _isSubmittingComment = MutableStateFlow(false)
    val isSubmittingComment: StateFlow<Boolean> = _isSubmittingComment.asStateFlow()

    private val _replyTargetComment = MutableStateFlow<Comment?>(null)
    val replyTargetComment: StateFlow<Comment?> = _replyTargetComment.asStateFlow()

    // --- NOTIFICATIONS STATE ---
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _unreadNotificationsCount = MutableStateFlow(0)
    val unreadNotificationsCount: StateFlow<Int> = _unreadNotificationsCount.asStateFlow()

    // --- ANIME DETAIL POPUP STATE ---
    private val _activeAnimeDetail = MutableStateFlow<JikanAnime?>(null)
    val activeAnimeDetail: StateFlow<JikanAnime?> = _activeAnimeDetail.asStateFlow()

    private val _activeAnimeEpisodes = MutableStateFlow<List<JikanEpisode>>(emptyList())
    val activeAnimeEpisodes: StateFlow<List<JikanEpisode>> = _activeAnimeEpisodes.asStateFlow()

    private val _activeAnimeCharacters = MutableStateFlow<List<JikanCharacterEntry>>(emptyList())
    val activeAnimeCharacters: StateFlow<List<JikanCharacterEntry>> = _activeAnimeCharacters.asStateFlow()

    private val _isAnimeDetailLoading = MutableStateFlow(false)
    val isAnimeDetailLoading: StateFlow<Boolean> = _isAnimeDetailLoading.asStateFlow()

    private var notificationPollJob: Job? = null

    init {
        // Start polling notifications
        startNotificationPolling()
    }

    // --- LIFECYCLE MANAGEMENT ---
    fun loadAllData() {
        fetchFeedPosts()
        fetchStories()
        fetchLikesAndFollows()
        fetchNotifications()
    }

    // --- NOTIFICATIONS POLLING ---
    private fun startNotificationPolling() {
        notificationPollJob?.cancel()
        notificationPollJob = viewModelScope.launch {
            while (true) {
                if (sessionManager.isLoggedIn) {
                    fetchNotifications()
                }
                delay(30000) // Poll every 30 seconds as specified
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        notificationPollJob?.cancel()
    }

    // --- HELPERS FOR LIKES AND FOLLOWS ---
    private fun fetchLikesAndFollows() {
        val myId = sessionManager.userId ?: return
        viewModelScope.launch {
            try {
                // Fetch post likes by user
                val postLikes = client.dbApi.getUserLikes("user_id=eq.$myId")
                _likedPostIds.value = postLikes.map { it.postId }.toSet()

                // Fetch follows where I am follower
                val follows = client.dbApi.getUserFollows("follower_id=eq.$myId")
                _followedUserIds.value = follows.map { it.followingId }.toSet()

                // Fetch comment likes by user
                val commentLikes = client.dbApi.getUserCommentLikes("user_id=eq.$myId")
                _likedCommentIds.value = commentLikes.map { it.commentId }.toSet()
            } catch (e: Exception) {
                // Squelch background helper errors
            }
        }
    }

    // --- FEED ACTIONS ---
    fun setFeedTab(tab: String) {
        _feedTab.value = tab
        fetchFeedPosts()
    }

    fun fetchFeedPosts() {
        val myId = sessionManager.userId ?: return
        viewModelScope.launch {
            _isRefreshingFeed.value = true
            try {
                fetchLikesAndFollows()
                if (_feedTab.value == "Diikuti") {
                    val followed = _followedUserIds.value
                    if (followed.isEmpty()) {
                        _posts.value = emptyList()
                    } else {
                        val idsString = "in.(${followed.joinToString(",")})"
                        val feedPosts = client.dbApi.getPostsInUserIds(userIdInFilter = idsString)
                        _posts.value = feedPosts
                    }
                } else {
                    val feedPosts = client.dbApi.getPosts()
                    _posts.value = feedPosts
                }
            } catch (e: Exception) {
                _posts.value = emptyList()
            } finally {
                _isRefreshingFeed.value = false
            }
        }
    }

    fun likePostToggle(post: Post) {
        val myId = sessionManager.userId ?: return
        viewModelScope.launch {
            val isLiked = _likedPostIds.value.contains(post.id)
            val updatedLiked = _likedPostIds.value.toMutableSet()
            val postsList = _posts.value.toMutableList()

            // Optimistic update
            if (isLiked) {
                updatedLiked.remove(post.id)
                val idx = postsList.indexOfFirst { it.id == post.id }
                if (idx != -1) {
                    postsList[idx] = postsList[idx].copy(likesCount = (postsList[idx].likesCount - 1).coerceAtLeast(0))
                }
            } else {
                updatedLiked.add(post.id)
                val idx = postsList.indexOfFirst { it.id == post.id }
                if (idx != -1) {
                    postsList[idx] = postsList[idx].copy(likesCount = postsList[idx].likesCount + 1)
                }
            }
            _likedPostIds.value = updatedLiked
            _posts.value = postsList

            try {
                if (isLiked) {
                    client.dbApi.unlikePost(userFilter = "eq.$myId", postFilter = "eq.${post.id}")
                } else {
                    client.dbApi.likePost(Like(userId = myId, postId = post.id))
                }
                // Refresh likes to stay in sync
                fetchLikesAndFollows()
            } catch (e: Exception) {
                // Revert optimistic state
                fetchFeedPosts()
            }
        }
    }

    fun deletePost(postId: String, imagePublicId: String?) {
        viewModelScope.launch {
            try {
                // Delete post record (DB enforces RLS)
                client.dbApi.deletePost("id=eq.$postId")

                // Cleanup image on Vercel
                if (imagePublicId != null) {
                    try {
                        client.vercelApi.cleanupImage(CleanupRequest(postId))
                    } catch (e: Exception) {
                        // Cleanup fails occasionally, not blocking
                    }
                }
                // Remove from feed
                _posts.value = _posts.value.filterNot { it.id == postId }
                _explorePosts.value = _explorePosts.value.filterNot { it.id == postId }
                _profilePosts.value = _profilePosts.value.filterNot { it.id == postId }
                fetchFeedPosts()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    // --- STORIES ACTIONS ---
    fun fetchStories() {
        viewModelScope.launch {
            try {
                // Expired handled by Postgres but filter active stories just in case
                val nowIso = Instant.now().toString()
                val activeStories = client.dbApi.getStories(expiresAtFilter = "gt.$nowIso")
                _stories.value = activeStories
            } catch (e: Exception) {
                _stories.value = emptyList()
            }
        }
    }

    fun uploadStory(bitmap: Bitmap, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val myId = sessionManager.userId ?: return
        viewModelScope.launch {
            _storyUploadState.value = OperationState.Loading
            try {
                // Compress bitmap to bytes
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                val imageBytes = stream.toByteArray()

                // Prep multipart
                val requestBody = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull(), 0, imageBytes.size)
                val body = MultipartBody.Part.createFormData("image", "story_${System.currentTimeMillis()}.jpg", requestBody)

                // Call Vercel Upload API
                val uploadResponse = client.vercelApi.uploadImage(folder = "posts", image = body)

                // Insert story in Supabase
                val newStory = Story(
                    id = UUID.randomUUID().toString(),
                    userId = myId,
                    imageUrl = uploadResponse.url,
                    imagePublicId = uploadResponse.publicId,
                    expiresAt = Instant.now().plus(24, ChronoUnit.HOURS).toString(),
                    createdAt = Instant.now().toString()
                )

                client.dbApi.createStory(newStory)
                fetchStories()
                _storyUploadState.value = OperationState.Success
                onSuccess()
            } catch (e: Exception) {
                _storyUploadState.value = OperationState.Error(e.localizedMessage ?: "Gagal mengunggah story.")
                onError(e.localizedMessage ?: "Gagal mengunggah story.")
            }
        }
    }

    fun deleteStory(storyId: String) {
        viewModelScope.launch {
            try {
                client.dbApi.deleteStory("id=eq.$storyId")
                _stories.value = _stories.value.filterNot { it.id == storyId }
                fetchStories()
            } catch (e: Exception) {
                // Handle deletion error
            }
        }
    }

    // --- EXPLORE ACTIONS ---
    fun setGenreFilter(genre: String) {
        _genreFilter.value = genre
        fetchExplorePosts()
    }

    fun fetchExplorePosts() {
        viewModelScope.launch {
            _isRefreshingExplore.value = true
            try {
                fetchLikesAndFollows()
                val allPosts = client.dbApi.getPosts()
                val selectedGenre = _genreFilter.value

                if (selectedGenre == "Semua") {
                    _explorePosts.value = allPosts
                } else {
                    // Filter posts where any tag's anime has genre containing the selected value
                    _explorePosts.value = allPosts.filter { post ->
                        post.postAnimeTags?.any { tag ->
                            tag.animes?.genre?.any { g -> g.equals(selectedGenre, ignoreCase = true) } == true
                        } == true
                    }
                }
            } catch (e: Exception) {
                _explorePosts.value = emptyList()
            } finally {
                _isRefreshingExplore.value = false
            }
        }
    }

    // --- CREATE POST ACTIONS ---
    fun updateCaption(text: String) {
        _caption.value = text.take(2200) // DB Constraint
    }

    fun searchAnimeForTag(query: String) {
        if (query.isBlank()) {
            _animeSearchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            try {
                val results = client.vercelApi.searchAnime(query)
                _animeSearchResults.value = results
            } catch (e: Exception) {
                _animeSearchResults.value = emptyList()
            }
        }
    }

    fun addAnimeTag(anime: Anime) {
        val current = _selectedAnimeTags.value.toMutableList()
        if (current.none { it.id == anime.id }) {
            current.add(anime)
            _selectedAnimeTags.value = current
        }
    }

    fun removeAnimeTag(anime: Anime) {
        _selectedAnimeTags.value = _selectedAnimeTags.value.filterNot { it.id == anime.id }
    }

    fun createPost(bitmap: Bitmap?, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val myId = sessionManager.userId ?: return
        if (_caption.value.isBlank()) {
            onError("Caption tidak boleh kosong.")
            return
        }

        viewModelScope.launch {
            _createPostState.value = OperationState.Loading
            try {
                var imageUrl: String? = null
                var imagePublicId: String? = null

                // Upload image if selected
                if (bitmap != null) {
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                    val imageBytes = stream.toByteArray()

                    val requestBody = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull(), 0, imageBytes.size)
                    val body = MultipartBody.Part.createFormData("image", "post_${System.currentTimeMillis()}.jpg", requestBody)

                    val uploadResponse = client.vercelApi.uploadImage(folder = "posts", image = body)
                    imageUrl = uploadResponse.url
                    imagePublicId = uploadResponse.publicId
                }

                val postId = UUID.randomUUID().toString()

                // Insert tagged animes into DB cache if not present (upsert with resolution=ignore-duplicates)
                _selectedAnimeTags.value.forEach { anime ->
                    try {
                        client.dbApi.upsertAnime(anime)
                    } catch (e: Exception) {
                        // Squelch anime upsert failure
                    }
                }

                // Create Post object
                val post = Post(
                    id = postId,
                    userId = myId,
                    caption = _caption.value,
                    imageUrl = imageUrl,
                    imagePublicId = imagePublicId,
                    likesCount = 0,
                    commentsCount = 0,
                    createdAt = Instant.now().toString()
                )

                client.dbApi.createPost(post)

                // Write tags to post_anime_tags table
                if (_selectedAnimeTags.value.isNotEmpty()) {
                    val tags = _selectedAnimeTags.value.map { anime ->
                        PostAnimeTag(postId = postId, animeId = anime.id)
                    }
                    client.dbApi.createPostAnimeTags(tags)
                }

                // Clean up state
                _caption.value = ""
                _selectedAnimeTags.value = emptyList()
                _animeSearchResults.value = emptyList()

                fetchFeedPosts()
                _createPostState.value = OperationState.Success
                onSuccess()
            } catch (e: Exception) {
                _createPostState.value = OperationState.Error(e.localizedMessage ?: "Gagal membuat postingan.")
                onError(e.localizedMessage ?: "Gagal membuat postingan.")
            }
        }
    }

    // --- PROFILE ACTIONS ---
    fun setProfileTab(tab: String) {
        _profileTab.value = tab
    }

    fun fetchProfileDetails(userId: String) {
        viewModelScope.launch {
            _isRefreshingProfile.value = true
            try {
                fetchLikesAndFollows()
                val profiles = client.dbApi.getProfile("id=eq.$userId")
                if (profiles.isNotEmpty()) {
                    val profile = profiles.first()
                    _activeProfile.value = profile

                    // Fetch profile's posts
                    val posts = client.dbApi.getPostsForUser("user_id=eq.$userId")
                    _profilePosts.value = posts

                    // Fetch user's liked posts (all posts, filtered locally by whether my user liked them, or querying likes table)
                    // Wait, we can query posts where id is in the liked posts
                    val myLikes = client.dbApi.getUserLikes("user_id=eq.$userId")
                    if (myLikes.isEmpty()) {
                        _profileLikedPosts.value = emptyList()
                    } else {
                        val idsString = "in.(${myLikes.joinToString(",") { it.postId }})"
                        val liked = client.dbApi.getPostsInUserIds(userIdInFilter = idsString)
                        _profileLikedPosts.value = liked
                    }
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isRefreshingProfile.value = false
            }
        }
    }

    fun followUserToggle(profileId: String) {
        val myId = sessionManager.userId ?: return
        if (myId == profileId) return

        viewModelScope.launch {
            val isFollowing = _followedUserIds.value.contains(profileId)
            val updatedFollowed = _followedUserIds.value.toMutableSet()
            val profile = _activeProfile.value

            // Optimistic update
            if (isFollowing) {
                updatedFollowed.remove(profileId)
                if (profile != null && profile.id == profileId) {
                    _activeProfile.value = profile.copy(followersCount = (profile.followersCount - 1).coerceAtLeast(0))
                }
            } else {
                updatedFollowed.add(profileId)
                if (profile != null && profile.id == profileId) {
                    _activeProfile.value = profile.copy(followersCount = profile.followersCount + 1)
                }
            }
            _followedUserIds.value = updatedFollowed

            try {
                if (isFollowing) {
                    client.dbApi.unfollowUser(followerFilter = "eq.$myId", followingFilter = "eq.$profileId")
                } else {
                    client.dbApi.followUser(Follow(followerId = myId, followingId = profileId))
                }
                fetchLikesAndFollows()
            } catch (e: Exception) {
                // Revert
                fetchProfileDetails(profileId)
            }
        }
    }

    // --- EDIT PROFILE ---
    fun updateProfile(
        displayName: String,
        bio: String,
        avatarBitmap: Bitmap?,
        website: String,
        twitter: String,
        instagram: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val myId = sessionManager.userId ?: return
        viewModelScope.launch {
            _editProfileState.value = OperationState.Loading
            try {
                var avatarUrl = _activeProfile.value?.avatarUrl

                if (avatarBitmap != null) {
                    val stream = ByteArrayOutputStream()
                    avatarBitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                    val imageBytes = stream.toByteArray()

                    val requestBody = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull(), 0, imageBytes.size)
                    val body = MultipartBody.Part.createFormData("image", "avatar_${System.currentTimeMillis()}.jpg", requestBody)

                    val uploadResponse = client.vercelApi.uploadImage(folder = "posts", image = body)
                    avatarUrl = uploadResponse.url
                }

                val updateBody = mapOf(
                    "display_name" to displayName,
                    "bio" to bio,
                    "avatar_url" to avatarUrl,
                    "website_url" to website.ifBlank { null },
                    "twitter_url" to twitter.ifBlank { null },
                    "instagram_url" to instagram.ifBlank { null }
                )

                val updatedProfiles = client.dbApi.updateProfile("id=eq.$myId", updateBody)
                if (updatedProfiles.isNotEmpty()) {
                    val updated = updatedProfiles.first()
                    _activeProfile.value = updated
                    sessionManager.displayName = updated.displayName
                    sessionManager.avatarUrl = updated.avatarUrl
                }

                _editProfileState.value = OperationState.Success
                onSuccess()
                fetchFeedPosts() // Keep main feeds fresh
            } catch (e: Exception) {
                _editProfileState.value = OperationState.Error(e.localizedMessage ?: "Gagal memperbarui profil.")
                onError(e.localizedMessage ?: "Gagal memperbarui profil.")
            }
        }
    }

    // --- 2FA ENROLLMENT / UNENROLLMENT ---
    fun check2faStatus() {
        viewModelScope.launch {
            try {
                val factors = client.authApi.listFactors()
                val active2fa = factors.any { it.factorType == "totp" && it.status == "verified" }
                _is2faEnabled.value = active2fa
            } catch (e: Exception) {
                _is2faEnabled.value = false
            }
        }
    }

    fun start2faEnrollment() {
        val myUsername = sessionManager.username ?: "user"
        _isEnrolling2fa.value = true
        _enrollmentError.value = null
        viewModelScope.launch {
            try {
                // 1. List current factors
                val factors = client.authApi.listFactors()

                // 2. Clean up any previous unverified factors
                factors.filter { it.status != "verified" }.forEach { factor ->
                    try {
                        client.authApi.unenrollFactor(factor.id)
                    } catch (e: Exception) {
                        // Squelch cleanup error
                    }
                }

                // 3. Start enrollment
                val enrollment = client.authApi.enrollFactor(
                    EnrollFactorRequest(friendlyName = myUsername)
                )

                _enrollmentFactorId.value = enrollment.id
                _enrollmentSecret.value = enrollment.totp?.secret
                _enrollmentQrCode.value = enrollment.totp?.qrCode
            } catch (e: Exception) {
                _enrollmentError.value = "Gagal memulai pendaftaran 2FA: ${e.localizedMessage}"
            }
        }
    }

    fun confirm2faEnrollment(code: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val factorId = _enrollmentFactorId.value ?: return
        viewModelScope.launch {
            try {
                // 1. Challenge factor
                val challenge = client.authApi.challengeFactor(factorId)

                // 2. Verify challenge
                client.authApi.verifyChallenge(
                    factorId = factorId,
                    body = VerifyFactorRequest(challengeId = challenge.id, code = code)
                )

                // Verification succeeded! Update local status
                _is2faEnabled.value = true
                _isEnrolling2fa.value = false
                _enrollmentFactorId.value = null
                _enrollmentSecret.value = null
                _enrollmentQrCode.value = null

                onSuccess()
                check2faStatus()
            } catch (e: Exception) {
                onError("Gagal memverifikasi kode 2FA: ${e.localizedMessage}")
            }
        }
    }

    fun cancel2faEnrollment() {
        val factorId = _enrollmentFactorId.value
        _isEnrolling2fa.value = false
        _enrollmentFactorId.value = null
        _enrollmentSecret.value = null
        _enrollmentQrCode.value = null
        _enrollmentError.value = null

        if (factorId != null) {
            viewModelScope.launch {
                try {
                    client.authApi.unenrollFactor(factorId)
                } catch (e: Exception) {
                    // Squelch
                }
            }
        }
    }

    fun disable2fa(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val factors = client.authApi.listFactors()
                val verifiedTotp = factors.firstOrNull { it.factorType == "totp" && it.status == "verified" }
                if (verifiedTotp != null) {
                    client.authApi.unenrollFactor(verifiedTotp.id)
                    _is2faEnabled.value = false
                    onSuccess()
                } else {
                    _is2faEnabled.value = false
                    onSuccess()
                }
            } catch (e: Exception) {
                onError("Gagal menonaktifkan 2FA: ${e.localizedMessage}")
            }
        }
    }

    // --- COMMENTS ACTIONS ---
    fun setReplyTarget(comment: Comment?) {
        _replyTargetComment.value = comment
    }

    fun fetchComments(postId: String) {
        viewModelScope.launch {
            try {
                val postComments = client.dbApi.getCommentsForPost(postIdFilter = "eq.$postId")
                _comments.value = postComments
            } catch (e: Exception) {
                _comments.value = emptyList()
            }
        }
    }

    fun addComment(postId: String, content: String, onSuccess: () -> Unit) {
        val myId = sessionManager.userId ?: return
        if (content.isBlank()) return

        viewModelScope.launch {
            _isSubmittingComment.value = true
            try {
                val commentId = UUID.randomUUID().toString()
                val parentId = _replyTargetComment.value?.id

                val newComment = Comment(
                    id = commentId,
                    postId = postId,
                    userId = myId,
                    parentId = parentId,
                    content = content.take(1000), // DB limit
                    createdAt = Instant.now().toString()
                )

                client.dbApi.createComment(newComment)

                // Refresh comments
                fetchComments(postId)

                // Locally increment comments count on the post in lists to meet requirement:
                // "After posting, increment the visible comment count on the parent post in the feed/explore/profile list"
                val listsToUpdate = listOf(_posts, _explorePosts, _profilePosts)
                listsToUpdate.forEach { flow ->
                    val updatedList = flow.value.map { post ->
                        if (post.id == postId) {
                            post.copy(commentsCount = post.commentsCount + 1)
                        } else {
                            post
                        }
                    }
                    flow.value = updatedList
                }

                _replyTargetComment.value = null
                _isSubmittingComment.value = false
                onSuccess()
            } catch (e: Exception) {
                _isSubmittingComment.value = false
            }
        }
    }

    fun likeCommentToggle(comment: Comment) {
        val myId = sessionManager.userId ?: return
        viewModelScope.launch {
            val isLiked = _likedCommentIds.value.contains(comment.id)
            val updatedLiked = _likedCommentIds.value.toMutableSet()
            val commentsList = _comments.value.toMutableList()

            // Optimistic update
            if (isLiked) {
                updatedLiked.remove(comment.id)
                val idx = commentsList.indexOfFirst { it.id == comment.id }
                if (idx != -1) {
                    commentsList[idx] = commentsList[idx].copy(likesCount = (commentsList[idx].likesCount - 1).coerceAtLeast(0))
                }
            } else {
                updatedLiked.add(comment.id)
                val idx = commentsList.indexOfFirst { it.id == comment.id }
                if (idx != -1) {
                    commentsList[idx] = commentsList[idx].copy(likesCount = commentsList[idx].likesCount + 1)
                }
            }
            _likedCommentIds.value = updatedLiked
            _comments.value = commentsList

            try {
                if (isLiked) {
                    client.dbApi.unlikeComment(userFilter = "eq.$myId", commentFilter = "eq.${comment.id}")
                } else {
                    client.dbApi.likeComment(CommentLike(userId = myId, commentId = comment.id))
                }
                fetchLikesAndFollows()
            } catch (e: Exception) {
                // Revert
                fetchComments(comment.postId)
            }
        }
    }

    // --- NOTIFICATIONS ACTIONS ---
    fun fetchNotifications() {
        val myId = sessionManager.userId ?: return
        viewModelScope.launch {
            try {
                val notifs = client.dbApi.getNotifications(recipientIdFilter = "eq.$myId")
                _notifications.value = notifs
                _unreadNotificationsCount.value = notifs.count { !it.isRead }
            } catch (e: Exception) {
                // Squelch background errors
            }
        }
    }

    fun markAllNotificationsAsRead() {
        val myId = sessionManager.userId ?: return
        viewModelScope.launch {
            try {
                client.dbApi.markNotificationsRead(
                    recipientIdFilter = "eq.$myId",
                    isReadFilter = "eq.false",
                    body = mapOf("is_read" to true)
                )
                // Update local state
                _notifications.value = _notifications.value.map { it.copy(isRead = true) }
                _unreadNotificationsCount.value = 0
            } catch (e: Exception) {
                // Squelch
            }
        }
    }

    // --- ANIME DETAILS (JIKAN API) ---
    fun fetchAnimeDetail(malId: Int) {
        viewModelScope.launch {
            _isAnimeDetailLoading.value = true
            _activeAnimeDetail.value = null
            _activeAnimeEpisodes.value = emptyList()
            _activeAnimeCharacters.value = emptyList()

            try {
                // 1. Fetch info
                val detail = client.jikanApi.getAnimeDetail(malId)
                _activeAnimeDetail.value = detail.data

                // 2. Fetch episodes
                try {
                    val episodes = client.jikanApi.getAnimeEpisodes(malId)
                    _activeAnimeEpisodes.value = episodes.data
                } catch (e: Exception) {
                    _activeAnimeEpisodes.value = emptyList()
                }

                // 3. Fetch characters
                try {
                    val chars = client.jikanApi.getAnimeCharacters(malId)
                    _activeAnimeCharacters.value = chars.data ?: emptyList()
                } catch (e: Exception) {
                    _activeAnimeCharacters.value = emptyList()
                }

            } catch (e: Exception) {
                // Detail fails
            } finally {
                _isAnimeDetailLoading.value = false
            }
        }
    }

    fun closeAnimeDetail() {
        _activeAnimeDetail.value = null
        _activeAnimeEpisodes.value = emptyList()
        _activeAnimeCharacters.value = emptyList()
    }
}
