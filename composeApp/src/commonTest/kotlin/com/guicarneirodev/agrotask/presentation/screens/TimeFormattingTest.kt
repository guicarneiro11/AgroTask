package com.guicarneirodev.agrotask.presentation.screens

import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class TimeFormattingTest {

    @Test
    fun `formatTime deve retornar hora e minuto com zero padding`() {
        val dateTime1 = LocalDateTime(2025, 8, 20, 9, 5)
        val dateTime2 = LocalDateTime(2025, 8, 20, 14, 30)
        val dateTime3 = LocalDateTime(2025, 8, 20, 0, 0)

        assertEquals("09:05", formatTime(dateTime1))
        assertEquals("14:30", formatTime(dateTime2))
        assertEquals("00:00", formatTime(dateTime3))
    }

    @Test
    fun `formatActivityTime deve retornar hora e minuto formatados`() {
        val morning = LocalDateTime(2025, 8, 20, 6, 15)
        val afternoon = LocalDateTime(2025, 8, 20, 15, 45)
        val night = LocalDateTime(2025, 8, 20, 23, 59)

        assertEquals("06:15", formatActivityTime(morning))
        assertEquals("15:45", formatActivityTime(afternoon))
        assertEquals("23:59", formatActivityTime(night))
    }
}