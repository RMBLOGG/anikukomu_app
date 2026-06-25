package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.AuthViewModel
import com.example.ui.MainViewModel
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppEntry()
            }
        }
    }
}

@Composable
fun MainAppEntry() {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel()
    val mainViewModel: MainViewModel = viewModel()

    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val isCheckingSession by authViewModel.isCheckingSession.collectAsState()

    var showRegisterScreen by remember { mutableStateOf(false) }

    // On start, check local session
    LaunchedEffect(Unit) {
        authViewModel.checkSession()
    }

    if (isCheckingSession) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundBase),
            contentAlignment = Alignment.Center
        ) {
            GradientLoading()
        }
    } else if (!isLoggedIn) {
        if (showRegisterScreen) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { showRegisterScreen = false }
            )
        } else {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { showRegisterScreen = true },
                onLoginSuccess = {
                    authViewModel.checkSession()
                }
            )
        }
    } else {
        // Authenticated Main Scaffolding
        MainAppScaffolding(viewModel = mainViewModel, onLogout = { authViewModel.logout() })
    }
}

@Composable
fun MainAppScaffolding(
    viewModel: MainViewModel,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var currentProfileUserId by remember { mutableStateOf(viewModel.sessionManager.userId ?: "") }
    var isEditingProfile by remember { mutableStateOf(false) }

    // Track active overlays (comment sheet, anime detail sheet, story viewer)
    var activeCommentPost by remember { mutableStateOf<Post?>(null) }
    var activeAnimeMalId by remember { mutableStateOf<Int?>(null) }
    var activeStoryGroupList by remember { mutableStateOf<List<Story>?>(null) }
    var activeStoryGroupIndex by remember { mutableStateOf(0) }

    val notifications by viewModel.notifications.collectAsState()
    val unreadNotifCount = remember(notifications) { notifications.count { !it.isRead } }

    // Intercept hardware back button to handle nested view states gracefully
    BackHandler(enabled = selectedTab != 0 || isEditingProfile || currentProfileUserId != (viewModel.sessionManager.userId ?: "")) {
        if (isEditingProfile) {
            isEditingProfile = false
        } else if (currentProfileUserId != (viewModel.sessionManager.userId ?: "")) {
            currentProfileUserId = viewModel.sessionManager.userId ?: ""
        } else {
            selectedTab = 0
        }
    }

    // Refresh notification counts periodically on background
    LaunchedEffect(Unit) {
        viewModel.fetchNotifications()
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceCard,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .border(width = 1.dp, color = BorderDivider, shape = RoundedCornerShape(0.dp))
                    .height(84.dp)
            ) {
                // Tab 1: Home
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        isEditingProfile = false
                        currentProfileUserId = viewModel.sessionManager.userId ?: ""
                    },
                    icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Beranda") },
                    label = { Text("Beranda", fontSize = 11.sp, fontFamily = LabelFontFamily) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryAccent,
                        selectedTextColor = PrimaryAccent,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = PrimaryAccent.copy(alpha = 0.15f)
                    )
                )

                // Tab 2: Explore
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        isEditingProfile = false
                    },
                    icon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Explore") },
                    label = { Text("Explore", fontSize = 11.sp, fontFamily = LabelFontFamily) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryAccent,
                        selectedTextColor = PrimaryAccent,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = PrimaryAccent.copy(alpha = 0.15f)
                    )
                )

                // Tab 3: Post (Interactive Center Plus)
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        isEditingProfile = false
                    },
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(AnikuGradient()),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Post Baru",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    label = { Text("Post", fontSize = 11.sp, fontFamily = LabelFontFamily) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryAccent,
                        selectedTextColor = PrimaryAccent,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = Color.Transparent
                    )
                )

                // Tab 4: Notifications (With Dynamic Badge counter)
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = {
                        selectedTab = 3
                        isEditingProfile = false
                    },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (unreadNotifCount > 0) {
                                    Badge(
                                        containerColor = ErrorRed,
                                        contentColor = Color.White
                                    ) {
                                        Text(text = unreadNotifCount.toString(), fontSize = 10.sp, fontFamily = LabelFontFamily)
                                    }
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notifikasi")
                        }
                    },
                    label = { Text("Notifikasi", fontSize = 11.sp, fontFamily = LabelFontFamily) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryAccent,
                        selectedTextColor = PrimaryAccent,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = PrimaryAccent.copy(alpha = 0.15f)
                    )
                )

                // Tab 5: Profile
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = {
                        selectedTab = 4
                        currentProfileUserId = viewModel.sessionManager.userId ?: ""
                        isEditingProfile = false
                    },
                    icon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Profil") },
                    label = { Text("Profil", fontSize = 11.sp, fontFamily = LabelFontFamily) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryAccent,
                        selectedTextColor = PrimaryAccent,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = PrimaryAccent.copy(alpha = 0.15f)
                    )
                )
            }
        },
        containerColor = BackgroundBase
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> FeedScreen(
                    viewModel = viewModel,
                    onCommentClick = { activeCommentPost = it },
                    onAnimeTagClick = { activeAnimeMalId = it },
                    onStoryClick = { stories, idx ->
                        activeStoryGroupList = stories
                        activeStoryGroupIndex = idx
                    }
                )
                1 -> ExploreScreen(
                    viewModel = viewModel,
                    onPostClick = { activeCommentPost = it }
                )
                2 -> CreatePostScreen(
                    viewModel = viewModel,
                    onSuccess = { selectedTab = 0 }
                )
                3 -> NotificationsScreen(viewModel = viewModel)
                4 -> {
                    if (isEditingProfile) {
                        EditProfileScreen(
                            viewModel = viewModel,
                            onBack = { isEditingProfile = false }
                        )
                    } else {
                        ProfileScreen(
                            userId = currentProfileUserId,
                            viewModel = viewModel,
                            onEditProfileClick = { isEditingProfile = true },
                            onPostClick = { activeCommentPost = it }
                        )
                    }
                }
            }
        }
    }

    // Modal Overlays Manager
    // 1. Threaded Comment bottom sheet
    if (activeCommentPost != null) {
        CommentSheet(
            postId = activeCommentPost!!.id,
            viewModel = viewModel,
            onDismiss = { activeCommentPost = null }
        )
    }

    // 2. Anime Detail bottom sheet
    if (activeAnimeMalId != null) {
        LaunchedEffect(activeAnimeMalId) {
            viewModel.fetchAnimeDetail(activeAnimeMalId!!)
        }

        AnimeDetailSheet(
            viewModel = viewModel,
            onDismiss = { activeAnimeMalId = null }
        )
    }

    // 3. Immersive Story Viewer
    if (activeStoryGroupList != null) {
        StoryViewer(
            allStories = activeStoryGroupList!!,
            initialUserGroupIndex = activeStoryGroupIndex,
            viewModel = viewModel,
            onDismiss = { activeStoryGroupList = null }
        )
    }
}
