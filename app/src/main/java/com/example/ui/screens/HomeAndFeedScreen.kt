package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.example.ui.theme.NexBluePrimary
import com.example.ui.theme.NexPurpleSecondary
import com.example.ui.theme.SuccessGreen
import com.example.viewmodel.NexEarnViewModel

@Composable
fun HomeScreen(
    viewModel: NexEarnViewModel,
    lang: AppLanguage
) {
    val currentUser = viewModel.getCurrentUser() ?: return
    val allPosts by viewModel.posts.collectAsState()
    val feedFilter by viewModel.feedFilter.collectAsState()
    val missions by viewModel.missions.collectAsState()

    val visiblePosts = remember(allPosts, feedFilter, currentUser) {
        val base = allPosts.filter { post ->
            post.isApproved &&
                !post.isSpam &&
                post.groupId == null &&
                !currentUser.hiddenPostIds.contains(post.postId) &&
                !currentUser.blockedUids.contains(post.authorUid) &&
                when (post.audience) {
                    PostAudience.PUBLIC -> true
                    PostAudience.FRIENDS -> post.authorUid == currentUser.uid || currentUser.friendUids.contains(post.authorUid)
                    PostAudience.ONLY_ME -> post.authorUid == currentUser.uid
                }
        }
        when (feedFilter) {
            FeedFilter.LATEST -> base.sortedByDescending { it.createdTimestampMs }
            FeedFilter.POPULAR -> base.sortedByDescending { it.likedByUids.size * 3 + it.comments.size * 2 + it.viewedByUids.size }
            FeedFilter.FRIENDS -> base.filter { currentUser.friendUids.contains(it.authorUid) || it.authorUid == currentUser.uid }
            FeedFilter.SUGGESTED -> base.filter { !currentUser.friendUids.contains(it.authorUid) || it.likedByUids.size >= 2 }
        }
    }

    val unclaimedMissionsCount = missions.count { it.isActive && !it.claimedByUids.contains(currentUser.uid) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        // Quick Earning & Mission Banner
        item {
            ElevatedCard(
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(NexBluePrimary, NexPurpleSecondary)
                            )
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tr(lang, "আজকের কাজ ও গিফট মিশন", "Today's Tasks & Gift Missions"),
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = tr(
                                    lang,
                                    "আপনার ব্যালেন্স: ${currentUser.currentBalance} Coins • মোট আয়: ${currentUser.totalEarnedCoins} Coins",
                                    "Balance: ${currentUser.currentBalance} Coins • Total Earned: ${currentUser.totalEarnedCoins} Coins"
                                ),
                                color = Color.White.copy(alpha = 0.9f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(
                                onClick = { viewModel.navigateTo(ScreenRoute.WORK) },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(tr(lang, "কাজ করুন", "Work"), fontWeight = FontWeight.Bold)
                            }
                            if (unclaimedMissionsCount > 0) {
                                FilledTonalButton(
                                    onClick = { viewModel.navigateTo(ScreenRoute.GIFTS) },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(tr(lang, "গিফট ($unclaimedMissionsCount)", "Gifts ($unclaimedMissionsCount)"), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Create Post Composer Card
        item {
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.navigateTo(ScreenRoute.CREATE_POST) }
                            .testTag("home_create_post_prompt")
                    ) {
                        UserAvatar(
                            name = currentUser.fullName,
                            colorHex = currentUser.avatarColorHex,
                            size = 42.dp,
                            isOnline = true
                        )
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = tr(
                                    lang,
                                    "আপনার মনে কী চলছে, ${currentUser.fullName.substringBefore(" ")}?",
                                    "What's on your mind, ${currentUser.fullName.substringBefore(" ")}?"
                                ),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TextButton(onClick = { viewModel.navigateTo(ScreenRoute.CREATE_POST) }) {
                            Icon(Icons.Outlined.Article, contentDescription = null, tint = NexBluePrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(tr(lang, "টেক্সট", "Text"))
                        }
                        TextButton(onClick = { viewModel.navigateTo(ScreenRoute.CREATE_POST) }) {
                            Icon(Icons.Outlined.PhotoLibrary, contentDescription = null, tint = SuccessGreen)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(tr(lang, "ছবি", "Photo"))
                        }
                        TextButton(onClick = { viewModel.navigateTo(ScreenRoute.CREATE_POST) }) {
                            Icon(Icons.Outlined.Videocam, contentDescription = null, tint = NexPurpleSecondary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(tr(lang, "ভিডিও", "Video"))
                        }
                    }
                }
            }
        }

        // Feed Filters (Section 4.2: Latest, Popular, Friends' Posts, Suggested Posts)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FeedFilter.entries.forEach { filter ->
                    val selected = feedFilter == filter
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.setFeedFilter(filter) },
                        label = {
                            Text(tr(lang, filter.labelBn, filter.labelEn))
                        },
                        leadingIcon = if (selected) {
                            { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }
        }

        // Feed Posts List
        if (visiblePosts.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Outlined.DynamicFeed,
                    title = tr(lang, "কোনো পোস্ট পাওয়া যায়নি", "No posts found"),
                    subtitle = tr(
                        lang,
                        "এই ফিল্টারে এখনো কোনো পোস্ট নেই। নতুন একটি পোস্ট তৈরি করুন!",
                        "Be the first to share a post in this feed!"
                    ),
                    actionLabel = tr(lang, "নতুন পোস্ট করুন", "Create Post"),
                    onAction = { viewModel.navigateTo(ScreenRoute.CREATE_POST) }
                )
            }
        } else {
            items(visiblePosts, key = { it.postId }) { post ->
                LaunchedEffect(post.postId) {
                    viewModel.registerUniquePostView(post.postId)
                }
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

/**
 * Section 4.1: Full Create Post Screen
 * Supports Text Post, Photo Post, Video Post, Multiple Photos, Caption, and Audience (Public, Friends, Only Me).
 */
@Composable
fun CreatePostScreen(
    viewModel: NexEarnViewModel,
    lang: AppLanguage
) {
    val currentUser = viewModel.getCurrentUser() ?: return
    var caption by remember { mutableStateOf("") }
    var selectedMediaType by remember { mutableStateOf(PostMediaType.TEXT) }
    var selectedAudience by remember { mutableStateOf(PostAudience.PUBLIC) }
    var mediaItems by remember { mutableStateOf(listOf<String>()) }
    var customMediaTitle by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ElevatedCard(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    UserAvatar(
                        name = currentUser.fullName,
                        colorHex = currentUser.avatarColorHex,
                        size = 46.dp,
                        isOnline = true
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser.fullName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "@${currentUser.username}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Audience Selector Chips (Public, Friends, Only Me)
                Text(
                    text = tr(lang, "কে দেখতে পাবে (Audience):", "Select Audience:"),
                    style = MaterialTheme.typography.labelLarge
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PostAudience.entries.forEach { aud ->
                        FilterChip(
                            selected = selectedAudience == aud,
                            onClick = { selectedAudience = aud },
                            label = { Text(tr(lang, aud.labelBn, aud.labelEn)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = when (aud) {
                                        PostAudience.PUBLIC -> Icons.Outlined.Public
                                        PostAudience.FRIENDS -> Icons.Outlined.Group
                                        PostAudience.ONLY_ME -> Icons.Outlined.Lock
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }

                // Post Media Type Selector (Text, Photo, Video, Multiple Photos)
                Text(
                    text = tr(lang, "পোস্টের ধরন (Post Type):", "Post Format:"),
                    style = MaterialTheme.typography.labelLarge
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PostMediaType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedMediaType == type,
                            onClick = {
                                selectedMediaType = type
                                mediaItems = when (type) {
                                    PostMediaType.TEXT -> emptyList()
                                    PostMediaType.PHOTO -> listOf("High-Res Photo • Uploaded")
                                    PostMediaType.VIDEO -> listOf("HD Video Clip • 01:30")
                                    PostMediaType.MULTI_PHOTO -> listOf("Album Photo #1", "Album Photo #2", "Album Photo #3")
                                }
                            },
                            label = { Text(tr(lang, type.labelBn, type.labelEn)) }
                        )
                    }
                }

                // Caption Text Field
                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = {
                        Text(tr(lang, "ক্যাপশন বা আপনার কথা লিখুন...", "Write your caption..."))
                    },
                    minLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_post_caption_input")
                )

                // Optional Media Attachment Manager
                if (selectedMediaType != PostMediaType.TEXT) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = tr(lang, "সংযুক্ত মিডিয়া (${mediaItems.size}):", "Attached Media (${mediaItems.size}):"),
                                style = MaterialTheme.typography.labelLarge
                            )
                            mediaItems.forEachIndexed { idx, label ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (selectedMediaType == PostMediaType.VIDEO) Icons.Filled.PlayCircle else Icons.Filled.Image,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(label, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    IconButton(
                                        onClick = {
                                            mediaItems = mediaItems.filterIndexed { i, _ -> i != idx }
                                        }
                                    ) {
                                        Icon(Icons.Outlined.Close, contentDescription = "Remove")
                                    }
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = customMediaTitle,
                                    onValueChange = { customMediaTitle = it },
                                    placeholder = { Text(tr(lang, "নতুন ছবি/ভিডিও নাম যুক্ত করুন", "Add media title")) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                FilledTonalButton(
                                    onClick = {
                                        if (customMediaTitle.isNotBlank()) {
                                            mediaItems = mediaItems + customMediaTitle.trim()
                                            customMediaTitle = ""
                                        } else {
                                            mediaItems = mediaItems + "Media File #${mediaItems.size + 1}"
                                        }
                                    }
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = null)
                                    Text(tr(lang, "যুক্ত", "Add"))
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        viewModel.createPost(
                            caption = caption,
                            mediaType = selectedMediaType,
                            mediaLabels = mediaItems,
                            audience = selectedAudience
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("publish_post_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tr(lang, "পোস্ট প্রকাশ করুন (Post)", "Publish Post"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

/**
 * Section 4.2: Post Card with Like, Comment, Share, View & Complete Three-dot Menu
 */
@Composable
fun PostCardItem(
    post: PostItem,
    currentUser: UserProfile,
    lang: AppLanguage,
    viewModel: NexEarnViewModel
) {
    val isMyPost = post.authorUid == currentUser.uid
    val isLiked = post.likedByUids.contains(currentUser.uid)
    val isSaved = currentUser.savedPostIds.contains(post.postId)

    var menuExpanded by remember { mutableStateOf(false) }
    var showCommentsSection by remember { mutableStateOf(false) }
    var commentDraft by remember { mutableStateOf("") }

    // Modals for Three-Dot Menu Actions
    var showEditDialog by remember { mutableStateOf(false) }
    var showStatsDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }

    ElevatedCard(
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("post_card_${post.postId}")
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            // Post Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.openUserProfile(post.authorUid) }
                ) {
                    UserAvatar(
                        name = post.authorName,
                        colorHex = post.authorAvatarColor,
                        size = 44.dp,
                        isOnline = true
                    )
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = post.authorName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = "Verified",
                                tint = NexBluePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "@${post.authorUsername} • ${post.timeAgo}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                imageVector = when (post.audience) {
                                    PostAudience.PUBLIC -> Icons.Outlined.Public
                                    PostAudience.FRIENDS -> Icons.Outlined.Group
                                    PostAudience.ONLY_ME -> Icons.Outlined.Lock
                                },
                                contentDescription = post.audience.labelEn,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Three-dot Menu (Section 4.2)
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.testTag("post_menu_${post.postId}")
                    ) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Post Menu")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        if (isMyPost) {
                            // Own Post Menu: Edit Post, Delete Post, Change Audience, View Statistics
                            DropdownMenuItem(
                                text = { Text(tr(lang, "পোস্ট এডিট ও অডিয়েন্স পরিবর্তন (Edit Post)", "Edit Post & Audience")) },
                                leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    showEditDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(tr(lang, "পরিসংখ্যান দেখুন (View Statistics)", "View Statistics")) },
                                leadingIcon = { Icon(Icons.Outlined.BarChart, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    showStatsDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(tr(lang, "পোস্ট মুছে ফেলুন (Delete Post)", "Delete Post"), color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    menuExpanded = false
                                    viewModel.deletePost(post.postId)
                                }
                            )
                        } else {
                            // Other's Post Menu: Save Post, Report Post, Hide Post, Copy Link
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (isSaved) tr(lang, "সংরক্ষণ সরান (Unsave Post)", "Unsave Post")
                                        else tr(lang, "সংরক্ষণ করুন (Save Post)", "Save Post")
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    viewModel.toggleSavePost(post.postId)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(tr(lang, "পোস্ট লুকান (Hide Post)", "Hide Post")) },
                                leadingIcon = { Icon(Icons.Outlined.VisibilityOff, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    viewModel.hidePost(post.postId)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(tr(lang, "লিংক কপি করুন (Copy Link)", "Copy Link")) },
                                leadingIcon = { Icon(Icons.Outlined.Link, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    viewModel.showFeedback("পোস্টের লিংক ক্লিপবোর্ডে কপি করা হয়েছে!", "Post link copied!")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(tr(lang, "রিপোর্ট করুন (Report Post)", "Report Post"), color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(Icons.Outlined.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    menuExpanded = false
                                    showReportDialog = true
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Post Caption
            if (post.caption.isNotBlank()) {
                Text(
                    text = post.caption,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Media Preview Card (if Photo, Video, or Multi-Photo)
            if (post.mediaType != PostMediaType.TEXT && post.mediaLabels.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    post.mediaLabels.forEachIndexed { idx, label ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (post.mediaType == PostMediaType.MULTI_PHOTO) 110.dp else 165.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.linearGradient(
                                        if (idx % 2 == 0) listOf(Color(0xFF1E3A8A), Color(0xFF5B21B6))
                                        else listOf(Color(0xFF065F46), Color(0xFF1E40AF))
                                    )
                                )
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = when (post.mediaType) {
                                        PostMediaType.VIDEO -> Icons.Filled.PlayCircleFilled
                                        PostMediaType.MULTI_PHOTO -> Icons.Filled.Collections
                                        else -> Icons.Filled.Photo
                                    },
                                    contentDescription = label,
                                    tint = Color.White,
                                    modifier = Modifier.size(38.dp)
                                )
                                Text(
                                    text = label,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Stats Row (Likes, Comments, Shares, Unique Views)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${post.likedByUids.size} Likes • ${post.comments.size} Comments",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${post.sharesCount} Shares • ${post.viewedByUids.size} Views",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()

            // Action Buttons: Like, Comment, Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = { viewModel.toggleLikePost(post.postId) },
                    modifier = Modifier.testTag("like_btn_${post.postId}")
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                        contentDescription = "Like",
                        tint = if (isLiked) NexBluePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = tr(lang, "লাইক (${post.likedByUids.size})", "Like (${post.likedByUids.size})"),
                        color = if (isLiked) NexBluePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                TextButton(
                    onClick = { showCommentsSection = !showCommentsSection },
                    modifier = Modifier.testTag("comment_btn_${post.postId}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ModeComment,
                        contentDescription = "Comment",
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(tr(lang, "কমেন্ট (${post.comments.size})", "Comment (${post.comments.size})"))
                }

                TextButton(
                    onClick = { viewModel.sharePost(post.postId) },
                    modifier = Modifier.testTag("share_btn_${post.postId}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share",
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(tr(lang, "শেয়ার", "Share"))
                }
            }

            // Expandable Comments Section
            AnimatedVisibility(visible = showCommentsSection) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider()
                    post.comments.forEach { cmt ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${cmt.authorName} (@${cmt.authorUsername})",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = cmt.timeAgo,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = cmt.text, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = commentDraft,
                            onValueChange = { commentDraft = it },
                            placeholder = { Text(tr(lang, "একটি কমেন্ট লিখুন...", "Write a comment...")) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                if (commentDraft.isNotBlank()) {
                                    viewModel.addCommentToPost(post.postId, commentDraft)
                                    commentDraft = ""
                                }
                            }
                        ) {
                            Icon(Icons.Filled.Send, contentDescription = "Send Comment", tint = NexBluePrimary)
                        }
                    }
                }
            }
        }
    }

    // Edit Post & Change Audience Dialog
    if (showEditDialog) {
        var editedCaption by remember { mutableStateOf(post.caption) }
        var editedAudience by remember { mutableStateOf(post.audience) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(tr(lang, "পোস্ট ও অডিয়েন্স পরিবর্তন", "Edit Post & Audience")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editedCaption,
                        onValueChange = { editedCaption = it },
                        label = { Text(tr(lang, "ক্যাপশন", "Caption")) },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(tr(lang, "অডিয়েন্স নির্বাচন:", "Audience:"), style = MaterialTheme.typography.labelLarge)
                    PostAudience.entries.forEach { aud ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { editedAudience = aud }
                        ) {
                            RadioButton(selected = editedAudience == aud, onClick = { editedAudience = aud })
                            Text(tr(lang, aud.labelBn, aud.labelEn))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.editPost(post.postId, editedCaption, editedAudience)
                        showEditDialog = false
                    }
                ) {
                    Text(tr(lang, "সংরক্ষণ করুন", "Save Changes"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text(tr(lang, "বাতিল", "Cancel"))
                }
            }
        )
    }

    // View Post Statistics Dialog
    if (showStatsDialog) {
        AlertDialog(
            onDismissRequest = { showStatsDialog = false },
            title = { Text(tr(lang, "পোস্টের বিস্তারিত পরিসংখ্যান", "Post Performance Statistics")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• মোট ইউনিক ভিউ (Total Views): ${post.viewedByUids.size}")
                    Text("• মোট লাইক (Total Likes): ${post.likedByUids.size}")
                    Text("• মোট কমেন্ট (Total Comments): ${post.comments.size}")
                    Text("• মোট শেয়ার (Total Shares): ${post.sharesCount}")
                    Text("• বর্তমান অডিয়েন্স: ${tr(lang, post.audience.labelBn, post.audience.labelEn)}")
                    Text("• স্ট্যাটাস: অনুমোদিত (Approved)")
                }
            },
            confirmButton = {
                Button(onClick = { showStatsDialog = false }) {
                    Text(tr(lang, "ঠিক আছে", "Close"))
                }
            }
        )
    }

    // Report Post Dialog
    if (showReportDialog) {
        var reason by remember { mutableStateOf("স্প্যাম বা বিভ্রান্তিকর তথ্য (Spam / Misleading)") }
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text(tr(lang, "পোস্ট রিপোর্ট করুন", "Report Post")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(tr(lang, "রিপোর্ট করার কারণ লিখুন:", "Reason for reporting:"))
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.reportContent("Post", post.postId, reason)
                        showReportDialog = false
                    }
                ) {
                    Text(tr(lang, "রিপোর্ট জমা দিন", "Submit Report"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text(tr(lang, "বাতিল", "Cancel"))
                }
            }
        )
    }
}

/**
 * Section 3 & 21: Global Search Screen (Users, Posts, Groups, Jobs)
 */
@Composable
fun GlobalSearchScreen(
    viewModel: NexEarnViewModel,
    lang: AppLanguage
) {
    val query by viewModel.globalSearchQuery.collectAsState()
    val users by viewModel.users.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val jobs by viewModel.jobs.collectAsState()

    val q = query.trim().lowercase()

    val matchedUsers = users.filter {
        it.privacySettings.whoCanFindProfile != PrivacyOption.ONLY_ME &&
            (q.isEmpty() || it.fullName.lowercase().contains(q) || it.username.lowercase().contains(q))
    }
    val matchedPosts = posts.filter {
        it.isApproved && (q.isEmpty() || it.caption.lowercase().contains(q) || it.authorName.lowercase().contains(q))
    }
    val matchedGroups = groups.filter {
        q.isEmpty() || it.name.lowercase().contains(q) || it.description.lowercase().contains(q)
    }
    val matchedJobs = jobs.filter {
        q.isEmpty() || it.title.lowercase().contains(q) || it.description.lowercase().contains(q)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.setGlobalSearchQuery(it) },
                label = { Text(tr(lang, "ইউজার, পোস্ট, গ্রুপ বা কাজ খুঁজুন...", "Search users, posts, groups, jobs...")) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setGlobalSearchQuery("") }) {
                            Icon(Icons.Outlined.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("global_search_input")
            )
        }

        // Users Section
        item {
            Text(
                text = tr(lang, "ব্যবহারকারী (${matchedUsers.size})", "Users (${matchedUsers.size})"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        items(matchedUsers.take(4), key = { "u_${it.uid}" }) { u ->
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
                        UserAvatar(name = u.fullName, colorHex = u.avatarColorHex, size = 40.dp)
                        Column {
                            Text(u.fullName, fontWeight = FontWeight.Bold)
                            Text("@${u.username} • ${u.country}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    CoinBadge(coins = u.totalEarnedCoins)
                }
            }
        }

        // Jobs Section
        item {
            Text(
                text = tr(lang, "উপলভ্য কাজ (${matchedJobs.size})", "Jobs (${matchedJobs.size})"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        items(matchedJobs.take(3), key = { "j_${it.jobId}" }) { j ->
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(ScreenRoute.WORK) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(j.title, fontWeight = FontWeight.Bold)
                        Text("${j.remainingWorkers}/${j.totalWorkers} Slots • ${j.requiredDurationSec}s", style = MaterialTheme.typography.bodySmall)
                    }
                    CoinBadge(coins = j.rewardPerWorker)
                }
            }
        }

        // Groups Section
        item {
            Text(
                text = tr(lang, "গ্রুপসমূহ (${matchedGroups.size})", "Groups (${matchedGroups.size})"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        items(matchedGroups.take(3), key = { "g_${it.groupId}" }) { g ->
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.openGroupDetails(g.groupId) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(g.name, fontWeight = FontWeight.Bold)
                        Text("${g.memberUids.size} Members • ${g.postsCount} Posts", style = MaterialTheme.typography.bodySmall)
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null)
                }
            }
        }

        // Posts Section
        item {
            Text(
                text = tr(lang, "পোস্টসমূহ (${matchedPosts.size})", "Posts (${matchedPosts.size})"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        items(matchedPosts.take(3), key = { "p_${it.postId}" }) { p ->
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(ScreenRoute.HOME) }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("${p.authorName} (@${p.authorUsername})", fontWeight = FontWeight.Bold)
                    Text(p.caption, maxLines = 2, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
