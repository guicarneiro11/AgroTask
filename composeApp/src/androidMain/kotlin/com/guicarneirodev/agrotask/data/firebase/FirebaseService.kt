package com.guicarneirodev.agrotask.data.firebase

import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.guicarneirodev.agrotask.domain.model.ActivityRecord
import com.guicarneirodev.agrotask.domain.model.Task
import com.guicarneirodev.agrotask.domain.model.TaskStatus
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.LocalDateTime

class FirebaseService() {

    private var firestore: FirebaseFirestore? = null
    private val isFirebaseAvailable: Boolean
        get() = try {
            FirebaseApp.getInstance()
            true
        } catch (_: Exception) {
            false
        }

    private fun getFirestore(): FirebaseFirestore? {
        if (firestore == null && isFirebaseAvailable) {
            firestore = FirebaseFirestore.getInstance().apply {
                firestoreSettings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                    .build()
            }
        }
        return firestore
    }

    suspend fun syncTask(task: Task) {
        val db = getFirestore() ?: return

        try {
            val taskMap = hashMapOf(
                "id" to task.id,
                "activityName" to task.activityName,
                "field" to task.field,
                "scheduledTime" to task.scheduledTime.toString(),
                "status" to task.status.name,
                "syncedWithFirebase" to true,
                "createdAt" to task.createdAt.toString(),
                "updatedAt" to task.updatedAt.toString()
            )

            db.collection("tasks")
                .document(task.id)
                .set(taskMap)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getAllTasks(): List<Task> {
        val db = getFirestore() ?: return emptyList()

        return try {
            val snapshot = db.collection("tasks").get().await()

            snapshot.documents.mapNotNull { document ->
                try {
                    val data = document.data ?: return@mapNotNull null

                    Task(
                        id = data["id"] as? String ?: return@mapNotNull null,
                        activityName = data["activityName"] as? String ?: "",
                        field = data["field"] as? String ?: "",
                        scheduledTime = parseLocalDateTime(data["scheduledTime"] as? String),
                        status = parseTaskStatus(data["status"] as? String),
                        syncedWithFirebase = true,
                        createdAt = parseLocalDateTime(data["createdAt"] as? String),
                        updatedAt = parseLocalDateTime(data["updatedAt"] as? String)
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun deleteTask(taskId: String) {
        val db = getFirestore() ?: return

        try {
            db.collection("tasks")
                .document(taskId)
                .delete()
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun syncActivityRecord(record: ActivityRecord) {
        val db = getFirestore() ?: return

        try {
            val recordMap = hashMapOf(
                "id" to record.id,
                "activityType" to record.activityType,
                "field" to record.field,
                "startTime" to record.startTime.toString(),
                "endTime" to record.endTime.toString(),
                "observations" to record.observations,
                "syncedWithFirebase" to true,
                "createdAt" to record.createdAt.toString()
            )

            db.collection("activities")
                .document(record.id)
                .set(recordMap)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getAllActivityRecords(): List<ActivityRecord> {
        val db = getFirestore() ?: return emptyList()

        return try {
            val snapshot = db.collection("activities").get().await()

            snapshot.documents.mapNotNull { document ->
                try {
                    val data = document.data ?: return@mapNotNull null

                    ActivityRecord(
                        id = data["id"] as? String ?: return@mapNotNull null,
                        activityType = data["activityType"] as? String ?: "",
                        field = data["field"] as? String ?: "",
                        startTime = parseLocalDateTime(data["startTime"] as? String),
                        endTime = parseLocalDateTime(data["endTime"] as? String),
                        observations = data["observations"] as? String ?: "",
                        syncedWithFirebase = true,
                        createdAt = parseLocalDateTime(data["createdAt"] as? String)
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun deleteActivityRecord(recordId: String) {
        val db = getFirestore() ?: return

        try {
            db.collection("activities")
                .document(recordId)
                .delete()
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun parseLocalDateTime(dateTimeString: String?): LocalDateTime {
        if (dateTimeString == null) {
            return LocalDateTime(2024, 1, 1, 0, 0)
        }

        return try {
            val parts = dateTimeString.split("T")
            val dateParts = parts[0].split("-")
            val timeParts = parts.getOrNull(1)?.split(":") ?: listOf("00", "00")

            LocalDateTime(year = dateParts[0].toInt(),
                month = dateParts[1].toInt(),
                day = dateParts[2].toInt(),
                hour = timeParts[0].toInt(),
                minute = timeParts.getOrNull(1)?.substringBefore(".")?.toInt() ?: 0,
                second = 0, nanosecond = 0
            )
        } catch (_: Exception) {
            LocalDateTime(2024, 1, 1, 0, 0)
        }
    }

    private fun parseTaskStatus(statusString: String?): TaskStatus {
        return try {
            TaskStatus.valueOf(statusString ?: "PENDING")
        } catch (_: Exception) {
            TaskStatus.PENDING
        }
    }
}