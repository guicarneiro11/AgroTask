package com.guicarneirodev.agrotask.domain.sync

import app.cash.turbine.test
import com.guicarneirodev.agrotask.domain.network.NetworkObserver
import com.guicarneirodev.agrotask.domain.network.NetworkStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class FakeSyncTaskRepository : com.guicarneirodev.agrotask.domain.repository.TaskRepository {
    var syncCalls = 0

    override fun getAllTasks() = kotlinx.coroutines.flow.flowOf(emptyList<com.guicarneirodev.agrotask.domain.model.Task>())
    override fun getTodayTasks() = kotlinx.coroutines.flow.flowOf(emptyList<com.guicarneirodev.agrotask.domain.model.Task>())
    override suspend fun getTaskById(taskId: String) = null
    override suspend fun insertTask(task: com.guicarneirodev.agrotask.domain.model.Task) {}
    override suspend fun updateTaskStatus(taskId: String, status: com.guicarneirodev.agrotask.domain.model.TaskStatus) {}
    override suspend fun deleteTask(task: com.guicarneirodev.agrotask.domain.model.Task) {}
    override suspend fun deleteTaskById(taskId: String) {}
    override suspend fun syncWithFirebase() {
        syncCalls++
    }
}

class FakeSyncActivityRepository : com.guicarneirodev.agrotask.domain.repository.ActivityRepository {
    var syncCalls = 0

    override fun getAllActivityRecords() = kotlinx.coroutines.flow.flowOf(emptyList<com.guicarneirodev.agrotask.domain.model.ActivityRecord>())
    override suspend fun getActivityRecordById(recordId: String) = null
    override suspend fun insertActivityRecord(record: com.guicarneirodev.agrotask.domain.model.ActivityRecord) {}
    override suspend fun updateActivityRecord(record: com.guicarneirodev.agrotask.domain.model.ActivityRecord) {}
    override suspend fun deleteActivityRecord(record: com.guicarneirodev.agrotask.domain.model.ActivityRecord) {}
    override suspend fun syncWithFirebase() {
        syncCalls++
    }
}

class FakeSyncNetworkObserver : NetworkObserver {
    val networkFlow = MutableStateFlow(NetworkStatus.Available)
    override fun observe(): Flow<NetworkStatus> = networkFlow
}

class SyncManagerTest {

    @Test
    fun `sincronizacao nao deve ocorrer quando offline`() = runTest {
        val taskRepo = FakeSyncTaskRepository()
        val activityRepo = FakeSyncActivityRepository()
        val network = FakeSyncNetworkObserver()
        network.networkFlow.value = NetworkStatus.Unavailable

        val syncManager = SyncManager(taskRepo, activityRepo, network)

        syncManager.syncState.test {
            skipItems(1)
            syncManager.performFullSync()
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(0, taskRepo.syncCalls)
        assertEquals(0, activityRepo.syncCalls)
    }

    @Test
    fun `mudanca de rede deve atualizar estado`() = runTest {
        val taskRepo = FakeSyncTaskRepository()
        val activityRepo = FakeSyncActivityRepository()
        val network = FakeSyncNetworkObserver()

        val syncManager = SyncManager(taskRepo, activityRepo, network)

        syncManager.syncState.test {
            val initial = awaitItem()
            assertTrue(initial.isOnline)

            network.networkFlow.value = NetworkStatus.Lost
            val lostState = awaitItem()
            assertFalse(lostState.isOnline)

            network.networkFlow.value = NetworkStatus.Available
            val availableState = awaitItem()
            assertTrue(availableState.isOnline)
        }
    }
}