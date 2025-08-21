package com.guicarneirodev.agrotask.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.guicarneirodev.agrotask.domain.model.TaskStatus

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val activityName: String,
    val field: String,
    val scheduledTime: Long,
    val status: TaskStatus,
    val syncedWithFirebase: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)