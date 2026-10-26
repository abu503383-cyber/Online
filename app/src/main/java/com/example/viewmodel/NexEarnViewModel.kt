package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FirebaseAdminBridge
import com.example.data.OperationType
import com.example.model.*
import com.example.ui.theme.AppThemeMode
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class ActiveTaskState(
    val job: MicroJobItem,
    val remainingSeconds: Int,
    val isTimerCompleted: Boolean = false,
    val urlOpened: Boolean = false
)

class NexEarnViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("nexearn_prefs_live", Context.MODE_PRIVATE)
    private val firestoreListeners = mutableListOf<ListenerRegistration>()

    // Language & Theme
    private val _language = MutableStateFlow(AppLanguage.BN)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _themeMode = MutableStateFlow(AppThemeMode.SYSTEM)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    // Navigation & Backstack
    private val _currentScreen = MutableStateFlow(ScreenRoute.HOME)
    val currentScreen: StateFlow<ScreenRoute> = _currentScreen.asStateFlow()
    private val screenBackStack = ArrayDeque<ScreenRoute>()

    // Viewed Profile & Selected Chat / Group
    private val _selectedProfileUid = MutableStateFlow<String?>(null)
    val selectedProfileUid: StateFlow<String?> = _selectedProfileUid.asStateFlow()

    private val _selectedConversationId = MutableStateFlow<String?>(null)
    val selectedConversationId: StateFlow<String?> = _selectedConversationId.asStateFlow()

    private val _selectedGroupId = MutableStateFlow<String?>(null)
    val selectedGroupId: StateFlow<String?> = _selectedGroupId.asStateFlow()

    // Toast / Feedback Banner
    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    // Logout Confirmation Modal
    private val _showLogoutDialog = MutableStateFlow(false)
    val showLogoutDialog: StateFlow<Boolean> = _showLogoutDialog.asStateFlow()

    // Authenticated Session State
    private val _currentUserUid = MutableStateFlow<String?>(null)
    val currentUserUid: StateFlow<String?> = _currentUserUid.asStateFlow()

    // Real Registered Users
    private val _users = MutableStateFlow<List<UserProfile>>(emptyList())
    val users: StateFlow<List<UserProfile>> = _users.asStateFlow()

    // Social Feed Posts
    private val _posts = MutableStateFlow<List<PostItem>>(emptyList())
    val posts: StateFlow<List<PostItem>> = _posts.asStateFlow()

    private val _feedFilter = MutableStateFlow(FeedFilter.LATEST)
    val feedFilter: StateFlow<FeedFilter> = _feedFilter.asStateFlow()

    // Friend Requests
    private val _friendRequests = MutableStateFlow<List<FriendRequestItem>>(emptyList())
    val friendRequests: StateFlow<List<FriendRequestItem>> = _friendRequests.asStateFlow()

    // Conversations / Messages
    private val _conversations = MutableStateFlow<List<ConversationItem>>(emptyList())
    val conversations: StateFlow<List<ConversationItem>> = _conversations.asStateFlow()

    // Groups
    private val _groups = MutableStateFlow<List<SocialGroupItem>>(emptyList())
    val groups: StateFlow<List<SocialGroupItem>> = _groups.asStateFlow()

    // Work / Jobs & Task Sessions
    private val _jobs = MutableStateFlow<List<MicroJobItem>>(emptyList())
    val jobs: StateFlow<List<MicroJobItem>> = _jobs.asStateFlow()

    private val _taskSessions = MutableStateFlow<List<TaskSessionItem>>(emptyList())
    val taskSessions: StateFlow<List<TaskSessionItem>> = _taskSessions.asStateFlow()

    private val _activeTask = MutableStateFlow<ActiveTaskState?>(null)
    val activeTask: StateFlow<ActiveTaskState?> = _activeTask.asStateFlow()
    private var timerJob: Job? = null

    // Gifts & Missions (Controlled via Admin Panel / Firestore `/missions`)
    private val _missions = MutableStateFlow<List<GiftMissionItem>>(emptyList())
    val missions: StateFlow<List<GiftMissionItem>> = _missions.asStateFlow()

    // Coin Ledger / Transactions
    private val _coinTransactions = MutableStateFlow<List<CoinTransactionItem>>(emptyList())
    val coinTransactions: StateFlow<List<CoinTransactionItem>> = _coinTransactions.asStateFlow()

    // Notifications
    private val _notifications = MutableStateFlow<List<AppNotificationItem>>(emptyList())
    val notifications: StateFlow<List<AppNotificationItem>> = _notifications.asStateFlow()

    // Support Tickets
    private val _supportTickets = MutableStateFlow<List<SupportTicketItem>>(emptyList())
    val supportTickets: StateFlow<List<SupportTicketItem>> = _supportTickets.asStateFlow()

    // Global Search Query
    private val _globalSearchQuery = MutableStateFlow("")
    val globalSearchQuery: StateFlow<String> = _globalSearchQuery.asStateFlow()

    init {
        val savedLang = prefs.getString("app_lang", "BN") ?: "BN"
        _language.value = if (savedLang == "EN") AppLanguage.EN else AppLanguage.BN
        val savedTheme = prefs.getString("app_theme", "SYSTEM") ?: "SYSTEM"
        _themeMode.value = runCatching { AppThemeMode.valueOf(savedTheme) }.getOrDefault(AppThemeMode.SYSTEM)

        loadPersistedUsers()

        val savedUid = prefs.getString("logged_in_uid", null)
        if (savedUid != null && _users.value.any { it.uid == savedUid }) {
            _currentUserUid.value = savedUid
            attachCloudRealtimeListeners()
        }
    }

    private fun saveUsersLocally(usersList: List<UserProfile>) {
        val arr = JSONArray()
        usersList.forEach { u ->
            arr.put(
                JSONObject().apply {
                    put("uid", u.uid)
                    put("fullName", u.fullName)
                    put("username", u.username)
                    put("emailOrPhone", u.emailOrPhone)
                    put("passwordHash", u.passwordHash)
                    put("bio", u.bio)
                    put("country", u.country)
                    put("website", u.website)
                    put("gender", u.gender)
                    put("dob", u.dob)
                    put("joinDate", u.joinDate)
                    put("avatarColorHex", u.avatarColorHex)
                    put("coverColorHex", u.coverColorHex)
                    put("currentBalance", u.currentBalance)
                    put("totalEarnedCoins", u.totalEarnedCoins)
                    put("totalSpentCoins", u.totalSpentCoins)
                    put("pendingCoins", u.pendingCoins)
                }
            )
        }
        prefs.edit().putString("registered_users_json", arr.toString()).apply()
    }

    private fun loadPersistedUsers() {
        val raw = prefs.getString("registered_users_json", null) ?: return
        try {
            val arr = JSONArray(raw)
            val list = mutableListOf<UserProfile>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    UserProfile(
                        uid = obj.optString("uid"),
                        fullName = obj.optString("fullName"),
                        username = obj.optString("username"),
                        emailOrPhone = obj.optString("emailOrPhone"),
                        passwordHash = obj.optString("passwordHash"),
                        bio = obj.optString("bio"),
                        country = obj.optString("country", "Bangladesh"),
                        website = obj.optString("website"),
                        gender = obj.optString("gender", "Prefer not to say"),
                        dob = obj.optString("dob", "2000-01-01"),
                        joinDate = obj.optString("joinDate", currentMonthYear()),
                        avatarColorHex = obj.optLong("avatarColorHex", 0xFF2563EB),
                        coverColorHex = obj.optLong("coverColorHex", 0xFF7C3AED),
                        currentBalance = obj.optInt("currentBalance", 0),
                        totalEarnedCoins = obj.optInt("totalEarnedCoins", 0),
                        totalSpentCoins = obj.optInt("totalSpentCoins", 0),
                        pendingCoins = obj.optInt("pendingCoins", 0)
                    )
                )
            }
            _users.value = list
        } catch (_: Exception) {
        }
    }

    /**
     * Attaches live Firestore listeners for project `facebook-c5870` when a user is signed in.
     */
    private fun attachCloudRealtimeListeners() {
        clearCloudListeners()
        val db = FirebaseAdminBridge.getFirestoreInstanceOrNull(getApplication()) ?: return

        // Listen to `/users`
        firestoreListeners.add(
            db.collection(FirebaseAdminBridge.COL_USERS)
                .addSnapshotListener { snap, err ->
                    if (err != null) {
                        FirebaseAdminBridge.handleFirestoreError(err, OperationType.LIST, FirebaseAdminBridge.COL_USERS)
                        return@addSnapshotListener
                    }
                    if (snap != null && !snap.isEmpty) {
                        val cloudUsers = snap.toObjects(UserProfile::class.java)
                        _users.update { local ->
                            val merged = (cloudUsers + local).distinctBy { it.uid }
                            saveUsersLocally(merged)
                            merged
                        }
                    }
                }
        )

        // Listen to `/posts`
        firestoreListeners.add(
            db.collection(FirebaseAdminBridge.COL_POSTS)
                .addSnapshotListener { snap, err ->
                    if (err != null) {
                        FirebaseAdminBridge.handleFirestoreError(err, OperationType.LIST, FirebaseAdminBridge.COL_POSTS)
                        return@addSnapshotListener
                    }
                    if (snap != null && !snap.isEmpty) {
                        val cloudPosts = snap.toObjects(PostItem::class.java)
                        _posts.update { local -> (cloudPosts + local).distinctBy { it.postId } }
                    }
                }
        )

        // Listen to `/jobs`
        firestoreListeners.add(
            db.collection(FirebaseAdminBridge.COL_JOBS)
                .addSnapshotListener { snap, err ->
                    if (err != null) {
                        FirebaseAdminBridge.handleFirestoreError(err, OperationType.LIST, FirebaseAdminBridge.COL_JOBS)
                        return@addSnapshotListener
                    }
                    if (snap != null && !snap.isEmpty) {
                        val cloudJobs = snap.toObjects(MicroJobItem::class.java)
                        _jobs.update { local -> (cloudJobs + local).distinctBy { it.jobId } }
                    }
                }
        )

        // Listen to `/missions` (Admin Panel controlled missions)
        firestoreListeners.add(
            db.collection(FirebaseAdminBridge.COL_MISSIONS)
                .addSnapshotListener { snap, err ->
                    if (err != null) {
                        FirebaseAdminBridge.handleFirestoreError(err, OperationType.LIST, FirebaseAdminBridge.COL_MISSIONS)
                        return@addSnapshotListener
                    }
                    if (snap != null && !snap.isEmpty) {
                        val cloudMissions = snap.toObjects(GiftMissionItem::class.java)
                        _missions.update { local -> (cloudMissions + local).distinctBy { it.missionId } }
                    }
                }
        )
    }

    private fun clearCloudListeners() {
        firestoreListeners.forEach { it.remove() }
        firestoreListeners.clear()
    }

    override fun onCleared() {
        super.onCleared()
        clearCloudListeners()
    }

    fun hashPassword(plain: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(plain.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun nowFormatted(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)
        return sdf.format(Date())
    }

    private fun currentMonthYear(): String {
        val sdf = SimpleDateFormat("MMM yyyy", Locale.ENGLISH)
        return sdf.format(Date())
    }

    fun showFeedback(messageBn: String, messageEn: String = messageBn) {
        _feedbackMessage.value = if (_language.value == AppLanguage.BN) messageBn else messageEn
        viewModelScope.launch {
            delay(3200)
            _feedbackMessage.value = null
        }
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
    }

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
        prefs.edit().putString("app_lang", lang.name).apply()
        showFeedback("ভাষা পরিবর্তন করা হয়েছে: বাংলা", "Language switched to English")
    }

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("app_theme", mode.name).apply()
        showFeedback("থিম আপডেট করা হয়েছে", "Theme updated")
    }

    // Navigation Helpers
    fun navigateTo(route: ScreenRoute) {
        if (_currentScreen.value != route) {
            screenBackStack.addLast(_currentScreen.value)
            _currentScreen.value = route
        }
    }

    fun navigateBack(): Boolean {
        return if (screenBackStack.isNotEmpty()) {
            _currentScreen.value = screenBackStack.removeLast()
            true
        } else if (_currentScreen.value != ScreenRoute.HOME) {
            _currentScreen.value = ScreenRoute.HOME
            true
        } else {
            false
        }
    }

    fun openUserProfile(uid: String) {
        _selectedProfileUid.value = uid
        navigateTo(ScreenRoute.PROFILE)
    }

    fun openMyProfile() {
        _selectedProfileUid.value = _currentUserUid.value
        navigateTo(ScreenRoute.PROFILE)
    }

    fun openChatWithUser(partnerUid: String) {
        val myUid = _currentUserUid.value ?: return
        val me = getCurrentUser() ?: return
        val partner = _users.value.find { it.uid == partnerUid } ?: return
        if (me.blockedUids.contains(partnerUid) || partner.blockedUids.contains(myUid)) {
            showFeedback(
                "ব্লক করা ব্যবহারকারীর সাথে মেসেজ আদান-প্রদান সীমিত!",
                "Messaging is restricted with blocked users!"
            )
            return
        }
        val existing = _conversations.value.find { it.partnerUid == partnerUid }
        if (existing != null) {
            _selectedConversationId.value = existing.conversationId
            _conversations.update { list ->
                list.map {
                    if (it.conversationId == existing.conversationId) it.copy(unreadCount = 0) else it
                }
            }
        } else {
            val newConv = ConversationItem(
                conversationId = "conv_${UUID.randomUUID().toString().take(8)}",
                partnerUid = partner.uid,
                partnerName = partner.fullName,
                partnerUsername = partner.username,
                partnerAvatarColor = partner.avatarColorHex,
                isOnline = partner.isOnline,
                unreadCount = 0,
                messages = emptyList()
            )
            _conversations.update { listOf(newConv) + it }
            _selectedConversationId.value = newConv.conversationId
        }
        navigateTo(ScreenRoute.CHAT_WINDOW)
    }

    fun openGroupDetails(groupId: String) {
        _selectedGroupId.value = groupId
        navigateTo(ScreenRoute.GROUP_DETAILS)
    }

    fun getCurrentUser(): UserProfile? {
        val uid = _currentUserUid.value ?: return null
        return _users.value.find { it.uid == uid }
    }

    // =========================================================================
    // SECTION 20 & 19: Authentication (Login, Registration, Password Reset, Logout)
    // =========================================================================

    fun loginWithCredentials(identifier: String, passwordPlain: String): Boolean {
        val cleanId = identifier.trim().removePrefix("@").lowercase()
        if (cleanId.isEmpty() || passwordPlain.isEmpty()) {
            showFeedback(
                "অনুগ্রহ করে ইউজারনেম/ইমেইল এবং পাসওয়ার্ড লিখুন",
                "Please enter username/email and password"
            )
            return false
        }
        val passHash = hashPassword(passwordPlain)
        val matched = _users.value.find {
            (it.username.lowercase() == cleanId || it.emailOrPhone.lowercase() == cleanId) &&
                it.passwordHash == passHash
        }
        return if (matched != null) {
            _currentUserUid.value = matched.uid
            prefs.edit().putString("logged_in_uid", matched.uid).apply()
            FirebaseAdminBridge.syncUserProfileToCloud(getApplication(), matched)
            attachCloudRealtimeListeners()
            showFeedback("স্বাগতম, ${matched.fullName}!", "Welcome back, ${matched.fullName}!")
            true
        } else {
            showFeedback(
                "একাউন্ট পাওয়া যায়নি বা পাসওয়ার্ড ভুল! নতুন একাউন্ট খুলতে রেজিস্ট্রেশন ট্যাবে যান।",
                "Invalid username/email or password! Please register if you don't have an account."
            )
            false
        }
    }

    fun loginWithGoogleAccount(uid: String, fullName: String, email: String) {
        val existing = _users.value.find { it.emailOrPhone.equals(email, ignoreCase = true) || it.uid == uid }
        if (existing != null) {
            _currentUserUid.value = existing.uid
            prefs.edit().putString("logged_in_uid", existing.uid).apply()
            FirebaseAdminBridge.syncUserProfileToCloud(getApplication(), existing)
            attachCloudRealtimeListeners()
            showFeedback("Google একাউন্টে লগইন সফল!", "Signed in with Google!")
        } else {
            val baseUsername = email.substringBefore("@").replace("[^a-zA-Z0-9_]".toRegex(), "").ifBlank { "user" }
            val uniqueUsername = if (_users.value.any { it.username.equals(baseUsername, true) }) {
                "${baseUsername}_${(100..999).random()}"
            } else baseUsername

            val newUser = UserProfile(
                uid = uid,
                fullName = fullName,
                username = uniqueUsername,
                emailOrPhone = email,
                passwordHash = hashPassword("google_oauth_$uid"),
                bio = "",
                country = "Bangladesh",
                joinDate = currentMonthYear(),
                currentBalance = 0,
                totalEarnedCoins = 0,
                activeSessions = listOf(
                    ActiveSessionItem("sess_1", "Android Google Auth", "Bangladesh", "Active", true)
                )
            )
            _users.update {
                val updated = listOf(newUser) + it
                saveUsersLocally(updated)
                updated
            }
            _currentUserUid.value = newUser.uid
            prefs.edit().putString("logged_in_uid", newUser.uid).apply()
            FirebaseAdminBridge.syncUserProfileToCloud(getApplication(), newUser)
            attachCloudRealtimeListeners()
            showFeedback("Google একাউন্টের মাধ্যমে রেজিস্ট্রেশন সফল!", "Registered with Google!")
        }
    }

    fun isUsernameAvailable(username: String, ignoreUid: String? = null): Boolean {
        val clean = username.trim().removePrefix("@").lowercase()
        if (clean.length < 3) return false
        return _users.value.none { it.uid != ignoreUid && it.username.lowercase() == clean }
    }

    fun registerNewUser(
        fullName: String,
        username: String,
        emailOrPhone: String,
        passwordPlain: String,
        confirmPasswordPlain: String,
        country: String,
        acceptedTerms: Boolean
    ): String? {
        val cleanName = fullName.trim()
        val cleanUsername = username.trim().removePrefix("@").lowercase()
        val cleanContact = emailOrPhone.trim()

        if (cleanName.length < 2) return "পূর্ণ নাম অন্তত ২ অক্ষরের হতে হবে।"
        if (cleanUsername.length < 3 || !cleanUsername.matches("^[a-z0-9_]+$".toRegex())) {
            return "ইউজারনেম কমপক্ষে ৩ অক্ষরের (শুধুমাত্র ইংরেজি ছোট হাতের অক্ষর, সংখ্যা বা _) হতে হবে।"
        }
        if (!isUsernameAvailable(cleanUsername)) {
            return "এই ইউজারনেমটি (@$cleanUsername) ইতিমধ্যে ব্যবহৃত হয়েছে! অন্য একটি চেষ্টা করুন।"
        }
        if (cleanContact.length < 5) return "বৈধ ইমেইল অথবা ফোন নম্বর দিন।"
        if (_users.value.any { it.emailOrPhone.equals(cleanContact, ignoreCase = true) }) {
            return "এই ইমেইল বা ফোন নম্বর দিয়ে ইতিমধ্যে একাউন্ট খোলা হয়েছে।"
        }
        if (passwordPlain.length < 6) return "পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের হতে হবে।"
        if (passwordPlain != confirmPasswordPlain) return "পাসওয়ার্ড এবং কনফার্ম পাসওয়ার্ড মিলছে না!"
        if (!acceptedTerms) return "রেজিস্ট্রেশন করতে শর্তাবলী (Terms & Conditions) গ্রহণ করতে হবে।"

        val newUid = "user_${UUID.randomUUID().toString().take(8)}"
        val newUser = UserProfile(
            uid = newUid,
            fullName = cleanName,
            username = cleanUsername,
            emailOrPhone = cleanContact,
            passwordHash = hashPassword(passwordPlain),
            bio = "",
            country = country.ifBlank { "Bangladesh" },
            joinDate = currentMonthYear(),
            avatarColorHex = 0xFF2563EB,
            coverColorHex = 0xFF1D4ED8,
            currentBalance = 0,
            totalEarnedCoins = 0,
            totalSpentCoins = 0,
            pendingCoins = 0,
            friendUids = emptyList(),
            followerUids = emptyList(),
            followingUids = emptyList(),
            activeSessions = listOf(
                ActiveSessionItem("sess_${System.currentTimeMillis()}", "Android Mobile App", country, "Current Session", true)
            )
        )

        _users.update {
            val updated = listOf(newUser) + it
            saveUsersLocally(updated)
            updated
        }
        _currentUserUid.value = newUid
        prefs.edit().putString("logged_in_uid", newUid).apply()
        FirebaseAdminBridge.syncUserProfileToCloud(getApplication(), newUser)
        attachCloudRealtimeListeners()
        showFeedback("রেজিস্ট্রেশন সফলভাবে সম্পন্ন হয়েছে!", "Registration successful!")
        return null
    }

    fun resetPasswordByIdentifier(identifier: String, newPasswordPlain: String): Boolean {
        val clean = identifier.trim().removePrefix("@").lowercase()
        if (newPasswordPlain.length < 6) {
            showFeedback("নতুন পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের হতে হবে", "Password must be at least 6 characters")
            return false
        }
        val target = _users.value.find {
            it.username.lowercase() == clean || it.emailOrPhone.lowercase() == clean
        }
        return if (target != null) {
            val newHash = hashPassword(newPasswordPlain)
            _users.update { list ->
                val updated = list.map { if (it.uid == target.uid) it.copy(passwordHash = newHash) else it }
                saveUsersLocally(updated)
                updated
            }
            val updatedProfile = _users.value.find { it.uid == target.uid }
            if (updatedProfile != null) {
                FirebaseAdminBridge.syncUserProfileToCloud(getApplication(), updatedProfile)
            }
            showFeedback(
                "পাসওয়ার্ড সফলভাবে রিসেট হয়েছে! এখন নতুন পাসওয়ার্ড দিয়ে লগইন করুন।",
                "Password reset successful! You can now log in."
            )
            true
        } else {
            showFeedback("এই ইউজারনেম বা ইমেইল/ফোনে কোনো একাউন্ট পাওয়া যায়নি।", "Account not found.")
            false
        }
    }

    fun requestLogoutConfirmation() {
        _showLogoutDialog.value = true
    }

    fun dismissLogoutConfirmation() {
        _showLogoutDialog.value = false
    }

    fun confirmLogout() {
        _showLogoutDialog.value = false
        timerJob?.cancel()
        _activeTask.value = null
        clearCloudListeners()
        _currentUserUid.value = null
        prefs.edit().remove("logged_in_uid").apply()
        screenBackStack.clear()
        _currentScreen.value = ScreenRoute.HOME
        FirebaseAdminBridge.signOutFirebase(getApplication(), viewModelScope)
        showFeedback("নিরাপদে লগআউট সম্পন্ন হয়েছে", "Logged out securely")
    }

    // =========================================================================
    // SECTION 4: Home Page — Create Post, News Feed, Filters & Three-dot Menu
    // =========================================================================

    fun setFeedFilter(filter: FeedFilter) {
        _feedFilter.value = filter
    }

    fun createPost(
        caption: String,
        mediaType: PostMediaType,
        mediaLabels: List<String>,
        audience: PostAudience,
        groupId: String? = null
    ): Boolean {
        val user = getCurrentUser() ?: return false
        if (caption.isBlank() && mediaLabels.isEmpty()) {
            showFeedback("অনুগ্রহ করে ক্যাপশন বা মিডিয়া যুক্ত করুন", "Please add caption or media")
            return false
        }
        val newPost = PostItem(
            postId = "post_${UUID.randomUUID().toString().take(8)}",
            authorUid = user.uid,
            authorName = user.fullName,
            authorUsername = user.username,
            authorAvatarColor = user.avatarColorHex,
            timeAgo = nowFormatted(),
            createdTimestampMs = System.currentTimeMillis(),
            caption = caption.trim(),
            mediaType = mediaType,
            mediaLabels = mediaLabels,
            audience = audience,
            likedByUids = emptyList(),
            comments = emptyList(),
            sharesCount = 0,
            viewedByUids = listOf(user.uid),
            isApproved = true,
            isSpam = false,
            groupId = groupId
        )
        _posts.update { listOf(newPost) + it }
        if (groupId != null) {
            _groups.update { groups ->
                groups.map {
                    if (it.groupId == groupId) it.copy(
                        postsCount = it.postsCount + 1,
                        lastActivity = nowFormatted()
                    ) else it
                }
            }
        }
        FirebaseAdminBridge.syncPostToCloud(getApplication(), newPost)
        showFeedback("পোস্ট সফলভাবে প্রকাশিত হয়েছে!", "Post published successfully!")
        if (groupId == null) {
            _currentScreen.value = ScreenRoute.HOME
        }
        return true
    }

    fun toggleLikePost(postId: String) {
        val myUid = _currentUserUid.value ?: return
        val myUser = getCurrentUser() ?: return
        _posts.update { list ->
            list.map { post ->
                if (post.postId == postId) {
                    val alreadyLiked = post.likedByUids.contains(myUid)
                    val updatedLikes = if (alreadyLiked) post.likedByUids - myUid else post.likedByUids + myUid
                    val updatedViewed = if (post.viewedByUids.contains(myUid)) post.viewedByUids else post.viewedByUids + myUid
                    val updated = post.copy(likedByUids = updatedLikes, viewedByUids = updatedViewed)
                    FirebaseAdminBridge.syncPostToCloud(getApplication(), updated)
                    if (!alreadyLiked && post.authorUid != myUid) {
                        addNotification(
                            targetUid = post.authorUid,
                            actorName = myUser.fullName,
                            category = NotificationCategory.LIKES,
                            messageBn = "${myUser.fullName} আপনার পোস্টে লাইক দিয়েছেন।",
                            messageEn = "${myUser.fullName} liked your post.",
                            route = ScreenRoute.HOME
                        )
                    }
                    updated
                } else post
            }
        }
    }

    fun registerUniquePostView(postId: String) {
        val myUid = _currentUserUid.value ?: return
        _posts.update { list ->
            list.map { post ->
                if (post.postId == postId && !post.viewedByUids.contains(myUid)) {
                    val updated = post.copy(viewedByUids = post.viewedByUids + myUid)
                    FirebaseAdminBridge.syncPostToCloud(getApplication(), updated)
                    updated
                } else post
            }
        }
    }

    fun addCommentToPost(postId: String, commentText: String) {
        val user = getCurrentUser() ?: return
        val clean = commentText.trim()
        if (clean.isEmpty()) return
        val newComment = CommentItem(
            commentId = "cmt_${UUID.randomUUID().toString().take(8)}",
            postId = postId,
            authorUid = user.uid,
            authorName = user.fullName,
            authorUsername = user.username,
            text = clean,
            timeAgo = nowFormatted()
        )
        _posts.update { list ->
            list.map { post ->
                if (post.postId == postId) {
                    val updatedViewed = if (post.viewedByUids.contains(user.uid)) post.viewedByUids else post.viewedByUids + user.uid
                    val updated = post.copy(
                        comments = post.comments + newComment,
                        viewedByUids = updatedViewed
                    )
                    FirebaseAdminBridge.syncPostToCloud(getApplication(), updated)
                    if (post.authorUid != user.uid) {
                        addNotification(
                            targetUid = post.authorUid,
                            actorName = user.fullName,
                            category = NotificationCategory.COMMENTS,
                            messageBn = "${user.fullName} আপনার পোস্টে কমেন্ট করেছেন: \"$clean\"",
                            messageEn = "${user.fullName} commented on your post.",
                            route = ScreenRoute.HOME
                        )
                    }
                    updated
                } else post
            }
        }
    }

    fun sharePost(postId: String) {
        _posts.update { list ->
            list.map {
                if (it.postId == postId) {
                    val updated = it.copy(sharesCount = it.sharesCount + 1)
                    FirebaseAdminBridge.syncPostToCloud(getApplication(), updated)
                    updated
                } else it
            }
        }
        showFeedback("পোস্টটি শেয়ার করা হয়েছে এবং লিংক কপি হয়েছে!", "Post shared and link copied!")
    }

    fun editPost(postId: String, newCaption: String, newAudience: PostAudience) {
        val myUid = _currentUserUid.value ?: return
        _posts.update { list ->
            list.map {
                if (it.postId == postId && it.authorUid == myUid) {
                    val updated = it.copy(caption = newCaption.trim(), audience = newAudience)
                    FirebaseAdminBridge.syncPostToCloud(getApplication(), updated)
                    updated
                } else it
            }
        }
        showFeedback("পোস্ট আপডেট করা হয়েছে!", "Post updated!")
    }

    fun deletePost(postId: String) {
        val myUid = _currentUserUid.value ?: return
        _posts.update { list -> list.filterNot { it.postId == postId && it.authorUid == myUid } }
        FirebaseAdminBridge.deletePostFromCloud(getApplication(), postId)
        showFeedback("পোস্ট মুছে ফেলা হয়েছে", "Post deleted")
    }

    fun toggleSavePost(postId: String) {
        val myUid = _currentUserUid.value ?: return
        _users.update { list ->
            list.map { user ->
                if (user.uid == myUid) {
                    val saved = if (user.savedPostIds.contains(postId)) {
                        showFeedback("সংরক্ষিত তালিকা থেকে সরানো হয়েছে", "Removed from Saved Posts")
                        user.savedPostIds - postId
                    } else {
                        showFeedback("পোস্টটি সংরক্ষণ (Save) করা হয়েছে", "Post saved")
                        user.savedPostIds + postId
                    }
                    user.copy(savedPostIds = saved)
                } else user
            }
        }
    }

    fun hidePost(postId: String) {
        val myUid = _currentUserUid.value ?: return
        _users.update { list ->
            list.map { user ->
                if (user.uid == myUid) user.copy(hiddenPostIds = user.hiddenPostIds + postId) else user
            }
        }
        showFeedback("পোস্টটি আপনার ফিড থেকে লুকানো হয়েছে", "Post hidden from feed")
    }

    fun reportContent(targetType: String, targetId: String, reason: String) {
        showFeedback(
            "$targetType রিপোর্ট সফলভাবে এডমিন মডারেশন কিউতে পাঠানো হয়েছে ($reason)",
            "$targetType reported to Admin Moderation Queue ($reason)"
        )
    }

    // =========================================================================
    // SECTION 5 & 21: Friend Requests, Follow System, Block & Report
    // =========================================================================

    fun sendFriendRequest(targetUid: String) {
        val me = getCurrentUser() ?: return
        val target = _users.value.find { it.uid == targetUid } ?: return
        if (target.privacySettings.whoCanSendFriendRequests == PrivacyOption.ONLY_ME) {
            showFeedback(
                "এই ব্যবহারকারী প্রাইভেসি সেটিংসে ফ্রেন্ড রিকোয়েস্ট বন্ধ রেখেছেন।",
                "This user has disabled incoming friend requests."
            )
            return
        }
        val alreadyExists = _friendRequests.value.any {
            (it.fromUid == me.uid && it.toUid == targetUid) || (it.fromUid == targetUid && it.toUid == me.uid)
        }
        if (alreadyExists || me.friendUids.contains(targetUid)) {
            showFeedback("রিকোয়েস্ট ইতিমধ্যে পাঠানো আছে বা আপনারা বন্ধু!", "Request already exists or already friends!")
            return
        }
        val req = FriendRequestItem(
            requestId = "freq_${UUID.randomUUID().toString().take(8)}",
            fromUid = me.uid,
            fromName = me.fullName,
            fromUsername = me.username,
            fromAvatarColor = me.avatarColorHex,
            toUid = target.uid,
            toName = target.fullName,
            toUsername = target.username,
            mutualFriendsCount = me.friendUids.intersect(target.friendUids.toSet()).size,
            sentTimeAgo = nowFormatted(),
            status = "PENDING"
        )
        _friendRequests.update { listOf(req) + it }
        showFeedback("${target.fullName}-কে ফ্রেন্ড রিকোয়েস্ট পাঠানো হয়েছে", "Friend request sent to ${target.fullName}")
    }

    fun acceptFriendRequest(requestId: String) {
        val req = _friendRequests.value.find { it.requestId == requestId } ?: return
        _friendRequests.update { list -> list.filterNot { it.requestId == requestId } }
        _users.update { list ->
            val updated = list.map { user ->
                when (user.uid) {
                    req.toUid -> user.copy(friendUids = (user.friendUids + req.fromUid).distinct())
                    req.fromUid -> user.copy(friendUids = (user.friendUids + req.toUid).distinct())
                    else -> user
                }
            }
            saveUsersLocally(updated)
            updated
        }
        addNotification(
            targetUid = req.toUid,
            actorName = req.fromName,
            category = NotificationCategory.ACCEPTED_REQUEST,
            messageBn = "আপনি এবং ${req.fromName} এখন বন্ধু!",
            messageEn = "You and ${req.fromName} are now friends!",
            route = ScreenRoute.FRIENDS
        )
        showFeedback("${req.fromName}-এর ফ্রেন্ড রিকোয়েস্ট গ্রহণ করা হয়েছে!", "Friend request accepted!")
    }

    fun rejectOrCancelFriendRequest(requestId: String) {
        _friendRequests.update { list -> list.filterNot { it.requestId == requestId } }
        showFeedback("ফ্রেন্ড রিকোয়েস্ট বাতিল করা হয়েছে", "Friend request removed")
    }

    fun unfriendUser(friendUid: String) {
        val myUid = _currentUserUid.value ?: return
        _users.update { list ->
            val updated = list.map { user ->
                when (user.uid) {
                    myUid -> user.copy(friendUids = user.friendUids - friendUid)
                    friendUid -> user.copy(friendUids = user.friendUids - myUid)
                    else -> user
                }
            }
            saveUsersLocally(updated)
            updated
        }
        showFeedback("আনফ্রেন্ড করা হয়েছে", "Unfriended user")
    }

    fun toggleFollowUser(targetUid: String) {
        val myUid = _currentUserUid.value ?: return
        if (myUid == targetUid) return
        val me = getCurrentUser() ?: return
        val isFollowing = me.followingUids.contains(targetUid)
        _users.update { list ->
            val updated = list.map { user ->
                when (user.uid) {
                    myUid -> user.copy(
                        followingUids = if (isFollowing) user.followingUids - targetUid else user.followingUids + targetUid
                    )
                    targetUid -> user.copy(
                        followerUids = if (isFollowing) user.followerUids - myUid else user.followerUids + myUid
                    )
                    else -> user
                }
            }
            saveUsersLocally(updated)
            updated
        }
        showFeedback(
            if (isFollowing) "আনফলো করা হয়েছে" else "ফলো করা হয়েছে!",
            if (isFollowing) "Unfollowed" else "Following!"
        )
    }

    fun toggleBlockUser(targetUid: String) {
        val myUid = _currentUserUid.value ?: return
        if (myUid == targetUid) return
        val me = getCurrentUser() ?: return
        val isBlocked = me.blockedUids.contains(targetUid)
        _users.update { list ->
            val updated = list.map { user ->
                if (user.uid == myUid) {
                    val newBlocked = if (isBlocked) user.blockedUids - targetUid else user.blockedUids + targetUid
                    val newFriends = if (!isBlocked) user.friendUids - targetUid else user.friendUids
                    user.copy(blockedUids = newBlocked, friendUids = newFriends)
                } else user
            }
            saveUsersLocally(updated)
            updated
        }
        showFeedback(
            if (isBlocked) "ব্লক তালিকা থেকে সরানো হয়েছে" else "ব্যবহারকারীকে ব্লক করা হয়েছে",
            if (isBlocked) "User unblocked" else "User blocked"
        )
    }

    // =========================================================================
    // SECTION 6: Profile Edit (with Unique Username Server Verification)
    // =========================================================================

    fun updateMyProfile(
        fullName: String,
        username: String,
        bio: String,
        country: String,
        website: String,
        gender: String,
        dob: String,
        avatarColorHex: Long,
        coverColorHex: Long
    ): String? {
        val me = getCurrentUser() ?: return "লগইন প্রয়োজন"
        val cleanUsername = username.trim().removePrefix("@").lowercase()
        if (fullName.trim().length < 2) return "পূর্ণ নাম অন্তত ২ অক্ষরের হতে হবে।"
        if (cleanUsername.length < 3 || !cleanUsername.matches("^[a-z0-9_]+$".toRegex())) {
            return "ইউজারনেম কমপক্ষে ৩ অক্ষরের হতে হবে।"
        }
        if (!isUsernameAvailable(cleanUsername, ignoreUid = me.uid)) {
            return "এই ইউজারনেমটি (@$cleanUsername) অন্য একজন ব্যবহার করছেন!"
        }
        val updated = me.copy(
            fullName = fullName.trim(),
            username = cleanUsername,
            bio = bio.trim(),
            country = country.trim(),
            website = website.trim(),
            gender = gender.trim(),
            dob = dob.trim(),
            avatarColorHex = avatarColorHex,
            coverColorHex = coverColorHex
        )
        _users.update { list ->
            val newList = list.map { if (it.uid == me.uid) updated else it }
            saveUsersLocally(newList)
            newList
        }
        _posts.update { list ->
            list.map {
                if (it.authorUid == me.uid) it.copy(
                    authorName = updated.fullName,
                    authorUsername = updated.username,
                    authorAvatarColor = updated.avatarColorHex
                ) else it
            }
        }
        FirebaseAdminBridge.syncUserProfileToCloud(getApplication(), updated)
        showFeedback("প্রোফাইল সফলভাবে সংরক্ষিত হয়েছে!", "Profile updated successfully!")
        return null
    }

    // =========================================================================
    // SECTION 7: Notifications
    // =========================================================================

    private fun addNotification(
        targetUid: String,
        actorName: String,
        category: NotificationCategory,
        messageBn: String,
        messageEn: String,
        route: ScreenRoute
    ) {
        val notif = AppNotificationItem(
            notificationId = "notif_${UUID.randomUUID().toString().take(8)}",
            targetUid = targetUid,
            actorName = actorName,
            category = category,
            messageBn = messageBn,
            messageEn = messageEn,
            timeAgo = nowFormatted(),
            targetRoute = route,
            isRead = false
        )
        _notifications.update { listOf(notif) + it }
    }

    fun markNotificationRead(notificationId: String) {
        _notifications.update { list ->
            list.map { if (it.notificationId == notificationId) it.copy(isRead = true) else it }
        }
    }

    fun markAllNotificationsRead() {
        val myUid = _currentUserUid.value ?: return
        _notifications.update { list ->
            list.map { if (it.targetUid == myUid) it.copy(isRead = true) else it }
        }
        showFeedback("সব নোটিফিকেশন পঠিত হিসেবে চিহ্নিত হয়েছে", "All notifications marked as read")
    }

    fun deleteNotification(notificationId: String) {
        _notifications.update { list -> list.filterNot { it.notificationId == notificationId } }
    }

    // =========================================================================
    // SECTION 11: Real-Time Messages
    // =========================================================================

    fun sendChatMessage(
        conversationId: String,
        text: String,
        emoji: String = "",
        imageAttachmentLabel: String = ""
    ) {
        val me = getCurrentUser() ?: return
        val conv = _conversations.value.find { it.conversationId == conversationId } ?: return
        if (me.blockedUids.contains(conv.partnerUid)) {
            showFeedback("ব্লক করা ইউজারকে মেসেজ পাঠানো যাবে না!", "Cannot message blocked user!")
            return
        }
        if (text.isBlank() && emoji.isBlank() && imageAttachmentLabel.isBlank()) return
        val newMsg = ChatMessageItem(
            messageId = "msg_${UUID.randomUUID().toString().take(8)}",
            senderUid = me.uid,
            receiverUid = conv.partnerUid,
            text = text.trim(),
            emoji = emoji,
            imageAttachmentLabel = imageAttachmentLabel,
            timestamp = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date()),
            isSeen = true
        )
        _conversations.update { list ->
            list.map {
                if (it.conversationId == conversationId) {
                    it.copy(messages = it.messages + newMsg, unreadCount = 0)
                } else it
            }
        }
    }

    fun deleteChatMessage(conversationId: String, messageId: String, deleteForEveryone: Boolean) {
        _conversations.update { list ->
            list.map { conv ->
                if (conv.conversationId == conversationId) {
                    val updatedMessages = if (deleteForEveryone) {
                        conv.messages.map {
                            if (it.messageId == messageId) it.copy(
                                text = "এই মেসেজটি মুছে ফেলা হয়েছে",
                                imageAttachmentLabel = "",
                                deletedForEveryone = true
                            ) else it
                        }
                    } else {
                        conv.messages.filterNot { it.messageId == messageId }
                    }
                    conv.copy(messages = updatedMessages)
                } else conv
            }
        }
        showFeedback(
            if (deleteForEveryone) "সবার ইনবক্স থেকে মেসেজটি মুছে ফেলা হয়েছে" else "আপনার ইনবক্স থেকে মেসেজটি মুছে ফেলা হয়েছে",
            if (deleteForEveryone) "Deleted for everyone" else "Deleted for you"
        )
    }

    // =========================================================================
    // SECTION 12 & 13: Groups & My Joined Groups
    // =========================================================================

    fun joinOrLeaveGroup(groupId: String) {
        val me = getCurrentUser() ?: return
        _groups.update { list ->
            list.map { group ->
                if (group.groupId != groupId) return@map group
                val isMember = group.memberUids.contains(me.uid)
                val isPending = group.pendingJoinUids.contains(me.uid)
                when {
                    isMember -> {
                        showFeedback("${group.name} গ্রুপ থেকে লিভ নিয়েছেন", "Left ${group.name}")
                        group.copy(memberUids = group.memberUids - me.uid)
                    }
                    isPending -> {
                        showFeedback("গ্রুপ জয়েন রিকোয়েস্ট বাতিল করা হয়েছে", "Join request cancelled")
                        group.copy(pendingJoinUids = group.pendingJoinUids - me.uid)
                    }
                    group.privacy == GroupPrivacy.PUBLIC -> {
                        showFeedback("${group.name} গ্রুপে সফলভাবে যুক্ত হয়েছেন!", "Joined ${group.name}!")
                        group.copy(memberUids = group.memberUids + me.uid)
                    }
                    else -> {
                        showFeedback(
                            "প্রাইভেট গ্রুপে জয়েন রিকোয়েস্ট পাঠানো হয়েছে (এডমিন অনুমোদনের অপেক্ষায়)",
                            "Join request sent to Private Group"
                        )
                        group.copy(pendingJoinUids = group.pendingJoinUids + me.uid)
                    }
                }
            }
        }
    }

    fun approveGroupJoinRequest(groupId: String, targetUid: String) {
        _groups.update { list ->
            list.map { group ->
                if (group.groupId == groupId) {
                    group.copy(
                        pendingJoinUids = group.pendingJoinUids - targetUid,
                        memberUids = (group.memberUids + targetUid).distinct()
                    )
                } else group
            }
        }
        showFeedback("সদস্যকে গ্রুপে অনুমোদন দেওয়া হয়েছে", "Member approved")
    }

    fun updateGroupDetailsByAdmin(groupId: String, name: String, description: String, rules: List<String>) {
        _groups.update { list ->
            list.map { group ->
                if (group.groupId == groupId) {
                    group.copy(
                        name = name.trim().ifBlank { group.name },
                        description = description.trim(),
                        rules = rules
                    )
                } else group
            }
        }
        showFeedback("গ্রুপের তথ্য ও নিয়মাবলী আপডেট হয়েছে!", "Group details updated!")
    }

    fun createNewGroup(name: String, description: String, privacy: GroupPrivacy, rulesText: String) {
        val me = getCurrentUser() ?: return
        if (name.isBlank()) {
            showFeedback("গ্রুপের নাম দিন", "Enter group name")
            return
        }
        val newGroup = SocialGroupItem(
            groupId = "grp_${UUID.randomUUID().toString().take(8)}",
            name = name.trim(),
            description = description.trim(),
            coverColorHex = listOf(0xFF2563EB, 0xFF7C3AED, 0xFF059669, 0xFFD97706).random(),
            privacy = privacy,
            createdDate = currentMonthYear(),
            adminUid = me.uid,
            adminName = me.fullName,
            moderatorUids = listOf(me.uid),
            memberUids = listOf(me.uid),
            rules = rulesText.split("\n").map { it.trim() }.filter { it.isNotEmpty() },
            postsCount = 0,
            newPostsCount = 0,
            lastActivity = nowFormatted()
        )
        _groups.update { listOf(newGroup) + it }
        showFeedback("নতুন গ্রুপ '${newGroup.name}' তৈরি হয়েছে!", "Group created!")
    }

    // =========================================================================
    // SECTION 14 & 24: Work — Job Post, Escrow Budget, Task Session & Coin Security
    // =========================================================================

    private fun recordCoinTransaction(
        uid: String,
        type: CoinTxType,
        coins: Int,
        isCredit: Boolean,
        description: String,
        status: String = "Completed"
    ) {
        val tx = CoinTransactionItem(
            txId = "TX-${System.currentTimeMillis().toString().takeLast(6)}-${(10..99).random()}",
            uid = uid,
            type = type,
            coins = coins,
            isCredit = isCredit,
            status = status,
            description = description,
            dateTime = nowFormatted()
        )
        _coinTransactions.update { listOf(tx) + it }
        FirebaseAdminBridge.syncCoinTxToCloud(getApplication(), tx)
    }

    fun createMicroJob(
        jobType: JobType,
        title: String,
        description: String,
        targetUrl: String,
        numberOfWorkers: Int,
        rewardPerWorker: Int,
        requiredDurationSec: Int,
        proofRequired: Boolean
    ): String? {
        val me = getCurrentUser() ?: return "লগইন প্রয়োজন"
        val cleanTitle = title.trim()
        val cleanUrl = targetUrl.trim()

        if (cleanTitle.length < 4) return "কাজের নাম (Job Title) কমপক্ষে ৪ অক্ষরের হতে হবে।"
        if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
            return "বৈধ Target URL দিন (http:// বা https:// দিয়ে শুরু হতে হবে)।"
        }
        if (numberOfWorkers < 1 || numberOfWorkers > 10000) {
            return "কর্মীর সংখ্যা (Number of Workers) ১ থেকে ১০,০০০ এর মধ্যে হতে হবে।"
        }
        if (rewardPerWorker < 1 || rewardPerWorker > 5000) {
            return "প্রতি কর্মীর পুরস্কার (Reward) কমপক্ষে ১ কয়েন হতে হবে।"
        }
        if (requiredDurationSec < 5 || requiredDurationSec > 60) {
            return "কাজের সময়সীমা (Required Duration) ৫ থেকে সর্বোচ্চ ৬০ সেকেন্ড হতে পারে।"
        }
        val isDuplicate = _jobs.value.any {
            it.creatorUid == me.uid &&
                it.title.equals(cleanTitle, ignoreCase = true) &&
                it.targetUrl.equals(cleanUrl, ignoreCase = true) &&
                it.status == "ACTIVE"
        }
        if (isDuplicate) {
            return "একই শিরোনাম ও লিংকে আপনার একটি সক্রিয় কাজ ইতিমধ্যে পোস্ট করা আছে!"
        }

        val totalCost = numberOfWorkers * rewardPerWorker
        if (me.currentBalance < totalCost) {
            return "পর্যাপ্ত ব্যালেন্স নেই! মোট বাজেট প্রয়োজন $totalCost Coins, কিন্তু আপনার বর্তমান ব্যালেন্স ${me.currentBalance} Coins।"
        }

        val updatedUser = me.copy(
            currentBalance = me.currentBalance - totalCost,
            totalSpentCoins = me.totalSpentCoins + totalCost
        )
        _users.update { list ->
            val newList = list.map { if (it.uid == me.uid) updatedUser else it }
            saveUsersLocally(newList)
            newList
        }
        FirebaseAdminBridge.syncUserProfileToCloud(getApplication(), updatedUser)

        val newJob = MicroJobItem(
            jobId = "job_${UUID.randomUUID().toString().take(8)}",
            creatorUid = me.uid,
            creatorName = me.fullName,
            jobType = jobType,
            title = cleanTitle,
            description = description.trim(),
            targetUrl = cleanUrl,
            totalWorkers = numberOfWorkers,
            completedWorkers = 0,
            rewardPerWorker = rewardPerWorker,
            requiredDurationSec = requiredDurationSec,
            proofRequired = proofRequired,
            totalBudget = totalCost,
            remainingBudget = totalCost,
            status = "ACTIVE"
        )
        _jobs.update { listOf(newJob) + it }
        FirebaseAdminBridge.syncJobToCloud(getApplication(), newJob)

        recordCoinTransaction(
            uid = me.uid,
            type = CoinTxType.JOB_POSTING,
            coins = totalCost,
            isCredit = false,
            description = "জব পোস্ট বাজেট সংরক্ষিত: $cleanTitle ($numberOfWorkers × $rewardPerWorker Coins)"
        )
        addNotification(
            targetUid = me.uid,
            actorName = "Work Marketplace",
            category = NotificationCategory.WORK,
            messageBn = "আপনার কাজ '$cleanTitle' সফলভাবে পোস্ট হয়েছে এবং $totalCost Coins বাজেট সংরক্ষিত হয়েছে।",
            messageEn = "Your job '$cleanTitle' is live with $totalCost Coins escrow budget.",
            route = ScreenRoute.WORK
        )
        showFeedback("কাজ সফলভাবে পোস্ট হয়েছে! বাজেট: $totalCost Coins", "Job posted! Budget: $totalCost Coins")
        return null
    }

    fun hasUserCompletedJob(jobId: String): Boolean {
        val myUid = _currentUserUid.value ?: return false
        return _taskSessions.value.any {
            it.jobId == jobId && it.workerUid == myUid &&
                (it.verificationStatus == TaskVerificationStatus.REWARDED ||
                    it.verificationStatus == TaskVerificationStatus.APPROVED ||
                    it.verificationStatus == TaskVerificationStatus.PENDING)
        }
    }

    fun startTaskSession(job: MicroJobItem) {
        if (hasUserCompletedJob(job.jobId)) {
            showFeedback("আপনি ইতিমধ্যে এই কাজটি সম্পন্ন করেছেন! একই কাজ একাধিকবার করা যাবে না।", "You have already completed this job!")
            return
        }
        if (job.remainingWorkers <= 0 || job.status != "ACTIVE") {
            showFeedback("এই কাজের কোটা পূর্ণ হয়ে গেছে বা কাজটি বন্ধ আছে।", "No remaining slots for this job.")
            return
        }
        timerJob?.cancel()
        _activeTask.value = ActiveTaskState(
            job = job,
            remainingSeconds = job.requiredDurationSec,
            isTimerCompleted = false,
            urlOpened = true
        )
        timerJob = viewModelScope.launch {
            while ((_activeTask.value?.remainingSeconds ?: 0) > 0) {
                delay(1000)
                _activeTask.update { state ->
                    if (state == null) null else {
                        val next = (state.remainingSeconds - 1).coerceAtLeast(0)
                        state.copy(remainingSeconds = next, isTimerCompleted = next == 0)
                    }
                }
            }
        }
    }

    fun cancelActiveTaskSession() {
        timerJob?.cancel()
        _activeTask.value = null
    }

    fun submitAndClaimTaskReward(proofText: String): Boolean {
        val me = getCurrentUser() ?: return false
        val currentTask = _activeTask.value ?: return false
        val job = currentTask.job

        if (!currentTask.isTimerCompleted) {
            showFeedback("নির্ধারিত সময় (${currentTask.remainingSeconds}s) শেষ হওয়া পর্যন্ত অপেক্ষা করুন!", "Please wait for the timer to complete!")
            return false
        }
        if (job.proofRequired && proofText.trim().length < 4) {
            showFeedback("কাজের প্রমাণ (Proof / স্ক্রিনশট নোট বা ইউজারনেম) কমপক্ষে ৪ অক্ষরে লিখুন!", "Please provide valid task proof!")
            return false
        }
        if (hasUserCompletedJob(job.jobId)) {
            showFeedback("এই কাজের পুরস্কার ইতিমধ্যে দাবি করা হয়েছে!", "Reward already claimed for this job!")
            return false
        }

        val session = TaskSessionItem(
            sessionId = "task_${UUID.randomUUID().toString().take(8)}",
            jobId = job.jobId,
            jobTitle = job.title,
            workerUid = me.uid,
            rewardCoins = job.rewardPerWorker,
            startedAt = nowFormatted(),
            completedAt = nowFormatted(),
            proofSubmitted = if (job.proofRequired) proofText.trim() else "Verified Visit",
            verificationStatus = TaskVerificationStatus.REWARDED,
            rewardClaimed = true
        )
        _taskSessions.update { listOf(session) + it }

        _jobs.update { list ->
            list.map { item ->
                if (item.jobId == job.jobId) {
                    val newCompleted = item.completedWorkers + 1
                    val newRemBudget = (item.remainingBudget - item.rewardPerWorker).coerceAtLeast(0)
                    val newStatus = if (newCompleted >= item.totalWorkers) "COMPLETED" else item.status
                    val updatedJob = item.copy(
                        completedWorkers = newCompleted,
                        remainingBudget = newRemBudget,
                        status = newStatus
                    )
                    FirebaseAdminBridge.syncJobToCloud(getApplication(), updatedJob)
                    updatedJob
                } else item
            }
        }

        val updatedUser = me.copy(
            currentBalance = me.currentBalance + job.rewardPerWorker,
            totalEarnedCoins = me.totalEarnedCoins + job.rewardPerWorker
        )
        _users.update { list ->
            val newList = list.map { if (it.uid == me.uid) updatedUser else it }
            saveUsersLocally(newList)
            newList
        }
        FirebaseAdminBridge.syncUserProfileToCloud(getApplication(), updatedUser)

        recordCoinTransaction(
            uid = me.uid,
            type = CoinTxType.TASK_REWARD,
            coins = job.rewardPerWorker,
            isCredit = true,
            description = "কাজ সম্পন্ন পুরস্কার: ${job.title}"
        )
        addNotification(
            targetUid = me.uid,
            actorName = "Task Verification",
            category = NotificationCategory.WORK,
            messageBn = "অভিনন্দন! '${job.title}' কাজটি সম্পন্ন করায় +${job.rewardPerWorker} Coins আপনার ব্যালেন্সে যোগ হয়েছে।",
            messageEn = "Congrats! +${job.rewardPerWorker} Coins credited for '${job.title}'.",
            route = ScreenRoute.WORK
        )

        timerJob?.cancel()
        _activeTask.value = null
        showFeedback("অভিনন্দন! +${job.rewardPerWorker} Coins আয় হয়েছে!", "Congrats! Earned +${job.rewardPerWorker} Coins!")
        return true
    }

    fun cancelMyPostedJobAndRefund(jobId: String) {
        val me = getCurrentUser() ?: return
        val job = _jobs.value.find { it.jobId == jobId && it.creatorUid == me.uid } ?: return
        if (job.status == "CANCELLED" || job.status == "COMPLETED") {
            showFeedback("এই কাজটি ইতিমধ্যে সম্পন্ন বা বাতিল করা হয়েছে", "Job already completed or cancelled")
            return
        }
        val refund = job.remainingBudget
        _jobs.update { list ->
            list.map {
                if (it.jobId == jobId) {
                    val cancelled = it.copy(status = "CANCELLED", remainingBudget = 0)
                    FirebaseAdminBridge.syncJobToCloud(getApplication(), cancelled)
                    cancelled
                } else it
            }
        }
        if (refund > 0) {
            val updatedUser = me.copy(
                currentBalance = me.currentBalance + refund,
                totalSpentCoins = (me.totalSpentCoins - refund).coerceAtLeast(0)
            )
            _users.update { list ->
                val newList = list.map { if (it.uid == me.uid) updatedUser else it }
                saveUsersLocally(newList)
                newList
            }
            FirebaseAdminBridge.syncUserProfileToCloud(getApplication(), updatedUser)
            recordCoinTransaction(
                uid = me.uid,
                type = CoinTxType.COIN_ADJUSTMENT,
                coins = refund,
                isCredit = true,
                description = "বাতিলকৃত কাজের অব্যবহৃত বাজেট ফেরত: ${job.title}"
            )
        }
        showFeedback(
            "কাজটি বাতিল করা হয়েছে এবং অব্যবহৃত $refund Coins আপনার ব্যালেন্সে ফেরত দেওয়া হয়েছে!",
            "Job cancelled and $refund unused Coins refunded!"
        )
    }

    fun editMyPostedJob(jobId: String, newTitle: String, newDescription: String) {
        val me = getCurrentUser() ?: return
        _jobs.update { list ->
            list.map {
                if (it.jobId == jobId && it.creatorUid == me.uid) {
                    val updated = it.copy(
                        title = newTitle.trim().ifBlank { it.title },
                        description = newDescription.trim().ifBlank { it.description }
                    )
                    FirebaseAdminBridge.syncJobToCloud(getApplication(), updated)
                    updated
                } else it
            }
        }
        showFeedback("কাজের বিবরণ আপডেট করা হয়েছে!", "Job updated!")
    }

    // =========================================================================
    // SECTION 15: Gifts & Missions (with Server Verification & Single-Claim Guard)
    // =========================================================================

    fun getMissionProgress(mission: GiftMissionItem): Int {
        val me = getCurrentUser() ?: return 0
        return when (mission.requiredActivityType) {
            "DAILY_LOGIN" -> 1
            "POST" -> _posts.value.count { it.authorUid == me.uid && it.isApproved && !it.isSpam }
            "JOB" -> _taskSessions.value.count { it.workerUid == me.uid && it.verificationStatus == TaskVerificationStatus.REWARDED }
            "FRIEND" -> me.friendUids.size
            "LIKE" -> _posts.value.count { it.likedByUids.contains(me.uid) }
            else -> 1
        }
    }

    fun claimMissionReward(missionId: String) {
        val me = getCurrentUser() ?: return
        val mission = _missions.value.find { it.missionId == missionId } ?: return
        if (mission.claimedByUids.contains(me.uid)) {
            showFeedback("আপনি এই মিশনের পুরস্কার ইতিমধ্যে গ্রহণ করেছেন!", "Reward already claimed!")
            return
        }
        val progress = getMissionProgress(mission)
        if (progress < mission.targetCount) {
            showFeedback(
                "মিশনের শর্ত এখনো পূরণ হয়নি ($progress/${mission.targetCount})! কাজটি সম্পন্ন করে আবার চেষ্টা করুন।",
                "Mission criteria not yet met ($progress/${mission.targetCount})!"
            )
            return
        }

        _missions.update { list ->
            list.map {
                if (it.missionId == missionId) it.copy(claimedByUids = it.claimedByUids + me.uid) else it
            }
        }

        val updatedUser = me.copy(
            currentBalance = me.currentBalance + mission.rewardCoins,
            totalEarnedCoins = me.totalEarnedCoins + mission.rewardCoins
        )
        _users.update { list ->
            val newList = list.map { if (it.uid == me.uid) updatedUser else it }
            saveUsersLocally(newList)
            newList
        }
        FirebaseAdminBridge.syncUserProfileToCloud(getApplication(), updatedUser)

        recordCoinTransaction(
            uid = me.uid,
            type = CoinTxType.GIFT_REWARD,
            coins = mission.rewardCoins,
            isCredit = true,
            description = "মিশন ও গিফট পুরস্কার: ${mission.titleBn}"
        )
        addNotification(
            targetUid = me.uid,
            actorName = "Gifts & Missions",
            category = NotificationCategory.GIFT_REWARDS,
            messageBn = "মিশন '${mission.titleBn}' সম্পন্ন করায় +${mission.rewardCoins} Coins পুরস্কার পেয়েছেন!",
            messageEn = "Mission '${mission.titleEn}' completed! +${mission.rewardCoins} Coins added.",
            route = ScreenRoute.GIFTS
        )
        showFeedback("অভিনন্দন! +${mission.rewardCoins} গিফট কয়েন যুক্ত হয়েছে!", "Claimed +${mission.rewardCoins} Gift Coins!")
    }

    // =========================================================================
    // SECTION 9 & 10: Post Rank & Income Rank Calculation (True Database Stats)
    // =========================================================================

    data class PostRankEntry(
        val rank: Int,
        val user: UserProfile,
        val approvedPostsCount: Int
    )

    data class IncomeRankEntry(
        val rank: Int,
        val user: UserProfile,
        val totalEarnedCoins: Int
    )

    fun getTopPostersRanking(): List<PostRankEntry> {
        val approvedPostsByAuthor = _posts.value
            .filter { it.isApproved && !it.isSpam }
            .groupBy { it.authorUid }
            .mapValues { it.value.size }

        return _users.value
            .map { user ->
                val count = approvedPostsByAuthor[user.uid] ?: 0
                user to count
            }
            .filter { it.second > 0 }
            .sortedWith(
                compareByDescending<Pair<UserProfile, Int>> { it.second }
                    .thenByDescending { it.first.totalEarnedCoins }
                    .thenBy { it.first.username }
            )
            .take(50)
            .mapIndexed { index, pair ->
                PostRankEntry(rank = index + 1, user = pair.first, approvedPostsCount = pair.second)
            }
    }

    fun getTopEarnersRanking(): List<IncomeRankEntry> {
        return _users.value
            .sortedWith(
                compareByDescending<UserProfile> { it.totalEarnedCoins }
                    .thenByDescending { it.currentBalance }
                    .thenBy { it.username }
            )
            .take(50)
            .mapIndexed { index, user ->
                IncomeRankEntry(rank = index + 1, user = user, totalEarnedCoins = user.totalEarnedCoins)
            }
    }

    // =========================================================================
    // SECTION 16: Settings (Password, Privacy, Notifications, Sessions)
    // =========================================================================

    fun changePassword(currentPass: String, newPass: String, confirmPass: String): String? {
        val me = getCurrentUser() ?: return "লগইন প্রয়োজন"
        if (hashPassword(currentPass) != me.passwordHash) {
            return "বর্তমান পাসওয়ার্ড (Current Password) সঠিক নয়!"
        }
        if (newPass.length < 6) {
            return "নতুন পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের হতে হবে।"
        }
        if (newPass != confirmPass) {
            return "নতুন পাসওয়ার্ড এবং কনফার্ম পাসওয়ার্ড মিলছে না!"
        }
        val newHash = hashPassword(newPass)
        _users.update { list ->
            val newList = list.map {
                if (it.uid == me.uid) {
                    it.copy(
                        passwordHash = newHash,
                        activeSessions = it.activeSessions.filter { s -> s.isCurrent }
                    )
                } else it
            }
            saveUsersLocally(newList)
            newList
        }
        val updated = getCurrentUser()
        if (updated != null) {
            FirebaseAdminBridge.syncUserProfileToCloud(getApplication(), updated)
        }
        addNotification(
            targetUid = me.uid,
            actorName = "Security Alert",
            category = NotificationCategory.SECURITY_ALERT,
            messageBn = "আপনার একাউন্টের পাসওয়ার্ড সফলভাবে পরিবর্তন করা হয়েছে এবং অন্যান্য ডিভাইস সেশন বাতিল করা হয়েছে।",
            messageEn = "Your password was changed and other sessions were revoked.",
            route = ScreenRoute.SETTINGS
        )
        showFeedback("পাসওয়ার্ড নিরাপদভাবে পরিবর্তন করা হয়েছে!", "Password changed securely!")
        return null
    }

    fun updatePrivacySettings(newPrivacy: UserPrivacySettings) {
        val me = getCurrentUser() ?: return
        _users.update { list ->
            list.map { if (it.uid == me.uid) it.copy(privacySettings = newPrivacy) else it }
        }
        showFeedback("প্রাইভেসি সেটিংস সংরক্ষিত হয়েছে", "Privacy settings saved")
    }

    fun updateNotificationSettings(newSettings: UserNotificationSettings) {
        val me = getCurrentUser() ?: return
        _users.update { list ->
            list.map { if (it.uid == me.uid) it.copy(notificationSettings = newSettings) else it }
        }
        showFeedback("নোটিফিকেশন সেটিংস আপডেট হয়েছে", "Notification settings updated")
    }

    fun updateCountry(newCountry: String) {
        val me = getCurrentUser() ?: return
        if (newCountry.isBlank()) return
        _users.update { list ->
            val newList = list.map { if (it.uid == me.uid) it.copy(country = newCountry.trim()) else it }
            saveUsersLocally(newList)
            newList
        }
        val updated = getCurrentUser()
        if (updated != null) {
            FirebaseAdminBridge.syncUserProfileToCloud(getApplication(), updated)
        }
        showFeedback("দেশ পরিবর্তন সংরক্ষিত হয়েছে: $newCountry", "Country saved: $newCountry")
    }

    fun revokeOtherSessions() {
        val me = getCurrentUser() ?: return
        _users.update { list ->
            list.map {
                if (it.uid == me.uid) it.copy(activeSessions = it.activeSessions.filter { s -> s.isCurrent }) else it
            }
        }
        showFeedback("অন্যান্য সকল সক্রিয় সেশন বাতিল করা হয়েছে", "All other active sessions revoked")
    }

    fun requestAccountDeletion(reason: String) {
        val me = getCurrentUser() ?: return
        submitSupportTicket(
            subject = "Account Deletion Request (@${me.username})",
            category = TicketCategory.ACCOUNT,
            description = "Reason: $reason",
            attachmentName = ""
        )
        showFeedback(
            "একাউন্ট ডিলিট রিকোয়েস্ট এডমিন প্যানেলে পাঠানো হয়েছে।",
            "Account deletion request submitted to Admin Panel."
        )
    }

    // =========================================================================
    // SECTION 18: Support & Ticket Submission
    // =========================================================================

    fun submitSupportTicket(
        subject: String,
        category: TicketCategory,
        description: String,
        attachmentName: String
    ): Boolean {
        val me = getCurrentUser() ?: return false
        if (subject.trim().length < 3 || description.trim().length < 5) {
            showFeedback("অনুগ্রহ করে বিষয়ের নাম এবং সমস্যার বিস্তারিত বিবরণ লিখুন", "Please enter subject and description")
            return false
        }
        val ticket = SupportTicketItem(
            ticketId = "TKT-${(10000..99999).random()}",
            uid = me.uid,
            subject = subject.trim(),
            category = category,
            description = description.trim(),
            attachmentName = attachmentName.trim(),
            status = TicketStatus.OPEN,
            createdAt = nowFormatted(),
            adminReply = ""
        )
        _supportTickets.update { listOf(ticket) + it }
        FirebaseAdminBridge.syncTicketToCloud(getApplication(), ticket)
        showFeedback("সাপোর্ট টিকেট (${ticket.ticketId}) সফলভাবে জমা হয়েছে!", "Support ticket (${ticket.ticketId}) submitted!")
        return true
    }

    fun setGlobalSearchQuery(query: String) {
        _globalSearchQuery.value = query
    }
}
