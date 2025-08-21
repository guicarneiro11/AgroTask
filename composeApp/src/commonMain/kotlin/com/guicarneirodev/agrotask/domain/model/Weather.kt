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
    val lastUpdated: LocalDateTime,
    val isDay: Boolean = true
)

@Serializable
data class HourlyForecast(
    val time: LocalDateTime,
    val temperature: Double,
    val humidity: Int,
    val condition: WeatherCondition,
    val iconUrl: String,
    val isDay: Boolean = true
)

@Serializable
enum class WeatherCondition {
    CLEAR_DAY,
    CLEAR_NIGHT,
    PARTLY_CLOUDY_DAY,
    PARTLY_CLOUDY_NIGHT,
    CLOUDY,
    LIGHT_RAIN,
    RAIN,
    HEAVY_RAIN,
    STORM,
    SNOW,
    FOG,
    UNKNOWN
}

fun WeatherCondition.getDescription(): String {
    return when (this) {
        WeatherCondition.CLEAR_DAY -> "Limpo"
        WeatherCondition.CLEAR_NIGHT -> "Limpo"
        WeatherCondition.PARTLY_CLOUDY_DAY -> "Parcialmente nublado"
        WeatherCondition.PARTLY_CLOUDY_NIGHT -> "Parcialmente nublado"
        WeatherCondition.CLOUDY -> "Nublado"
        WeatherCondition.LIGHT_RAIN -> "Chuva leve"
        WeatherCondition.RAIN -> "Chuva"
        WeatherCondition.HEAVY_RAIN -> "Chuva forte"
        WeatherCondition.STORM -> "Tempestade"
        WeatherCondition.SNOW -> "Neve"
        WeatherCondition.FOG -> "Neblina"
        WeatherCondition.UNKNOWN -> "Desconhecido"
    }
}