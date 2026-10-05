package com.example.ui

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.LandRecordStorageManager
import com.example.data.model.District
import com.example.data.model.DownloadTask
import com.example.data.model.LandRecordsDataset
import com.example.data.model.MasterArchive
import com.example.data.model.Mouza
import com.example.data.model.RecordType
import com.example.data.model.SavedRecord
import com.example.data.model.TaskStatus
import com.example.data.model.Upazila
import com.example.data.model.UpazilaFetchInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.io.File
import kotlin.random.Random
import android.net.Uri
import android.widget.Toast

data class LRUiState(
    val selectedDistrictId: String = "cumilla",
    val selectedUpazilaId: String? = null,
    val isFetchingUpazila: Boolean = false,
    val upazilaFetchInfo: UpazilaFetchInfo? = null,
    val targetTotalMouzas: Int = 0,
    val selectedMouzaIds: Set<String> = emptySet(),
    val downloadedMouzaIds: Set<String> = emptySet(),
    val selectedRecordTypes: Set<RecordType> = setOf(RecordType.RS),
    val searchQuery: String = "",
    val queue: List<DownloadTask> = emptyList(),
    val isPaused: Boolean = false,
    val isAutoBatchMode: Boolean = true,
    val currentBatchNumber: Int = 1,
    val batchSize: Int = 7,
    val masterArchive: MasterArchive? = null,
    val isShowingHistoryDialog: Boolean = false,
    val isShowingLedgerDialog: Boolean = false,
    val isShowingSaveSuccessDialog: Boolean = false,
    val cacheNotificationMessage: String? = null
)

class LRDownloaderViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.savedRecordDao()

    val savedRecords: StateFlow<List<SavedRecord>> = dao.getAllSavedRecords()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow(LRUiState())
    val uiState: StateFlow<LRUiState> = _uiState.asStateFlow()

    // Bounded Concurrency Limiter: strictly max 2 concurrent downloads
    private val concurrencyLimiter = Semaphore(2)
    private val taskJobs = mutableMapOf<String, Job>()

    val allDistricts: List<District> = LandRecordsDataset.districts

    val currentDistrict: District?
        get() = allDistricts.find { it.id == _uiState.value.selectedDistrictId }

    val currentUpazilas: List<Upazila>
        get() = currentDistrict?.upazilas ?: emptyList()

    val currentUpazila: Upazila?
        get() = currentUpazilas.find { it.id == _uiState.value.selectedUpazilaId }

    val currentMouzas: List<Mouza>
        get() = currentUpazila?.mouzas ?: emptyList()

    val totalBatches: Int
        get() {
            val total = _uiState.value.targetTotalMouzas.coerceAtLeast(currentMouzas.size)
            if (total == 0) return 0
            val size = _uiState.value.batchSize
            return (total + size - 1) / size
        }

    fun onDistrictChanged(newDistrictId: String) {
        clearQueue()
        _uiState.value = _uiState.value.copy(
            selectedDistrictId = newDistrictId,
            selectedUpazilaId = null,
            isFetchingUpazila = false,
            upazilaFetchInfo = null,
            targetTotalMouzas = 0,
            selectedMouzaIds = emptySet(),
            downloadedMouzaIds = emptySet(),
            currentBatchNumber = 1,
            masterArchive = null,
            searchQuery = "",
            cacheNotificationMessage = null
        )
    }

    /**
     * When an Upazila is selected, first fetch how many mouzas are in that upazila.
     * Select strictly initial 7 mouzas for Batch 1.
     */
    fun onUpazilaChanged(newUpazilaId: String?) {
        clearQueue()
        if (newUpazilaId == null) {
            _uiState.value = _uiState.value.copy(
                selectedUpazilaId = null,
                isFetchingUpazila = false,
                upazilaFetchInfo = null,
                targetTotalMouzas = 0,
                selectedMouzaIds = emptySet(),
                downloadedMouzaIds = emptySet(),
                currentBatchNumber = 1,
                masterArchive = null,
                searchQuery = "",
                cacheNotificationMessage = null
            )
            return
        }

        val upazila = currentUpazilas.find { it.id == newUpazilaId }
        val district = currentDistrict

        if (upazila != null && district != null) {
            val foundMouzaCount = upazila.mouzas.size
            val batchSize = _uiState.value.batchSize
            val totalBatchesCount = (foundMouzaCount + batchSize - 1) / batchSize

            // Select strictly the first 7 mouzas for initial batch queue
            val firstSevenIds = upazila.mouzas.take(batchSize).map { it.id }.toSet()

            val fetchInfo = UpazilaFetchInfo(
                upazilaId = upazila.id,
                upazilaNameBn = upazila.nameBn,
                upazilaNameEn = upazila.nameEn,
                districtNameBn = district.nameBn,
                totalMouzaCount = foundMouzaCount,
                batchCount = totalBatchesCount,
                message = "Upazila Survey Index: Found $foundMouzaCount mouzas in ${upazila.nameBn}. Sequential Target set to all $foundMouzaCount mouzas in $totalBatchesCount batches."
            )

            _uiState.value = _uiState.value.copy(
                selectedUpazilaId = newUpazilaId,
                isFetchingUpazila = false,
                upazilaFetchInfo = fetchInfo,
                targetTotalMouzas = foundMouzaCount,
                selectedMouzaIds = firstSevenIds,
                downloadedMouzaIds = emptySet(),
                currentBatchNumber = 1,
                masterArchive = null,
                searchQuery = "",
                cacheNotificationMessage = null
            )
        }
    }

    fun toggleRecordType(type: RecordType) {
        val current = _uiState.value.selectedRecordTypes
        val newSet = if (current.contains(type)) {
            if (current.size > 1) current - type else current
        } else {
            current + type
        }
        _uiState.value = _uiState.value.copy(selectedRecordTypes = newSet)
    }

    fun selectAllRecordTypes() {
        _uiState.value = _uiState.value.copy(
            selectedRecordTypes = RecordType.values().toSet()
        )
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun toggleMouza(mouzaId: String) {
        val currentSet = _uiState.value.selectedMouzaIds
        val newSet = if (currentSet.contains(mouzaId)) {
            currentSet - mouzaId
        } else {
            currentSet + mouzaId
        }
        _uiState.value = _uiState.value.copy(selectedMouzaIds = newSet)
    }

    fun selectAllMouzas() {
        val allIds = currentMouzas.map { it.id }.toSet()
        _uiState.value = _uiState.value.copy(selectedMouzaIds = allIds)
    }

    fun selectNextSevenMouzas() {
        val undownloaded = currentMouzas.filter { it.id !in _uiState.value.downloadedMouzaIds }
        val nextSeven = undownloaded.take(_uiState.value.batchSize).map { it.id }.toSet()
        _uiState.value = _uiState.value.copy(selectedMouzaIds = nextSeven)
    }

    fun deselectAllMouzas() {
        _uiState.value = _uiState.value.copy(selectedMouzaIds = emptySet())
    }

    fun setShowHistoryDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(isShowingHistoryDialog = show)
    }

    fun setShowLedgerDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(isShowingLedgerDialog = show)
    }

    fun queueSelectedDownloads() {
        val state = _uiState.value
        val upazila = currentUpazila ?: return
        val district = currentDistrict ?: return

        val candidates = if (state.selectedMouzaIds.isNotEmpty()) {
            upazila.mouzas.filter { it.id in state.selectedMouzaIds && it.id !in state.downloadedMouzaIds }
        } else {
            upazila.mouzas.filter { it.id !in state.downloadedMouzaIds }.take(state.batchSize)
        }

        if (candidates.isEmpty()) return

        enqueueBatch(candidates, district, upazila, state.currentBatchNumber)
    }

    private fun enqueueBatch(
        mouzasToQueue: List<Mouza>,
        district: District,
        upazila: Upazila,
        batchNum: Int
    ) {
        val timestamp = System.currentTimeMillis()
        val recordTypes = _uiState.value.selectedRecordTypes

        val newTasks = mouzasToQueue.mapIndexed { index, mouza ->
            val taskId = "${mouza.id}-${batchNum}-$timestamp-$index"
            val totalKhatians = (30 + Random.nextInt(50)) * recordTypes.size
            val fileSizeBytes = totalKhatians * 110000L

            DownloadTask(
                id = taskId,
                mouzaId = mouza.id,
                mouzaNameBn = mouza.nameBn,
                mouzaNameEn = mouza.nameEn,
                jlNo = mouza.jlNo,
                upazilaNameBn = upazila.nameBn,
                districtNameBn = district.nameBn,
                recordTypes = recordTypes,
                status = TaskStatus.PENDING,
                progress = 0,
                totalKhatians = totalKhatians,
                downloadedKhatians = 0,
                fileSizeBytes = fileSizeBytes,
                speedKbps = 0,
                batchNumber = batchNum,
                timestamp = timestamp
            )
        }

        _uiState.value = _uiState.value.copy(
            queue = _uiState.value.queue + newTasks
        )

        newTasks.forEach { task ->
            launchTaskWorker(task.id)
        }
    }

    private fun launchTaskWorker(taskId: String) {
        val job = viewModelScope.launch {
            concurrencyLimiter.withPermit {
                val currentTask = _uiState.value.queue.find { it.id == taskId } ?: return@withPermit
                if (currentTask.status == TaskStatus.COMPLETED) return@withPermit

                updateTask(taskId) {
                    it.copy(status = TaskStatus.DOWNLOADING, progress = 5)
                }

                val totalSteps = 10
                for (step in 1..totalSteps) {
                    while (_uiState.value.isPaused) {
                        delay(300)
                    }

                    val exists = _uiState.value.queue.any { it.id == taskId }
                    if (!exists) return@withPermit

                    delay(160 + Random.nextLong(100))

                    val progress = (step * 10).coerceAtMost(100)
                    val speed = 320 + Random.nextInt(260)

                    updateTask(taskId) { t ->
                        val downloaded = (progress * t.totalKhatians) / 100
                        t.copy(
                            progress = progress,
                            downloadedKhatians = downloaded,
                            speedKbps = speed,
                            status = if (progress >= 100) TaskStatus.COMPLETED else TaskStatus.DOWNLOADING
                        )
                    }
                }

                val finished = _uiState.value.queue.find { it.id == taskId }
                if (finished != null && finished.status == TaskStatus.COMPLETED) {
                    val updatedDownloaded = _uiState.value.downloadedMouzaIds + finished.mouzaId
                    _uiState.value = _uiState.value.copy(downloadedMouzaIds = updatedDownloaded)

                    // Write individual Mouza record directly to phone storage: Download/LandRecords/
                    val currentDist = currentDistrict
                    val currentUpaz = currentUpazila
                    if (currentDist != null && currentUpaz != null) {
                        try {
                            LandRecordStorageManager.saveMouzaRecordFile(
                                getApplication(),
                                finished,
                                currentDist,
                                currentUpaz
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    val sizeMb = String.format("%.2f MB", finished.fileSizeBytes / (1024.0 * 1024.0))
                    dao.insertRecord(
                        SavedRecord(
                            id = finished.id,
                            mouzaId = finished.mouzaId,
                            mouzaNameBn = finished.mouzaNameBn,
                            mouzaNameEn = finished.mouzaNameEn,
                            jlNo = finished.jlNo,
                            upazilaNameBn = finished.upazilaNameBn,
                            districtNameBn = finished.districtNameBn,
                            recordType = finished.recordTypeLabel,
                            khatianCount = finished.totalKhatians,
                            fileSizeFormatted = sizeMb
                        )
                    )

                    checkAndTriggerNextBatch()
                }
            }
        }
        taskJobs[taskId] = job
    }

    private fun checkAndTriggerNextBatch() {
        val state = _uiState.value
        val upazila = currentUpazila ?: return
        val district = currentDistrict ?: return

        val allQueuedDone = state.queue.isNotEmpty() && state.queue.all { it.status == TaskStatus.COMPLETED }
        if (!allQueuedDone) return

        val totalMouzas = state.targetTotalMouzas.coerceAtLeast(upazila.mouzas.size)
        val downloadedCount = state.downloadedMouzaIds.size

        if (downloadedCount < totalMouzas && state.isAutoBatchMode) {
            val remainingMouzas = upazila.mouzas.filter { it.id !in state.downloadedMouzaIds }
            val nextBatch = remainingMouzas.take(state.batchSize)

            if (nextBatch.isNotEmpty()) {
                val nextBatchNumber = state.currentBatchNumber + 1
                _uiState.value = _uiState.value.copy(
                    currentBatchNumber = nextBatchNumber,
                    selectedMouzaIds = nextBatch.map { it.id }.toSet()
                )
                enqueueBatch(nextBatch, district, upazila, nextBatchNumber)
                return
            }
        }

        if (downloadedCount >= totalMouzas || state.queue.all { it.status == TaskStatus.COMPLETED }) {
            compileMasterArchive(district, upazila)
        }
    }

    private fun compileMasterArchive(district: District, upazila: Upazila) {
        val totalKhatians = _uiState.value.queue.sumOf { it.totalKhatians }
        val totalBytes = _uiState.value.queue.sumOf { it.fileSizeBytes }
        val sizeMb = String.format("%.2f MB", totalBytes / (1024.0 * 1024.0))
        val typesText = _uiState.value.selectedRecordTypes.joinToString("+") { it.code }
        val fileName = "${district.nameEn}_${upazila.nameEn}_Master_Land_Records_2026.json"

        val archive = MasterArchive(
            upazilaId = upazila.id,
            upazilaNameBn = upazila.nameBn,
            upazilaNameEn = upazila.nameEn,
            districtNameBn = district.nameBn,
            districtNameEn = district.nameEn,
            totalMouzasCount = _uiState.value.downloadedMouzaIds.size,
            recordTypesIncluded = typesText,
            totalKhatiansCount = totalKhatians,
            totalSizeMb = sizeMb,
            fileName = fileName,
            isReady = true,
            isDownloadedToPhone = false,
            phoneSavedPath = null
        )

        _uiState.value = _uiState.value.copy(masterArchive = archive)
    }

    /**
     * One-Click Download: Saves Master JSON, TXT ledger, and CSV spreadsheet
     * directly into the public Download/LandRecords folder on phone storage.
     * Automatically clears temporary cache & memory while preserving downloaded records.
     */
    fun downloadMasterFileToPhone(context: Context) {
        val archive = _uiState.value.masterArchive ?: return
        val upazila = currentUpazila ?: return
        val district = currentDistrict ?: return

        viewModelScope.launch {
            val queueTasks = _uiState.value.queue

            val saveResult = LandRecordStorageManager.saveMasterLandRecordsArchive(
                context = context,
                archive = archive,
                district = district,
                upazila = upazila,
                queueTasks = queueTasks
            )

            // Automatically clear cache and app memory without removing downloaded records
            LandRecordStorageManager.clearCacheAndMemory(context)

            _uiState.value = _uiState.value.copy(
                masterArchive = archive.copy(
                    isDownloadedToPhone = true,
                    phoneSavedPath = saveResult.jsonPath,
                    textFileSavedPath = saveResult.textPath,
                    csvFileSavedPath = saveResult.csvPath,
                    savedFilesList = saveResult.allFiles,
                    fileContentPreview = saveResult.filePreview
                ),
                isShowingSaveSuccessDialog = true,
                cacheNotificationMessage = "Master & Mouza files saved to Download/LandRecords/! Cache cleared."
            )
        }
    }

    fun dismissSaveSuccessDialog() {
        _uiState.value = _uiState.value.copy(isShowingSaveSuccessDialog = false)
    }

    fun openDownloadsFolder(context: Context) {
        LandRecordStorageManager.openDownloadsFolder(context)
    }

    fun exportArchiveToSafUri(context: Context, uri: Uri) {
        val preview = _uiState.value.masterArchive?.fileContentPreview ?: ""
        val success = LandRecordStorageManager.writeToSafUri(context, uri, preview)
        if (success) {
            Toast.makeText(context, "Archive saved to selected folder successfully!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Failed to save file to selected location.", Toast.LENGTH_SHORT).show()
        }
    }

    fun clearAppMemoryAndCache(context: Context) {
        LandRecordStorageManager.clearCacheAndMemory(context)
    }

    fun dismissCacheNotification() {
        _uiState.value = _uiState.value.copy(cacheNotificationMessage = null)
    }

    private fun updateTask(taskId: String, transform: (DownloadTask) -> DownloadTask) {
        val updated = _uiState.value.queue.map { task ->
            if (task.id == taskId) transform(task) else task
        }
        _uiState.value = _uiState.value.copy(queue = updated)
    }

    fun togglePauseQueue() {
        _uiState.value = _uiState.value.copy(isPaused = !_uiState.value.isPaused)
    }

    fun clearQueue() {
        taskJobs.values.forEach { it.cancel() }
        taskJobs.clear()
        _uiState.value = _uiState.value.copy(
            queue = emptyList(),
            isPaused = false
        )
    }

    fun deleteSavedRecord(id: String) {
        viewModelScope.launch {
            dao.deleteRecordById(id)
        }
    }

    fun clearAllSavedRecords() {
        viewModelScope.launch {
            dao.clearAllRecords()
        }
    }
}
