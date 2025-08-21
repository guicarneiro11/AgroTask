package com.guicarneirodev.agrotask.domain.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Weather(
    val temperature: Double,
    val humidity: Int,
    val condition: WeatherCondition,
    val description: String,
    val iconUrl: String,
    val hourlyForecast: List<HourlyForecast>,
    val isFromCache: Boolean = false,
    val lastUpdated: LocalDateTime
)

@Serializable
data class HourlyForecast(
    val time: LocalDateTime,
    val temperature: Double,
    val condition: WeatherCondition,
    val iconUrl: String
)

@Serializable
enum class WeatherCondition {
    SUNNY,
    PARTLY_CLOUDY,
    CLOUDY,
    RAINY,
    STORMY,
    SNOWY,
    FOGGY,
    UNKNOWN
}