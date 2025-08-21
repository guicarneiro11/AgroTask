package com.guicarneirodev.agrotask.data.local.dao

import androidx.room.*
import com.guicarneirodev.agrotask.data.local.entity.WeatherCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherCacheDao {

    @Query("SELECT * FROM weather_cache WHERE id = 1")
    suspend fun getWeatherCache(): WeatherCacheEntity?

    @Query("SELECT * FROM weather_cache WHERE id = 1")
    fun getWeatherCacheFlow(): Flow<WeatherCacheEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherCache(weather: WeatherCacheEntity)

    @Query("DELETE FROM weather_cache")
    suspend fun clearCache()
}