package com.guicarneirodev.agrotask.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guicarneirodev.agrotask.core.util.IdGenerator
import com.guicarneirodev.agrotask.domain.sync.SyncEvent
import com.guicarneirodev.agrotask.domain.sync.SyncManager
import com.guicarneirodev.agrotask.domain.sync.SyncState
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
    private val taskRepository: TaskRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _todayTasks = MutableStateFlow<List<Task>>(emptyList())
    val todayTasks: StateFlow<List<Task>> = _todayTasks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val syncState: StateFlow<SyncState> = syncManager.syncState

    private val _lastSyncEvent = MutableStateFlow<SyncEvent?>(null)
    val lastSyncEvent: StateFlow<SyncEvent?> = _lastSyncEvent.asStateFlow()

    init {
        loadTodayTasks()
        observeSyncEvents()
        syncManager.performFullSync()
    }

    private fun observeSyncEvents() {
        viewModelScope.launch {
            syncManager.syncEvents.collect { event ->
                _lastSyncEvent.value = event
            }
        }
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
                if (syncState.value.isOnline) {
                    syncManager.performFullSync()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    fun createTask(
        activityName: String,
        field: String,
        scheduledTime: kotlinx.datetime.LocalDateTime,
        initialStatus: TaskStatus = TaskStatus.PENDING
    ) {
        viewModelScope.launch {
            try {
                val now = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val task = Task(
                    id = IdGenerator.generateId(),
                    activityName = activityName,
                    field = field,
                    scheduledTime = scheduledTime,
                    status = initialStatus,
                    syncedWithFirebase = false,
                    createdAt = now,
                    updatedAt = now
                )
                taskRepository.insertTask(task)
                if (syncState.value.isOnline) {
                    syncManager.performFullSync()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            try {
                taskRepository.deleteTaskById(taskId)
                if (syncState.value.isOnline) {
                    syncManager.performFullSync()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun syncTasks() {
        syncManager.performFullSync()
    }

    fun retrySync() {
        syncManager.retrySync()
    }

    override fun onCleared() {
        super.onCleared()
        syncManager.onCleared()
    }
}