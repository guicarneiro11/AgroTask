package com.guicarneirodev.agrotask.domain.repository

import com.guicarneirodev.agrotask.domain.model.ActivityRecord
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {
    fun getAllActivityRecords(): Flow<List<ActivityRecord>>
    suspend fun getActivityRecordById(recordId: String): ActivityRecord?
    suspend fun insertActivityRecord(record: ActivityRecord)
    suspend fun updateActivityRecord(record: ActivityRecord)
    suspend fun deleteActivityRecord(record: ActivityRecord)
    suspend fun syncWithFirebase()
}