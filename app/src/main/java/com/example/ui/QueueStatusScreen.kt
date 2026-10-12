package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DownloadTask
import com.example.data.model.TaskStatus
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberDark
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.BlueContainer
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.RoseContainer
import com.example.ui.theme.RoseError
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

/**
 * Dedicated Material 3 Screen to display:
 * 1. Current overall Queue Status & WorkManager Engine
 * 2. Progress bars & live telemetry for Active Downloads
 * 3. List of Pending Tasks waiting in queue with priority & metadata
 */
@Composable
fun QueueStatusScreen(
    viewModel: LRDownloaderViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Intercept hardware/system back gesture to return to Downloader
    BackHandler {
        onNavigateBack()
    }

    val uiState by viewModel.uiState.collectAsState()
    val tasks = uiState.queue

    val activeTasks = remember(tasks) { tasks.filter { it.status == TaskStatus.DOWNLOADING } }
    val pendingTasks = remember(tasks) { tasks.filter { it.status == TaskStatus.PENDING } }
    val completedTasks = remember(tasks) { tasks.filter { it.status == TaskStatus.COMPLETED } }

    val totalTasksCount = tasks.size
    val overallProgress = if (totalTasksCount == 0) 0 else {
        tasks.sumOf { it.progress } / totalTasksCount
    }

    var showCompletedList by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(Slate50)
            .testTag("queue_status_screen"),
        topBar = {
            QueueStatusTopBar(
                isPaused = uiState.isPaused,
                activeCount = activeTasks.size,
                pendingCount = pendingTasks.size,
                onNavigateBack = onNavigateBack,
                onTogglePause = { viewModel.togglePauseQueue() },
                onClearQueue = { viewModel.clearQueue() }
            )
        },
        bottomBar = {
            QueueStickyBottomBar(
                isPaused = uiState.isPaused,
                totalTasks = totalTasksCount,
                pendingTasks = pendingTasks.size,
                onTogglePause = { viewModel.togglePauseQueue() },
                onClearQueue = { viewModel.clearQueue() },
                onReturnToSelection = onNavigateBack
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Overall Queue Status Hero Banner
            item {
                QueueStatusHeroCard(
                    isPaused = uiState.isPaused,
                    totalTasks = totalTasksCount,
                    completedCount = completedTasks.size,
                    activeCount = activeTasks.size,
                    pendingCount = pendingTasks.size,
                    overallProgress = overallProgress,
                    currentBatch = uiState.currentBatchNumber,
                    totalBatches = viewModel.totalBatches,
                    targetTotalMouzas = uiState.targetTotalMouzas,
                    isWorkManagerActive = uiState.isWorkManagerActive,
                    useWorkManagerService = uiState.useWorkManagerBackgroundService,
                    antiBlockingDescription = uiState.antiBlockingDelayDescription,
                    onToggleWorkManager = { viewModel.setUseWorkManagerBackgroundService(it) }
                )
            }

            // 2. ACTIVE DOWNLOADS SECTION WITH PROGRESS BARS
            item {
                SectionHeaderRow(
                    title = "Active Downloads (চলমান ডাউনলোড)",
                    badgeCount = activeTasks.size,
                    badgeColor = BlueAccent,
                    badgeBg = BlueContainer,
                    testTag = "active_downloads_header"
                )
            }

            if (activeTasks.isEmpty()) {
                item {
                    EmptyActiveDownloadsCard(
                        isPaused = uiState.isPaused,
                        hasPending = pendingTasks.isNotEmpty(),
                        onResume = { viewModel.togglePauseQueue() }
                    )
                }
            } else {
                items(activeTasks, key = { it.id }) { task ->
                    ActiveDownloadProgressCard(task = task)
                }
            }

            // 3. PENDING TASKS SECTION
            item {
                SectionHeaderRow(
                    title = "Pending Queue (অপেক্ষমান তালিকা)",
                    badgeCount = pendingTasks.size,
                    badgeColor = AmberDark,
                    badgeBg = AmberContainer,
                    testTag = "pending_tasks_header"
                )
            }

            if (pendingTasks.isEmpty()) {
                item {
                    EmptyPendingTasksCard(
                        totalCompleted = completedTasks.size,
                        onAddMore = onNavigateBack
                    )
                }
            } else {
                itemsIndexed(pendingTasks, key = { _, task -> task.id }) { index, task ->
                    PendingTaskCard(
                        task = task,
                        queuePosition = index + 1
                    )
                }
            }

            // 4. COMPLETED TASKS COLLAPSIBLE ACCORDION
            if (completedTasks.isNotEmpty()) {
                item {
                    CompletedTasksAccordionHeader(
                        completedCount = completedTasks.size,
                        isExpanded = showCompletedList,
                        onToggle = { showCompletedList = !showCompletedList }
                    )
                }

                if (showCompletedList) {
                    items(completedTasks, key = { it.id }) { task ->
                        CompletedTaskCard(task = task)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Top App Bar with M3 back button and queue controls
 */
@Composable
private fun QueueStatusTopBar(
    isPaused: Boolean,
    activeCount: Int,
    pendingCount: Int,
    onNavigateBack: () -> Unit,
    onTogglePause: () -> Unit,
    onClearQueue: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("queue_screen_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Downloader",
                            tint = Slate800
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Queue Status",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isPaused) AmberContainer else EmeraldContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isPaused) "PAUSED" else "LIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isPaused) AmberDark else EmeraldDark
                                    )
                                )
                            }
                        }
                        Text(
                            text = "ভূমি রেকর্ড গণ-ডাউনলোড কিউ মনিটর",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Slate500,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Quick Pause/Resume
                    IconButton(
                        onClick = onTogglePause,
                        modifier = Modifier.testTag("top_bar_toggle_pause")
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (isPaused) "Resume Queue" else "Pause Queue",
                            tint = if (isPaused) EmeraldPrimary else AmberDark
                        )
                    }

                    // Clear Queue
                    IconButton(
                        onClick = onClearQueue,
                        modifier = Modifier.testTag("top_bar_clear_queue")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear Queue",
                            tint = RoseError
                        )
                    }
                }
            }
        }
    }
}

/**
 * Hero Card summarizing the queue metrics, overall progress bar, and WorkManager status
 */
@Composable
private fun QueueStatusHeroCard(
    isPaused: Boolean,
    totalTasks: Int,
    completedCount: Int,
    activeCount: Int,
    pendingCount: Int,
    overallProgress: Int,
    currentBatch: Int,
    totalBatches: Int,
    targetTotalMouzas: Int,
    isWorkManagerActive: Boolean,
    useWorkManagerService: Boolean,
    antiBlockingDescription: String,
    onToggleWorkManager: (Boolean) -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("queue_hero_status_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Status + Batch Number
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isPaused -> AmberAccent
                                    activeCount > 0 -> EmeraldLight
                                    else -> Slate400
                                }
                            )
                    )
                    Text(
                        text = when {
                            isPaused -> "Queue Paused (স্থগিত)"
                            activeCount > 0 -> "Processing Batch #$currentBatch (সক্রিয়)"
                            totalTasks == 0 -> "Queue Ready (প্রস্তুত)"
                            else -> "Batch Pipeline Idle"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Slate900
                        )
                    )
                }

                if (totalBatches > 0) {
                    Text(
                        text = "Batch $currentBatch of $totalBatches",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = EmeraldDark
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(EmeraldContainer)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Overall Progress Bar Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Batch Progress",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Slate600
                    )
                    Text(
                        text = "$overallProgress%",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldPrimary
                        )
                    )
                }

                LinearProgressIndicator(
                    progress = { overallProgress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .testTag("overall_queue_progress_bar"),
                    color = EmeraldPrimary,
                    trackColor = Slate200
                )
            }

            // Metric Summary Pills (Total, Done, Active, Queued)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricSummaryCard(
                    title = "Total",
                    count = totalTasks,
                    bgColor = Slate100,
                    textColor = Slate800,
                    modifier = Modifier.weight(1f)
                )
                MetricSummaryCard(
                    title = "Done",
                    count = completedCount,
                    bgColor = EmeraldContainer,
                    textColor = EmeraldDark,
                    modifier = Modifier.weight(1f)
                )
                MetricSummaryCard(
                    title = "Active",
                    count = activeCount,
                    bgColor = BlueContainer,
                    textColor = BlueAccent,
                    modifier = Modifier.weight(1f)
                )
                MetricSummaryCard(
                    title = "Queued",
                    count = pendingCount,
                    bgColor = AmberContainer,
                    textColor = AmberDark,
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(color = Slate200, thickness = 1.dp)

            // WorkManager Background Service & Anti-Blocking Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isWorkManagerActive) EmeraldContainer.copy(alpha = 0.4f) else Slate100)
                    .border(1.dp, if (isWorkManagerActive) EmeraldPrimary.copy(alpha = 0.4f) else Slate200, RoundedCornerShape(12.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = if (isWorkManagerActive) EmeraldPrimary else Slate500,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "WorkManager Background Engine",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isWorkManagerActive) EmeraldDark else Slate800
                        )
                        Text(
                            text = if (isWorkManagerActive) "Active • $antiBlockingDescription" else "Standby (Anti-Blocking Ready)",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = if (isWorkManagerActive) EmeraldPrimary else Slate500,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Switch(
                    checked = useWorkManagerService,
                    onCheckedChange = onToggleWorkManager,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = EmeraldPrimary,
                        checkedTrackColor = EmeraldContainer
                    ),
                    modifier = Modifier.testTag("queue_screen_workmanager_switch")
                )
            }
        }
    }
}

/**
 * Metric card pill
 */
@Composable
private fun MetricSummaryCard(
    title: String,
    count: Int,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            ),
            color = textColor.copy(alpha = 0.8f)
        )
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = textColor
        )
    }
}

/**
 * Card for an actively downloading Mouza task with detailed linear progress bar, speed, and khatian count
 */
@Composable
private fun ActiveDownloadProgressCard(task: DownloadTask) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, BlueAccent.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .animateContentSize()
            .testTag("active_task_${task.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Mouza Name, JL, and Live Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = task.mouzaNameBn,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = Slate900
                        )
                        Text(
                            text = "(${task.mouzaNameEn})",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = Slate500
                        )
                    }
                    Text(
                        text = "${task.districtNameBn} ➔ ${task.upazilaNameBn} • J.L. No. ${task.jlNo}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            color = Slate600
                        )
                    )
                }

                // Downloading Animated Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(BlueContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Downloading",
                        tint = BlueAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${task.progress}%",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = BlueAccent
                        )
                    )
                }
            }

            // PRIMARY PROGRESS BAR
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LinearProgressIndicator(
                    progress = { task.progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .testTag("task_progress_bar_${task.id}"),
                    color = BlueAccent,
                    trackColor = Slate200
                )

                // Stats under progress bar: Khatians, Speed, Document Types
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📥 ${task.downloadedKhatians} / ${task.totalKhatians} Khatians",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate700
                        )
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${task.speedKbps} KB/s",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }
            }

            // Bottom Badges: Record types + Anti-blocking protection
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Documents:",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = Slate500
                    )
                    Text(
                        text = task.recordTypeLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = EmeraldDark
                        ),
                        modifier = Modifier
                            .background(EmeraldContainer, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = "Anti-blocking active",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.sp,
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}

/**
 * Card for a pending task waiting in the queue
 */
@Composable
private fun PendingTaskCard(
    task: DownloadTask,
    queuePosition: Int
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate200, RoundedCornerShape(14.dp))
            .testTag("pending_task_${task.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Queue Position Badge
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (queuePosition <= 2) AmberContainer else Slate100),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#$queuePosition",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = if (queuePosition <= 2) AmberDark else Slate600
                    )
                )
            }

            // Mouza Information
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = task.mouzaNameBn,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Slate900
                    )
                    Text(
                        text = "(${task.mouzaNameEn})",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = Slate500
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "J.L. ${task.jlNo}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate700
                        ),
                        modifier = Modifier
                            .background(Slate100, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                    Text(
                        text = task.recordTypeLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldDark
                        ),
                        modifier = Modifier
                            .background(EmeraldContainer, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                    Text(
                        text = "${task.totalKhatians} Khatians",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = Slate500
                    )
                }
            }

            // Waiting Status Chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(AmberContainer.copy(alpha = 0.7f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassEmpty,
                        contentDescription = null,
                        tint = AmberDark,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = if (queuePosition <= 2) "Next Up" else "Waiting",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = AmberDark
                        )
                    )
                }
            }
        }
    }
}

/**
 * Card for completed task in the current batch
 */
@Composable
private fun CompletedTaskCard(task: DownloadTask) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Slate100.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate200, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Column {
                    Text(
                        text = "${task.mouzaNameBn} (${task.mouzaNameEn}) • J.L. ${task.jlNo}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = Slate800
                    )
                    Text(
                        text = "${task.recordTypeLabel} • ${task.totalKhatians} Khatians Saved",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = Slate500
                    )
                }
            }

            Text(
                text = "100%",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = EmeraldDark
                ),
                modifier = Modifier
                    .background(EmeraldContainer, RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

/**
 * Empty state for Active Downloads
 */
@Composable
private fun EmptyActiveDownloadsCard(
    isPaused: Boolean,
    hasPending: Boolean,
    onResume: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Slate100),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate200, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isPaused) Icons.Default.Pause else Icons.Default.CloudDownload,
                contentDescription = null,
                tint = if (isPaused) AmberAccent else Slate400,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isPaused) "Queue is currently Paused (স্থগিত)" else "No Downloads Currently Active",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = Slate700
            )
            Text(
                text = if (isPaused) {
                    "Tap Resume to continue processing pending tasks"
                } else if (hasPending) {
                    "Pending tasks will automatically start within the concurrency slot"
                } else {
                    "Add Mouza records to the queue to begin mass downloading"
                },
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = Slate500,
                textAlign = TextAlign.Center
            )

            if (isPaused) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onResume,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Resume Queue", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

/**
 * Empty state for Pending Tasks
 */
@Composable
private fun EmptyPendingTasksCard(
    totalCompleted: Int,
    onAddMore: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Slate100),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate200, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.HourglassTop,
                contentDescription = null,
                tint = Slate400,
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (totalCompleted > 0) "All Queued Tasks in Current Batch Completed! 🎉" else "Pending Queue is Empty",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = Slate700
            )
            Text(
                text = "Select more Mouzas from Upazila to queue next batch",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = Slate500
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onAddMore,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text("Select More Mouzas", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

/**
 * Section header row with count badge
 */
@Composable
private fun SectionHeaderRow(
    title: String,
    badgeCount: Int,
    badgeColor: Color,
    badgeBg: Color,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Slate800
            )
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(badgeBg)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = "$badgeCount",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = badgeColor
                )
            )
        }
    }
}

/**
 * Accordion header for recently completed tasks
 */
@Composable
private fun CompletedTasksAccordionHeader(
    completedCount: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(EmeraldContainer.copy(alpha = 0.5f))
            .clickable { onToggle() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = EmeraldDark,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Completed in this Session ($completedCount)",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = EmeraldDark
                )
            )
        }
        Text(
            text = if (isExpanded) "Hide ▲" else "Show ▼",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary
            )
        )
    }
}

/**
 * Persistent bottom control bar with actions
 */
@Composable
private fun QueueStickyBottomBar(
    isPaused: Boolean,
    totalTasks: Int,
    pendingTasks: Int,
    onTogglePause: () -> Unit,
    onClearQueue: () -> Unit,
    onReturnToSelection: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Pause / Resume Button
            Button(
                onClick = onTogglePause,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPaused) EmeraldPrimary else AmberDark
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1.3f)
                    .testTag("queue_screen_bottom_pause_resume_btn"),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Icon(
                    imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isPaused) "Resume Queue" else "Pause Queue",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Clear Queue Button
            OutlinedButton(
                onClick = onClearQueue,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseError),
                modifier = Modifier
                    .weight(1f)
                    .testTag("queue_screen_bottom_clear_btn"),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Clear",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
