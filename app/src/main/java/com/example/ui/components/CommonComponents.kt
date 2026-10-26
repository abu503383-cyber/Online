package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.ScreenRoute
import com.example.ui.theme.GoldCoin
import com.example.ui.theme.NexBluePrimary
import com.example.ui.theme.NexPurpleSecondary
import com.example.ui.theme.SuccessGreen

@Composable
fun tr(lang: AppLanguage, bn: String, en: String): String {
    return if (lang == AppLanguage.BN) bn else en
}

@Composable
fun UserAvatar(
    name: String,
    colorHex: Long,
    size: Dp = 44.dp,
    isOnline: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val initials = name.trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "U" }

    Box(
        modifier = Modifier
            .size(size)
            .then(if (onClick != null) Modifier.clip(CircleShape).clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(colorHex),
                            Color(colorHex).copy(alpha = 0.78f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.36f).sp
            )
        }
        if (isOnline) {
            Box(
                modifier = Modifier
                    .size((size.value * 0.28f).dp.coerceAtLeast(10.dp))
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(SuccessGreen)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
        }
    }
}

@Composable
fun CoinBadge(
    coins: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = GoldCoin.copy(alpha = 0.16f),
        border = androidx.compose.foundation.BorderStroke(1.dp, GoldCoin.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.MonetizationOn,
                contentDescription = "Coins",
                tint = Color(0xFFD97706),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "$coins Coins",
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFFB45309),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Section 3: Header & Navigation
 * Application Logo (Home), Search Bar, Notification Icon (Unread Badge),
 * Message Icon (Unread Badge), Profile Icon.
 */
@Composable
fun NexEarnTopHeader(
    lang: AppLanguage,
    currentBalance: Int,
    unreadNotifications: Int,
    unreadMessages: Int,
    userName: String,
    userAvatarColor: Long,
    onLogoClick: () -> Unit,
    onSearchClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onMessagesClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Surface(
        tonalElevation = 3.dp,
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Application Logo -> Click goes to Home
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onLogoClick() }
                        .padding(vertical = 4.dp, horizontal = 4.dp)
                        .testTag("header_logo_button"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(NexBluePrimary, NexPurpleSecondary)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "N",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                    }
                    Column {
                        Text(
                            text = "NexEarn",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "$currentBalance Coins",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFD97706),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Search Bar Pill
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onSearchClick() }
                        .testTag("header_search_bar"),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = tr(lang, "খুঁজুন (ইউজার, পোস্ট, গ্রুপ, কাজ)...", "Search users, posts, jobs..."),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Action Icons: Notification, Message, Profile
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = onNotificationsClick,
                        modifier = Modifier.testTag("header_notifications_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotifications > 0) {
                                    Badge(containerColor = MaterialTheme.colorScheme.error) {
                                        Text("$unreadNotifications")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notifications"
                            )
                        }
                    }

                    IconButton(
                        onClick = onMessagesClick,
                        modifier = Modifier.testTag("header_messages_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadMessages > 0) {
                                    Badge(containerColor = NexPurpleSecondary) {
                                        Text("$unreadMessages")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ChatBubbleOutline,
                                contentDescription = "Messages"
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .testTag("header_profile_button")
                    ) {
                        UserAvatar(
                            name = userName,
                            colorHex = userAvatarColor,
                            size = 36.dp,
                            isOnline = true,
                            onClick = onProfileClick
                        )
                    }
                }
            }
        }
    }
}

data class BottomNavItem(
    val route: ScreenRoute,
    val labelBn: String,
    val labelEn: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val isAccentCenter: Boolean = false
)

val mainBottomNavItems = listOf(
    BottomNavItem(ScreenRoute.HOME, "হোম", "Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(ScreenRoute.FRIENDS, "বন্ধু", "Friends", Icons.Filled.People, Icons.Outlined.PeopleOutline),
    BottomNavItem(ScreenRoute.CREATE_POST, "পোস্ট (+)", "Post (+)", Icons.Filled.AddCircle, Icons.Outlined.AddCircleOutline, isAccentCenter = true),
    BottomNavItem(ScreenRoute.WORK, "কাজ ও আয়", "Work", Icons.Filled.Work, Icons.Outlined.WorkOutline),
    BottomNavItem(ScreenRoute.MENU, "মেনু", "Menu", Icons.Filled.Menu, Icons.Outlined.Menu)
)

@Composable
fun NexEarnBottomBar(
    lang: AppLanguage,
    currentRoute: ScreenRoute,
    pendingFriendRequests: Int,
    onNavigate: (ScreenRoute) -> Unit
) {
    NavigationBar(
        modifier = Modifier.navigationBarsPadding(),
        tonalElevation = 6.dp
    ) {
        mainBottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                modifier = Modifier.testTag("bottom_nav_${item.route.name.lowercase()}"),
                icon = {
                    if (item.route == ScreenRoute.FRIENDS && pendingFriendRequests > 0) {
                        BadgedBox(
                            badge = {
                                Badge { Text("$pendingFriendRequests") }
                            }
                        ) {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.labelEn
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.labelEn,
                            tint = if (item.isAccentCenter && !selected) NexPurpleSecondary else LocalContentColor.current
                        )
                    }
                },
                label = {
                    Text(
                        text = tr(lang, item.labelBn, item.labelEn),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    }
}

@Composable
fun NexEarnSideRail(
    lang: AppLanguage,
    currentRoute: ScreenRoute,
    pendingFriendRequests: Int,
    onNavigate: (ScreenRoute) -> Unit
) {
    NavigationRail(
        modifier = Modifier.fillMaxHeight(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        mainBottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationRailItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    if (item.route == ScreenRoute.FRIENDS && pendingFriendRequests > 0) {
                        BadgedBox(badge = { Badge { Text("$pendingFriendRequests") } }) {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.labelEn
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.labelEn
                        )
                    }
                },
                label = { Text(tr(lang, item.labelBn, item.labelEn)) }
            )
        }
    }
}

@Composable
fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (actionLabel != null && onAction != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Button(onClick = onAction) {
                    Text(actionLabel)
                }
            }
        }
    }
}
