package com.guicarneirodev.agrotask.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guicarneirodev.agrotask.core.util.IdGenerator
import com.guicarneirodev.agrotask.domain.sync.SyncEvent
import com.guicarneirodev.agrotask.domain.sync.SyncManager
import com.guicarneirodev.agrotask.domain.sync.SyncState
import com.guicarneirodev.agrotask.domain.model.ActivityRecord
import com.guicarneirodev.agrotask.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

class ActivityViewModel(
    private val activityRepository: ActivityRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _activityRecords = MutableStateFlow<List<ActivityRecord>>(emptyList())
    val activityRecords: StateFlow<List<ActivityRecord>> = _activityRecords.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentActivity = MutableStateFlow<ActivityFormState>(ActivityFormState())
    val currentActivity: StateFlow<ActivityFormState> = _currentActivity.asStateFlow()

    val syncState: StateFlow<SyncState> = syncManager.syncState

    private val _lastSyncEvent = MutableStateFlow<SyncEvent?>(null)
    val lastSyncEvent: StateFlow<SyncEvent?> = _lastSyncEvent.asStateFlow()

    init {
        loadActivityRecords()
        observeSyncEvents()
    }

    private fun observeSyncEvents() {
        viewModelScope.launch {
            syncManager.syncEvents.collect { event ->
                _lastSyncEvent.value = event
            }
        }
    }

    fun clearSyncEvent() {
        _lastSyncEvent.value = null
    }

    private fun loadActivityRecords() {
        viewModelScope.launch {
            activityRepository.getAllActivityRecords().collect { records ->
                _activityRecords.value = records
            }
        }
    }

    fun updateActivityType(type: String) {
        _currentActivity.value = _currentActivity.value.copy(activityType = type)
    }

    fun updateField(field: String) {
        _currentActivity.value = _currentActivity.value.copy(field = field)
    }

    fun updateStartTime(time: LocalDateTime) {
        _currentActivity.value = _currentActivity.value.copy(startTime = time)
    }

    fun updateEndTime(time: LocalDateTime) {
        _currentActivity.value = _currentActivity.value.copy(endTime = time)
    }

    fun updateObservations(observations: String) {
        _currentActivity.value = _currentActivity.value.copy(observations = observations)
    }

    @OptIn(ExperimentalTime::class)
    fun saveActivityRecord() {
        viewModelScope.launch {
            val state = _currentActivity.value
            if (!state.isValid()) return@launch

            _isLoading.value = true
            try {
                val record = ActivityRecord(
                    id = IdGenerator.generateId(),
                    activityType = state.activityType,
                    field = state.field,
                    startTime = state.startTime!!,
                    endTime = state.endTime!!,
                    observations = state.observations,
                    syncedWithFirebase = false,
                    createdAt = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                )

                activityRepository.insertActivityRecord(record)
                resetForm()

                viewModelScope.launch {
                    if (syncState.value.isOnline) {
                        syncManager.performFullSync()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun syncActivityRecords() {
        syncManager.performFullSync()
    }

    fun retrySync() {
        syncManager.retrySync()
    }

    fun resetForm() {
        _currentActivity.value = ActivityFormState()
    }

    override fun onCleared() {
        super.onCleared()
        syncManager.onCleared()
    }
}

data class ActivityFormState(
    val activityType: String = "",
    val field: String = "",
    val startTime: LocalDateTime? = null,
    val endTime: LocalDateTime? = null,
    val observations: String = ""
) {
    fun isValid(): Boolean {
        return activityType.isNotBlank() &&
                field.isNotBlank() &&
                startTime != null &&
                endTime != null
    }
}