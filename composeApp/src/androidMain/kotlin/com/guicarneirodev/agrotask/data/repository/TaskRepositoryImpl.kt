package com.guicarneirodev.agrotask.data.repository

import com.guicarneirodev.agrotask.data.MockDataProvider
import com.guicarneirodev.agrotask.data.firebase.FirebaseService
import com.guicarneirodev.agrotask.data.local.dao.TaskDao
import com.guicarneirodev.agrotask.data.local.mapper.toDomain
import com.guicarneirodev.agrotask.data.local.mapper.toEntity
import com.guicarneirodev.agrotask.domain.model.Task
import com.guicarneirodev.agrotask.domain.model.TaskStatus
import com.guicarneirodev.agrotask.domain.repository.TaskRepository
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

@OptIn(DelicateCoroutinesApi::class)
class TaskRepositoryImpl(
    private val taskDao: TaskDao,
    private val firebaseService: FirebaseService
) : TaskRepository {

    init {
        GlobalScope.launch {
            initializeWithMockDataIfNeeded()
        }
    }

    private suspend fun initializeWithMockDataIfNeeded() {
        try {
            val tasks = taskDao.getAllTasks().first()
            if (tasks.isEmpty()) {
                MockDataProvider.getInitialTasks().forEach { task ->
                    taskDao.insertTask(task.toEntity())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTodayTasks(): Flow<List<Task>> {
        return taskDao.getTodayTasks().map { entities ->
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

    override suspend fun syncWithFirebase() {
        try {
            val remoteTasks = firebaseService.getAllTasks()
            taskDao.insertTasks(remoteTasks.map { it.toEntity() })

            remoteTasks.forEach { task ->
                taskDao.markAsSynced(task.id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}