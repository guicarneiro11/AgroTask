package com.guicarneirodev.agrotask.domain.repository

import com.guicarneirodev.agrotask.domain.model.Weather
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    suspend fun getCurrentWeather(forceRefresh: Boolean = false): Weather
    fun getWeatherFlow(): Flow<Weather?>
    suspend fun clearCache()
}