package com.guicarneirodev.agrotask.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_records")
data class ActivityRecordEntity(
    @PrimaryKey
    val id: String,
    val activityType: String,
    val field: String,
    val startTime: Long,
    val endTime: Long,
    val observations: String,
    val syncedWithFirebase: Boolean,
    val createdAt: Long
)