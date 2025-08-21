package com.guicarneirodev.agrotask.domain.sync

import com.guicarneirodev.agrotask.domain.network.NetworkObserver
import com.guicarneirodev.agrotask.domain.network.NetworkStatus
import com.guicarneirodev.agrotask.domain.repository.ActivityRepository
import com.guicarneirodev.agrotask.domain.repository.TaskRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class SyncManager(
    private val taskRepository: TaskRepository,
    private val activityRepository: ActivityRepository,
    private val networkObserver: NetworkObserver
) {
    companion object {
        private const val SYNC_TIMEOUT = 8000L
        private const val NETWORK_STABILIZATION_DELAY = 1500L
    }

    private val _syncState = MutableStateFlow(SyncState())
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _syncEvents = MutableSharedFlow<SyncEvent>()
    val syncEvents: SharedFlow<SyncEvent> = _syncEvents.asSharedFlow()

    private var syncJob: Job? = null
    private var networkJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        observeNetworkChanges()
    }

    private fun observeNetworkChanges() {
        networkJob?.cancel()
        networkJob = scope.launch {
            networkObserver.observe()
                .distinctUntilChanged()
                .collect { status ->
                    val wasOnline = _syncState.value.isOnline
                    val isOnlineNow = status == NetworkStatus.Available

                    _syncState.update { it.copy(
                        isOnline = isOnlineNow,
                        networkStatus = status
                    ) }

                    when {
                        wasOnline && !isOnlineNow -> {
                            syncJob?.cancel()
                            _syncState.update { it.copy(
                                isSyncing = false,
                                syncProgress = 0f
                            ) }
                            _syncEvents.emit(SyncEvent.NetworkLost)
                        }

                        !wasOnline && isOnlineNow -> {
                            _syncState.update { it.copy(lastSyncError = null) }
                            _syncEvents.emit(SyncEvent.NetworkRestored)

                            delay(NETWORK_STABILIZATION_DELAY)

                            if (_syncState.value.isOnline) {
                                performFullSync()
                            }
                        }

                        status == NetworkStatus.Losing -> {
                            _syncEvents.emit(SyncEvent.NetworkLosing)
                        }
                    }
                }
        }
    }

    fun performFullSync() {
        if (syncJob?.isActive == true) return

        if (!_syncState.value.isOnline) {
            scope.launch {
                _syncEvents.emit(SyncEvent.SyncFailedNoNetwork)
            }
            return
        }

        syncJob = scope.launch {
            _syncState.update { it.copy(
                isSyncing = true,
                syncProgress = 0f,
                lastSyncError = null
            ) }
            _syncEvents.emit(SyncEvent.SyncStarted)

            try {
                withTimeout(SYNC_TIMEOUT) {
                    if (!_syncState.value.isOnline) {
                        throw Exception("Sem conexão com a internet")
                    }

                    _syncState.update { it.copy(syncProgress = 0.25f) }
                    delay(300)

                    ensureActive()
                    if (!_syncState.value.isOnline) {
                        throw Exception("Conexão perdida durante sincronização")
                    }

                    try {
                        taskRepository.syncWithFirebase()
                    } catch (e: Exception) {
                        if (!_syncState.value.isOnline) {
                            throw Exception("Sem conexão com a internet")
                        }
                        throw e
                    }

                    _syncState.update { it.copy(syncProgress = 0.5f) }
                    delay(300)

                    ensureActive()
                    if (!_syncState.value.isOnline) {
                        throw Exception("Conexão perdida durante sincronização")
                    }

                    try {
                        activityRepository.syncWithFirebase()
                    } catch (e: Exception) {
                        if (!_syncState.value.isOnline) {
                            throw Exception("Sem conexão com a internet")
                        }
                        throw e
                    }

                    _syncState.update { it.copy(syncProgress = 0.75f) }
                    delay(300)

                    if (!_syncState.value.isOnline) {
                        throw Exception("Conexão perdida ao finalizar sincronização")
                    }

                    _syncState.update { it.copy(syncProgress = 1f) }
                    delay(200)
                }

                if (_syncState.value.isOnline) {
                    _syncState.update { it.copy(
                        isSyncing = false,
                        syncProgress = 0f,
                        lastSyncTime = getCurrentTimeMillis(),
                        lastSyncError = null
                    ) }
                    _syncEvents.emit(SyncEvent.SyncCompleted)
                } else {
                    throw Exception("Conexão perdida")
                }

            } catch (e: TimeoutCancellationException) {
                _syncState.update { it.copy(
                    isSyncing = false,
                    syncProgress = 0f,
                    lastSyncError = if (_syncState.value.isOnline)
                        "Tempo esgotado - servidor não respondeu"
                    else "Sem conexão com a internet"
                ) }

                if (!_syncState.value.isOnline) {
                    _syncEvents.emit(SyncEvent.SyncFailedNoNetwork)
                } else {
                    _syncEvents.emit(SyncEvent.SyncFailed("Tempo esgotado"))
                }

            } catch (e: CancellationException) {
                _syncState.update { it.copy(
                    isSyncing = false,
                    syncProgress = 0f
                ) }

                if (!_syncState.value.isOnline) {
                    _syncEvents.emit(SyncEvent.NetworkLost)
                }

            } catch (e: Exception) {
                val errorMessage = when {
                    !_syncState.value.isOnline -> "Sem conexão com a internet"
                    e.message?.contains("internet", ignoreCase = true) == true -> "Sem conexão com a internet"
                    e.message?.contains("network", ignoreCase = true) == true -> "Erro de rede"
                    else -> e.message ?: "Erro desconhecido"
                }

                _syncState.update { it.copy(
                    isSyncing = false,
                    syncProgress = 0f,
                    lastSyncError = errorMessage
                ) }

                if (!_syncState.value.isOnline) {
                    _syncEvents.emit(SyncEvent.SyncFailedNoNetwork)
                } else {
                    _syncEvents.emit(SyncEvent.SyncFailed(errorMessage))
                }
            }
        }
    }

    fun retrySync() {
        _syncState.update { it.copy(lastSyncError = null) }
        performFullSync()
    }

    fun cancelSync() {
        syncJob?.cancel()
        _syncState.update { it.copy(
            isSyncing = false,
            syncProgress = 0f
        ) }
    }

    fun onCleared() {
        scope.cancel()
    }

    @OptIn(ExperimentalTime::class)
    private fun getCurrentTimeMillis(): Long {
        return Clock.System.now().toEpochMilliseconds()
    }
}

data class SyncState(
    val isSyncing: Boolean = false,
    val syncProgress: Float = 0f,
    val isOnline: Boolean = true,
    val networkStatus: NetworkStatus = NetworkStatus.Available,
    val lastSyncTime: Long? = null,
    val lastSyncError: String? = null
)

sealed class SyncEvent {
    object SyncStarted : SyncEvent()
    object SyncCompleted : SyncEvent()
    data class SyncFailed(val error: String) : SyncEvent()
    object NetworkRestored : SyncEvent()
    object NetworkLost : SyncEvent()
    object NetworkLosing : SyncEvent()
    object SyncFailedNoNetwork : SyncEvent()
}