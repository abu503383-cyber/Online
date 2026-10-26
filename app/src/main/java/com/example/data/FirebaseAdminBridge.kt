package com.example.data

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.BuildConfig
import com.example.model.*
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

enum class OperationType {
    CREATE, UPDATE, DELETE, LIST, GET, WRITE
}

/**
 * Structured Firestore error logger & Admin Panel Cloud Bridge.
 * Pre-configured with the user's `facebook-c5870` Firebase project configuration.
 */
object FirebaseAdminBridge {
    private const val TAG = "FirebaseAdminBridge"

    // Connected Firebase Project Constants (from user's firebaseConfig)
    const val PROJECT_ID = "facebook-c5870"
    const val AUTH_DOMAIN = "facebook-c5870.firebaseapp.com"
    const val DATABASE_URL = "https://facebook-c5870-default-rtdb.firebaseio.com"
    const val STORAGE_BUCKET = "facebook-c5870.firebasestorage.app"
    const val MESSAGING_SENDER_ID = "580606904829"
    const val APP_ID = "1:580606904829:web:816df6958f8f949ac53194"

    // Standard Collection Names shared between User Panel and Admin Panel
    const val COL_USERS = "users"
    const val COL_POSTS = "posts"
    const val COL_FRIEND_REQUESTS = "friend_requests"
    const val COL_CONVERSATIONS = "conversations"
    const val COL_GROUPS = "groups"
    const val COL_JOBS = "jobs"
    const val COL_TASK_SESSIONS = "task_sessions"
    const val COL_MISSIONS = "missions"
    const val COL_COIN_LEDGER = "coin_ledger"
    const val COL_SUPPORT_TICKETS = "support_tickets"
    const val COL_NOTIFICATIONS = "notifications"

    @Synchronized
    fun ensureFirebaseInitialized(context: Context): Boolean {
        return try {
            val existingApps = FirebaseApp.getApps(context)
            if (existingApps.isNotEmpty()) {
                true
            } else {
                val apiKey = BuildConfig.FIREBASE_API_KEY
                if (apiKey.isNotEmpty()) {
                    val options = FirebaseOptions.Builder()
                        .setApiKey(apiKey)
                        .setApplicationId(APP_ID)
                        .setProjectId(PROJECT_ID)
                        .setDatabaseUrl(DATABASE_URL)
                        .setStorageBucket(STORAGE_BUCKET)
                        .setGcmSenderId(MESSAGING_SENDER_ID)
                        .build()
                    FirebaseApp.initializeApp(context.applicationContext, options)
                    Log.i(TAG, "Firebase initialized programmatically for project: $PROJECT_ID")
                    true
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase initialization check: ${e.message}")
            FirebaseApp.getApps(context).isNotEmpty()
        }
    }

    fun isFirebaseConfigured(context: Context): Boolean {
        return ensureFirebaseInitialized(context)
    }

    fun getDatabaseId(context: Context): String {
        return try {
            val resId = context.resources.getIdentifier(
                "firestore_database_id",
                "string",
                context.packageName
            )
            if (resId != 0) context.getString(resId) else "(default)"
        } catch (e: Exception) {
            "(default)"
        }
    }

    fun getFirestoreInstanceOrNull(context: Context): FirebaseFirestore? {
        return try {
            if (!ensureFirebaseInitialized(context)) return null
            val dbId = getDatabaseId(context)
            FirebaseFirestore.getInstance(dbId)
        } catch (e: Exception) {
            Log.w(TAG, "Firestore instance error: ${e.message}")
            null
        }
    }

    fun handleFirestoreError(
        exception: Exception,
        operationType: OperationType,
        path: String?
    ) {
        val currentUser = try {
            Firebase.auth.currentUser
        } catch (e: Exception) {
            null
        }
        val providerArray = JSONArray().apply {
            currentUser?.providerData?.forEach { profile ->
                put(
                    JSONObject().apply {
                        put("providerId", profile.providerId)
                        put("displayName", profile.displayName)
                        put("email", profile.email)
                    }
                )
            }
        }
        val errInfo = JSONObject().apply {
            put("error", exception.message ?: "Unknown Firestore exception")
            put("operationType", operationType.name.lowercase())
            put("path", path)
            put(
                "authInfo",
                JSONObject().apply {
                    put("userId", currentUser?.uid)
                    put("email", currentUser?.email)
                    put("emailVerified", currentUser?.isEmailVerified)
                    put("providerInfo", providerArray)
                }
            )
        }
        Log.e("FirestoreError", errInfo.toString(), exception)
    }

    /**
     * Syncs User Profile to Firestore `/users/{uid}`.
     */
    fun syncUserProfileToCloud(context: Context, profile: UserProfile) {
        val db = getFirestoreInstanceOrNull(context) ?: return
        val docRef = db.collection(COL_USERS).document(profile.uid)
        val payload = mapOf(
            "uid" to profile.uid,
            "fullName" to profile.fullName,
            "username" to profile.username,
            "emailOrPhone" to profile.emailOrPhone,
            "passwordHash" to profile.passwordHash,
            "bio" to profile.bio,
            "country" to profile.country,
            "website" to profile.website,
            "gender" to profile.gender,
            "dob" to profile.dob,
            "joinDate" to profile.joinDate,
            "avatarColorHex" to profile.avatarColorHex,
            "coverColorHex" to profile.coverColorHex,
            "currentBalance" to profile.currentBalance,
            "totalEarnedCoins" to profile.totalEarnedCoins,
            "totalSpentCoins" to profile.totalSpentCoins,
            "pendingCoins" to profile.pendingCoins,
            "friendUids" to profile.friendUids,
            "followerUids" to profile.followerUids,
            "followingUids" to profile.followingUids,
            "blockedUids" to profile.blockedUids,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload, SetOptions.merge())
            .addOnFailureListener { e ->
                handleFirestoreError(e, OperationType.WRITE, docRef.path)
            }
    }

    /**
     * Syncs a created/updated Post to Firestore `/posts/{postId}`.
     */
    fun syncPostToCloud(context: Context, post: PostItem) {
        val db = getFirestoreInstanceOrNull(context) ?: return
        val docRef = db.collection(COL_POSTS).document(post.postId)
        val payload = mapOf(
            "postId" to post.postId,
            "authorUid" to post.authorUid,
            "authorName" to post.authorName,
            "authorUsername" to post.authorUsername,
            "authorAvatarColor" to post.authorAvatarColor,
            "timeAgo" to post.timeAgo,
            "createdTimestampMs" to post.createdTimestampMs,
            "caption" to post.caption,
            "mediaType" to post.mediaType.name,
            "mediaLabels" to post.mediaLabels,
            "audience" to post.audience.name,
            "likedByUids" to post.likedByUids,
            "viewedByUids" to post.viewedByUids,
            "approvalStatus" to if (post.isApproved) "APPROVED" else "PENDING",
            "isApproved" to post.isApproved,
            "isSpam" to post.isSpam,
            "likesCount" to post.likedByUids.size,
            "commentsCount" to post.comments.size,
            "sharesCount" to post.sharesCount,
            "viewsCount" to post.viewedByUids.size,
            "groupId" to post.groupId,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload, SetOptions.merge())
            .addOnFailureListener { e ->
                handleFirestoreError(e, OperationType.WRITE, docRef.path)
            }
    }

    fun deletePostFromCloud(context: Context, postId: String) {
        val db = getFirestoreInstanceOrNull(context) ?: return
        val docRef = db.collection(COL_POSTS).document(postId)
        docRef.delete().addOnFailureListener { e ->
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
        }
    }

    /**
     * Syncs a MicroJob to Firestore `/jobs/{jobId}`.
     */
    fun syncJobToCloud(context: Context, job: MicroJobItem) {
        val db = getFirestoreInstanceOrNull(context) ?: return
        val docRef = db.collection(COL_JOBS).document(job.jobId)
        val payload = mapOf(
            "jobId" to job.jobId,
            "creatorUid" to job.creatorUid,
            "creatorName" to job.creatorName,
            "jobType" to job.jobType.name,
            "title" to job.title,
            "description" to job.description,
            "targetUrl" to job.targetUrl,
            "totalWorkers" to job.totalWorkers,
            "completedWorkers" to job.completedWorkers,
            "rewardPerWorker" to job.rewardPerWorker,
            "requiredDurationSec" to job.requiredDurationSec,
            "proofRequired" to job.proofRequired,
            "totalBudget" to job.totalBudget,
            "remainingBudget" to job.remainingBudget,
            "status" to job.status,
            "createdTimestampMs" to job.createdTimestampMs,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload, SetOptions.merge())
            .addOnFailureListener { e ->
                handleFirestoreError(e, OperationType.WRITE, docRef.path)
            }
    }

    /**
     * Syncs a Coin Ledger Transaction to Firestore `/coin_ledger/{txId}`.
     */
    fun syncCoinTxToCloud(context: Context, tx: CoinTransactionItem) {
        val db = getFirestoreInstanceOrNull(context) ?: return
        val docRef = db.collection(COL_COIN_LEDGER).document(tx.txId)
        val payload = mapOf(
            "txId" to tx.txId,
            "uid" to tx.uid,
            "type" to tx.type.name,
            "coins" to tx.coins,
            "isCredit" to tx.isCredit,
            "status" to tx.status,
            "description" to tx.description,
            "dateTime" to tx.dateTime,
            "createdAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload, SetOptions.merge())
            .addOnFailureListener { e ->
                handleFirestoreError(e, OperationType.CREATE, docRef.path)
            }
    }

    /**
     * Syncs a Support Ticket to Firestore `/support_tickets/{ticketId}`.
     */
    fun syncTicketToCloud(context: Context, ticket: SupportTicketItem) {
        val db = getFirestoreInstanceOrNull(context) ?: return
        val docRef = db.collection(COL_SUPPORT_TICKETS).document(ticket.ticketId)
        val payload = mapOf(
            "ticketId" to ticket.ticketId,
            "uid" to ticket.uid,
            "subject" to ticket.subject,
            "category" to ticket.category.name,
            "description" to ticket.description,
            "attachmentName" to ticket.attachmentName,
            "status" to ticket.status.name,
            "createdAt" to ticket.createdAt,
            "adminReply" to ticket.adminReply,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload, SetOptions.merge())
            .addOnFailureListener { e ->
                handleFirestoreError(e, OperationType.CREATE, docRef.path)
            }
    }

    /**
     * Interactive Google Sign-In via CredentialManager (GetSignInWithGoogleOption).
     */
    fun signInWithGoogle(
        context: Context,
        scope: CoroutineScope,
        onSuccess: (uid: String, name: String, email: String) -> Unit,
        onError: (String) -> Unit
    ) {
        ensureFirebaseInitialized(context)
        val clientIdRes = context.resources.getIdentifier(
            "default_web_client_id",
            "string",
            context.packageName
        )
        if (clientIdRes == 0) {
            onError("Google Sign-In ব্যবহারের জন্য Firebase Console থেকে default_web_client_id সংযুক্ত করুন অথবা ইমেইল/ইউজারনেম ও পাসওয়ার্ড দিয়ে রেজিস্ট্রেশন ও লগইন করুন।")
            return
        }
        val clientId = context.getString(clientIdRes)
        val credentialManager = CredentialManager.create(context)
        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

        scope.launch {
            try {
                val activity = context as? Activity
                if (activity == null) {
                    onError("Activity context required for Google Sign-In")
                    return@launch
                }
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleToken = GoogleIdTokenCredential.createFrom(credential.data)
                    val authCredential = GoogleAuthProvider.getCredential(googleToken.idToken, null)
                    val authResult = Firebase.auth.signInWithCredential(authCredential).await()
                    val user = authResult.user
                    onSuccess(
                        user?.uid ?: "google_${System.currentTimeMillis()}",
                        user?.displayName ?: googleToken.displayName ?: "Google User",
                        user?.email ?: googleToken.id
                    )
                } else {
                    onError("Unexpected credential type")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w(TAG, "Google Sign-In cancelled or dismissed: ${e.message}", e)
                onError("Google Sign-In বাতিল করা হয়েছে")
            } catch (e: Exception) {
                Log.e(TAG, "Google Sign-In error", e)
                onError(e.localizedMessage ?: "Google Sign-In ব্যর্থ হয়েছে")
            }
        }
    }

    fun signOutFirebase(context: Context, scope: CoroutineScope) {
        try {
            if (isFirebaseConfigured(context)) {
                Firebase.auth.signOut()
            }
        } catch (_: Exception) {
        }
        scope.launch {
            try {
                CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
            } catch (_: Exception) {
            }
        }
    }
}
