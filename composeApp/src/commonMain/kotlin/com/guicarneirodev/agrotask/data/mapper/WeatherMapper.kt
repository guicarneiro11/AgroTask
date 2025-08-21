package com.guicarneirodev.agrotask.data.mapper

import com.guicarneirodev.agrotask.data.remote.dto.WeatherApiResponse
import com.guicarneirodev.agrotask.domain.model.HourlyForecast
import com.guicarneirodev.agrotask.domain.model.Weather
import com.guicarneirodev.agrotask.domain.model.WeatherCondition
import com.guicarneirodev.agrotask.domain.model.getDescription
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

object WeatherMapper {

    @OptIn(ExperimentalTime::class)
    fun mapToDomain(response: WeatherApiResponse): Weather {
        val systemTimeZone = TimeZone.currentSystemDefault()

        val hourlyForecasts = response.forecast.forecastDay.flatMap { forecastDay ->
            forecastDay.hour.map { hour ->
                val instant = Instant.fromEpochSeconds(hour.timeEpoch)
                val localDateTime = instant.toLocalDateTime(systemTimeZone)
                val hourOfDay = localDateTime.hour

                val isDayTime = hourOfDay in 6..17

                HourlyForecast(
                    time = localDateTime,
                    temperature = hour.tempC,
                    humidity = hour.humidity,
                    condition = mapConditionCode(hour.condition.code, isDayTime),
                    iconUrl = "https:${hour.condition.icon}",
                    isDay = isDayTime
                )
            }
        }

        val currentHour = kotlin.time.Clock.System.now()
            .toLocalDateTime(systemTimeZone).hour
        val isCurrentlyDay = currentHour in 6..17

        val condition = mapConditionCode(response.current.condition.code, isCurrentlyDay)

        return Weather(
            temperature = response.current.tempC,
            humidity = response.current.humidity,
            condition = condition,
            description = condition.getDescription(),
            iconUrl = "https:${response.current.condition.icon}",
            hourlyForecast = hourlyForecasts,
            isFromCache = false,
            lastUpdated = kotlin.time.Clock.System.now().toLocalDateTime(systemTimeZone),
            isDay = isCurrentlyDay
        )
    }

    private fun mapConditionCode(code: Int, isDay: Boolean): WeatherCondition {
        return when(code) {
            1000 -> if (isDay) WeatherCondition.CLEAR_DAY else WeatherCondition.CLEAR_NIGHT

            1003 -> if (isDay) WeatherCondition.CLEAR_DAY else WeatherCondition.CLEAR_NIGHT

            1006 -> if (isDay) WeatherCondition.PARTLY_CLOUDY_DAY else WeatherCondition.PARTLY_CLOUDY_NIGHT

            1009, 1030, 1135, 1147 -> WeatherCondition.CLOUDY

            1063, 1150, 1153, 1168, 1171, 1072, 1180, 1183 -> WeatherCondition.LIGHT_RAIN

            1186, 1189, 1192, 1195, 1198, 1201, 1240, 1243 -> WeatherCondition.RAIN

            1246, 1252, 1258, 1264 -> WeatherCondition.HEAVY_RAIN

            1087, 1273, 1276, 1279, 1282 -> WeatherCondition.STORM

            1066, 1069, 1114, 1117, 1204, 1207, 1210, 1213, 1216, 1219, 1222, 1225, 1237, 1249, 1255, 1261 -> WeatherCondition.SNOW

            1135, 1147 -> WeatherCondition.FOG

            else -> WeatherCondition.UNKNOWN
        }
    }
}