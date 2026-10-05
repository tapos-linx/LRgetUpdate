package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DownloadTask
import com.example.data.model.Mouza
import com.example.data.model.RecordType
import com.example.data.model.SavedRecord
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
import com.example.ui.theme.OnEmeraldContainer
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

@Composable
fun LRMassDownloaderScreen(
    viewModel: LRDownloaderViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val savedRecords by viewModel.savedRecords.collectAsState()

    val availableMouzas = viewModel.currentMouzas
    val filteredMouzas = remember(availableMouzas, uiState.searchQuery) {
        if (uiState.searchQuery.isBlank()) {
            availableMouzas
        } else {
            val q = uiState.searchQuery.lowercase().trim()
            availableMouzas.filter {
                it.nameBn.lowercase().contains(q) ||
                        it.nameEn.lowercase().contains(q) ||
                        it.jlNo.contains(q)
            }
        }
    }

    val totalTasks = uiState.queue.size
    val completedTasks = uiState.queue.count { it.status == TaskStatus.COMPLETED }
    val downloadingTasks = uiState.queue.count { it.status == TaskStatus.DOWNLOADING }
    val pendingTasks = uiState.queue.count { it.status == TaskStatus.PENDING }
    val overallProgress = if (totalTasks == 0) 0 else {
        uiState.queue.sumOf { it.progress } / totalTasks
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(Slate50),
        topBar = {
            GovTechHeader(
                savedRecordsCount = savedRecords.size,
                onOpenSavedRecords = { viewModel.setShowHistoryDialog(true) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(2.dp)) }

            // 1. Cascading State Filter Card
            item {
                CascadingHierarchyCard(
                    allDistricts = viewModel.allDistricts,
                    selectedDistrictId = uiState.selectedDistrictId,
                    onDistrictSelected = { viewModel.onDistrictChanged(it) },
                    upazilas = viewModel.currentUpazilas,
                    selectedUpazilaId = uiState.selectedUpazilaId,
                    onUpazilaSelected = { viewModel.onUpazilaChanged(it) },
                    selectedRecordType = uiState.selectedRecordType,
                    onRecordTypeSelected = { viewModel.onRecordTypeChanged(it) }
                )
            }

            // 2. Mouza Registry & Selection Card
            item {
                MouzaRegistryCard(
                    currentUpazilaName = viewModel.currentUpazila?.nameBn,
                    selectedCount = uiState.selectedMouzaIds.size,
                    totalCount = availableMouzas.size,
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChange = { viewModel.onSearchQueryChanged(it) },
                    onSelectAll = { viewModel.selectAllMouzas() },
                    onDeselectAll = { viewModel.deselectAllMouzas() },
                    mouzas = filteredMouzas,
                    selectedMouzaIds = uiState.selectedMouzaIds,
                    onToggleMouza = { viewModel.toggleMouza(it) },
                    isUpazilaSelected = uiState.selectedUpazilaId != null,
                    onQueueSelected = { viewModel.queueSelectedDownloads() }
                )
            }

            // 3. Concurrency-Limited Queue HUD Card
            item {
                QueueHudCard(
                    totalTasks = totalTasks,
                    completedTasks = completedTasks,
                    downloadingTasks = downloadingTasks,
                    pendingTasks = pendingTasks,
                    overallProgress = overallProgress,
                    isPaused = uiState.isPaused,
                    onTogglePause = { viewModel.togglePauseQueue() },
                    onClearQueue = { viewModel.clearQueue() },
                    tasks = uiState.queue
                )
            }

            // Footer notes
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "গণপ্রজাতন্ত্রী বাংলাদেশ সরকার • ভূমি রেকর্ড ও জরিপ অধিদপ্তর (DLR&S)",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Slate400,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "LR Mass Downloader (Updated) • Cumilla & Brahmanbaria Districts",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = Slate400,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    // Saved Records Vault Dialog
    if (uiState.isShowingHistoryDialog) {
        SavedRecordsDialog(
            records = savedRecords,
            onDismiss = { viewModel.setShowHistoryDialog(false) },
            onDelete = { viewModel.deleteSavedRecord(it) },
            onClearAll = { viewModel.clearAllSavedRecords() }
        )
    }
}

@Composable
fun GovTechHeader(
    savedRecordsCount: Int,
    onOpenSavedRecords: () -> Unit
) {
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Surface(
        color = EmeraldPrimary,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = topInset)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Seal Emblem
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(EmeraldDark)
                            .border(1.dp, EmeraldLight.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Land Records Seal",
                            tint = AmberAccent,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "GovTech BD",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = EmeraldContainer,
                                modifier = Modifier
                                    .background(EmeraldDark, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                            Text(
                                text = "ভূমি রেকর্ড সেবা",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = EmeraldContainer.copy(alpha = 0.9f)
                            )
                        }
                        Text(
                            text = "LR Mass Downloader",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            ),
                            color = Color.White
                        )
                    }
                }

                // Vault Button
                OutlinedButton(
                    onClick = onOpenSavedRecords,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White,
                        containerColor = EmeraldDark.copy(alpha = 0.7f)
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(EmeraldLight.copy(alpha = 0.4f))
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("open_vault_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderSpecial,
                        contentDescription = "Vault",
                        modifier = Modifier.size(16.dp),
                        tint = AmberAccent
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Vault ($savedRecordsCount)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CascadingHierarchyCard(
    allDistricts: List<com.example.data.model.District>,
    selectedDistrictId: String,
    onDistrictSelected: (String) -> Unit,
    upazilas: List<com.example.data.model.Upazila>,
    selectedUpazilaId: String?,
    onUpazilaSelected: (String?) -> Unit,
    selectedRecordType: RecordType,
    onRecordTypeSelected: (RecordType) -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "ফিল্টার ও স্তর নির্বাচন (Cascading Hierarchy)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Slate800
                    )
                }

                Text(
                    text = "Pure Cascade",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    ),
                    modifier = Modifier
                        .background(EmeraldContainer, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            // 1. District & Upazila Selectors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // District Dropdown
                var districtExpanded by remember { mutableStateOf(false) }
                val currentDistrict = allDistricts.find { it.id == selectedDistrictId }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "১. জেলা (District)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Slate600,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Slate100)
                                .border(1.dp, Slate200, RoundedCornerShape(14.dp))
                                .clickable { districtExpanded = true }
                                .padding(horizontal = 12.dp, vertical = 14.dp)
                                .testTag("district_dropdown_trigger"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = currentDistrict?.let { "${it.nameBn} (${it.nameEn})" } ?: "জেলা নির্বাচন",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Slate800,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Slate600)
                        }

                        DropdownMenu(
                            expanded = districtExpanded,
                            onDismissRequest = { districtExpanded = false }
                        ) {
                            allDistricts.forEach { dist ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${dist.nameBn} (${dist.nameEn})",
                                            fontWeight = if (dist.id == selectedDistrictId) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        onDistrictSelected(dist.id)
                                        districtExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Upazila Dropdown
                var upazilaExpanded by remember { mutableStateOf(false) }
                val currentUpazila = upazilas.find { it.id == selectedUpazilaId }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "২. উপজেলা (Upazila)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Slate600,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (selectedUpazilaId != null) EmeraldContainer.copy(alpha = 0.4f) else Slate100)
                                .border(
                                    1.dp,
                                    if (selectedUpazilaId != null) EmeraldPrimary else Slate200,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { upazilaExpanded = true }
                                .padding(horizontal = 12.dp, vertical = 14.dp)
                                .testTag("upazila_dropdown_trigger"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = currentUpazila?.let { "${it.nameBn} (${it.nameEn})" } ?: "-- উপজেলা নির্বাচন --",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (selectedUpazilaId != null) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedUpazilaId != null) Slate900 else Slate400
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Slate600)
                        }

                        DropdownMenu(
                            expanded = upazilaExpanded,
                            onDismissRequest = { upazilaExpanded = false }
                        ) {
                            upazilas.forEach { upa ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = "${upa.nameBn} (${upa.nameEn})",
                                                fontWeight = if (upa.id == selectedUpazilaId) FontWeight.Bold else FontWeight.Normal
                                            )
                                            Text(
                                                text = "${upa.mouzas.size} টি মৌজা",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Slate400
                                            )
                                        }
                                    },
                                    onClick = {
                                        onUpazilaSelected(upa.id)
                                        upazilaExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 2. Record Type Filter Chips
            Column {
                Text(
                    text = "৩. জরিপ রেকর্ড ধরন (Record Type Filter)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Slate600,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RecordType.values().forEach { type ->
                        val isSelected = selectedRecordType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) EmeraldPrimary else Slate100)
                                .border(
                                    1.dp,
                                    if (isSelected) EmeraldDark else Slate200,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { onRecordTypeSelected(type) }
                                .padding(vertical = 10.dp, horizontal = 6.dp)
                                .testTag("record_type_${type.code.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = type.code,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                                    color = if (isSelected) Color.White else Slate800
                                )
                                Text(
                                    text = type.bengaliName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = if (isSelected) EmeraldContainer else Slate500,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MouzaRegistryCard(
    currentUpazilaName: String?,
    selectedCount: Int,
    totalCount: Int,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    mouzas: List<Mouza>,
    selectedMouzaIds: Set<String>,
    onToggleMouza: (String) -> Unit,
    isUpazilaSelected: Boolean,
    onQueueSelected: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with badge counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "মৌজা তালিকা (Mouza Registry)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Slate800
                        )
                        if (currentUpazilaName != null) {
                            Text(
                                text = currentUpazilaName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary
                                ),
                                modifier = Modifier
                                    .background(EmeraldContainer, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "মৌজা নির্বাচন ও ডাউনলোড কিউতে প্রেরণ",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Slate400
                    )
                }

                // Live Badge Counter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate100)
                        .border(1.dp, Slate200, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (selectedCount > 0) EmeraldPrimary else Slate400)
                        )
                        Text(
                            text = "Selected: $selectedCount / Total: $totalCount",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate800
                            )
                        )
                    }
                }
            }

            // Toolbar: Select All / Deselect All + Search
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onSelectAll,
                    enabled = isUpazilaSelected && totalCount > 0,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = EmeraldPrimary,
                        containerColor = EmeraldContainer.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.testTag("select_all_button")
                ) {
                    Text("Select All", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }

                OutlinedButton(
                    onClick = onDeselectAll,
                    enabled = selectedCount > 0,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Slate600,
                        containerColor = Slate100
                    ),
                    modifier = Modifier.testTag("deselect_all_button")
                ) {
                    Text("Deselect", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("খুঁজুন...", fontSize = 12.sp, color = Slate400) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Slate400, modifier = Modifier.size(18.dp))
                    },
                    singleLine = true,
                    enabled = isUpazilaSelected,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Slate100,
                        unfocusedContainerColor = Slate50,
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = Slate200
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("search_mouza_input")
                )
            }

            // Mouza list container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Slate50)
                    .border(1.dp, Slate200, RoundedCornerShape(16.dp))
                    .padding(8.dp)
            ) {
                if (!isUpazilaSelected) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Slate400,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "উপজেলা নির্বাচন করুন",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Slate600
                        )
                        Text(
                            text = "উপজেলা নির্বাচন করলে সকল মৌজা স্বয়ংক্রিয়ভাবে সিলেক্ট হবে",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = Slate400,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                } else if (mouzas.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("কোনো মৌজা পাওয়া যায়নি", color = Slate400, style = MaterialTheme.typography.bodySmall)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(mouzas, key = { it.id }) { mouza ->
                            val isChecked = selectedMouzaIds.contains(mouza.id)
                            MouzaItemCard(
                                mouza = mouza,
                                isChecked = isChecked,
                                onClick = { onToggleMouza(mouza.id) }
                            )
                        }
                    }
                }
            }

            // Primary Queue CTA
            Button(
                onClick = onQueueSelected,
                enabled = selectedCount > 0,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("queue_selected_downloads_button")
            ) {
                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Queue Selected Downloads ($selectedCount Mouzas)",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun MouzaItemCard(
    mouza: Mouza,
    isChecked: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isChecked) EmeraldContainer.copy(alpha = 0.5f) else Color.White)
            .border(
                1.dp,
                if (isChecked) EmeraldPrimary else Slate200,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("mouza_card_${mouza.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Checkbox icon
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isChecked) EmeraldPrimary else Slate100)
                    .border(
                        1.5.dp,
                        if (isChecked) EmeraldDark else Slate400,
                        RoundedCornerShape(6.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isChecked) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Checked",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Column {
                Text(
                    text = mouza.nameBn,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Slate800
                )
                Text(
                    text = mouza.nameEn,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = Slate400
                )
            }
        }

        // J.L. No badge pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "J.L.",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                color = Slate400
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Slate100)
                    .border(1.dp, Slate200, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = mouza.jlNo,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Slate800
                    )
                )
            }
        }
    }
}

@Composable
fun QueueHudCard(
    totalTasks: Int,
    completedTasks: Int,
    downloadingTasks: Int,
    pendingTasks: Int,
    overallProgress: Int,
    isPaused: Boolean,
    onTogglePause: () -> Unit,
    onClearQueue: () -> Unit,
    tasks: List<DownloadTask>
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with Pause/Resume and Clear actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.4f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "alpha"
                    )

                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (downloadingTasks > 0) EmeraldPrimary.copy(alpha = alpha) else Slate400
                            )
                    )

                    Text(
                        text = "Queue HUD Pipeline",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Slate800
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onTogglePause,
                        enabled = totalTasks > 0 && completedTasks < totalTasks,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isPaused) AmberDark else Slate700,
                            containerColor = if (isPaused) AmberContainer else Slate100
                        ),
                        modifier = Modifier.testTag("pause_resume_button")
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isPaused) "Resume" else "Pause",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    OutlinedButton(
                        onClick = onClearQueue,
                        enabled = totalTasks > 0,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = RoseError,
                            containerColor = RoseContainer.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.testTag("clear_queue_button")
                    ) {
                        Text(
                            text = "Clear",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // Overall Progress
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Overall Queue Progress",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Slate600
                    )
                    Text(
                        text = "$overallProgress%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldPrimary
                        )
                    )
                }

                LinearProgressIndicator(
                    progress = { overallProgress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = EmeraldPrimary,
                    trackColor = Slate200
                )
            }

            // Live count summary badges (Total, Done, Active, Queued)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryPill(title = "Total", count = totalTasks, bgColor = Slate100, textColor = Slate800, modifier = Modifier.weight(1f))
                SummaryPill(title = "Done", count = completedTasks, bgColor = EmeraldContainer, textColor = EmeraldDark, modifier = Modifier.weight(1f))
                SummaryPill(title = "Active", count = downloadingTasks, bgColor = BlueContainer, textColor = BlueAccent, modifier = Modifier.weight(1f))
                SummaryPill(title = "Queued", count = pendingTasks, bgColor = AmberContainer, textColor = AmberDark, modifier = Modifier.weight(1f))
            }

            // Concurrency limiter note
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate100)
                    .border(1.dp, Slate200, RoundedCornerShape(12.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(EmeraldPrimary)
                )
                Text(
                    text = "Anti-Freeze Concurrency: Max 2 parallel tasks to protect device memory and prevent UI stutter.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = Slate600
                )
            }

            // Scrollable Task List
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Slate50)
                    .border(1.dp, Slate200, RoundedCornerShape(16.dp))
                    .padding(8.dp)
            ) {
                if (tasks.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = Slate400,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "ডাউনলোড কিউ খালি",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Slate600
                        )
                        Text(
                            text = "মৌজা সিলেক্ট করে Queue বাটনে ক্লিক করুন",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = Slate400
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(tasks, key = { it.id }) { task ->
                            DownloadTaskCard(task = task)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryPill(
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
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
            color = textColor.copy(alpha = 0.8f)
        )
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = textColor
        )
    }
}

@Composable
fun DownloadTaskCard(task: DownloadTask) {
    val isDone = task.status == TaskStatus.COMPLETED
    val isRunning = task.status == TaskStatus.DOWNLOADING

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    isDone -> Slate100.copy(alpha = 0.7f)
                    isRunning -> EmeraldContainer.copy(alpha = 0.4f)
                    else -> Color.White
                }
            )
            .border(
                1.dp,
                when {
                    isDone -> Slate200
                    isRunning -> EmeraldPrimary.copy(alpha = 0.7f)
                    else -> Slate200
                },
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = task.mouzaNameBn,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Slate800
                )
                Text(
                    text = "(${task.mouzaNameEn})",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = Slate500
                )
                Text(
                    text = "J.L. ${task.jlNo}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                    color = Slate700,
                    modifier = Modifier
                        .background(Slate200, RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                )
                Text(
                    text = task.recordType.code,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = EmeraldPrimary
                    ),
                    modifier = Modifier
                        .background(EmeraldContainer, RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }

            // Status Badge
            when (task.status) {
                TaskStatus.COMPLETED -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier
                            .background(EmeraldContainer, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(12.dp))
                        Text("Done", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = EmeraldDark))
                    }
                }
                TaskStatus.DOWNLOADING -> {
                    Text(
                        text = "${task.progress}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, color = BlueAccent),
                        modifier = Modifier
                            .background(BlueContainer, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                TaskStatus.PENDING -> {
                    Text(
                        text = "Pending",
                        style = MaterialTheme.typography.labelSmall.copy(color = Slate500),
                        modifier = Modifier
                            .background(Slate200, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                TaskStatus.PAUSED -> {
                    Text(
                        text = "Paused",
                        style = MaterialTheme.typography.labelSmall.copy(color = AmberDark),
                        modifier = Modifier
                            .background(AmberContainer, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        LinearProgressIndicator(
            progress = { task.progress / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (isDone) EmeraldPrimary else BlueAccent,
            trackColor = Slate200
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${task.upazilaNameBn} • Khatians: ${task.downloadedKhatians}/${task.totalKhatians}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = Slate500
            )
            Text(
                text = if (isRunning) "${task.speedKbps} KB/s" else if (isDone) "Archived" else "Queued",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                color = if (isRunning) BlueAccent else Slate400
            )
        }
    }
}

@Composable
fun SavedRecordsDialog(
    records: List<SavedRecord>,
    onDismiss: () -> Unit,
    onDelete: (String) -> Unit,
    onClearAll: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.FolderSpecial, contentDescription = null, tint = EmeraldPrimary)
                    Text("Downloaded Records Vault", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
                if (records.isNotEmpty()) {
                    IconButton(onClick = onClearAll) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear all", tint = RoseError)
                    }
                }
            }
        },
        text = {
            if (records.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "এখনো কোনো রেকর্ড সম্পন্ন হয়নি।\nমৌজা ডাউনলোড সম্পন্ন হলে এখানে সংরক্ষিত হবে।",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(records, key = { it.id }) { rec ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Slate100)
                                .border(1.dp, Slate200, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = rec.mouzaNameBn,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Slate900
                                    )
                                    Text(
                                        text = rec.recordType,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary),
                                        modifier = Modifier
                                            .background(EmeraldContainer, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                                Text(
                                    text = "${rec.districtNameBn} • ${rec.upazilaNameBn} • J.L. ${rec.jlNo}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate600
                                )
                                Text(
                                    text = "খতিয়ান সংখ্যা: ${rec.khatianCount} • আকার: ${rec.fileSizeFormatted}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = Slate400
                                )
                            }

                            IconButton(onClick = { onDelete(rec.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Slate400)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("Close")
            }
        }
    )
}
