package com.guicarneirodev.agrotask.data.repository

import com.guicarneirodev.agrotask.data.firebase.FirebaseService
import com.guicarneirodev.agrotask.data.local.dao.TaskDao
import com.guicarneirodev.agrotask.data.local.mapper.toDomain
import com.guicarneirodev.agrotask.data.local.mapper.toEntity
import com.guicarneirodev.agrotask.domain.model.Task
import com.guicarneirodev.agrotask.domain.model.TaskStatus
import com.guicarneirodev.agrotask.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.*
import kotlin.time.ExperimentalTime

class TaskRepositoryImpl(
    private val taskDao: TaskDao,
    private val firebaseService: FirebaseService
) : TaskRepository {

    override fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    @OptIn(ExperimentalTime::class)
    override fun getTodayTasks(): Flow<List<Task>> {
        val timeZone = TimeZone.currentSystemDefault()
        val now = kotlin.time.Clock.System.now()
        val todayStart = now.toLocalDateTime(timeZone).date.atStartOfDayIn(timeZone)
        val tomorrowStart = todayStart.plus(1, DateTimeUnit.DAY, timeZone)

        return taskDao.getTasksByDateRange(
            startOfDay = todayStart.toEpochMilliseconds(),
            endOfDay = tomorrowStart.toEpochMilliseconds()
        ).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTaskById(taskId: String): Task? {
        return taskDao.getTaskById(taskId)?.toDomain()
    }

    override suspend fun insertTask(task: Task) {
        taskDao.insertTask(task.toEntity())
        firebaseService.syncTask(task)
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun updateTaskStatus(taskId: String, status: TaskStatus) {
        val updatedAt = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        taskDao.updateTaskStatus(
            taskId = taskId,
            status = status.name,
            updatedAt = updatedAt.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        )

        getTaskById(taskId)?.let { task ->
            firebaseService.syncTask(task.copy(status = status, updatedAt = updatedAt))
        }
    }

    override suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task.toEntity())
        firebaseService.deleteTask(task.id)
    }

    override suspend fun deleteTaskById(taskId: String) {
        getTaskById(taskId)?.let { task ->
            deleteTask(task)
        }
    }

    override suspend fun syncWithFirebase() {
        try {
            val unsyncedTasks = taskDao.getUnsyncedTasks()
            unsyncedTasks.forEach { entity ->
                val task = entity.toDomain()
                firebaseService.syncTask(task)
                taskDao.markAsSynced(task.id)
            }

            val remoteTasks = firebaseService.getAllTasks()
            taskDao.insertTasks(remoteTasks.map { it.toEntity() })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}