package com.guicarneirodev.agrotask.data.local.dao

import androidx.room.*
import com.guicarneirodev.agrotask.data.local.entity.ActivityRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityRecordDao {

    @Query("SELECT * FROM activity_records ORDER BY createdAt DESC")
    fun getAllActivityRecords(): Flow<List<ActivityRecordEntity>>

    @Query("SELECT * FROM activity_records WHERE id = :recordId")
    suspend fun getActivityRecordById(recordId: String): ActivityRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityRecord(record: ActivityRecordEntity)

    @Update
    suspend fun updateActivityRecord(record: ActivityRecordEntity)

    @Delete
    suspend fun deleteActivityRecord(record: ActivityRecordEntity)

    @Query("UPDATE activity_records SET syncedWithFirebase = 1 WHERE id = :recordId")
    suspend fun markAsSynced(recordId: String)

    @Query("SELECT * FROM activity_records WHERE syncedWithFirebase = 0")
    suspend fun getUnsyncedRecords(): List<ActivityRecordEntity>
}