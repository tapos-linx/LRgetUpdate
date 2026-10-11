package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DownloadTask
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadQueueDao {
    @Query("SELECT * FROM download_queue ORDER BY timestamp ASC")
    fun getQueue(): Flow<List<DownloadTask>>

    @Query("SELECT * FROM download_queue WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): DownloadTask?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<DownloadTask>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: DownloadTask)

    @Update
    suspend fun updateTask(task: DownloadTask)

    @Query("DELETE FROM download_queue WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)

    @Query("DELETE FROM download_queue")
    suspend fun clearQueue()

    @Query("UPDATE download_queue SET status = 'PENDING' WHERE status = 'DOWNLOADING'")
    suspend fun resetInterruptedTasks()
}
