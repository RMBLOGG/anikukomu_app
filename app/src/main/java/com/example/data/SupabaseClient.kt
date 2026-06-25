package com.example.data

import android.content.Context
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

// ==========================================
// API INTERFACES
// ==========================================

interface SupabaseAuthApi {
    @POST("signup")
    suspend fun signUp(
        @Body body: SupabaseSignUpRequest
    ): SupabaseAuthResponse

    @POST("token")
    suspend fun signIn(
        @Query("grant_type") grantType: String = "password",
        @Body body: SupabaseAuthRequest
    ): SupabaseAuthResponse

    @GET("user")
    suspend fun getCurrentUser(): SupabaseUser

    @GET("factors")
    suspend fun listFactors(): List<SupabaseFactor>

    @POST("factors")
    suspend fun enrollFactor(
        @Body body: EnrollFactorRequest
    ): SupabaseFactor

    @POST("factors/{factorId}/challenge")
    suspend fun challengeFactor(
        @Path("factorId") factorId: String
    ): ChallengeResponse

    @POST("factors/{factorId}/verify")
    suspend fun verifyChallenge(
        @Path("factorId") factorId: String,
        @Body body: VerifyFactorRequest
    ): SupabaseAuthResponse

    @DELETE("factors/{factorId}")
    suspend fun unenrollFactor(
        @Path("factorId") factorId: String
    )
}

interface SupabaseDbApi {
    @GET("profiles")
    suspend fun getProfile(
        @Query("id") idFilter: String,
        @Query("select") select: String = "*"
    ): List<Profile>

    @POST("profiles")
    @Headers("Prefer: return=representation")
    suspend fun createProfile(
        @Body profile: Profile
    ): List<Profile>

    @PATCH("profiles")
    @Headers("Prefer: return=representation")
    suspend fun updateProfile(
        @Query("id") idFilter: String,
        @Body profile: Map<String, String?>
    ): List<Profile>

    @GET("posts")
    suspend fun getPosts(
        @Query("select") select: String = "*,profiles(*),post_anime_tags(anime_id,animes(*))",
        @Query("order") order: String = "created_at.desc"
    ): List<Post>

    @GET("posts")
    suspend fun getPostsForUser(
        @Query("user_id") userIdFilter: String,
        @Query("select") select: String = "*,profiles(*),post_anime_tags(anime_id,animes(*))",
        @Query("order") order: String = "created_at.desc"
    ): List<Post>

    @GET("posts")
    suspend fun getPostsInUserIds(
        @Query("user_id") userIdInFilter: String, // e.g. "in.(uuid1,uuid2)"
        @Query("select") select: String = "*,profiles(*),post_anime_tags(anime_id,animes(*))",
        @Query("order") order: String = "created_at.desc"
    ): List<Post>

    @POST("posts")
    @Headers("Prefer: return=representation")
    suspend fun createPost(
        @Body post: Post
    ): List<Post>

    @DELETE("posts")
    suspend fun deletePost(
        @Query("id") idFilter: String
    )

    @GET("stories")
    suspend fun getStories(
        @Query("expires_at") expiresAtFilter: String, // e.g. "gt.2026-06-25T00:00:00Z"
        @Query("select") select: String = "*,profiles(*)",
        @Query("order") order: String = "created_at.desc"
    ): List<Story>

    @POST("stories")
    @Headers("Prefer: return=representation")
    suspend fun createStory(
        @Body story: Story
    ): List<Story>

    @DELETE("stories")
    suspend fun deleteStory(
        @Query("id") idFilter: String
    )

    @GET("likes")
    suspend fun getUserLikes(
        @Query("user_id") userIdFilter: String
    ): List<Like>

    @POST("likes")
    suspend fun likePost(
        @Body like: Like
    )

    @DELETE("likes")
    suspend fun unlikePost(
        @Query("user_id") userFilter: String,
        @Query("post_id") postFilter: String
    )

    @GET("comments")
    suspend fun getCommentsForPost(
        @Query("post_id") postIdFilter: String,
        @Query("select") select: String = "*,profiles(*)",
        @Query("order") order: String = "created_at.asc"
    ): List<Comment>

    @POST("comments")
    @Headers("Prefer: return=representation")
    suspend fun createComment(
        @Body comment: Comment
    ): List<Comment>

    @GET("comment_likes")
    suspend fun getUserCommentLikes(
        @Query("user_id") userIdFilter: String
    ): List<CommentLike>

    @POST("comment_likes")
    suspend fun likeComment(
        @Body commentLike: CommentLike
    )

    @DELETE("comment_likes")
    suspend fun unlikeComment(
        @Query("user_id") userFilter: String,
        @Query("comment_id") commentFilter: String
    )

    @GET("follows")
    suspend fun getUserFollows(
        @Query("follower_id") followerIdFilter: String
    ): List<Follow>

    @GET("follows")
    suspend fun getUserFollowers(
        @Query("following_id") followingIdFilter: String
    ): List<Follow>

    @POST("follows")
    suspend fun followUser(
        @Body follow: Follow
    )

    @DELETE("follows")
    suspend fun unfollowUser(
        @Query("follower_id") followerFilter: String,
        @Query("following_id") followingFilter: String
    )

    @GET("notifications")
    suspend fun getNotifications(
        @Query("recipient_id") recipientIdFilter: String,
        @Query("select") select: String = "*,profiles:actor_id(*)",
        @Query("order") order: String = "created_at.desc"
    ): List<Notification>

    @PATCH("notifications")
    suspend fun markNotificationsRead(
        @Query("recipient_id") recipientIdFilter: String,
        @Query("is_read") isReadFilter: String = "eq.false",
        @Body body: Map<String, Boolean> // e.g. { "is_read": true }
    )

    @POST("post_anime_tags")
    suspend fun createPostAnimeTags(
        @Body tags: List<PostAnimeTag>
    )

    @POST("animes")
    @Headers("Prefer: resolution=ignore-duplicates")
    suspend fun upsertAnime(
        @Body anime: Anime
    )
}

interface VercelApi {
    @Multipart
    @POST("api/upload")
    suspend fun uploadImage(
        @Query("folder") folder: String = "posts",
        @Part image: MultipartBody.Part
    ): UploadResponse

    @POST("api/post/cleanup-image")
    suspend fun cleanupImage(
        @Body request: CleanupRequest
    )

    @GET("api/anime/search")
    suspend fun searchAnime(
        @Query("q") query: String
    ): List<Anime>
}

interface JikanApi {
    @GET("anime/{mal_id}")
    suspend fun getAnimeDetail(
        @Path("mal_id") malId: Int
    ): JikanAnimeResponse

    @GET("anime/{mal_id}/episodes")
    suspend fun getAnimeEpisodes(
        @Path("mal_id") malId: Int
    ): JikanEpisodesResponse

    @GET("anime/{mal_id}/characters")
    suspend fun getAnimeCharacters(
        @Path("mal_id") malId: Int
    ): JikanCharactersResponse
}

// ==========================================
// CLIENT BUILDER
// ==========================================

class SupabaseClient(private val context: Context) {
    val sessionManager = SessionManager(context)

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val supabaseUrl = "https://sfyyljfpnutskeenlysb.supabase.co/"
    private val anonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNmeXlsamZwbnV0c2tlZW5seXNiIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODIxNDAzNTcsImV4cCI6MjA5NzcxNjM1N30.Qaecy5q6GyI_pbG-LgtzqXJrTtu6AXMshA20P20n2pc" 

    private val baseOkHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val headerInterceptor = Interceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("apikey", anonKey)
                .header("Content-Type", "application/json")

            sessionManager.accessToken?.let { token ->
                requestBuilder.header("Authorization", "Bearer $token")
            }

            chain.proceed(requestBuilder.build())
        }

        OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .addInterceptor(logging)
            .build()
    }

    private val vercelOkHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val headerInterceptor = Interceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()

            sessionManager.accessToken?.let { token ->
                requestBuilder.header("Authorization", "Bearer $token")
            }

            chain.proceed(requestBuilder.build())
        }

        OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .addInterceptor(logging)
            .build()
    }

    private val jikanOkHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    val authApi: SupabaseAuthApi by lazy {
        Retrofit.Builder()
            .baseUrl("${supabaseUrl}auth/v1/")
            .client(baseOkHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SupabaseAuthApi::class.java)
    }

    val dbApi: SupabaseDbApi by lazy {
        Retrofit.Builder()
            .baseUrl("${supabaseUrl}rest/v1/")
            .client(baseOkHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SupabaseDbApi::class.java)
    }

    val vercelApi: VercelApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://anikukomu.vercel.app/")
            .client(vercelOkHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(VercelApi::class.java)
    }

    val jikanApi: JikanApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.jikan.moe/v4/")
            .client(jikanOkHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(JikanApi::class.java)
    }

    companion object {
        @Volatile
        private var INSTANCE: SupabaseClient? = null

        fun getInstance(context: Context): SupabaseClient {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SupabaseClient(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
