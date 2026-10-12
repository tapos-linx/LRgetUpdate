package com.example.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Controller and bridge for scheduling, observing, and controlling
 * the background WorkManager queue download worker.
 */
object DownloadWorkManagerHelper {

    const val UNIQUE_WORK_NAME = "lr_download_queue_work"
    const val TAG_ANTI_FREEZE = "anti_freeze_downloader"

    /**
     * Enqueues the WorkManager download worker.
     * Uses ExistingWorkPolicy.KEEP by default to avoid duplicate jobs,
     * or ExistingWorkPolicy.REPLACE when user explicitly resumes/re-triggers.
     */
    fun enqueueDownloadWork(context: Context, replace: Boolean = false) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<DownloadQueueWorker>()
            .setConstraints(constraints)
            .addTag(TAG_ANTI_FREEZE)
            .build()

        val policy = if (replace) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP
        WorkManager.getInstance(context).enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            policy,
            workRequest
        )
    }

    /**
     * Cancels the active WorkManager job.
     */
    fun cancelDownloadWork(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
    }

    /**
     * Observes whether the WorkManager job is actively ENQUEUED or RUNNING.
     */
    fun isWorkRunningFlow(context: Context): Flow<Boolean> {
        return WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkFlow(UNIQUE_WORK_NAME)
            .map { workInfoList ->
                workInfoList.any { info ->
                    info.state == WorkInfo.State.RUNNING || info.state == WorkInfo.State.ENQUEUED
                }
            }
    }

    /**
     * Observes the active WorkInfo list for detailed diagnostics.
     */
    fun getWorkInfosFlow(context: Context): Flow<List<WorkInfo>> {
        return WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkFlow(UNIQUE_WORK_NAME)
    }
}
