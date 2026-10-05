package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.District
import com.example.data.model.DownloadTask
import com.example.data.model.LandRecordsDataset
import com.example.data.model.Mouza
import com.example.data.model.RecordType
import com.example.data.model.SavedRecord
import com.example.data.model.TaskStatus
import com.example.data.model.Upazila
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
import kotlin.random.Random

data class LRUiState(
    val selectedDistrictId: String = "cumilla",
    val selectedUpazilaId: String? = null,
    val selectedMouzaIds: Set<String> = emptySet(),
    val selectedRecordType: RecordType = RecordType.RS,
    val searchQuery: String = "",
    val queue: List<DownloadTask> = emptyList(),
    val isPaused: Boolean = false,
    val isShowingHistoryDialog: Boolean = false
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

    /**
     * Critical Cascading Handler 1: District Changed
     * Clears selected upazila, clears selected mouzas, dynamically updates list.
     */
    fun onDistrictChanged(newDistrictId: String) {
        _uiState.value = _uiState.value.copy(
            selectedDistrictId = newDistrictId,
            selectedUpazilaId = null,
            selectedMouzaIds = emptySet(),
            searchQuery = ""
        )
    }

    /**
     * Critical Cascading Handler 2: Upazila Changed
     * MANDATORY AUTO-SELECT LOGIC:
     * In the EXACT same event handler, find the selected upazila's mouza array
     * and IMMEDIATELY initialize selectedMouzaIds state with ALL mouza IDs in that list.
     */
    fun onUpazilaChanged(newUpazilaId: String?) {
        if (newUpazilaId == null) {
            _uiState.value = _uiState.value.copy(
                selectedUpazilaId = null,
                selectedMouzaIds = emptySet(),
                searchQuery = ""
            )
            return
        }

        val upazila = currentUpazilas.find { it.id == newUpazilaId }
        val allMouzaIds = upazila?.mouzas?.map { it.id }?.toSet() ?: emptySet()

        _uiState.value = _uiState.value.copy(
            selectedUpazilaId = newUpazilaId,
            selectedMouzaIds = allMouzaIds, // MANDATORY: checked by default immediately
            searchQuery = ""
        )
    }

    fun onRecordTypeChanged(recordType: RecordType) {
        _uiState.value = _uiState.value.copy(selectedRecordType = recordType)
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

    fun deselectAllMouzas() {
        _uiState.value = _uiState.value.copy(selectedMouzaIds = emptySet())
    }

    fun setShowHistoryDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(isShowingHistoryDialog = show)
    }

    /**
     * Queue Selected Downloads:
     * Creates DownloadTask instances for all currently checked mouzas and enqueues them.
     */
    fun queueSelectedDownloads() {
        val state = _uiState.value
        val upazila = currentUpazila ?: return
        val district = currentDistrict ?: return
        if (state.selectedMouzaIds.isEmpty()) return

        val newTasks = mutableListOf<DownloadTask>()
        val timestamp = System.currentTimeMillis()

        state.selectedMouzaIds.forEachIndexed { index, mouzaId ->
            val mouza = upazila.mouzas.find { it.id == mouzaId } ?: return@forEachIndexed
            val taskId = "${mouza.id}-${state.selectedRecordType.code}-$timestamp-$index"
            val totalKhatians = 35 + Random.nextInt(95)
            val fileSizeBytes = totalKhatians * 115000L

            val task = DownloadTask(
                id = taskId,
                mouzaId = mouza.id,
                mouzaNameBn = mouza.nameBn,
                mouzaNameEn = mouza.nameEn,
                jlNo = mouza.jlNo,
                upazilaNameBn = upazila.nameBn,
                districtNameBn = district.nameBn,
                recordType = state.selectedRecordType,
                status = TaskStatus.PENDING,
                progress = 0,
                totalKhatians = totalKhatians,
                downloadedKhatians = 0,
                fileSizeBytes = fileSizeBytes,
                speedKbps = 0,
                timestamp = timestamp
            )
            newTasks.add(task)
        }

        _uiState.value = _uiState.value.copy(
            queue = _uiState.value.queue + newTasks
        )

        // Launch worker dispatcher for new tasks
        newTasks.forEach { task ->
            launchTaskWorker(task.id)
        }
    }

    private fun launchTaskWorker(taskId: String) {
        val job = viewModelScope.launch {
            // Wait for concurrency permit (max 2 parallel downloads)
            concurrencyLimiter.withPermit {
                // Check if task was cancelled/removed
                val currentTask = _uiState.value.queue.find { it.id == taskId } ?: return@withPermit
                if (currentTask.status == TaskStatus.COMPLETED) return@withPermit

                // Mark task as DOWNLOADING
                updateTask(taskId) {
                    it.copy(status = TaskStatus.DOWNLOADING, progress = 5)
                }

                // Simulate realistic chunk-based download progression with anti-freeze
                val totalSteps = 10
                for (step in 1..totalSteps) {
                    // Check pause state
                    while (_uiState.value.isPaused) {
                        delay(350)
                    }

                    // Check if cleared
                    val exists = _uiState.value.queue.any { it.id == taskId }
                    if (!exists) return@withPermit

                    delay(250 + Random.nextLong(150))

                    val progress = (step * 10).coerceAtMost(100)
                    val speed = 280 + Random.nextInt(240)

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

                // Persist completed record in Room DB
                val finished = _uiState.value.queue.find { it.id == taskId }
                if (finished != null && finished.status == TaskStatus.COMPLETED) {
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
                            recordType = finished.recordType.code,
                            khatianCount = finished.totalKhatians,
                            fileSizeFormatted = sizeMb
                        )
                    )
                }
            }
        }
        taskJobs[taskId] = job
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
