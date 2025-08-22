package com.guicarneirodev.agrotask.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.guicarneirodev.agrotask.domain.network.NetworkObserver
import com.guicarneirodev.agrotask.domain.network.NetworkStatus
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.net.InetSocketAddress
import java.net.Socket

class AndroidNetworkObserver(
    context: Context
) : NetworkObserver {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun observe(): Flow<NetworkStatus> = callbackFlow {
        var activeNetworkCheckJob: Job? = null

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                activeNetworkCheckJob?.cancel()
                activeNetworkCheckJob = scope.launch {
                    val hasInternet = checkInternetConnection()
                    if (hasInternet) {
                        trySend(NetworkStatus.Available)
                    } else {
                        trySend(NetworkStatus.Unavailable)
                    }
                }
            }

            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                super.onCapabilitiesChanged(network, capabilities)
                activeNetworkCheckJob?.cancel()
                activeNetworkCheckJob = scope.launch {
                    val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) &&
                            checkInternetConnection()

                    if (hasInternet) {
                        trySend(NetworkStatus.Available)
                    } else {
                        trySend(NetworkStatus.Losing)
                    }
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                activeNetworkCheckJob?.cancel()
                trySend(NetworkStatus.Lost)
            }

            override fun onUnavailable() {
                super.onUnavailable()
                activeNetworkCheckJob?.cancel()
                trySend(NetworkStatus.Unavailable)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        scope.launch {
            val initialStatus = getCurrentNetworkStatus()
            trySend(initialStatus)
        }

        connectivityManager.registerNetworkCallback(request, callback)

        val periodicCheckJob = scope.launch {
            while (isActive) {
                delay(5000)
                val currentStatus = getCurrentNetworkStatus()
                trySend(currentStatus)
            }
        }

        awaitClose {
            activeNetworkCheckJob?.cancel()
            periodicCheckJob.cancel()
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()

    private suspend fun getCurrentNetworkStatus(): NetworkStatus {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        return when {
            network == null || capabilities == null -> NetworkStatus.Unavailable
            !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> NetworkStatus.Unavailable
            else -> {
                if (checkInternetConnection()) {
                    NetworkStatus.Available
                } else {
                    NetworkStatus.Unavailable
                }
            }
        }
    }

    private suspend fun checkInternetConnection(): Boolean = withContext(Dispatchers.IO) {
        try {
            val socket = Socket()
            val socketAddress = InetSocketAddress("8.8.8.8", 53)
            socket.connect(socketAddress, 2000)
            socket.close()
            true
        } catch (_: Exception) {
            try {
                val socket = Socket()
                val socketAddress = InetSocketAddress("1.1.1.1", 53)
                socket.connect(socketAddress, 2000)
                socket.close()
                true
            } catch (_: Exception) {
                false
            }
        }
    }
}