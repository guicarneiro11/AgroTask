package com.guicarneirodev.agrotask.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationService(private val context: Context) {

    data class Coordinates(
        val latitude: Double,
        val longitude: Double
    )

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Coordinates? {
        if (!hasLocationPermission()) {
            return Coordinates(-22.296933, -48.553894)
        }

        return suspendCancellableCoroutine { continuation ->
            try {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

                val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                if (lastKnownLocation != null) {
                    continuation.resume(
                        Coordinates(
                            lastKnownLocation.latitude,
                            lastKnownLocation.longitude
                        )
                    )
                } else {
                    continuation.resume(Coordinates(-22.296933, -48.553894))
                }
            } catch (_: Exception) {
                continuation.resume(Coordinates(-22.296933, -48.553894))
            }
        }
    }
}