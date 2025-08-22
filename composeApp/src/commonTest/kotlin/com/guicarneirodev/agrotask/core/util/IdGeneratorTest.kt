package com.guicarneirodev.agrotask.core.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class IdGeneratorTest {

    @Test
    fun `gerar IDs unicos em sequencia`() {
        val ids = mutableSetOf<String>()

        repeat(100) {
            ids.add(IdGenerator.generateId())
        }

        assertEquals(100, ids.size, "Todos os IDs devem ser únicos")
    }

    @Test
    fun `formato do ID deve conter timestamp e numero aleatorio`() {
        val id = IdGenerator.generateId()

        assertTrue(id.contains("-"), "ID deve conter separador")

        val parts = id.split("-")
        assertEquals(2, parts.size, "ID deve ter duas partes")

        val timestamp = parts[0].toLongOrNull()
        assertNotNull(timestamp, "Primeira parte deve ser timestamp")

        val random = parts[1].toIntOrNull()
        assertNotNull(random, "Segunda parte deve ser número")
        assertTrue(random in 100000..999999, "Número aleatório deve estar no range correto")
    }
}