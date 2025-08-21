package com.guicarneirodev.agrotask.domain.permissions

interface PermissionHandler {
    suspend fun checkLocationPermission(): Boolean
    suspend fun requestLocationPermission(): Boolean
}