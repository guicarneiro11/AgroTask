package com.guicarneirodev.agrotask.data.repository

import com.guicarneirodev.agrotask.data.firebase.FirebaseService
import com.guicarneirodev.agrotask.data.local.dao.ActivityRecordDao
import com.guicarneirodev.agrotask.data.local.mapper.toDomain
import com.guicarneirodev.agrotask.data.local.mapper.toEntity
import com.guicarneirodev.agrotask.domain.model.ActivityRecord
import com.guicarneirodev.agrotask.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ActivityRepositoryImpl(
    private val activityRecordDao: ActivityRecordDao,
    private val firebaseService: FirebaseService
) : ActivityRepository {

    override fun getAllActivityRecords(): Flow<List<ActivityRecord>> {
        return activityRecordDao.getAllActivityRecords().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getActivityRecordById(recordId: String): ActivityRecord? {
        return activityRecordDao.getActivityRecordById(recordId)?.toDomain()
    }

    override suspend fun insertActivityRecord(record: ActivityRecord) {
        activityRecordDao.insertActivityRecord(record.toEntity())
        firebaseService.syncActivityRecord(record)
    }

    override suspend fun updateActivityRecord(record: ActivityRecord) {
        activityRecordDao.updateActivityRecord(record.toEntity())
        firebaseService.syncActivityRecord(record)
    }

    override suspend fun deleteActivityRecord(record: ActivityRecord) {
        activityRecordDao.deleteActivityRecord(record.toEntity())
        firebaseService.deleteActivityRecord(record.id)
    }

    override suspend fun syncWithFirebase() {
        try {
            val unsyncedRecords = activityRecordDao.getUnsyncedRecords()
            unsyncedRecords.forEach { entity ->
                val record = entity.toDomain()
                firebaseService.syncActivityRecord(record)
                activityRecordDao.markAsSynced(record.id)
            }

            val remoteRecords = firebaseService.getAllActivityRecords()
            remoteRecords.forEach { record ->
                activityRecordDao.insertActivityRecord(record.toEntity())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}