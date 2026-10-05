package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.SavedRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedRecordDao {
    @Query("SELECT * FROM saved_land_records ORDER BY downloadedAt DESC")
    fun getAllSavedRecords(): Flow<List<SavedRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: SavedRecord)

    @Query("DELETE FROM saved_land_records WHERE id = :id")
    suspend fun deleteRecordById(id: String)

    @Query("DELETE FROM saved_land_records")
    suspend fun clearAllRecords()
}

@Database(entities = [SavedRecord::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun savedRecordDao(): SavedRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lr_mass_downloader.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
