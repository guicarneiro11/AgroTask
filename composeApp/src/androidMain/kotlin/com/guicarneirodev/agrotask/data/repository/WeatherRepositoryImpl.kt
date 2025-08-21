package com.guicarneirodev.agrotask.data.repository

import com.guicarneirodev.agrotask.data.local.dao.WeatherCacheDao
import com.guicarneirodev.agrotask.data.local.mapper.toDomain
import com.guicarneirodev.agrotask.data.local.mapper.toEntity
import com.guicarneirodev.agrotask.data.location.LocationService
import com.guicarneirodev.agrotask.data.mapper.WeatherMapper
import com.guicarneirodev.agrotask.data.remote.WeatherApiService
import com.guicarneirodev.agrotask.domain.model.Weather
import com.guicarneirodev.agrotask.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.ExperimentalTime

class WeatherRepositoryImpl(
    private val weatherApiService: WeatherApiService,
    private val weatherCacheDao: WeatherCacheDao,
    private val locationService: LocationService,
    private val apiKey: String
) : WeatherRepository {

    override suspend fun getCurrentWeather(forceRefresh: Boolean): Weather {
        if (!forceRefresh) {
            val cachedWeather = weatherCacheDao.getWeatherCache()
            if (cachedWeather != null && isCacheValid(cachedWeather.lastUpdated)) {
                return cachedWeather.toDomain()
            }
        }

        return try {
            val location = locationService.getCurrentLocation()
            val lat = location?.latitude ?: -22.296933
            val lon = location?.longitude ?: -48.553894

            val response = weatherApiService.getCurrentWeather(lat, lon, apiKey)
            val weather = WeatherMapper.mapToDomain(response)
            weatherCacheDao.insertWeatherCache(weather.toEntity())
            weather
        } catch (e: Exception) {
            val cachedWeather = weatherCacheDao.getWeatherCache()
            cachedWeather?.toDomain() ?: throw e
        }
    }

    override fun getWeatherFlow(): Flow<Weather?> {
        return weatherCacheDao.getWeatherCacheFlow().map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun clearCache() {
        weatherCacheDao.clearCache()
    }

    @OptIn(ExperimentalTime::class)
    private fun isCacheValid(lastUpdated: Long): Boolean {
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
        val thirtyMinutesInMillis = 30 * 60 * 1000
        return (now - lastUpdated) < thirtyMinutesInMillis
    }
}