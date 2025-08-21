package com.guicarneirodev.agrotask.data.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.guicarneirodev.agrotask.domain.permissions.PermissionHandler

class AndroidPermissionHandler(
    private val context: Context
) : PermissionHandler {

    override suspend fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override suspend fun requestLocationPermission(): Boolean {
        return checkLocationPermission()
    }
}