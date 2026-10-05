package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.LandRecordsDataset
import com.example.data.model.RecordType
import com.example.ui.LRDownloaderViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun testAppNameString() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("LR Downloader", appName)
    }

    @Test
    fun testDatasetIntegrityWithRealisticMouzaCounts() {
        val cumilla = LandRecordsDataset.districts.find { it.id == "cumilla" }
        assertNotNull(cumilla)
        assertEquals(17, cumilla?.upazilas?.size)

        val debidwar = cumilla?.upazilas?.find { it.id == "debidwar" }
        assertNotNull(debidwar)
        assertEquals(136, debidwar?.mouzas?.size)

        val brahmanbaria = LandRecordsDataset.districts.find { it.id == "brahmanbaria" }
        assertNotNull(brahmanbaria)
        assertEquals(9, brahmanbaria?.upazilas?.size)

        val bbSadar = brahmanbaria?.upazilas?.find { it.id == "brahmanbaria-sadar" }
        assertNotNull(bbSadar)
        assertEquals(126, bbSadar?.mouzas?.size)
    }

    @Test
    fun testUpazilaFetchAndInitialSevenQueueLogic() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = LRDownloaderViewModel(app)

        // 1. Initial State
        viewModel.onDistrictChanged("cumilla")
        assertEquals("cumilla", viewModel.uiState.value.selectedDistrictId)
        assertNull(viewModel.uiState.value.selectedUpazilaId)
        assertNull(viewModel.uiState.value.upazilaFetchInfo)

        // 2. Select Upazila: FIRST fetch how many mouzas in that upazila!
        viewModel.onUpazilaChanged("debidwar")

        val state = viewModel.uiState.value
        assertEquals("debidwar", state.selectedUpazilaId)
        assertNotNull(state.upazilaFetchInfo)
        assertEquals(136, state.targetTotalMouzas) // Found actual count
        assertEquals(136, state.upazilaFetchInfo?.totalMouzaCount)

        // Verify selector strictly selects the first 7 mouzas for Batch 1 (not all 136)
        assertEquals(7, state.selectedMouzaIds.size)
        assertEquals(1, state.currentBatchNumber)

        // 3. Test Select All Documents
        viewModel.selectAllRecordTypes()
        assertEquals(4, viewModel.uiState.value.selectedRecordTypes.size)

        // 4. Test Enqueue
        viewModel.queueSelectedDownloads()
        assertEquals(7, viewModel.uiState.value.queue.size)
        viewModel.uiState.value.queue.forEach { task ->
            assertEquals(4, task.recordTypes.size)
        }

        // 5. Test Next 7 Selection without repeats
        viewModel.clearQueue()
        viewModel.selectNextSevenMouzas()
        assertEquals(7, viewModel.uiState.value.selectedMouzaIds.size)
    }

    @Test
    fun testLandRecordStorageManagerWritesToDownloads() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val result = com.example.data.local.LandRecordStorageManager.saveFileToLandRecords(
            context,
            "test_mouza.json",
            "{\"test\": true}",
            "application/json"
        )
        assertNotNull(result)
        assertTrue(result.primaryDisplayPath.contains("LandRecords"))
    }
}
