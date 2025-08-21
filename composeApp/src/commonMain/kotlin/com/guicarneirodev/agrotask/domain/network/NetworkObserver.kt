package com.guicarneirodev.agrotask.domain.network

import kotlinx.coroutines.flow.Flow

interface NetworkObserver {
    fun observe(): Flow<NetworkStatus>
}

enum class NetworkStatus {
    Available,
    Unavailable,
    Losing,
    Lost
}

fun NetworkStatus.isConnected(): Boolean = this == NetworkStatus.Available