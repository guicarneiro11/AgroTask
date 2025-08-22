package com.guicarneirodev.agrotask.domain.model

import com.guicarneirodev.agrotask.presentation.screens.getStatusColor
import com.guicarneirodev.agrotask.presentation.screens.getStatusText
import com.guicarneirodev.agrotask.presentation.theme.Amber60
import com.guicarneirodev.agrotask.presentation.theme.Blue40
import com.guicarneirodev.agrotask.presentation.theme.Green60
import kotlin.test.Test
import kotlin.test.assertEquals

class TaskStatusTest {
    @Test
    fun `todos os status devem ter texto em portugues`() {
        assertEquals("Pendente", getStatusText(TaskStatus.PENDING))
        assertEquals("Em Andamento", getStatusText(TaskStatus.IN_PROGRESS))
        assertEquals("Concluída", getStatusText(TaskStatus.COMPLETED))
    }

    @Test
    fun `todos os status devem ter cor associada`() {
        assertEquals(Amber60, getStatusColor(TaskStatus.PENDING))
        assertEquals(Blue40, getStatusColor(TaskStatus.IN_PROGRESS))
        assertEquals(Green60, getStatusColor(TaskStatus.COMPLETED))
    }

    @Test
    fun `enum deve ter exatamente 3 estados`() {
        assertEquals(3, TaskStatus.entries.size)
    }
}