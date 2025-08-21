package com.guicarneirodev.agrotask.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey
    val id: Int = 1,
    val temperature: Double,
    val humidity: Int,
    val condition: String,
    val description: String,
    val iconUrl: String,
    val hourlyForecastJson: String,
    val lastUpdated: Long
)