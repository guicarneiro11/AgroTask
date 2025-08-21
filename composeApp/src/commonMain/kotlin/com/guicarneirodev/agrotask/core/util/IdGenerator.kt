package com.guicarneirodev.agrotask.core.util

import kotlin.time.ExperimentalTime

object IdGenerator {
    @OptIn(ExperimentalTime::class)
    fun generateId(): String {
        val timestamp = kotlin.time.Clock.System.now().toEpochMilliseconds()
        val random = (100000..999999).random()
        return "$timestamp-$random"
    }
}