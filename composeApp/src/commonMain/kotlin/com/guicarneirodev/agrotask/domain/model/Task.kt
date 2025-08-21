package com.guicarneirodev.agrotask.domain.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: String,
    val activityName: String,
    val field: String,
    val scheduledTime: LocalDateTime,
    val status: TaskStatus,
    val syncedWithFirebase: Boolean = false,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

@Serializable
enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED
}