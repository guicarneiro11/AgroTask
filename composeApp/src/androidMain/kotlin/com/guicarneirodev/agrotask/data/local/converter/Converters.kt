package com.guicarneirodev.agrotask.data.local.converter

import androidx.room.TypeConverter
import com.guicarneirodev.agrotask.domain.model.TaskStatus

class Converters {

    @TypeConverter
    fun fromTaskStatus(status: TaskStatus): String {
        return status.name
    }

    @TypeConverter
    fun toTaskStatus(status: String): TaskStatus {
        return TaskStatus.valueOf(status)
    }
}