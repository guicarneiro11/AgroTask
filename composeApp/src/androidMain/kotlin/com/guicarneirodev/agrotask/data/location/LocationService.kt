package com.guicarneirodev.agrotask.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

class LocationService(private val context: Context) {

    data class Coordinates(
        val latitude: Double,
        val longitude: Double
    )

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Coordinates? {
        if (!hasLocationPermission()) {
            return null
        }

        val fusedLocation = tryGetFusedLocation()
        if (fusedLocation != null) return fusedLocation

        val managerLocation = tryGetManagerLocation()
        if (managerLocation != null) return managerLocation

        return Coordinates(
            latitude = -22.2906,
            longitude = -48.5592
        )
    }

    @SuppressLint("MissingPermission")
    private suspend fun tryGetFusedLocation(): Coordinates? {
        return withTimeoutOrNull(5000) {
            suspendCancellableCoroutine { continuation ->
                try {
                    val cancellationToken = CancellationTokenSource()

                    fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        cancellationToken.token
                    ).addOnSuccessListener { location ->
                        if (location != null) {
                            continuation.resume(
                                Coordinates(location.latitude, location.longitude)
                            )
                        } else {
                            continuation.resume(null)
                        }
                    }.addOnFailureListener {
                        continuation.resume(null)
                    }

                    continuation.invokeOnCancellation {
                        cancellationToken.cancel()
                    }
                } catch (_: Exception) {
                    continuation.resume(null)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun tryGetManagerLocation(): Coordinates? {
        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            val providers = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
            )

            for (provider in providers) {
                if (locationManager.isProviderEnabled(provider)) {
                    val location = locationManager.getLastKnownLocation(provider)
                    if (location != null && location.latitude != 0.0) {
                        return Coordinates(location.latitude, location.longitude)
                    }
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }
}