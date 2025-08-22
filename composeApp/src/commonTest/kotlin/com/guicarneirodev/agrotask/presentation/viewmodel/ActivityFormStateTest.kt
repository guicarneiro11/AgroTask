package com.guicarneirodev.agrotask.presentation.viewmodel

import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ActivityFormStateTest {

    @Test
    fun `formulario valido quando todos campos obrigatorios preenchidos`() {
        val form = ActivityFormState(
            activityType = "Plantio",
            field = "Talhão A1",
            startTime = LocalDateTime(2024, 8, 20, 8, 0),
            endTime = LocalDateTime(2024, 8, 20, 12, 0),
            observations = "Teste"
        )

        assertTrue(form.isValid())
    }

    @Test
    fun `formulario invalido quando falta tipo de atividade`() {
        val form = ActivityFormState(
            activityType = "",
            field = "Talhão A1",
            startTime = LocalDateTime(2024, 8, 20, 8, 0),
            endTime = LocalDateTime(2024, 8, 20, 12, 0)
        )

        assertFalse(form.isValid())
    }

    @Test
    fun `formulario invalido quando falta horario`() {
        val form = ActivityFormState(
            activityType = "Plantio",
            field = "Talhão A1",
            startTime = null,
            endTime = LocalDateTime(2024, 8, 20, 12, 0)
        )

        assertFalse(form.isValid())
    }

    @Test
    fun `observacoes sao opcionais para validacao`() {
        val form = ActivityFormState(
            activityType = "Plantio",
            field = "Talhão A1",
            startTime = LocalDateTime(2024, 8, 20, 8, 0),
            endTime = LocalDateTime(2024, 8, 20, 12, 0),
            observations = ""
        )

        assertTrue(form.isValid())
    }
}