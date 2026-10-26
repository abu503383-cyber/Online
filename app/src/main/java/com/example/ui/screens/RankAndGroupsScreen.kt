package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.CoinBadge
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.UserAvatar
import com.example.ui.components.tr
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.NexBluePrimary
import com.example.ui.theme.NexPurpleSecondary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningOrange
import com.example.viewmodel.NexEarnViewModel

private data class MenuOptionEntry(
    val titleBn: String,
    val titleEn: String,
    val subtitleBn: String,
    val subtitleEn: String,
    val icon: ImageVector,
    val accent: Color,
    val route: ScreenRoute? = null,
    val isLogout: Boolean = false,
    val isMyProfile: Boolean = false
)

/**
 * Section 8 & 22: Three-Line Menu Screen (All 14 Menu Options + Firebase Bridge)
 */
@Composable
fun MenuScreen(
    viewModel: NexEarnViewModel,
    lang: AppLanguage
) {
    val currentUser = viewModel.getCurrentUser() ?: return

    val menuItems = listOf(
        MenuOptionEntry("Home (হোম পেজ)", "Home", "নিউজ ফিড ও পোস্ট", "News feed & posts", Icons.Outlined.Home, NexBluePrimary, ScreenRoute.HOME),
        MenuOptionEntry("My Profile (আমার প্রোফাইল)", "My Profile", "ড্যাশবোর্ড ও কয়েন হিস্ট্রি", "Dashboard & Coin History", Icons.Outlined.Person, NexPurpleSecondary, isMyProfile = true),
        MenuOptionEntry("Friend Requests (বন্ধুর অনুরোধ)", "Friend Requests", "প্রাপ্ত ও পেন্ডিং রিকোয়েস্ট", "Incoming & pending requests", Icons.Outlined.PersonAdd, NexBluePrimary, ScreenRoute.FRIENDS),
        MenuOptionEntry("Rank (পোস্ট র‍্যাঙ্কিং)", "Post Rank", "Top 50 Posters ও আপনার অবস্থান", "Top 50 Posters & My Rank", Icons.Outlined.EmojiEvents, WarningOrange, ScreenRoute.POST_RANK),
        MenuOptionEntry("Income Rank (আয়ের র‍্যাঙ্কিং)", "Income Rank", "Top 50 Earners (মোট বৈধ আয়)", "Top 50 Earners Leaderboard", Icons.Outlined.WorkspacePremium, SuccessGreen, ScreenRoute.INCOME_RANK),
        MenuOptionEntry("Messages (চ্যাট ও ইনবক্স)", "Messages", "রিয়েল-টাইম কথোপকথন", "Chat & Inbox", Icons.Outlined.Chat, NexBluePrimary, ScreenRoute.MESSAGES),
        MenuOptionEntry("Groups (সব গ্রুপ)", "Groups", "কমিউনিটি গ্রুপ খুঁজুন ও যোগ দিন", "Explore & join groups", Icons.Outlined.Groups, NexPurpleSecondary, ScreenRoute.GROUPS),
        MenuOptionEntry("My Joined Groups (যোগ দেওয়া গ্রুপ)", "My Joined Groups", "আপনি যে গ্রুপগুলোর সদস্য", "Groups you are a member of", Icons.Outlined.GroupWork, SuccessGreen, ScreenRoute.JOINED_GROUPS),
        MenuOptionEntry("Work (কাজ ও আয়)", "Work", "কাজ সম্পন্ন ও নতুন জব পোস্ট", "Complete tasks & post jobs", Icons.Outlined.WorkOutline, NexBluePrimary, ScreenRoute.WORK),
        MenuOptionEntry("Gifts (মিশন ও পুরস্কার)", "Gifts & Missions", "মিশন সম্পন্ন করে গিফট কয়েন", "Active missions & rewards", Icons.Outlined.CardGiftcard, WarningOrange, ScreenRoute.GIFTS),
        MenuOptionEntry("Settings (অ্যাকাউন্ট ও সেটিংস)", "Settings", "পাসওয়ার্ড, প্রাইভেসি, ভাষা ও থিম", "Account, privacy, language, theme", Icons.Outlined.Settings, NexPurpleSecondary, ScreenRoute.SETTINGS),
        MenuOptionEntry("Total Users (ব্যবহারকারী পরিসংখ্যান)", "Total Users", "সমষ্টিগত প্ল্যাটফর্ম পরিসংখ্যান", "Live platform user stats", Icons.Outlined.Analytics, SuccessGreen, ScreenRoute.TOTAL_USERS),
        MenuOptionEntry("Support (হেল্প ও সাপোর্ট টিকেট)", "Support", "হেল্প সেন্টার, FAQ ও টিকেট জমা", "Help center & support tickets", Icons.Outlined.SupportAgent, NexBluePrimary, ScreenRoute.SUPPORT),
        MenuOptionEntry("Firebase & Admin Bridge", "Firebase & Admin Bridge", "এডমিন প্যানেল ও ফায়ারবেস কালেকশন স্ট্যাটাস", "Admin Panel & Firestore Sync Schema", Icons.Outlined.CloudSync, NexPurpleSecondary, ScreenRoute.FIREBASE_BRIDGE),
        MenuOptionEntry("Logout (লগআউট)", "Logout", "নিরাপদে সেশন সমাপ্ত করুন", "Sign out securely", Icons.AutoMirrored.Filled.ExitToApp, ErrorRed, isLogout = true)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // User Profile Summary Banner
        item {
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.openMyProfile() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        UserAvatar(
                            name = currentUser.fullName,
                            colorHex = currentUser.avatarColorHex,
                            size = 54.dp,
                            isOnline = true
                        )
                        Column {
                            Text(
                                text = currentUser.fullName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "@${currentUser.username} • ${currentUser.country}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    CoinBadge(coins = currentUser.currentBalance)
                }
            }
        }

        items(menuItems, key = { it.titleEn }) { item ->
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        when {
                            item.isLogout -> viewModel.requestLogoutConfirmation()
                            item.isMyProfile -> viewModel.openMyProfile()
                            item.route != null -> viewModel.navigateTo(item.route)
                        }
                    }
                    .testTag("menu_item_${item.titleEn.lowercase().replace(" ", "_")}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(item.accent.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = item.icon, contentDescription = null, tint = item.accent)
                        }
                        Column {
                            Text(
                                text = tr(lang, item.titleBn, item.titleEn),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (item.isLogout) ErrorRed else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = tr(lang, item.subtitleBn, item.subtitleEn),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Section 9: Rank — Post Ranking (Top 3 Separate Podium Cards, Top 50 List, My Rank)
 * Only approved non-spam posts count.
 */
@Composable
fun PostRankScreen(
    viewModel: NexEarnViewModel,
    lang: AppLanguage
) {
    val currentUser = viewModel.getCurrentUser() ?: return
    val rankings = viewModel.getTopPostersRanking()
    val myRankEntry = rankings.find { it.user.uid == currentUser.uid }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // My Rank Card
        item {
            ElevatedCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(NexBluePrimary, NexPurpleSecondary)))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = tr(lang, "আমার পোস্ট র‍্যাঙ্ক (My Post Rank)", "My Post Rank"),
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (myRankEntry != null)
                                tr(lang, "অবস্থান: #${myRankEntry.rank} • অনুমোদিত পোস্ট: ${myRankEntry.approvedPostsCount}টি", "Rank: #${myRankEntry.rank} • Approved Posts: ${myRankEntry.approvedPostsCount}")
                            else
                                tr(lang, "র‍্যাঙ্ক পেতে অন্তত ১টি অনুমোদিত পোস্ট করুন", "Publish at least 1 approved post to be ranked"),
                            color = Color.White.copy(alpha = 0.92f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(38.dp)
                    )
                }
            }
        }

        // Top 3 Separate Podium Cards
        item {
            Text(
                text = tr(lang, "টপ ৩ পোস্টার (Top 3 Posters)", "Top 3 Posters Podium"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rankings.take(3).forEach { entry ->
                    ElevatedCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.openUserProfile(entry.user.uid) },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "#${entry.rank}",
                                fontWeight = FontWeight.ExtraBold,
                                color = WarningOrange,
                                fontSize = 18.sp
                            )
                            UserAvatar(name = entry.user.fullName, colorHex = entry.user.avatarColorHex, size = 48.dp)
                            Text(
                                text = entry.user.fullName.substringBefore(" "),
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "${entry.approvedPostsCount} Posts",
                                style = MaterialTheme.typography.labelLarge,
                                color = NexBluePrimary
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = tr(lang, "টপ ৫০ পোস্ট র‍্যাঙ্কিং তালিকা (Top 50 List)", "Top 50 Posters Leaderboard"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(rankings, key = { it.user.uid }) { entry ->
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.openUserProfile(entry.user.uid) },
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "#${entry.rank}",
                            fontWeight = FontWeight.ExtraBold,
                            color = if (entry.rank <= 3) WarningOrange else MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp
                        )
                        UserAvatar(name = entry.user.fullName, colorHex = entry.user.avatarColorHex, size = 42.dp)
                        Column {
                            Text(entry.user.fullName, fontWeight = FontWeight.Bold)
                            Text("@${entry.user.username}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Surface(
                        color = NexBluePrimary.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "${entry.approvedPostsCount} Posts",
                            color = NexBluePrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Section 10: Income Rank — Ranked strictly by Verified Total Earned Coins (NOT Current Balance)
 */
@Composable
fun IncomeRankScreen(
    viewModel: NexEarnViewModel,
    lang: AppLanguage
) {
    val currentUser = viewModel.getCurrentUser() ?: return
    val rankings = viewModel.getTopEarnersRanking()
    val myRankEntry = rankings.find { it.user.uid == currentUser.uid }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // My Income Rank Banner
        item {
            ElevatedCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(Color(0xFF047857), NexBluePrimary)))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = tr(lang, "আমার আয়ের র‍্যাঙ্ক (My Income Rank)", "My Income Rank"),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = tr(
                            lang,
                            "আপনার র‍্যাঙ্ক: #${myRankEntry?.rank ?: "-"} • মোট বৈধ আয় (Total Earned): ${currentUser.totalEarnedCoins} Coins",
                            "Your Rank: #${myRankEntry?.rank ?: "-"} • Total Earned: ${currentUser.totalEarnedCoins} Coins"
                        ),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = tr(
                            lang,
                            "নোট: র‍্যাঙ্কিং বর্তমান অবশিষ্ট ব্যালেন্স (${currentUser.currentBalance} Coins) নয়, বরং যাচাইকৃত মোট আয়ের ভিত্তিতে গণনা করা হয়।",
                            "Note: Ranking is calculated strictly on verified Total Earned Coins, not remaining Current Balance."
                        ),
                        color = Color.White.copy(alpha = 0.88f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        // Top 3 Earners Podium
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rankings.take(3).forEach { entry ->
                    ElevatedCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.openUserProfile(entry.user.uid) },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "🏆 #${entry.rank}",
                                fontWeight = FontWeight.ExtraBold,
                                color = WarningOrange
                            )
                            UserAvatar(name = entry.user.fullName, colorHex = entry.user.avatarColorHex, size = 48.dp)
                            Text(entry.user.fullName.substringBefore(" "), fontWeight = FontWeight.Bold, maxLines = 1)
                            CoinBadge(coins = entry.totalEarnedCoins)
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = tr(lang, "টপ ৫০ আয়ের তালিকা (Top 50 Earners)", "Top 50 Earners Leaderboard"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(rankings, key = { it.user.uid }) { entry ->
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.openUserProfile(entry.user.uid) },
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "#${entry.rank}",
                            fontWeight = FontWeight.ExtraBold,
                            color = if (entry.rank <= 3) WarningOrange else SuccessGreen,
                            fontSize = 16.sp
                        )
                        UserAvatar(name = entry.user.fullName, colorHex = entry.user.avatarColorHex, size = 42.dp)
                        Column {
                            Text(entry.user.fullName, fontWeight = FontWeight.Bold)
                            Text(
                                text = "@${entry.user.username} • Balance: ${entry.user.currentBalance} C",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    CoinBadge(coins = entry.totalEarnedCoins)
                }
            }
        }
    }
}

/**
 * Section 12 & 13: Groups List, My Joined Groups & Group Details
 */
@Composable
fun GroupsScreen(
    viewModel: NexEarnViewModel,
    lang: AppLanguage,
    onlyJoinedGroups: Boolean = false
) {
    val currentUser = viewModel.getCurrentUser() ?: return
    val groups by viewModel.groups.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var filterTab by remember { mutableIntStateOf(0) } // 0: All/Popular, 1: Recent, 2: Recommended
    var showCreateGroupModal by remember { mutableStateOf(false) }

    val displayedGroups = remember(groups, onlyJoinedGroups, searchQuery, filterTab, currentUser) {
        var list = if (onlyJoinedGroups) {
            groups.filter { it.memberUids.contains(currentUser.uid) }
        } else {
            groups
        }
        if (searchQuery.isNotBlank()) {
            list = list.filter { it.name.contains(searchQuery, true) || it.description.contains(searchQuery, true) }
        }
        when (filterTab) {
            0 -> list.sortedByDescending { it.memberUids.size }
            1 -> list.reversed()
            else -> list.filter { it.isRecommended }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (onlyJoinedGroups)
                        tr(lang, "আমার যুক্ত গ্রুপসমূহ (${displayedGroups.size})", "My Joined Groups (${displayedGroups.size})")
                    else
                        tr(lang, "সকল কমিউনিটি গ্রুপ (${displayedGroups.size})", "Community Groups (${displayedGroups.size})"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                FilledTonalButton(onClick = { showCreateGroupModal = true }) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(tr(lang, "নতুন গ্রুপ", "New Group"))
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(tr(lang, "গ্রুপ খুঁজুন (Search Groups)...", "Search Groups...")) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (!onlyJoinedGroups) {
            item {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        0 to tr(lang, "জনপ্রিয় (Popular Groups)", "Popular Groups"),
                        1 to tr(lang, "সাম্প্রতিক (Recent Groups)", "Recent Groups"),
                        2 to tr(lang, "প্রস্তাবিত (Recommended)", "Recommended")
                    ).forEach { (idx, label) ->
                        FilterChip(
                            selected = filterTab == idx,
                            onClick = { filterTab = idx },
                            label = { Text(label) }
                        )
                    }
                }
            }
        }

        if (displayedGroups.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Outlined.Groups,
                    title = tr(lang, "কোনো গ্রুপ পাওয়া যায়নি", "No groups found"),
                    subtitle = tr(lang, "নতুন গ্রুপে যোগ দিন অথবা নিজেই একটি গ্রুপ তৈরি করুন।", "Join or create a group.")
                )
            }
        } else {
            items(displayedGroups, key = { it.groupId }) { group ->
                val isMember = group.memberUids.contains(currentUser.uid)
                val isPending = group.pendingJoinUids.contains(currentUser.uid)

                ElevatedCard(
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(group.coverColorHex), NexPurpleSecondary)
                                    )
                                )
                                .padding(14.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = group.name,
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    color = Color.White.copy(alpha = 0.22f),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = tr(lang, group.privacy.labelBn, group.privacy.labelEn),
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(group.description, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "👥 ${group.memberUids.size} Members • 📝 ${group.postsCount} Posts • 🆕 ${group.newPostsCount} New • Active ${group.lastActivity}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.openGroupDetails(group.groupId) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(tr(lang, "গ্রুপ দেখুন (Open Group)", "Open Group"))
                                }
                                Button(
                                    onClick = { viewModel.joinOrLeaveGroup(group.groupId) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        when {
                                            isMember -> tr(lang, "সদস্য (Leave)", "Joined (Leave)")
                                            isPending -> tr(lang, "রিকোয়েস্ট পেন্ডিং", "Request Pending")
                                            group.privacy == GroupPrivacy.PUBLIC -> tr(lang, "যোগ দিন (Join)", "Join Group")
                                            else -> tr(lang, "জয়েন রিকোয়েস্ট পাঠান", "Request to Join")
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateGroupModal) {
        var name by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        var privacy by remember { mutableStateOf(GroupPrivacy.PUBLIC) }
        var rules by remember { mutableStateOf("১. স্প্যাম নিষিদ্ধ\n২. সম্মানজনক আচরণ বজায় রাখুন") }

        AlertDialog(
            onDismissRequest = { showCreateGroupModal = false },
            title = { Text(tr(lang, "নতুন গ্রুপ তৈরি করুন", "Create New Group")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Group Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                    GroupPrivacy.entries.forEach { gp ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { privacy = gp }) {
                            RadioButton(selected = privacy == gp, onClick = { privacy = gp })
                            Text(tr(lang, gp.labelBn, gp.labelEn))
                        }
                    }
                    OutlinedTextField(value = rules, onValueChange = { rules = it }, label = { Text("Group Rules") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createNewGroup(name, desc, privacy, rules)
                        showCreateGroupModal = false
                    }
                ) {
                    Text(tr(lang, "তৈরি করুন", "Create"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateGroupModal = false }) {
                    Text(tr(lang, "বাতিল", "Cancel"))
                }
            }
        )
    }
}

/**
 * Section 12.2: Group Details Screen
 * Tabs: Posts, Members, About, Rules + Admin Tools (Edit Details, Approve Requests, Update Rules)
 */
@Composable
fun GroupDetailsScreen(
    viewModel: NexEarnViewModel,
    lang: AppLanguage
) {
    val currentUser = viewModel.getCurrentUser() ?: return
    val groups by viewModel.groups.collectAsState()
    val allPosts by viewModel.posts.collectAsState()
    val allUsers by viewModel.users.collectAsState()
    val selectedGroupId by viewModel.selectedGroupId.collectAsState()

    val group = groups.find { it.groupId == selectedGroupId } ?: groups.firstOrNull() ?: return
    val isMember = group.memberUids.contains(currentUser.uid)
    val isAdmin = group.adminUid == currentUser.uid
    val canViewContent = group.privacy == GroupPrivacy.PUBLIC || isMember

    var selectedTab by remember { mutableIntStateOf(0) }
    var groupPostDraft by remember { mutableStateOf("") }
    var showAdminEditModal by remember { mutableStateOf(false) }

    val groupPosts = allPosts.filter { it.groupId == group.groupId && it.isApproved }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Group Header
        item {
            ElevatedCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(Brush.horizontalGradient(listOf(Color(group.coverColorHex), NexBluePrimary)))
                            .padding(16.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Column {
                            Text(group.name, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${tr(lang, group.privacy.labelBn, group.privacy.labelEn)} • ${group.memberUids.size} Members",
                                color = Color.White.copy(alpha = 0.9f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(group.description, style = MaterialTheme.typography.bodyMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.joinOrLeaveGroup(group.groupId) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    if (isMember) tr(lang, "গ্রুপ ত্যাগ করুন (Leave Group)", "Leave Group")
                                    else tr(lang, "গ্রুপে যোগ দিন (Join Group)", "Join Group")
                                )
                            }
                            if (isAdmin) {
                                OutlinedButton(
                                    onClick = { showAdminEditModal = true },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Outlined.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(tr(lang, "Admin Tools", "Admin Tools"))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Tabs: Posts, Members, About, Rules
        item {
            ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 4.dp) {
                listOf(
                    tr(lang, "পোস্ট (Posts)", "Posts"),
                    tr(lang, "সদস্য (${group.memberUids.size})", "Members (${group.memberUids.size})"),
                    tr(lang, "পরিচিতি (About)", "About"),
                    tr(lang, "নিয়মাবলী (Rules)", "Rules")
                ).forEachIndexed { idx, title ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }

        when (selectedTab) {
            0 -> {
                if (!canViewContent) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Outlined.Lock,
                            title = tr(lang, "প্রাইভেট গ্রুপ কনটেন্ট সংরক্ষিত", "Private Group Content"),
                            subtitle = tr(lang, "এই প্রাইভেট গ্রুপের পোস্ট দেখতে ও পোস্ট করতে আগে সদস্য হিসেবে যুক্ত হোন।", "Join this private group to view and create posts.")
                        )
                    }
                } else {
                    if (isMember) {
                        item {
                            ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = groupPostDraft,
                                        onValueChange = { groupPostDraft = it },
                                        placeholder = { Text(tr(lang, "গ্রুপে নতুন পোস্ট লিখুন...", "Write a post in this group...")) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Button(
                                        onClick = {
                                            if (viewModel.createPost(groupPostDraft, PostMediaType.TEXT, emptyList(), PostAudience.PUBLIC, groupId = group.groupId)) {
                                                groupPostDraft = ""
                                            }
                                        },
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Text(tr(lang, "গ্রুপে পোস্ট করুন", "Post to Group"))
                                    }
                                }
                            }
                        }
                    } else {
                        item {
                            Surface(
                                color = WarningOrange.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = tr(lang, "গ্রুপে পোস্ট করার জন্য আগে গ্রুপে Join করুন।", "Join this group to publish posts."),
                                    modifier = Modifier.padding(12.dp),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    items(groupPosts, key = { it.postId }) { p ->
                        PostCardItem(post = p, currentUser = currentUser, lang = lang, viewModel = viewModel)
                    }
                }
            }

            1 -> {
                // Members + Admin Pending Join Requests
                if (isAdmin && group.pendingJoinUids.isNotEmpty()) {
                    item {
                        Text(
                            text = tr(lang, "জয়েন রিকোয়েস্ট অনুমোদন (Pending Join Requests):", "Pending Join Requests:"),
                            fontWeight = FontWeight.Bold,
                            color = WarningOrange
                        )
                    }
                    items(allUsers.filter { group.pendingJoinUids.contains(it.uid) }, key = { "pend_${it.uid}" }) { u ->
                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${u.fullName} (@${u.username})", fontWeight = FontWeight.Bold)
                                Button(onClick = { viewModel.approveGroupJoinRequest(group.groupId, u.uid) }) {
                                    Text(tr(lang, "Approve", "Approve"))
                                }
                            }
                        }
                    }
                }

                items(allUsers.filter { group.memberUids.contains(it.uid) }, key = { it.uid }) { member ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.openUserProfile(member.uid) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                UserAvatar(name = member.fullName, colorHex = member.avatarColorHex, size = 40.dp)
                                Column {
                                    Text(member.fullName, fontWeight = FontWeight.Bold)
                                    Text("@${member.username}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            if (member.uid == group.adminUid) {
                                Surface(color = NexBluePrimary.copy(alpha = 0.14f), shape = RoundedCornerShape(50)) {
                                    Text("Admin", color = NexBluePrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // About
                item {
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("• Group Name: ${group.name}", fontWeight = FontWeight.Bold)
                            Text("• Created Date: ${group.createdDate}")
                            Text("• Admin: ${group.adminName}")
                            Text("• Moderators Count: ${group.moderatorUids.size}")
                            Text("• Privacy Status: ${tr(lang, group.privacy.labelBn, group.privacy.labelEn)}")
                            Text("• Description: ${group.description}")
                        }
                    }
                }
            }

            3 -> {
                // Rules
                item {
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(tr(lang, "গ্রুপের নিয়মাবলী (Group Rules)", "Group Rules"), fontWeight = FontWeight.Bold)
                            group.rules.forEach { rule ->
                                Text(rule, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdminEditModal) {
        var editName by remember { mutableStateOf(group.name) }
        var editDesc by remember { mutableStateOf(group.description) }
        var editRules by remember { mutableStateOf(group.rules.joinToString("\n")) }

        AlertDialog(
            onDismissRequest = { showAdminEditModal = false },
            title = { Text(tr(lang, "গ্রুপ এডমিন টুলস (Admin Tools)", "Group Admin Tools")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Group Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editDesc, onValueChange = { editDesc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editRules, onValueChange = { editRules = it }, label = { Text("Group Rules") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateGroupDetailsByAdmin(
                            groupId = group.groupId,
                            name = editName,
                            description = editDesc,
                            rules = editRules.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
                        )
                        showAdminEditModal = false
                    }
                ) {
                    Text(tr(lang, "সংরক্ষণ করুন", "Save"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminEditModal = false }) {
                    Text(tr(lang, "বাতিল", "Cancel"))
                }
            }
        )
    }
}
