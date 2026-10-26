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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.CoinBadge
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.tr
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.NexBluePrimary
import com.example.ui.theme.NexPurpleSecondary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningOrange
import com.example.viewmodel.NexEarnViewModel

/**
 * Section 14: Work — Job Marketplace, Job Posting, Filters, Task Session & My Posted Jobs
 */
@Composable
fun WorkScreen(
    viewModel: NexEarnViewModel,
    lang: AppLanguage
) {
    val currentUser = viewModel.getCurrentUser() ?: return
    val allJobs by viewModel.jobs.collectAsState()
    val allSessions by viewModel.taskSessions.collectAsState()
    val activeTask by viewModel.activeTask.collectAsState()

    var selectedWorkTab by remember { mutableIntStateOf(0) }
    var showCreateJobModal by remember { mutableStateOf(false) }
    var selectedJobForDetails by remember { mutableStateOf<MicroJobItem?>(null) }

    // 14.3 Job Filters (Job Type, Reward Range, Duration, Available Jobs Only, Newest Jobs, Highest Reward)
    var filterJobType by remember { mutableStateOf<JobType?>(null) }
    var filterAvailableOnly by remember { mutableStateOf(true) }
    var filterHighReward by remember { mutableStateOf(false) }
    var filterShortDuration by remember { mutableStateOf(false) }
    var sortByHighestReward by remember { mutableStateOf(false) }

    val myCompletedSessions = allSessions.filter { it.workerUid == currentUser.uid }
    val myPostedJobs = allJobs.filter { it.creatorUid == currentUser.uid }
    val myTaskEarnings = myCompletedSessions.sumOf { it.rewardCoins }

    val filteredJobs = remember(
        allJobs,
        filterJobType,
        filterAvailableOnly,
        filterHighReward,
        filterShortDuration,
        sortByHighestReward
    ) {
        var list = allJobs
        if (filterAvailableOnly) {
            list = list.filter { it.status == "ACTIVE" && it.remainingWorkers > 0 }
        }
        if (filterJobType != null) {
            list = list.filter { it.jobType == filterJobType }
        }
        if (filterHighReward) {
            list = list.filter { it.rewardPerWorker >= 25 }
        }
        if (filterShortDuration) {
            list = list.filter { it.requiredDurationSec <= 12 }
        }
        if (sortByHighestReward) {
            list.sortedByDescending { it.rewardPerWorker }
        } else {
            list.sortedByDescending { it.createdTimestampMs }
        }
    }

    val workTabs = listOf(
        tr(lang, "উপলভ্য কাজ (${filteredJobs.size})", "Available Jobs (${filteredJobs.size})"),
        tr(lang, "সম্পন্ন কাজ (${myCompletedSessions.size})", "Completed Jobs (${myCompletedSessions.size})"),
        tr(lang, "আমার পোস্টকৃত কাজ (${myPostedJobs.size})", "My Posted Jobs (${myPostedJobs.size})")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 14.1 Work Dashboard Banner
        item {
            ElevatedCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(NexBluePrimary, NexPurpleSecondary)
                            )
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = tr(lang, "ওয়ার্ক ও আর্নিং ড্যাশবোর্ড", "Work & Earning Dashboard"),
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = tr(
                                    lang,
                                    "যাচাইকৃত কাজ সম্পন্ন করে কয়েন আয় করুন অথবা নিজের কাজ পোস্ট করুন",
                                    "Complete verified tasks to earn coins or post your own jobs"
                                ),
                                color = Color.White.copy(alpha = 0.9f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        FilledTonalButton(
                            onClick = { showCreateJobModal = true },
                            modifier = Modifier.testTag("post_job_button")
                        ) {
                            Icon(Icons.Filled.AddTask, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(tr(lang, "কাজ পোস্ট (+)", "Post Job"), fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        WorkDashboardStat(tr(lang, "ব্যালেন্স", "Balance"), "${currentUser.currentBalance} C")
                        WorkDashboardStat(tr(lang, "কাজ থেকে আয়", "Job Earnings"), "$myTaskEarnings C")
                        WorkDashboardStat(tr(lang, "সম্পন্ন কাজ", "Completed"), "${myCompletedSessions.size}")
                        WorkDashboardStat(tr(lang, "আমার জব", "My Jobs"), "${myPostedJobs.size}")
                    }
                }
            }
        }

        // Work Tabs
        item {
            ScrollableTabRow(selectedTabIndex = selectedWorkTab, edgePadding = 4.dp) {
                workTabs.forEachIndexed { idx, title ->
                    Tab(
                        selected = selectedWorkTab == idx,
                        onClick = { selectedWorkTab = idx },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }

        when (selectedWorkTab) {
            0 -> {
                // 14.3 Job List & Filter Controls
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = tr(lang, "ফিল্টার মেনু (Filters):", "Filter Jobs:"),
                            style = MaterialTheme.typography.labelLarge
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = filterJobType == null,
                                onClick = { filterJobType = null },
                                label = { Text(tr(lang, "সব ধরন (All Types)", "All Types")) }
                            )
                            JobType.entries.forEach { jt ->
                                FilterChip(
                                    selected = filterJobType == jt,
                                    onClick = { filterJobType = if (filterJobType == jt) null else jt },
                                    label = { Text(tr(lang, jt.labelBn, jt.labelEn)) }
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = filterAvailableOnly,
                                onClick = { filterAvailableOnly = !filterAvailableOnly },
                                label = { Text(tr(lang, "উপলভ্য কাজ (Available Only)", "Available Only")) }
                            )
                            FilterChip(
                                selected = sortByHighestReward,
                                onClick = { sortByHighestReward = !sortByHighestReward },
                                label = { Text(tr(lang, "সর্বোচ্চ পুরস্কার (Highest Reward)", "Highest Reward")) }
                            )
                            FilterChip(
                                selected = filterHighReward,
                                onClick = { filterHighReward = !filterHighReward },
                                label = { Text(tr(lang, "২৫+ কয়েন (Reward ≥ 25)", "Reward ≥ 25")) }
                            )
                            FilterChip(
                                selected = filterShortDuration,
                                onClick = { filterShortDuration = !filterShortDuration },
                                label = { Text(tr(lang, "দ্রুত কাজ (≤ 12s)", "Quick (≤ 12s)")) }
                            )
                        }
                    }
                }

                if (filteredJobs.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Outlined.WorkHistory,
                            title = tr(lang, "এই ফিল্টারে কোনো কাজ পাওয়া যায়নি", "No matching jobs"),
                            subtitle = tr(lang, "ফিল্টার পরিবর্তন করুন অথবা নতুন একটি কাজ পোস্ট করুন।", "Reset filters or post a new job."),
                            actionLabel = tr(lang, "সব ফিল্টার রিসেট করুন", "Reset Filters"),
                            onAction = {
                                filterJobType = null
                                filterAvailableOnly = true
                                filterHighReward = false
                                filterShortDuration = false
                                sortByHighestReward = false
                            }
                        )
                    }
                } else {
                    items(filteredJobs, key = { it.jobId }) { job ->
                        val alreadyDone = viewModel.hasUserCompletedJob(job.jobId)
                        ElevatedCard(
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("job_card_${job.jobId}")
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = tr(lang, job.jobType.labelBn, job.jobType.labelEn),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    CoinBadge(coins = job.rewardPerWorker)
                                }

                                Text(
                                    text = job.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = tr(
                                        lang,
                                        "পোস্ট করেছেন: ${job.creatorName} • সময়: ${job.requiredDurationSec} সেকেন্ড",
                                        "By: ${job.creatorName} • Duration: ${job.requiredDurationSec}s"
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // Workers Progress Bar
                                val progress = (job.completedWorkers.toFloat() / job.totalWorkers.coerceAtLeast(1)).coerceIn(0f, 1f)
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = tr(
                                            lang,
                                            "সম্পন্ন: ${job.completedWorkers}/${job.totalWorkers}",
                                            "Completed: ${job.completedWorkers}/${job.totalWorkers}"
                                        ),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    Text(
                                        text = tr(
                                            lang,
                                            "অবশিষ্ট কোটা (Remaining): ${job.remainingWorkers}",
                                            "Remaining Workers: ${job.remainingWorkers}"
                                        ),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SuccessGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { selectedJobForDetails = job },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(tr(lang, "বিস্তারিত (View Details)", "View Details"))
                                    }
                                    Button(
                                        onClick = { viewModel.startTaskSession(job) },
                                        enabled = !alreadyDone && job.remainingWorkers > 0,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("start_task_${job.jobId}")
                                    ) {
                                        Text(
                                            text = if (alreadyDone) tr(lang, "সম্পন্ন হয়েছে ✓", "Completed ✓")
                                            else tr(lang, "কাজ শুরু (Start Task)", "Start Task"),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // 14.4 Completed Jobs List
                if (myCompletedSessions.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Outlined.TaskAlt,
                            title = tr(lang, "এখনো কোনো কাজ সম্পন্ন করেননি", "No completed jobs yet"),
                            subtitle = tr(lang, "উপলভ্য কাজ থেকে যেকোনো কাজ সম্পন্ন করলেই এখানে তালিকা ও রিওয়ার্ড স্ট্যাটাস দেখাবে।", "Complete tasks to see your verification & reward history.")
                        )
                    }
                } else {
                    items(myCompletedSessions, key = { it.sessionId }) { session ->
                        ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(session.jobTitle, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    CoinBadge(coins = session.rewardCoins)
                                }
                                Text(
                                    text = "Completion Time: ${session.completedAt}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "Proof: ${session.proofSubmitted}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = SuccessGreen.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = tr(lang, session.verificationStatus.labelBn, session.verificationStatus.labelEn),
                                            color = SuccessGreen,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Text(
                                        text = "• Reward Status: Credited Once (Verified)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SuccessGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // 14.5 My Posted Jobs (Total Budget, Remaining Budget, Completed/Remaining Workers, Edit & Cancel Job)
                if (myPostedJobs.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Outlined.PostAdd,
                            title = tr(lang, "আপনি এখনো কোনো কাজ পোস্ট করেননি", "You haven't posted any jobs"),
                            subtitle = tr(lang, "কয়েন ব্যালেন্স ব্যবহার করে আপনার ওয়েবসাইট বা সোশ্যাল পেজের জন্য কাজ পোস্ট করুন।", "Use your coin balance to create campaigns."),
                            actionLabel = tr(lang, "নতুন কাজ পোস্ট করুন", "Post a Job"),
                            onAction = { showCreateJobModal = true }
                        )
                    }
                } else {
                    items(myPostedJobs, key = { it.jobId }) { myJob ->
                        var showEditMyJobModal by remember { mutableStateOf(false) }

                        ElevatedCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(myJob.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Surface(
                                        color = if (myJob.status == "ACTIVE") SuccessGreen.copy(alpha = 0.15f) else ErrorRed.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = myJob.status,
                                            color = if (myJob.status == "ACTIVE") SuccessGreen else ErrorRed,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "Total Budget: ${myJob.totalBudget} Coins • Remaining Budget: ${myJob.remainingBudget} Coins",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NexBluePrimary
                                )
                                Text(
                                    text = "Completed Workers: ${myJob.completedWorkers} • Remaining Workers: ${myJob.remainingWorkers}",
                                    style = MaterialTheme.typography.bodySmall
                                )

                                if (myJob.status == "ACTIVE") {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(
                                            onClick = { showEditMyJobModal = true },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(tr(lang, "Edit Job", "Edit Job"))
                                        }
                                        Button(
                                            onClick = { viewModel.cancelMyPostedJobAndRefund(myJob.jobId) },
                                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Outlined.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(tr(lang, "Cancel & Refund", "Cancel & Refund"))
                                        }
                                    }
                                }
                            }
                        }

                        if (showEditMyJobModal) {
                            var editTitle by remember { mutableStateOf(myJob.title) }
                            var editDesc by remember { mutableStateOf(myJob.description) }
                            AlertDialog(
                                onDismissRequest = { showEditMyJobModal = false },
                                title = { Text(tr(lang, "কাজ সম্পাদনা (Edit Job)", "Edit Posted Job")) },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = editTitle,
                                            onValueChange = { editTitle = it },
                                            label = { Text("Job Title") },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        OutlinedTextField(
                                            value = editDesc,
                                            onValueChange = { editDesc = it },
                                            label = { Text("Job Instructions") },
                                            minLines = 3,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            viewModel.editMyPostedJob(myJob.jobId, editTitle, editDesc)
                                            showEditMyJobModal = false
                                        }
                                    ) {
                                        Text(tr(lang, "আপডেট করুন", "Save"))
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showEditMyJobModal = false }) {
                                        Text(tr(lang, "বাতিল", "Cancel"))
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // View Job Details Modal
    if (selectedJobForDetails != null) {
        val j = selectedJobForDetails!!
        AlertDialog(
            onDismissRequest = { selectedJobForDetails = null },
            title = { Text(j.title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• ধরন (Type): ${tr(lang, j.jobType.labelBn, j.jobType.labelEn)}")
                    Text("• পুরস্কার (Reward): ${j.rewardPerWorker} Coins")
                    Text("• সময়সীমা (Required Duration): ${j.requiredDurationSec} সেকেন্ড")
                    Text("• কাজের লিংক (Target URL): ${j.targetUrl}")
                    Text("• অবশিষ্ট কোটা: ${j.remainingWorkers}/${j.totalWorkers}")
                    Text("• নির্দেশনা:\n${j.description}")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val toStart = j
                        selectedJobForDetails = null
                        viewModel.startTaskSession(toStart)
                    }
                ) {
                    Text(tr(lang, "কাজ শুরু করুন (Start Task)", "Start Task"))
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedJobForDetails = null }) {
                    Text(tr(lang, "বন্ধ করুন", "Close"))
                }
            }
        )
    }

    // 14.4 Active Task Session & Live Countdown Timer Dialog
    if (activeTask != null) {
        val task = activeTask!!
        var proofInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { /* Prevent accidental dismiss while timer runs */ },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (task.isTimerCompleted) Icons.Filled.CheckCircle else Icons.Filled.Timer,
                        contentDescription = null,
                        tint = if (task.isTimerCompleted) SuccessGreen else WarningOrange
                    )
                    Text(
                        text = if (task.isTimerCompleted)
                            tr(lang, "সময় সম্পন্ন! এখন পুরস্কার দাবি করুন", "Timer Complete! Claim Reward")
                        else
                            tr(lang, "টাস্ক সেশন চলছে (${task.remainingSeconds}s)", "Task Session (${task.remainingSeconds}s)")
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(task.job.title, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Target URL: ${task.job.targetUrl}",
                        style = MaterialTheme.typography.bodySmall,
                        color = NexBluePrimary
                    )
                    Text(
                        text = task.job.description,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Surface(
                        color = if (task.isTimerCompleted) SuccessGreen.copy(alpha = 0.15f) else WarningOrange.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (task.isTimerCompleted)
                                    tr(lang, "✓ ভিজিট সময় যাচাই সফল হয়েছে!", "✓ Duration verified!")
                                else
                                    tr(lang, "অবশিষ্ট সময়: ${task.remainingSeconds} সেকেন্ড", "Remaining: ${task.remainingSeconds}s"),
                                fontWeight = FontWeight.Bold,
                                color = if (task.isTimerCompleted) SuccessGreen else WarningOrange
                            )
                            CoinBadge(coins = task.job.rewardPerWorker)
                        }
                    }

                    if (task.job.proofRequired) {
                        OutlinedTextField(
                            value = proofInput,
                            onValueChange = { proofInput = it },
                            label = {
                                Text(tr(lang, "কাজের প্রমাণ লিখুন (Proof / কোড / ইউজারনেম)", "Enter Task Proof"))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("task_proof_input")
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.submitAndClaimTaskReward(proofInput) },
                    enabled = task.isTimerCompleted,
                    modifier = Modifier.testTag("claim_task_reward_button")
                ) {
                    Text(tr(lang, "ভেরিফাই ও +${task.job.rewardPerWorker} Coins নিন", "Verify & Claim +${task.job.rewardPerWorker} Coins"))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelActiveTaskSession() }) {
                    Text(tr(lang, "সেশন বাতিল", "Cancel Task"))
                }
            }
        )
    }

    // 14.2 Job Post Form Modal
    if (showCreateJobModal) {
        var selectedType by remember { mutableStateOf(JobType.WEBSITE_VISIT) }
        var jobTitle by remember { mutableStateOf("") }
        var jobDesc by remember { mutableStateOf("") }
        var targetUrl by remember { mutableStateOf("https://") }
        var workersCountText by remember { mutableStateOf("50") }
        var rewardPerWorkerText by remember { mutableStateOf("10") }
        var durationSecText by remember { mutableStateOf("15") }
        var proofRequired by remember { mutableStateOf(true) }
        var postJobError by remember { mutableStateOf<String?>(null) }

        val workersInt = workersCountText.toIntOrNull() ?: 0
        val rewardInt = rewardPerWorkerText.toIntOrNull() ?: 0
        val totalCost = workersInt * rewardInt

        AlertDialog(
            onDismissRequest = { showCreateJobModal = false },
            title = { Text(tr(lang, "নতুন কাজ পোস্ট করুন (Job Post Form)", "Post a New Micro-Job")) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = tr(
                            lang,
                            "আপনার বর্তমান ব্যালেন্স: ${currentUser.currentBalance} Coins",
                            "Your Available Balance: ${currentUser.currentBalance} Coins"
                        ),
                        fontWeight = FontWeight.Bold,
                        color = NexBluePrimary
                    )

                    Text(tr(lang, "কাজের ধরন (Select Job Type):", "Select Job Type:"), style = MaterialTheme.typography.labelLarge)
                    JobType.entries.forEach { jt ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedType = jt }
                        ) {
                            RadioButton(selected = selectedType == jt, onClick = { selectedType = jt })
                            Text(tr(lang, jt.labelBn, jt.labelEn))
                        }
                    }

                    OutlinedTextField(
                        value = jobTitle,
                        onValueChange = { jobTitle = it },
                        label = { Text(tr(lang, "কাজের নাম (Job Title)", "Job Title")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = jobDesc,
                        onValueChange = { jobDesc = it },
                        label = { Text(tr(lang, "বিস্তারিত নির্দেশনা (Job Description)", "Job Description")) },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = targetUrl,
                        onValueChange = { targetUrl = it },
                        label = { Text(tr(lang, "টার্গেট লিংক (Target URL)", "Target URL")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = workersCountText,
                            onValueChange = { workersCountText = it.filter { c -> c.isDigit() } },
                            label = { Text(tr(lang, "কর্মী সংখ্যা (Workers)", "Workers")) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = rewardPerWorkerText,
                            onValueChange = { rewardPerWorkerText = it.filter { c -> c.isDigit() } },
                            label = { Text(tr(lang, "প্রতিজনের কয়েন (Reward)", "Reward/Worker")) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = durationSecText,
                        onValueChange = { durationSecText = it.filter { c -> c.isDigit() } },
                        label = { Text(tr(lang, "প্রয়োজনীয় সময় (৫-৬০ সেকেন্ড)", "Required Duration (5-60s)")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { proofRequired = !proofRequired }
                    ) {
                        Checkbox(checked = proofRequired, onCheckedChange = { proofRequired = it })
                        Text(tr(lang, "কাজের প্রমাণ আবশ্যক (Proof Requirement)", "Require Task Proof"))
                    }

                    // Auto-Calculated Total Cost Card (Section 14.2)
                    Surface(
                        color = if (totalCost <= currentUser.currentBalance) SuccessGreen.copy(alpha = 0.14f) else ErrorRed.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = tr(
                                    lang,
                                    "স্বয়ংক্রিয় মোট বাজেট (Total Cost): $workersInt জন × $rewardInt Coins = $totalCost Coins",
                                    "Total Budget: $workersInt × $rewardInt Coins = $totalCost Coins"
                                ),
                                fontWeight = FontWeight.Bold,
                                color = if (totalCost <= currentUser.currentBalance) SuccessGreen else ErrorRed
                            )
                            Text(
                                text = tr(
                                    lang,
                                    "পোস্ট করার সাথে সাথে এই বাজেট সংরক্ষিত (Escrow) হবে।",
                                    "Budget is reserved immediately on job creation."
                                ),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    AnimatedVisibility(visible = postJobError != null) {
                        Text(
                            text = postJobError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val err = viewModel.createMicroJob(
                            jobType = selectedType,
                            title = jobTitle,
                            description = jobDesc,
                            targetUrl = targetUrl,
                            numberOfWorkers = workersInt,
                            rewardPerWorker = rewardInt,
                            requiredDurationSec = durationSecText.toIntOrNull() ?: 15,
                            proofRequired = proofRequired
                        )
                        if (err == null) {
                            showCreateJobModal = false
                        } else {
                            postJobError = err
                        }
                    }
                ) {
                    Text(tr(lang, "বাজেট যাচাই ও জব পোস্ট করুন", "Verify & Post Job"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateJobModal = false }) {
                    Text(tr(lang, "বাতিল", "Cancel"))
                }
            }
        )
    }
}

@Composable
private fun WorkDashboardStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp
        )
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.85f),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

/**
 * Section 15: Gifts & Missions Screen
 * Available Gifts, Active Missions, Completed Missions, My Rewards & Gift History
 */
@Composable
fun GiftsAndMissionsScreen(
    viewModel: NexEarnViewModel,
    lang: AppLanguage
) {
    val currentUser = viewModel.getCurrentUser() ?: return
    val missions by viewModel.missions.collectAsState()
    val coinTransactions by viewModel.coinTransactions.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }

    val activeMissions = missions.filter { it.isActive && !it.claimedByUids.contains(currentUser.uid) }
    val completedMissions = missions.filter { it.claimedByUids.contains(currentUser.uid) }
    val giftHistory = coinTransactions.filter { it.uid == currentUser.uid && it.type == CoinTxType.GIFT_REWARD }

    val tabs = listOf(
        tr(lang, "সক্রিয় মিশন (${activeMissions.size})", "Active Missions (${activeMissions.size})"),
        tr(lang, "সম্পন্ন মিশন (${completedMissions.size})", "Completed (${completedMissions.size})"),
        tr(lang, "গিফট হিস্ট্রি (${giftHistory.size})", "Gift History (${giftHistory.size})")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ElevatedCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(NexPurpleSecondary, NexBluePrimary)
                            )
                        )
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tr(lang, "মিশন ও গিফট রিওয়ার্ড সেন্টার", "Gifts & Missions Hub"),
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = tr(
                                    lang,
                                    "প্রতিটি মিশন সম্পন্ন করে সার্ভার ভেরিফিকেশনের মাধ্যমে বোনাস কয়েন জিতে নিন!",
                                    "Complete daily & milestone missions to claim verified gift coins!"
                                ),
                                color = Color.White.copy(alpha = 0.9f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CardGiftcard,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 4.dp) {
                tabs.forEachIndexed { idx, label ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = { Text(label, fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }

        when (selectedTab) {
            0 -> {
                if (activeMissions.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Outlined.EmojiEvents,
                            title = tr(lang, "সব মিশন সম্পন্ন হয়েছে!", "All active missions completed!"),
                            subtitle = tr(lang, "আপনি সকল উপলভ্য মিশনের গিফট সংগ্রহ করেছেন।", "You have claimed all available mission gifts.")
                        )
                    }
                } else {
                    items(activeMissions, key = { it.missionId }) { mission ->
                        val progress = viewModel.getMissionProgress(mission)
                        val ratio = (progress.toFloat() / mission.targetCount.coerceAtLeast(1)).coerceIn(0f, 1f)
                        val isReady = progress >= mission.targetCount

                        ElevatedCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(NexPurpleSecondary.copy(alpha = 0.14f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Redeem,
                                                contentDescription = null,
                                                tint = NexPurpleSecondary
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = tr(lang, mission.titleBn, mission.titleEn),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Valid: ${mission.startDate} – ${mission.endDate}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    CoinBadge(coins = mission.rewardCoins)
                                }

                                Text(
                                    text = tr(lang, mission.descriptionBn, mission.descriptionEn),
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                LinearProgressIndicator(
                                    progress = { ratio },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = tr(
                                            lang,
                                            "অগ্রগতি (Progress): $progress / ${mission.targetCount}",
                                            "Progress: $progress / ${mission.targetCount}"
                                        ),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (isReady) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Button(
                                        onClick = { viewModel.claimMissionReward(mission.missionId) },
                                        modifier = Modifier.testTag("claim_mission_${mission.missionId}")
                                    ) {
                                        Text(
                                            text = if (isReady)
                                                tr(lang, "পুরস্কার নিন (+${mission.rewardCoins})", "Claim Reward (+${mission.rewardCoins})")
                                            else
                                                tr(lang, "যাচাই করুন (Continue)", "Verify / Continue"),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                if (completedMissions.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Outlined.Verified,
                            title = tr(lang, "এখনো কোনো মিশন ক্লেইম করা হয়নি", "No claimed missions yet"),
                            subtitle = tr(lang, "সক্রিয় মিশন ট্যাব থেকে পুরস্কার ক্লেইম করুন।", "Claim rewards from the Active Missions tab.")
                        )
                    }
                } else {
                    items(completedMissions, key = { it.missionId }) { mission ->
                        ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(tr(lang, mission.titleBn, mission.titleEn), fontWeight = FontWeight.Bold)
                                    Text(
                                        tr(lang, "স্ট্যাটাস: সম্পন্ন ও ক্লেইমকৃত (Completed ✓)", "Status: Completed & Rewarded ✓"),
                                        color = SuccessGreen,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                CoinBadge(coins = mission.rewardCoins)
                            }
                        }
                    }
                }
            }

            2 -> {
                items(giftHistory, key = { it.txId }) { tx ->
                    ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(tx.description, fontWeight = FontWeight.Bold)
                                Text("${tx.txId} • ${tx.dateTime}", style = MaterialTheme.typography.labelSmall)
                            }
                            Text(
                                text = "+${tx.coins} Coins",
                                color = SuccessGreen,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}
