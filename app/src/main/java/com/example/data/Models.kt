package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ==========================================
// AUTH ENTITIES
// ==========================================

@JsonClass(generateAdapter = true)
data class SupabaseAuthRequest(
    val email: String,
    val password: String,
    val data: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseAuthResponse(
    @Json(name = "access_token") val accessToken: String?,
    @Json(name = "refresh_token") val refreshToken: String?,
    @Json(name = "expires_in") val expiresIn: Long?,
    @Json(name = "token_type") val tokenType: String?,
    val user: SupabaseUser?
)

@JsonClass(generateAdapter = true)
data class SupabaseUser(
    val id: String,
    val email: String?,
    @Json(name = "user_metadata") val userMetadata: Map<String, Any>?,
    val factors: List<SupabaseFactor>?
)

@JsonClass(generateAdapter = true)
data class SupabaseFactor(
    val id: String,
    @Json(name = "factor_type") val factorType: String,
    val status: String,
    @Json(name = "friendly_name") val friendlyName: String?,
    val totp: SupabaseTotpDetails?
)

@JsonClass(generateAdapter = true)
data class SupabaseTotpDetails(
    @Json(name = "qr_code") val qrCode: String?,
    val secret: String?
)

@JsonClass(generateAdapter = true)
data class EnrollFactorRequest(
    @Json(name = "friendly_name") val friendlyName: String,
    @Json(name = "factor_type") val factorType: String = "totp"
)

@JsonClass(generateAdapter = true)
data class VerifyFactorRequest(
    @Json(name = "challenge_id") val challengeId: String,
    val code: String
)

@JsonClass(generateAdapter = true)
data class ChallengeResponse(
    val id: String,
    val type: String
)

// ==========================================
// DATABASE / POSTGREST ENTITIES
// ==========================================

@JsonClass(generateAdapter = true)
data class Profile(
    val id: String,
    val username: String,
    @Json(name = "display_name") val displayName: String?,
    @Json(name = "avatar_url") val avatarUrl: String?,
    val bio: String?,
    @Json(name = "website_url") val websiteUrl: String?,
    @Json(name = "twitter_url") val twitterUrl: String?,
    @Json(name = "instagram_url") val instagramUrl: String?,
    @Json(name = "followers_count") val followersCount: Int = 0,
    @Json(name = "following_count") val followingCount: Int = 0,
    @Json(name = "posts_count") val postsCount: Int = 0,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class Anime(
    val id: Long,
    @Json(name = "mal_id") val malId: Int?,
    val title: String?,
    @Json(name = "cover_url") val coverUrl: String?,
    val genre: List<String>? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class PostAnimeTag(
    @Json(name = "post_id") val postId: String,
    @Json(name = "anime_id") val animeId: Long,
    val animes: Anime? = null
)

@JsonClass(generateAdapter = true)
data class Post(
    val id: String,
    @Json(name = "user_id") val userId: String,
    val caption: String?,
    @Json(name = "image_url") val imageUrl: String?,
    @Json(name = "image_public_id") val imagePublicId: String?,
    @Json(name = "likes_count") val likesCount: Int = 0,
    @Json(name = "comments_count") val commentsCount: Int = 0,
    @Json(name = "created_at") val createdAt: String,
    val profiles: Profile? = null,
    @Json(name = "post_anime_tags") val postAnimeTags: List<PostAnimeTag>? = null
)

@JsonClass(generateAdapter = true)
data class Story(
    val id: String,
    @Json(name = "user_id") val userId: String,
    @Json(name = "image_url") val imageUrl: String,
    @Json(name = "image_public_id") val imagePublicId: String?,
    @Json(name = "expires_at") val expiresAt: String,
    @Json(name = "created_at") val createdAt: String,
    val profiles: Profile? = null
)

@JsonClass(generateAdapter = true)
data class Follow(
    @Json(name = "follower_id") val followerId: String,
    @Json(name = "following_id") val followingId: String,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class Like(
    @Json(name = "user_id") val userId: String,
    @Json(name = "post_id") val postId: String,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class Comment(
    val id: String,
    @Json(name = "post_id") val postId: String,
    @Json(name = "user_id") val userId: String,
    @Json(name = "parent_id") val parentId: String?,
    val content: String,
    @Json(name = "likes_count") val likesCount: Int = 0,
    @Json(name = "created_at") val createdAt: String,
    val profiles: Profile? = null
)

@JsonClass(generateAdapter = true)
data class CommentLike(
    @Json(name = "user_id") val userId: String,
    @Json(name = "comment_id") val commentId: String
)

@JsonClass(generateAdapter = true)
data class Notification(
    val id: String,
    @Json(name = "recipient_id") val recipientId: String,
    @Json(name = "actor_id") val actorId: String,
    val type: String, // 'like', 'follow', 'comment'
    @Json(name = "post_id") val postId: String?,
    @Json(name = "comment_id") val commentId: String?,
    @Json(name = "is_read") val isRead: Boolean = false,
    @Json(name = "created_at") val createdAt: String,
    val profiles: Profile? = null // Wait: actor's profile is joined as profiles because recipient_id or actor_id references profiles(id)
)

// ==========================================
// CUSTOM API UPLOAD RESPONSES
// ==========================================

@JsonClass(generateAdapter = true)
data class UploadResponse(
    val url: String,
    @Json(name = "public_id") val publicId: String
)

@JsonClass(generateAdapter = true)
data class CleanupRequest(
    @Json(name = "post_id") val postId: String
)

// ==========================================
// JIKAN API ENTITIES (MyAnimeList)
// ==========================================

@JsonClass(generateAdapter = true)
data class JikanSearchResponse(
    val data: List<JikanAnime>
)

@JsonClass(generateAdapter = true)
data class JikanAnimeResponse(
    val data: JikanAnime
)

@JsonClass(generateAdapter = true)
data class JikanAnime(
    @Json(name = "mal_id") val malId: Int,
    val title: String?,
    val images: JikanImages?,
    val genres: List<JikanGenre>?,
    val synopsis: String?,
    val score: Double?,
    val episodes: Int?,
    val year: Int?,
    val status: String?,
    val studios: List<JikanStudio>?,
    val producers: List<JikanProducer>?,
    val source: String?,
    val duration: String?,
    val rating: String?
)

@JsonClass(generateAdapter = true)
data class JikanImages(
    val jpg: JikanJpgImages?,
    val webp: JikanWebpImages?
)

@JsonClass(generateAdapter = true)
data class JikanJpgImages(
    @Json(name = "image_url") val imageUrl: String?,
    @Json(name = "large_image_url") val largeImageUrl: String?
)

@JsonClass(generateAdapter = true)
data class JikanWebpImages(
    @Json(name = "image_url") val imageUrl: String?,
    @Json(name = "large_image_url") val largeImageUrl: String?
)

@JsonClass(generateAdapter = true)
data class JikanGenre(
    val name: String
)

@JsonClass(generateAdapter = true)
data class JikanStudio(
    val name: String
)

@JsonClass(generateAdapter = true)
data class JikanProducer(
    val name: String
)

@JsonClass(generateAdapter = true)
data class JikanEpisodesResponse(
    val data: List<JikanEpisode>
)

@JsonClass(generateAdapter = true)
data class JikanEpisode(
    @Json(name = "mal_id") val malId: Int,
    val title: String?,
    val aired: String?,
    val filler: Boolean?,
    val recap: Boolean?
)

@JsonClass(generateAdapter = true)
data class JikanCharactersResponse(
    val data: List<JikanCharacterEntry>
)

@JsonClass(generateAdapter = true)
data class JikanCharacterEntry(
    val character: JikanCharacter?,
    val role: String?,
    @Json(name = "voice_actors") val voiceActors: List<JikanVoiceActorEntry>?
)

@JsonClass(generateAdapter = true)
data class JikanCharacter(
    val name: String?,
    val images: JikanCharacterImages?
)

@JsonClass(generateAdapter = true)
data class JikanCharacterImages(
    val jpg: JikanCharacterJpg?
)

@JsonClass(generateAdapter = true)
data class JikanCharacterJpg(
    @Json(name = "image_url") val imageUrl: String?
)

@JsonClass(generateAdapter = true)
data class JikanVoiceActorEntry(
    val person: JikanPerson?,
    val language: String?
)

@JsonClass(generateAdapter = true)
data class JikanPerson(
    val name: String?,
    val images: JikanPersonImages?
)

@JsonClass(generateAdapter = true)
data class JikanPersonImages(
    val jpg: JikanPersonJpg?
)

@JsonClass(generateAdapter = true)
data class JikanPersonJpg(
    @Json(name = "image_url") val imageUrl: String?
)
