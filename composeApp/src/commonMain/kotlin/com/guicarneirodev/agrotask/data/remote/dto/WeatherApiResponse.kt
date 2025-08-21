package com.guicarneirodev.agrotask.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherApiResponse(
    val location: Location,
    val current: Current,
    val forecast: Forecast
)

@Serializable
data class Location(
    val name: String,
    val region: String,
    val country: String,
    val lat: Double,
    val lon: Double,
    @SerialName("tz_id") val tzId: String = "",
    @SerialName("localtime_epoch") val localtimeEpoch: Long = 0,
    @SerialName("localtime") val localTime: String
)

@Serializable
data class Current(
    @SerialName("last_updated_epoch") val lastUpdatedEpoch: Long = 0,
    @SerialName("last_updated") val lastUpdated: String = "",
    @SerialName("temp_c") val tempC: Double,
    @SerialName("temp_f") val tempF: Double,
    val humidity: Int,
    val condition: Condition,
    @SerialName("feelslike_c") val feelsLikeC: Double,
    @SerialName("wind_kph") val windKph: Double,
    @SerialName("is_day") val isDay: Int = 1
)

@Serializable
data class Condition(
    val text: String,
    val icon: String,
    val code: Int
)

@Serializable
data class Forecast(
    @SerialName("forecastday") val forecastDay: List<ForecastDay>
)

@Serializable
data class ForecastDay(
    val date: String,
    @SerialName("date_epoch") val dateEpoch: Long = 0,
    val hour: List<Hour>
)

@Serializable
data class Hour(
    @SerialName("time_epoch") val timeEpoch: Long = 0,
    val time: String,
    @SerialName("temp_c") val tempC: Double,
    val humidity: Int,
    val condition: Condition,
    @SerialName("is_day") val isDay: Int = 1
)