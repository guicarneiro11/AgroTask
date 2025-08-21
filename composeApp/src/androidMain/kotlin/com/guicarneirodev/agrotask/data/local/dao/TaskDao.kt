package com.guicarneirodev.agrotask.data.local.dao

import androidx.room.*
import com.guicarneirodev.agrotask.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY scheduledTime ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("""
        SELECT * FROM tasks 
        WHERE scheduledTime >= :startOfDay 
        AND scheduledTime < :endOfDay 
        ORDER BY scheduledTime ASC
    """)
    fun getTasksByDateRange(startOfDay: Long, endOfDay: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)

    @Query("UPDATE tasks SET status = :status, updatedAt = :updatedAt WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: String, status: String, updatedAt: Long)

    @Query("UPDATE tasks SET syncedWithFirebase = 1 WHERE id = :taskId")
    suspend fun markAsSynced(taskId: String)

    @Query("SELECT * FROM tasks WHERE syncedWithFirebase = 0")
    suspend fun getUnsyncedTasks(): List<TaskEntity>
}