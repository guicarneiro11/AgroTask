package com.guicarneirodev.agrotask.domain.repository

import com.guicarneirodev.agrotask.domain.model.Task
import com.guicarneirodev.agrotask.domain.model.TaskStatus
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getAllTasks(): Flow<List<Task>>
    fun getTodayTasks(): Flow<List<Task>>
    suspend fun getTaskById(taskId: String): Task?
    suspend fun insertTask(task: Task)
    suspend fun updateTaskStatus(taskId: String, status: TaskStatus)
    suspend fun deleteTask(task: Task)
    suspend fun syncWithFirebase()
}