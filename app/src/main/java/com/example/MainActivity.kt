package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.FirebaseAdminBridge
import com.example.model.AppLanguage
import com.example.model.ScreenRoute
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.NexEarnTheme
import com.example.ui.theme.SuccessGreen
import com.example.viewmodel.NexEarnViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseAdminBridge.ensureFirebaseInitialized(this)
        enableEdgeToEdge()
        setContent {
            val vm: NexEarnViewModel = viewModel()
            val themeMode by vm.themeMode.collectAsState()
            val lang by vm.language.collectAsState()

            NexEarnTheme(themeMode = themeMode) {
                NexEarnAppRoot(viewModel = vm, lang = lang)
            }
        }
    }
}

@Composable
fun NexEarnAppRoot(
    viewModel: NexEarnViewModel,
    lang: AppLanguage
) {
    val currentUserUid by viewModel.currentUserUid.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val feedbackMessage by viewModel.feedbackMessage.collectAsState()
    val showLogoutDialog by viewModel.showLogoutDialog.collectAsState()

    val notifications by viewModel.notifications.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val friendRequests by viewModel.friendRequests.collectAsState()
    val users by viewModel.users.collectAsState()

    // Handle system back navigation across sub-screens
    BackHandler(enabled = currentUserUid != null && currentScreen != ScreenRoute.HOME) {
        viewModel.navigateBack()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (currentUserUid == null) {
            AuthScreen(viewModel = viewModel, lang = lang)
        } else {
            val currentUser = users.find { it.uid == currentUserUid } ?: users.firstOrNull()
            if (currentUser == null) {
                AuthScreen(viewModel = viewModel, lang = lang)
                return@Box
            }
            val unreadNotifs = notifications.count { it.targetUid == currentUser.uid && !it.isRead }
            val unreadMsgs = conversations.sumOf { it.unreadCount }
            val pendingRequests = friendRequests.count { it.toUid == currentUser.uid && it.status == "PENDING" }

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val isWideScreen = maxWidth >= 680.dp

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    topBar = {
                        NexEarnTopHeader(
                            lang = lang,
                            currentBalance = currentUser.currentBalance,
                            unreadNotifications = unreadNotifs,
                            unreadMessages = unreadMsgs,
                            userName = currentUser.fullName,
                            userAvatarColor = currentUser.avatarColorHex,
                            onLogoClick = { viewModel.navigateTo(ScreenRoute.HOME) },
                            onSearchClick = { viewModel.navigateTo(ScreenRoute.GLOBAL_SEARCH) },
                            onNotificationsClick = { viewModel.navigateTo(ScreenRoute.NOTIFICATIONS) },
                            onMessagesClick = { viewModel.navigateTo(ScreenRoute.MESSAGES) },
                            onProfileClick = { viewModel.openMyProfile() }
                        )
                    },
                    bottomBar = {
                        if (!isWideScreen) {
                            NexEarnBottomBar(
                                lang = lang,
                                currentRoute = currentScreen,
                                pendingFriendRequests = pendingRequests,
                                onNavigate = { viewModel.navigateTo(it) }
                            )
                        }
                    }
                ) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (isWideScreen) {
                            NexEarnSideRail(
                                lang = lang,
                                currentRoute = currentScreen,
                                pendingFriendRequests = pendingRequests,
                                onNavigate = { viewModel.navigateTo(it) }
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .widthIn(max = 780.dp)
                            ) {
                                when (currentScreen) {
                                    ScreenRoute.HOME -> HomeScreen(viewModel = viewModel, lang = lang)
                                    ScreenRoute.CREATE_POST -> CreatePostScreen(viewModel = viewModel, lang = lang)
                                    ScreenRoute.FRIENDS -> FriendsScreen(viewModel = viewModel, lang = lang)
                                    ScreenRoute.PROFILE -> ProfileScreen(viewModel = viewModel, lang = lang)
                                    ScreenRoute.WORK -> WorkScreen(viewModel = viewModel, lang = lang)
                                    ScreenRoute.GIFTS -> GiftsAndMissionsScreen(viewModel = viewModel, lang = lang)
                                    ScreenRoute.MENU -> MenuScreen(viewModel = viewModel, lang = lang)
                                    ScreenRoute.POST_RANK -> PostRankScreen(viewModel = viewModel, lang = lang)
                                    ScreenRoute.INCOME_RANK -> IncomeRankScreen(viewModel = viewModel, lang = lang)
                                    ScreenRoute.GROUPS -> GroupsScreen(viewModel = viewModel, lang = lang, onlyJoinedGroups = false)
                                    ScreenRoute.JOINED_GROUPS -> GroupsScreen(viewModel = viewModel, lang = lang, onlyJoinedGroups = true)
                                    ScreenRoute.GROUP_DETAILS -> GroupDetailsScreen(viewModel = viewModel, lang = lang)
                                    ScreenRoute.NOTIFICATIONS -> NotificationsScreen(viewModel = viewModel, lang = lang)
                                    ScreenRoute.MESSAGES -> MessagesInboxScreen(viewModel = viewModel, lang = lang)
                                    ScreenRoute.CHAT_WINDOW -> ChatWindowScreen(viewModel = viewModel, lang = lang)
                                    ScreenRoute.SETTINGS -> SettingsScreen(viewModel = viewModel, lang = lang, themeMode = themeMode)
                                    ScreenRoute.TOTAL_USERS -> TotalUsersScreen(viewModel = viewModel, lang = lang)
                                    ScreenRoute.SUPPORT -> SupportScreen(viewModel = viewModel, lang = lang)
                                    ScreenRoute.GLOBAL_SEARCH -> GlobalSearchScreen(viewModel = viewModel, lang = lang)
                                    ScreenRoute.FIREBASE_BRIDGE -> FirebaseBridgeScreen(lang = lang)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 19: Logout Confirmation Dialog (Cancel / Log Out)
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissLogoutConfirmation() },
                title = {
                    Text(tr(lang, "লগআউট নিশ্চিতকরণ (Confirm Logout)", "Confirm Logout"))
                },
                text = {
                    Text(
                        tr(
                            lang,
                            "আপনি কি নিশ্চিতভাবে বর্তমান সেশন বাতিল করে লগআউট করতে চান?",
                            "Are you sure you want to end your session and log out?"
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.confirmLogout() },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                    ) {
                        Text(tr(lang, "Log Out (লগআউট)", "Log Out"))
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { viewModel.dismissLogoutConfirmation() }) {
                        Text(tr(lang, "Cancel (বাতিল)", "Cancel"))
                    }
                }
            )
        }

        // Feedback Banner
        AnimatedVisibility(
            visible = feedbackMessage != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 64.dp, start = 16.dp, end = 16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF0F172A),
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen
                    )
                    Text(
                        text = feedbackMessage ?: "",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
