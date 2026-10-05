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
    fun testDatasetIntegrity() {
        val cumilla = LandRecordsDataset.districts.find { it.id == "cumilla" }
        assertNotNull(cumilla)
        assertEquals(17, cumilla?.upazilas?.size)

        val brahmanbaria = LandRecordsDataset.districts.find { it.id == "brahmanbaria" }
        assertNotNull(brahmanbaria)
        assertEquals(9, brahmanbaria?.upazilas?.size)
    }

    @Test
    fun testCascadingStateAndMandatoryAutoSelect() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = LRDownloaderViewModel(app)

        // 1. Initial State: District is "cumilla", upazila is null, no mouzas selected
        viewModel.onDistrictChanged("cumilla")
        assertEquals("cumilla", viewModel.uiState.value.selectedDistrictId)
        assertNull(viewModel.uiState.value.selectedUpazilaId)
        assertTrue(viewModel.uiState.value.selectedMouzaIds.isEmpty())

        // 2. Select Upazila: MANDATORY AUTO-SELECT ALL MOUZAS
        val adarshaSadar = viewModel.currentUpazilas.find { it.id == "adarsha-sadar" }
        assertNotNull(adarshaSadar)
        viewModel.onUpazilaChanged("adarsha-sadar")

        assertEquals("adarsha-sadar", viewModel.uiState.value.selectedUpazilaId)
        assertEquals(adarshaSadar?.mouzas?.size, viewModel.uiState.value.selectedMouzaIds.size)
        adarshaSadar?.mouzas?.forEach { mouza ->
            assertTrue(
                "Mouza ${mouza.nameBn} should be auto-selected",
                viewModel.uiState.value.selectedMouzaIds.contains(mouza.id)
            )
        }

        // 3. Change District: Cascading clear
        viewModel.onDistrictChanged("brahmanbaria")
        assertEquals("brahmanbaria", viewModel.uiState.value.selectedDistrictId)
        assertNull(viewModel.uiState.value.selectedUpazilaId)
        assertTrue(viewModel.uiState.value.selectedMouzaIds.isEmpty())

        // 4. Select Upazila in Brahmanbaria: Auto-selects all mouzas of that upazila
        val sarail = viewModel.currentUpazilas.find { it.id == "sarail" }
        assertNotNull(sarail)
        viewModel.onUpazilaChanged("sarail")
        assertEquals("sarail", viewModel.uiState.value.selectedUpazilaId)
        assertEquals(sarail?.mouzas?.size, viewModel.uiState.value.selectedMouzaIds.size)

        // 5. Test Deselect All & Select All
        viewModel.deselectAllMouzas()
        assertTrue(viewModel.uiState.value.selectedMouzaIds.isEmpty())

        viewModel.selectAllMouzas()
        assertEquals(sarail?.mouzas?.size, viewModel.uiState.value.selectedMouzaIds.size)

        // 6. Test Record Type Filter Change
        viewModel.onRecordTypeChanged(RecordType.CS)
        assertEquals(RecordType.CS, viewModel.uiState.value.selectedRecordType)

        // 7. Test Enqueue
        viewModel.queueSelectedDownloads()
        assertEquals(sarail?.mouzas?.size, viewModel.uiState.value.queue.size)
        assertFalse(viewModel.uiState.value.isPaused)

        // 8. Test Pause / Resume / Clear Queue
        viewModel.togglePauseQueue()
        assertTrue(viewModel.uiState.value.isPaused)

        viewModel.togglePauseQueue()
        assertFalse(viewModel.uiState.value.isPaused)

        viewModel.clearQueue()
        assertTrue(viewModel.uiState.value.queue.isEmpty())
    }
}
