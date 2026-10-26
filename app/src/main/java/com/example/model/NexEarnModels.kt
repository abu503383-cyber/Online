package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class AppLanguage(val labelBn: String, val labelEn: String) {
    BN("বাংলা", "Bengali"),
    EN("English", "English")
}

enum class ScreenRoute {
    HOME,
    FRIENDS,
    CREATE_POST,
    WORK,
    MENU,
    PROFILE,
    NOTIFICATIONS,
    POST_RANK,
    INCOME_RANK,
    MESSAGES,
    CHAT_WINDOW,
    GROUPS,
    GROUP_DETAILS,
    JOINED_GROUPS,
    GIFTS,
    SETTINGS,
    TOTAL_USERS,
    SUPPORT,
    GLOBAL_SEARCH,
    FIREBASE_BRIDGE
}

enum class PostAudience(val labelBn: String, val labelEn: String) {
    PUBLIC("পাবলিক (Public)", "Public"),
    FRIENDS("শুধু বন্ধু (Friends)", "Friends"),
    ONLY_ME("শুধু আমি (Only Me)", "Only Me")
}

enum class PostMediaType(val labelBn: String, val labelEn: String) {
    TEXT("টেক্সট পোস্ট", "Text Post"),
    PHOTO("ছবি পোস্ট", "Photo Post"),
    VIDEO("ভিডিও পোস্ট", "Video Post"),
    MULTI_PHOTO("একাধিক ছবি", "Multiple Photos")
}

enum class FeedFilter(val labelBn: String, val labelEn: String) {
    LATEST("সর্বশেষ (Latest)", "Latest"),
    POPULAR("জনপ্রিয় (Popular)", "Popular"),
    FRIENDS("বন্ধুদের পোস্ট", "Friends' Posts"),
    SUGGESTED("প্রস্তাবিত (Suggested)", "Suggested")
}

enum class JobType(val labelBn: String, val labelEn: String) {
    WEBSITE_VISIT("ওয়েবসাইট ভিজিট", "Website Visit"),
    SOCIAL_ENGAGEMENT("সোশ্যাল এনগেজমেন্ট", "Social Engagement"),
    CUSTOM_TASK("কাস্টম টাস্ক", "Custom Task")
}

enum class TaskVerificationStatus(val labelBn: String, val labelEn: String) {
    PENDING("যাচাইাধীন (Pending)", "Pending"),
    APPROVED("অনুমোদিত (Approved)", "Approved"),
    REJECTED("বাতিল (Rejected)", "Rejected"),
    REWARDED("পুরস্কৃত (Rewarded)", "Rewarded")
}

enum class CoinTxType(val labelBn: String, val labelEn: String) {
    TASK_REWARD("কাজের পুরস্কার (Task Reward)", "Task Reward"),
    GIFT_REWARD("মিশন ও গিফট (Gift Reward)", "Gift Reward"),
    JOB_POSTING("কাজ পোস্ট বাজেট (Job Posting)", "Job Posting"),
    COIN_ADJUSTMENT("কয়েন সমন্বয় (Coin Adjustment)", "Coin Adjustment")
}

enum class NotificationCategory(val labelBn: String, val labelEn: String) {
    FRIEND_REQUEST("বন্ধুর অনুরোধ", "Friend Requests"),
    ACCEPTED_REQUEST("গৃহীত অনুরোধ", "Accepted Requests"),
    FOLLOWERS("নতুন ফলোয়ার", "Followers"),
    LIKES("লাইক", "Likes"),
    COMMENTS("কমেন্ট", "Comments"),
    GROUPS("গ্রুপ অ্যাক্টিভিটি", "Groups"),
    MESSAGES("মেসেজ", "Messages"),
    WORK("কাজ ও জব", "Work"),
    GIFT_REWARDS("গিফট ও পুরস্কার", "Gift Rewards"),
    SECURITY_ALERT("নিরাপত্তা সতর্কতা", "Security Alerts")
}

enum class TicketCategory(val labelBn: String, val labelEn: String) {
    ACCOUNT("অ্যাকাউন্ট সমস্যা (Account Problem)", "Account Problem"),
    LOGIN("লগইন সমস্যা (Login Problem)", "Login Problem"),
    COIN("কয়েন সমস্যা (Coin Problem)", "Coin Problem"),
    WORK("কাজের সমস্যা (Work Problem)", "Work Problem"),
    GROUP("গ্রুপ সমস্যা (Group Problem)", "Group Problem"),
    PAYMENT("পেমেন্ট/উইথড্র সমস্যা (Payment Problem)", "Payment Problem"),
    OTHER("অন্যান্য (Other)", "Other")
}

enum class TicketStatus(val labelBn: String, val labelEn: String) {
    OPEN("খোলা (Open)", "Open"),
    IN_PROGRESS("প্রক্রিয়াধীন (In Progress)", "In Progress"),
    WAITING_FOR_USER("ব্যবহারকারীর উত্তরের অপেক্ষায়", "Waiting for User"),
    RESOLVED("সমাধানকৃত (Resolved)", "Resolved"),
    CLOSED("বন্ধ (Closed)", "Closed")
}

enum class PrivacyOption(val labelBn: String, val labelEn: String) {
    EVERYONE("সবাই (Everyone)", "Everyone"),
    FRIENDS("শুধু বন্ধু (Friends)", "Friends"),
    ONLY_ME("কেউ না / শুধু আমি (Only Me)", "Only Me")
}

data class UserPrivacySettings(
    val whoCanSeePosts: PrivacyOption = PrivacyOption.EVERYONE,
    val whoCanSendFriendRequests: PrivacyOption = PrivacyOption.EVERYONE,
    val whoCanMessageMe: PrivacyOption = PrivacyOption.EVERYONE,
    val whoCanSeeFriends: PrivacyOption = PrivacyOption.FRIENDS,
    val whoCanFindProfile: PrivacyOption = PrivacyOption.EVERYONE
)

data class UserNotificationSettings(
    val friendRequests: Boolean = true,
    val messages: Boolean = true,
    val likes: Boolean = true,
    val comments: Boolean = true,
    val groups: Boolean = true,
    val work: Boolean = true,
    val gifts: Boolean = true
)

data class ActiveSessionItem(
    val sessionId: String = "",
    val deviceName: String = "",
    val location: String = "",
    val loginTime: String = "",
    val isCurrent: Boolean = false
)

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val uid: String = "",
    val fullName: String = "",
    val username: String = "",
    val emailOrPhone: String = "",
    val passwordHash: String = "",
    val bio: String = "",
    val country: String = "Bangladesh",
    val website: String = "",
    val gender: String = "Prefer not to say",
    val dob: String = "2000-01-01",
    val joinDate: String = "Oct 2026",
    val avatarColorHex: Long = 0xFF2563EB,
    val coverColorHex: Long = 0xFF7C3AED,
    val currentBalance: Int = 0,
    val totalEarnedCoins: Int = 0,
    val totalSpentCoins: Int = 0,
    val pendingCoins: Int = 0,
    val friendUids: List<String> = emptyList(),
    val followerUids: List<String> = emptyList(),
    val followingUids: List<String> = emptyList(),
    val blockedUids: List<String> = emptyList(),
    val savedPostIds: List<String> = emptyList(),
    val hiddenPostIds: List<String> = emptyList(),
    val isOnline: Boolean = true,
    val isVerified: Boolean = true,
    val privacySettings: UserPrivacySettings = UserPrivacySettings(),
    val notificationSettings: UserNotificationSettings = UserNotificationSettings(),
    val activeSessions: List<ActiveSessionItem> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class CommentItem(
    val commentId: String = "",
    val postId: String = "",
    val authorUid: String = "",
    val authorName: String = "",
    val authorUsername: String = "",
    val text: String = "",
    val timeAgo: String = "এইমাত্র"
)

@IgnoreExtraProperties
data class PostItem(
    @DocumentId val postId: String = "",
    val authorUid: String = "",
    val authorName: String = "",
    val authorUsername: String = "",
    val authorAvatarColor: Long = 0xFF2563EB,
    val timeAgo: String = "এইমাত্র",
    val createdTimestampMs: Long = System.currentTimeMillis(),
    val caption: String = "",
    val mediaType: PostMediaType = PostMediaType.TEXT,
    val mediaLabels: List<String> = emptyList(),
    val audience: PostAudience = PostAudience.PUBLIC,
    val likedByUids: List<String> = emptyList(),
    val comments: List<CommentItem> = emptyList(),
    val sharesCount: Int = 0,
    val viewedByUids: List<String> = emptyList(),
    val isApproved: Boolean = true,
    val isSpam: Boolean = false,
    val groupId: String? = null
)

@IgnoreExtraProperties
data class FriendRequestItem(
    @DocumentId val requestId: String = "",
    val fromUid: String = "",
    val fromName: String = "",
    val fromUsername: String = "",
    val fromAvatarColor: Long = 0xFF2563EB,
    val toUid: String = "",
    val toName: String = "",
    val toUsername: String = "",
    val mutualFriendsCount: Int = 3,
    val sentTimeAgo: String = "১০ মিনিট আগে",
    val status: String = "PENDING"
)

data class ChatMessageItem(
    val messageId: String = "",
    val senderUid: String = "",
    val receiverUid: String = "",
    val text: String = "",
    val emoji: String = "",
    val imageAttachmentLabel: String = "",
    val timestamp: String = "১০:৩০ AM",
    val isSeen: Boolean = true,
    val deletedBySender: Boolean = false,
    val deletedForEveryone: Boolean = false
)

@IgnoreExtraProperties
data class ConversationItem(
    @DocumentId val conversationId: String = "",
    val partnerUid: String = "",
    val partnerName: String = "",
    val partnerUsername: String = "",
    val partnerAvatarColor: Long = 0xFF7C3AED,
    val isOnline: Boolean = true,
    val unreadCount: Int = 0,
    val messages: List<ChatMessageItem> = emptyList()
)

enum class GroupPrivacy(val labelBn: String, val labelEn: String) {
    PUBLIC("পাবলিক গ্রুপ (Public)", "Public Group"),
    PRIVATE("প্রাইভেট গ্রুপ (Private)", "Private Group")
}

@IgnoreExtraProperties
data class SocialGroupItem(
    @DocumentId val groupId: String = "",
    val name: String = "",
    val description: String = "",
    val coverColorHex: Long = 0xFF2563EB,
    val privacy: GroupPrivacy = GroupPrivacy.PUBLIC,
    val createdDate: String = "Jan 2026",
    val adminUid: String = "",
    val adminName: String = "",
    val moderatorUids: List<String> = emptyList(),
    val memberUids: List<String> = emptyList(),
    val pendingJoinUids: List<String> = emptyList(),
    val rules: List<String> = emptyList(),
    val postsCount: Int = 0,
    val newPostsCount: Int = 2,
    val lastActivity: String = "৫ মিনিট আগে",
    val isRecommended: Boolean = true
)

@IgnoreExtraProperties
data class MicroJobItem(
    @DocumentId val jobId: String = "",
    val creatorUid: String = "",
    val creatorName: String = "",
    val jobType: JobType = JobType.WEBSITE_VISIT,
    val title: String = "",
    val description: String = "",
    val targetUrl: String = "",
    val totalWorkers: Int = 50,
    val completedWorkers: Int = 0,
    val rewardPerWorker: Int = 15,
    val requiredDurationSec: Int = 15,
    val proofRequired: Boolean = true,
    val totalBudget: Int = 750,
    val remainingBudget: Int = 750,
    val status: String = "ACTIVE", // ACTIVE, PAUSED, CANCELLED, COMPLETED
    val createdTimestampMs: Long = System.currentTimeMillis()
) {
    val remainingWorkers: Int
        get() = (totalWorkers - completedWorkers).coerceAtLeast(0)
}

@IgnoreExtraProperties
data class TaskSessionItem(
    @DocumentId val sessionId: String = "",
    val jobId: String = "",
    val jobTitle: String = "",
    val workerUid: String = "",
    val rewardCoins: Int = 0,
    val startedAt: String = "",
    val completedAt: String = "",
    val proofSubmitted: String = "",
    val verificationStatus: TaskVerificationStatus = TaskVerificationStatus.REWARDED,
    val rewardClaimed: Boolean = true
)

@IgnoreExtraProperties
data class GiftMissionItem(
    @DocumentId val missionId: String = "",
    val titleBn: String = "",
    val titleEn: String = "",
    val descriptionBn: String = "",
    val descriptionEn: String = "",
    val requiredActivityType: String = "", // POST, LIKE, JOB, FRIEND, DAILY_LOGIN
    val targetCount: Int = 1,
    val rewardCoins: Int = 50,
    val startDate: String = "01 Oct 2026",
    val endDate: String = "31 Dec 2026",
    val isActive: Boolean = true,
    val claimedByUids: List<String> = emptyList()
)

@IgnoreExtraProperties
data class CoinTransactionItem(
    @DocumentId val txId: String = "",
    val uid: String = "",
    val type: CoinTxType = CoinTxType.TASK_REWARD,
    val coins: Int = 0,
    val isCredit: Boolean = true,
    val status: String = "Completed", // Pending, Completed
    val description: String = "",
    val dateTime: String = ""
)

@IgnoreExtraProperties
data class AppNotificationItem(
    @DocumentId val notificationId: String = "",
    val targetUid: String = "",
    val actorName: String = "",
    val actorAvatarColor: Long = 0xFF2563EB,
    val category: NotificationCategory = NotificationCategory.GIFT_REWARDS,
    val messageBn: String = "",
    val messageEn: String = "",
    val timeAgo: String = "এইমাত্র",
    val targetRoute: ScreenRoute = ScreenRoute.HOME,
    val isRead: Boolean = false
)

@IgnoreExtraProperties
data class SupportTicketItem(
    @DocumentId val ticketId: String = "",
    val uid: String = "",
    val subject: String = "",
    val category: TicketCategory = TicketCategory.OTHER,
    val description: String = "",
    val attachmentName: String = "",
    val status: TicketStatus = TicketStatus.OPEN,
    val createdAt: String = "",
    val adminReply: String = ""
)
