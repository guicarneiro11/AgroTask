package com.guicarneirodev.agrotask.domain.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class ActivityRecord(
    val id: String,
    val activityType: String,
    val field: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val observations: String,
    val syncedWithFirebase: Boolean = false,
    val createdAt: LocalDateTime
)