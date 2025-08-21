package com.guicarneirodev.agrotask.data

import com.guicarneirodev.agrotask.domain.model.Task
import com.guicarneirodev.agrotask.domain.model.TaskStatus
import kotlinx.datetime.*
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.number
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
object MockDataProvider {

    fun getInitialTasks(): List<Task> {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

        return listOf(
            Task(
                id = generateId(),
                activityName = "Irrigação do Milho",
                field = "Talhão A1",
                scheduledTime = LocalDateTime(now.year, now.month.number, now.day, 6, 0),
                status = TaskStatus.COMPLETED,
                syncedWithFirebase = false,
                createdAt = now,
                updatedAt = now
            ),
            Task(
                id = generateId(),
                activityName = "Aplicação de Fertilizante",
                field = "Talhão B2",
                scheduledTime = LocalDateTime(now.year, now.month.number, now.day, 9, 30),
                status = TaskStatus.IN_PROGRESS,
                syncedWithFirebase = false,
                createdAt = now,
                updatedAt = now
            ),
            Task(
                id = generateId(),
                activityName = "Monitoramento de Pragas",
                field = "Talhão C3",
                scheduledTime = LocalDateTime(now.year, now.month.number, now.day, 14, 0),
                status = TaskStatus.PENDING,
                syncedWithFirebase = false,
                createdAt = now,
                updatedAt = now
            ),
            Task(
                id = generateId(),
                activityName = "Preparo do Solo",
                field = "Talhão D4",
                scheduledTime = LocalDateTime(now.year, now.month.number, now.day, 16, 30),
                status = TaskStatus.PENDING,
                syncedWithFirebase = false,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    private fun generateId(): String {
        return Clock.System.now().toEpochMilliseconds().toString() +
                (1000..9999).random().toString()
    }
}