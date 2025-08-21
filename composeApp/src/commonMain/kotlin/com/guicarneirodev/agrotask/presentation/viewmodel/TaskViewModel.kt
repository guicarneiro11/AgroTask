package com.guicarneirodev.agrotask.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guicarneirodev.agrotask.core.util.IdGenerator
import com.guicarneirodev.agrotask.domain.model.Task
import com.guicarneirodev.agrotask.domain.model.TaskStatus
import com.guicarneirodev.agrotask.domain.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

class TaskViewModel(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _todayTasks = MutableStateFlow<List<Task>>(emptyList())
    val todayTasks: StateFlow<List<Task>> = _todayTasks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    init {
        loadTodayTasks()
        syncTasks()
    }

    private fun loadTodayTasks() {
        viewModelScope.launch {
            taskRepository.getTodayTasks().collect { tasks ->
                _todayTasks.value = tasks
            }
        }
    }

    fun updateTaskStatus(taskId: String, newStatus: TaskStatus) {
        viewModelScope.launch {
            try {
                taskRepository.updateTaskStatus(taskId, newStatus)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    fun createTask(
        activityName: String,
        field: String,
        scheduledTime: kotlinx.datetime.LocalDateTime
    ) {
        viewModelScope.launch {
            try {
                val now = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val task = Task(
                    id = IdGenerator.generateId(),
                    activityName = activityName,
                    field = field,
                    scheduledTime = scheduledTime,
                    status = TaskStatus.PENDING,
                    syncedWithFirebase = false,
                    createdAt = now,
                    updatedAt = now
                )
                taskRepository.insertTask(task)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun syncTasks() {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                taskRepository.syncWithFirebase()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSyncing.value = false
            }
        }
    }
}