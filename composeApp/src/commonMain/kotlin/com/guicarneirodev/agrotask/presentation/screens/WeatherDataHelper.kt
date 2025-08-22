package com.guicarneirodev.agrotask.presentation.screens

import com.guicarneirodev.agrotask.domain.model.HourlyForecast
import com.guicarneirodev.agrotask.domain.model.Weather
import com.guicarneirodev.agrotask.domain.model.WeatherCondition
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

object WeatherDataHelper {

    @OptIn(ExperimentalTime::class)
    fun weatherData(weather: Weather): WeatherData {
        val currentTime = Clock.System.now()
            .toLocalDateTime(TimeZone.Companion.currentSystemDefault())

        val currentState = WeatherState(
            temperature = weather.temperature,
            humidity = weather.humidity,
            condition = weather.condition,
            isDay = weather.isDay,
            time = currentTime
        )

        val forecasts = weather.hourlyForecast.map { forecast ->
            val isCurrentHour = forecast.time.hour == currentTime.hour &&
                    forecast.time.day == currentTime.day

            if (isCurrentHour) {
                forecast.copy(
                    temperature = weather.temperature,
                    humidity = weather.humidity,
                    condition = weather.condition,
                    isDay = weather.isDay
                )
            } else {
                forecast
            }
        }

        return WeatherData(
            current = currentState,
            hourlyForecasts = forecasts,
            isFromCache = weather.isFromCache,
            lastUpdated = weather.lastUpdated
        )
    }

    data class WeatherData(
        val current: WeatherState,
        val hourlyForecasts: List<HourlyForecast>,
        val isFromCache: Boolean,
        val lastUpdated: LocalDateTime
    )

    data class WeatherState(
        val temperature: Double,
        val humidity: Int,
        val condition: WeatherCondition,
        val isDay: Boolean,
        val time: LocalDateTime
    )
}