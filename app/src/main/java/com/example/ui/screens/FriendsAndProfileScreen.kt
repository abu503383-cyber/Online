package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

/**
 * Section 5: Friend Requests Screen
 * 4 Sections: Friend Requests (Received), Pending Requests (Sent), My Friends, Find Friends
 */
@Composable
fun FriendsScreen(
    viewModel: NexEarnViewModel,
    lang: AppLanguage
) {
    val currentUser = viewModel.getCurrentUser() ?: return
    val allUsers by viewModel.users.collectAsState()
    val allRequests by viewModel.friendRequests.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    val incomingRequests = allRequests.filter { it.toUid == currentUser.uid && it.status == "PENDING" }
    val pendingSentRequests = allRequests.filter { it.fromUid == currentUser.uid && it.status == "PENDING" }
    val myFriends = allUsers.filter {
        currentUser.friendUids.contains(it.uid) &&
            (searchQuery.isBlank() || it.fullName.contains(searchQuery, true) || it.username.contains(searchQuery, true))
    }
    val findableUsers = allUsers.filter { u ->
        u.uid != currentUser.uid &&
            !currentUser.friendUids.contains(u.uid) &&
            !currentUser.blockedUids.contains(u.uid) &&
            u.privacySettings.whoCanFindProfile != PrivacyOption.ONLY_ME &&
            (searchQuery.isBlank() || u.fullName.contains(searchQuery, true) || u.username.contains(searchQuery, true))
    }

    val tabs = listOf(
        tr(lang, "রিকোয়েস্ট (${incomingRequests.size})", "Requests (${incomingRequests.size})"),
        tr(lang, "পেন্ডিং (${pendingSentRequests.size})", "Pending (${pendingSentRequests.size})"),
        tr(lang, "আমার বন্ধু (${currentUser.friendUids.size})", "My Friends (${currentUser.friendUids.size})"),
        tr(lang, "বন্ধু খুঁজুন", "Find Friends")
    )

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 12.dp
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = FontWeight.SemiBold) }
                )
            }
        }

        if (selectedTab == 2 || selectedTab == 3) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(tr(lang, "নাম বা @username দিয়ে খুঁজুন...", "Search by name or @username...")) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // Incoming Friend Requests
                    if (incomingRequests.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Outlined.PersonAdd,
                                title = tr(lang, "নতুন কোনো ফ্রেন্ড রিকোয়েস্ট নেই", "No incoming friend requests"),
                                subtitle = tr(lang, "নতুন মানুষদের খুঁজে ফ্রেন্ড রিকোয়েস্ট পাঠাতে Find Friends ট্যাবে যান।", "Explore Find Friends to connect with more people.")
                            )
                        }
                    } else {
                        items(incomingRequests, key = { it.requestId }) { req ->
                            ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { viewModel.openUserProfile(req.fromUid) }
                                    ) {
                                        UserAvatar(name = req.fromName, colorHex = req.fromAvatarColor, size = 48.dp)
                                        Column {
                                            Text(req.fromName, fontWeight = FontWeight.Bold)
                                            Text("@${req.fromUsername} • ${req.mutualFriendsCount} Mutual Friends", style = MaterialTheme.typography.bodySmall)
                                            Text(req.sentTimeAgo, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Button(
                                            onClick = { viewModel.acceptFriendRequest(req.requestId) },
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(tr(lang, "Accept", "Accept"))
                                        }
                                        OutlinedButton(
                                            onClick = { viewModel.rejectOrCancelFriendRequest(req.requestId) },
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(tr(lang, "Reject", "Reject"))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Pending Sent Requests
                    if (pendingSentRequests.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Outlined.ScheduleSend,
                                title = tr(lang, "কোনো পেন্ডিং রিকোয়েস্ট নেই", "No pending requests"),
                                subtitle = tr(lang, "আপনার পাঠানো কিন্তু এখনো গৃহীত হয়নি এমন রিকোয়েস্ট এখানে দেখাবে।", "Requests you have sent will appear here.")
                            )
                        }
                    } else {
                        items(pendingSentRequests, key = { it.requestId }) { req ->
                            ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { viewModel.openUserProfile(req.toUid) }
                                    ) {
                                        Text(req.toName, fontWeight = FontWeight.Bold)
                                        Text("@${req.toUsername} • Sent ${req.sentTimeAgo}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    OutlinedButton(
                                        onClick = { viewModel.rejectOrCancelFriendRequest(req.requestId) }
                                    ) {
                                        Icon(Icons.Outlined.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(tr(lang, "Cancel Request", "Cancel Request"))
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // My Friends (Profile, Message, Unfriend, Block, Report)
                    if (myFriends.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Outlined.PeopleOutline,
                                title = tr(lang, "কোনো বন্ধু পাওয়া যায়নি", "No friends found"),
                                subtitle = tr(lang, "নতুন বন্ধু তৈরি করতে রিকোয়েস্ট গ্রহণ করুন বা Find Friends দেখুন।", "Add friends to see them here.")
                            )
                        }
                    } else {
                        items(myFriends, key = { it.uid }) { friend ->
                            var friendMenuOpen by remember { mutableStateOf(false) }
                            ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { viewModel.openUserProfile(friend.uid) }
                                    ) {
                                        UserAvatar(
                                            name = friend.fullName,
                                            colorHex = friend.avatarColorHex,
                                            size = 46.dp,
                                            isOnline = friend.isOnline
                                        )
                                        Column {
                                            Text(friend.fullName, fontWeight = FontWeight.Bold)
                                            Text("@${friend.username} • ${friend.country}", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        FilledTonalIconButton(
                                            onClick = { viewModel.openChatWithUser(friend.uid) }
                                        ) {
                                            Icon(Icons.Outlined.Chat, contentDescription = "Message")
                                        }
                                        Box {
                                            IconButton(onClick = { friendMenuOpen = true }) {
                                                Icon(Icons.Filled.MoreVert, contentDescription = "Friend Actions")
                                            }
                                            DropdownMenu(
                                                expanded = friendMenuOpen,
                                                onDismissRequest = { friendMenuOpen = false }
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text(tr(lang, "প্রোফাইল দেখুন (View Profile)", "View Profile")) },
                                                    leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                                                    onClick = {
                                                        friendMenuOpen = false
                                                        viewModel.openUserProfile(friend.uid)
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text(tr(lang, "আনফ্রেন্ড করুন (Unfriend)", "Unfriend")) },
                                                    leadingIcon = { Icon(Icons.Outlined.PersonRemove, contentDescription = null) },
                                                    onClick = {
                                                        friendMenuOpen = false
                                                        viewModel.unfriendUser(friend.uid)
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text(tr(lang, "ব্লক করুন (Block)", "Block User"), color = ErrorRed) },
                                                    leadingIcon = { Icon(Icons.Outlined.Block, contentDescription = null, tint = ErrorRed) },
                                                    onClick = {
                                                        friendMenuOpen = false
                                                        viewModel.toggleBlockUser(friend.uid)
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text(tr(lang, "রিপোর্ট করুন (Report)", "Report User"), color = ErrorRed) },
                                                    leadingIcon = { Icon(Icons.Outlined.Flag, contentDescription = null, tint = ErrorRed) },
                                                    onClick = {
                                                        friendMenuOpen = false
                                                        viewModel.reportContent("User Account", friend.uid, "Suspicious activity")
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

                3 -> {
                    // Find Friends
                    items(findableUsers, key = { it.uid }) { user ->
                        val isFollowing = currentUser.followingUids.contains(user.uid)
                        ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.openUserProfile(user.uid) }
                                ) {
                                    UserAvatar(name = user.fullName, colorHex = user.avatarColorHex, size = 46.dp)
                                    Column {
                                        Text(user.fullName, fontWeight = FontWeight.Bold)
                                        Text("@${user.username} • ${user.country}", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedButton(
                                        onClick = { viewModel.toggleFollowUser(user.uid) },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(if (isFollowing) tr(lang, "Following", "Following") else tr(lang, "Follow", "Follow"))
                                    }
                                    Button(
                                        onClick = { viewModel.sendFriendRequest(user.uid) },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(tr(lang, "Add Friend", "Add Friend"))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Section 6: Complete Profile Screen
 * 6.1 Profile Header & Info + Edit Profile
 * 6.2 Profile Statistics & Tabs (Posts, Friends, Followers/Following, Photos/Videos, About, Dashboard, Coin History)
 * 6.3 Dashboard (Total Posts, Likes, Comments, Views, Followers, Friends, Most Popular Post, Per-post performance)
 * 6.4 Coin History (Current Balance, Total Earned, Total Spent, Pending Coins, Transaction Ledger)
 */
@Composable
fun ProfileScreen(
    viewModel: NexEarnViewModel,
    lang: AppLanguage
) {
    val currentUser = viewModel.getCurrentUser() ?: return
    val allUsers by viewModel.users.collectAsState()
    val allPosts by viewModel.posts.collectAsState()
    val allTx by viewModel.coinTransactions.collectAsState()
    val selectedProfileUid by viewModel.selectedProfileUid.collectAsState()

    val profileUser = allUsers.find { it.uid == (selectedProfileUid ?: currentUser.uid) } ?: currentUser
    val isMyProfile = profileUser.uid == currentUser.uid

    val userPosts = allPosts.filter { it.authorUid == profileUser.uid && it.isApproved && !it.isSpam }
    val totalLikes = userPosts.sumOf { it.likedByUids.size }
    val totalComments = userPosts.sumOf { it.comments.size }
    val totalViews = userPosts.sumOf { it.viewedByUids.size }
    val mostPopularPost = userPosts.maxByOrNull { it.likedByUids.size * 2 + it.viewedByUids.size }

    val userTransactions = allTx.filter { it.uid == profileUser.uid }

    var selectedTab by remember { mutableIntStateOf(0) }
    var showEditProfileModal by remember { mutableStateOf(false) }

    val profileTabs = listOf(
        tr(lang, "পোস্ট (${userPosts.size})", "Posts (${userPosts.size})"),
        tr(lang, "ড্যাশবোর্ড (Dashboard)", "Dashboard"),
        tr(lang, "কয়েন হিস্ট্রি (Coin History)", "Coin History"),
        tr(lang, "বন্ধু ও ফলোয়ার", "Friends & Followers"),
        tr(lang, "ছবি ও ভিডিও", "Photos & Videos"),
        tr(lang, "পরিচিতি (About)", "About")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 6.1 Profile Header & Info
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column {
                    // Cover Photo Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(125.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(profileUser.coverColorHex),
                                        Color(profileUser.avatarColorHex)
                                    )
                                )
                            )
                            .padding(14.dp)
                    ) {
                        CoinBadge(
                            coins = profileUser.currentBalance,
                            modifier = Modifier.align(Alignment.TopEnd)
                        )
                    }

                    // Profile Avatar + Name + Bio + Actions
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UserAvatar(
                                name = profileUser.fullName,
                                colorHex = profileUser.avatarColorHex,
                                size = 72.dp,
                                isOnline = profileUser.isOnline
                            )

                            if (isMyProfile) {
                                Button(
                                    onClick = { showEditProfileModal = true },
                                    modifier = Modifier.testTag("edit_profile_button")
                                ) {
                                    Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(tr(lang, "প্রোফাইল এডিট (Edit Profile)", "Edit Profile"))
                                }
                            } else {
                                val isFriend = currentUser.friendUids.contains(profileUser.uid)
                                val isFollowing = currentUser.followingUids.contains(profileUser.uid)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Button(
                                        onClick = {
                                            if (isFriend) viewModel.unfriendUser(profileUser.uid)
                                            else viewModel.sendFriendRequest(profileUser.uid)
                                        }
                                    ) {
                                        Text(if (isFriend) tr(lang, "Friends ✓", "Friends ✓") else tr(lang, "Add Friend", "Add Friend"))
                                    }
                                    OutlinedButton(onClick = { viewModel.toggleFollowUser(profileUser.uid) }) {
                                        Text(if (isFollowing) tr(lang, "Following", "Following") else tr(lang, "Follow", "Follow"))
                                    }
                                    FilledTonalIconButton(onClick = { viewModel.openChatWithUser(profileUser.uid) }) {
                                        Icon(Icons.Outlined.Chat, contentDescription = "Message")
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = profileUser.fullName,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = "Verified",
                                tint = NexBluePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "@${profileUser.username} • ${profileUser.country} • Joined ${profileUser.joinDate}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (profileUser.bio.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = profileUser.bio,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Key Metrics Strip
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ProfileStatPill(tr(lang, "পোস্ট", "Posts"), "${userPosts.size}")
                            ProfileStatPill(tr(lang, "বন্ধু", "Friends"), "${profileUser.friendUids.size}")
                            ProfileStatPill(tr(lang, "ফলোয়ার", "Followers"), "${profileUser.followerUids.size}")
                            ProfileStatPill(tr(lang, "বর্তমান কয়েন", "Balance"), "${profileUser.currentBalance}")
                            ProfileStatPill(tr(lang, "মোট আয়", "Total Earned"), "${profileUser.totalEarnedCoins}")
                        }
                    }
                }
            }
        }

        // 6.2 Profile Tabs
        item {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 12.dp
            ) {
                profileTabs.forEachIndexed { idx, label ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = { Text(label, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }
        }

        // Tab Content
        when (selectedTab) {
            0 -> {
                // Posts Tab
                if (userPosts.isEmpty()) {
                    item {
                        Box(modifier = Modifier.padding(horizontal = 12.dp)) {
                            EmptyStateCard(
                                icon = Icons.Outlined.Article,
                                title = tr(lang, "এখনো কোনো পোস্ট নেই", "No posts yet"),
                                subtitle = tr(lang, "প্রোফাইলের সকল অনুমোদিত পোস্ট এখানে দেখাবে।", "Approved posts will appear here.")
                            )
                        }
                    }
                } else {
                    items(userPosts, key = { it.postId }) { post ->
                        Box(modifier = Modifier.padding(horizontal = 12.dp)) {
                            PostCardItem(
                                post = post,
                                currentUser = currentUser,
                                lang = lang,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }

            1 -> {
                // 6.3 Dashboard Tab (Total Posts, Likes, Comments, Views, Followers, Friends, Most Popular Post, Recent Post Performance)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ElevatedCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = tr(lang, "ক্রিয়েটর পারফরম্যান্স ড্যাশবোর্ড (Dashboard)", "Creator Performance Dashboard"),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    DashboardMetricBox(Modifier.weight(1f), tr(lang, "মোট পোস্ট", "Total Posts"), "${userPosts.size}", NexBluePrimary)
                                    DashboardMetricBox(Modifier.weight(1f), tr(lang, "মোট লাইক", "Total Likes"), "$totalLikes", SuccessGreen)
                                    DashboardMetricBox(Modifier.weight(1f), tr(lang, "মোট কমেন্ট", "Total Comments"), "$totalComments", NexPurpleSecondary)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    DashboardMetricBox(Modifier.weight(1f), tr(lang, "মোট ভিউ", "Total Views"), "$totalViews", WarningOrange)
                                    DashboardMetricBox(Modifier.weight(1f), tr(lang, "মোট ফলোয়ার", "Total Followers"), "${profileUser.followerUids.size}", NexBluePrimary)
                                    DashboardMetricBox(Modifier.weight(1f), tr(lang, "মোট বন্ধু", "Total Friends"), "${profileUser.friendUids.size}", NexPurpleSecondary)
                                }
                            }
                        }

                        // Most Popular Post
                        if (mostPopularPost != null) {
                            ElevatedCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = WarningOrange)
                                        Text(
                                            text = tr(lang, "সবচেয়ে জনপ্রিয় পোস্ট (Most Popular Post)", "Most Popular Post"),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(mostPopularPost.caption, style = MaterialTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Likes: ${mostPopularPost.likedByUids.size} • Comments: ${mostPopularPost.comments.size} • Views: ${mostPopularPost.viewedByUids.size}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = NexBluePrimary
                                    )
                                }
                            }
                        }

                        // Per-Post Performance Breakdown
                        ElevatedCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = tr(lang, "পোস্টভিত্তিক পারফরম্যান্স (Recent Post Performance)", "Recent Post Performance"),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                userPosts.forEach { p ->
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(p.caption, maxLines = 1, fontWeight = FontWeight.SemiBold)
                                            Text(
                                                text = "👍 ${p.likedByUids.size} Likes   💬 ${p.comments.size} Comments   👁️ ${p.viewedByUids.size} Views",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // 6.4 Coin History Tab (Current Balance, Total Earned, Total Spent, Pending Coins, Completed Transactions)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ElevatedCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = tr(lang, "কয়েন ও আয়ের সারসংক্ষেপ (Coin Wallet Summary)", "Coin Wallet & Ledger Summary"),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    DashboardMetricBox(Modifier.weight(1f), tr(lang, "বর্তমান ব্যালেন্স (Current Balance)", "Current Balance"), "${profileUser.currentBalance}", NexBluePrimary)
                                    DashboardMetricBox(Modifier.weight(1f), tr(lang, "মোট বৈধ আয় (Total Earned)", "Total Earned"), "${profileUser.totalEarnedCoins}", SuccessGreen)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    DashboardMetricBox(Modifier.weight(1f), tr(lang, "মোট ব্যয় (Total Spent)", "Total Spent"), "${profileUser.totalSpentCoins}", ErrorRed)
                                    DashboardMetricBox(Modifier.weight(1f), tr(lang, "পেন্ডিং কয়েন (Pending Coins)", "Pending Coins"), "${profileUser.pendingCoins}", WarningOrange)
                                }
                            }
                        }

                        Text(
                            text = tr(lang, "লেনদেনের ইতিহাস (Completed Transactions)", "Transaction Ledger History"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        userTransactions.forEach { tx ->
                            ElevatedCard(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = tr(lang, tx.type.labelBn, tx.type.labelEn),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = tx.description,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = "ID: ${tx.txId} • ${tx.dateTime} • Status: ${tx.status}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "${if (tx.isCredit) "+" else "-"}${tx.coins} Coins",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (tx.isCredit) SuccessGreen else ErrorRed,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            3 -> {
                // Friends / Followers / Following Tab
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = tr(
                                lang,
                                "বন্ধু (${profileUser.friendUids.size}) • ফলোয়ার (${profileUser.followerUids.size}) • ফলোয়িং (${profileUser.followingUids.size})",
                                "Friends (${profileUser.friendUids.size}) • Followers (${profileUser.followerUids.size}) • Following (${profileUser.followingUids.size})"
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        val connectedUids = (profileUser.friendUids + profileUser.followerUids + profileUser.followingUids).distinct()
                        allUsers.filter { connectedUids.contains(it.uid) }.forEach { u ->
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.openUserProfile(u.uid) }
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
                                        UserAvatar(name = u.fullName, colorHex = u.avatarColorHex, size = 42.dp)
                                        Column {
                                            Text(u.fullName, fontWeight = FontWeight.Bold)
                                            Text("@${u.username} • ${u.country}", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                    CoinBadge(coins = u.totalEarnedCoins)
                                }
                            }
                        }
                    }
                }
            }

            4 -> {
                // Photos / Videos Tab
                val mediaPosts = userPosts.filter { it.mediaType != PostMediaType.TEXT }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (mediaPosts.isEmpty()) {
                            EmptyStateCard(
                                icon = Icons.Outlined.PhotoLibrary,
                                title = tr(lang, "কোনো ছবি বা ভিডিও আপলোড করা হয়নি", "No uploaded media"),
                                subtitle = tr(lang, "আপলোড করা ছবি ও ভিডিও এখানে সংরক্ষিত থাকবে।", "Uploaded photos & videos appear here.")
                            )
                        } else {
                            mediaPosts.forEach { mp ->
                                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(tr(lang, mp.mediaType.labelBn, mp.mediaType.labelEn), fontWeight = FontWeight.Bold, color = NexBluePrimary)
                                        Text(mp.caption, style = MaterialTheme.typography.bodyMedium)
                                        mp.mediaLabels.forEach { lbl ->
                                            Text("• $lbl", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            5 -> {
                // About Tab
                item {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(tr(lang, "প্রোফাইল পরিচিতি (About)", "Profile Information"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("• Full Name: ${profileUser.fullName}")
                            Text("• Username: @${profileUser.username}")
                            Text("• Bio: ${profileUser.bio}")
                            Text("• Country: ${profileUser.country}")
                            Text("• Website: ${profileUser.website.ifBlank { "Not specified" }}")
                            Text("• Gender: ${profileUser.gender}")
                            Text("• Date of Birth: ${profileUser.dob}")
                            Text("• Joined Date: ${profileUser.joinDate}")
                        }
                    }
                }
            }
        }
    }

    // 6.1 Edit Profile Dialog (with Server Unique Username Verification)
    if (showEditProfileModal) {
        var editName by remember { mutableStateOf(currentUser.fullName) }
        var editUsername by remember { mutableStateOf(currentUser.username) }
        var editBio by remember { mutableStateOf(currentUser.bio) }
        var editCountry by remember { mutableStateOf(currentUser.country) }
        var editWebsite by remember { mutableStateOf(currentUser.website) }
        var editGender by remember { mutableStateOf(currentUser.gender) }
        var editDob by remember { mutableStateOf(currentUser.dob) }
        var editAvatarColor by remember { mutableLongStateOf(currentUser.avatarColorHex) }
        var editCoverColor by remember { mutableLongStateOf(currentUser.coverColorHex) }
        var errorMsg by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showEditProfileModal = false },
            title = { Text(tr(lang, "প্রোফাইল সম্পাদনা (Edit Profile)", "Edit Profile")) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editUsername,
                        onValueChange = { editUsername = it },
                        label = { Text("Unique Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Bio") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editCountry,
                        onValueChange = { editCountry = it },
                        label = { Text("Country") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editWebsite,
                        onValueChange = { editWebsite = it },
                        label = { Text("Website (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editGender,
                        onValueChange = { editGender = it },
                        label = { Text("Gender (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editDob,
                        onValueChange = { editDob = it },
                        label = { Text("Date of Birth (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(tr(lang, "প্রোফাইল ও কভার থিম কালার:", "Profile & Cover Color:"), style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0xFF2563EB, 0xFF7C3AED, 0xFF059669, 0xFFD97706, 0xFFDB2777).forEach { hex ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(hex))
                                    .clickable {
                                        editAvatarColor = hex
                                        editCoverColor = hex
                                    }
                            )
                        }
                    }

                    if (errorMsg != null) {
                        Text(errorMsg!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val err = viewModel.updateMyProfile(
                            fullName = editName,
                            username = editUsername,
                            bio = editBio,
                            country = editCountry,
                            website = editWebsite,
                            gender = editGender,
                            dob = editDob,
                            avatarColorHex = editAvatarColor,
                            coverColorHex = editCoverColor
                        )
                        if (err == null) {
                            showEditProfileModal = false
                        } else {
                            errorMsg = err
                        }
                    }
                ) {
                    Text(tr(lang, "সংরক্ষণ করুন", "Save"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileModal = false }) {
                    Text(tr(lang, "বাতিল", "Cancel"))
                }
            }
        )
    }
}

@Composable
private fun ProfileStatPill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DashboardMetricBox(
    modifier: Modifier,
    title: String,
    value: String,
    accent: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = accent.copy(alpha = 0.12f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = accent
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
