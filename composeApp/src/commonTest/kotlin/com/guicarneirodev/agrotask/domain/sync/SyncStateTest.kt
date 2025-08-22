package com.guicarneirodev.agrotask.domain.sync

import com.guicarneirodev.agrotask.domain.network.NetworkStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SyncStateTest {

    @Test
    fun `estado inicial deve ter valores padrao corretos`() {
        val state = SyncState()

        assertFalse(state.isSyncing)
        assertEquals(0f, state.syncProgress)
        assertTrue(state.isOnline)
        assertEquals(NetworkStatus.Available, state.networkStatus)
        assertEquals(null, state.lastSyncTime)
        assertEquals(null, state.lastSyncError)
    }

    @Test
    fun `estado offline deve ter network status correto`() {
        val state = SyncState(
            isOnline = false,
            networkStatus = NetworkStatus.Unavailable
        )

        assertFalse(state.isOnline)
        assertEquals(NetworkStatus.Unavailable, state.networkStatus)
    }

    @Test
    fun `progresso de sincronizacao deve estar entre 0 e 1`() {
        val validStates = listOf(
            SyncState(syncProgress = 0f),
            SyncState(syncProgress = 0.5f),
            SyncState(syncProgress = 1f)
        )

        validStates.forEach { state ->
            assertTrue(state.syncProgress in 0f..1f)
        }
    }
}