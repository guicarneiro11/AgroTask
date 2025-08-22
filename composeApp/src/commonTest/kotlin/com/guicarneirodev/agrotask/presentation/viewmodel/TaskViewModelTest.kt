package com.guicarneirodev.agrotask.presentation.viewmodel

import com.guicarneirodev.agrotask.domain.model.Task
import com.guicarneirodev.agrotask.domain.model.TaskStatus
import com.guicarneirodev.agrotask.domain.network.NetworkObserver
import com.guicarneirodev.agrotask.domain.network.NetworkStatus
import com.guicarneirodev.agrotask.domain.repository.ActivityRepository
import com.guicarneirodev.agrotask.domain.repository.TaskRepository
import com.guicarneirodev.agrotask.domain.sync.SyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import kotlinx.datetime.LocalDateTime
import kotlin.test.*

class FakeTaskRepository : TaskRepository {
    var updateStatusCalls = mutableListOf<Pair<String, TaskStatus>>()
    var insertedTasks = mutableListOf<Task>()

    override fun getAllTasks(): Flow<List<Task>> = flowOf(emptyList())
    override fun getTodayTasks(): Flow<List<Task>> = flowOf(emptyList())
    override suspend fun getTaskById(taskId: String): Task? = null
    override suspend fun insertTask(task: Task) {
        insertedTasks.add(task)
    }
    override suspend fun updateTaskStatus(taskId: String, status: TaskStatus) {
        updateStatusCalls.add(taskId to status)
    }
    override suspend fun deleteTask(task: Task) {}
    override suspend fun deleteTaskById(taskId: String) {}
    override suspend fun syncWithFirebase() {}
}

class FakeActivityRepository : ActivityRepository {
    override fun getAllActivityRecords() = flowOf(emptyList<com.guicarneirodev.agrotask.domain.model.ActivityRecord>())
    override suspend fun getActivityRecordById(recordId: String) = null
    override suspend fun insertActivityRecord(record: com.guicarneirodev.agrotask.domain.model.ActivityRecord) {}
    override suspend fun updateActivityRecord(record: com.guicarneirodev.agrotask.domain.model.ActivityRecord) {}
    override suspend fun deleteActivityRecord(record: com.guicarneirodev.agrotask.domain.model.ActivityRecord) {}
    override suspend fun syncWithFirebase() {}
}

class FakeNetworkObserver : NetworkObserver {
    val networkFlow = MutableStateFlow(NetworkStatus.Available)
    override fun observe(): Flow<NetworkStatus> = networkFlow
}

@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `quando criar tarefa deve inserir no repositorio`() = runTest {
        val fakeRepo = FakeTaskRepository()
        val fakeActivityRepo = FakeActivityRepository()
        val fakeNetwork = FakeNetworkObserver()
        val syncManager = SyncManager(fakeRepo, fakeActivityRepo, fakeNetwork)
        val viewModel = TaskViewModel(fakeRepo, syncManager)

        val activityName = "Plantio de Milho"
        val field = "Talhão A1"
        val scheduledTime = LocalDateTime(2024, 8, 20, 10, 0)

        viewModel.createTask(activityName, field, scheduledTime, TaskStatus.PENDING)

        advanceUntilIdle()

        assertEquals(1, fakeRepo.insertedTasks.size)
        val insertedTask = fakeRepo.insertedTasks.first()
        assertEquals(activityName, insertedTask.activityName)
        assertEquals(field, insertedTask.field)
        assertEquals(TaskStatus.PENDING, insertedTask.status)
    }

    @Test
    fun `quando atualizar status deve chamar repository`() = runTest {
        val fakeRepo = FakeTaskRepository()
        val fakeActivityRepo = FakeActivityRepository()
        val fakeNetwork = FakeNetworkObserver()
        val syncManager = SyncManager(fakeRepo, fakeActivityRepo, fakeNetwork)
        val viewModel = TaskViewModel(fakeRepo, syncManager)

        val taskId = "task-123"
        val newStatus = TaskStatus.COMPLETED

        viewModel.updateTaskStatus(taskId, newStatus)

        advanceUntilIdle()

        assertEquals(1, fakeRepo.updateStatusCalls.size)
        val (calledId, calledStatus) = fakeRepo.updateStatusCalls.first()
        assertEquals(taskId, calledId)
        assertEquals(newStatus, calledStatus)
    }
}