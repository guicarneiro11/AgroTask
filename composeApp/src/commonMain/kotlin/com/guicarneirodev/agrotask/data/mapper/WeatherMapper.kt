package com.guicarneirodev.agrotask.data.mapper

import com.guicarneirodev.agrotask.data.remote.dto.WeatherApiResponse
import com.guicarneirodev.agrotask.domain.model.HourlyForecast
import com.guicarneirodev.agrotask.domain.model.Weather
import com.guicarneirodev.agrotask.domain.model.WeatherCondition
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

object WeatherMapper {

    @OptIn(ExperimentalTime::class)
    fun mapToDomain(response: WeatherApiResponse): Weather {
        return Weather(
            temperature = response.current.tempC,
            humidity = response.current.humidity,
            condition = mapConditionCode(response.current.condition.code),
            description = response.current.condition.text,
            iconUrl = "https:${response.current.condition.icon}",
            hourlyForecast = response.forecast.forecastDay.firstOrNull()?.hour?.map { hour ->
                HourlyForecast(
                    time = parseDateTime(hour.time),
                    temperature = hour.tempC,
                    condition = mapConditionCode(hour.condition.code),
                    iconUrl = "https:${hour.condition.icon}"
                )
            } ?: emptyList(),
            isFromCache = false,
            lastUpdated = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        )
    }

    private fun mapConditionCode(code: Int): WeatherCondition {
        return when(code) {
            1000 -> WeatherCondition.SUNNY
            1003, 1006, 1009 -> WeatherCondition.PARTLY_CLOUDY
            1030, 1135, 1147 -> WeatherCondition.FOGGY
            1063, 1150, 1153, 1168, 1171, 1180, 1183, 1186, 1189, 1192, 1195, 1198, 1201, 1240, 1243, 1246 -> WeatherCondition.RAINY
            1087, 1273, 1276, 1279, 1282 -> WeatherCondition.STORMY
            1066, 1069, 1072, 1114, 1117, 1204, 1207, 1210, 1213, 1216, 1219, 1222, 1225, 1237, 1249, 1252, 1255, 1258, 1261, 1264 -> WeatherCondition.SNOWY
            else -> WeatherCondition.UNKNOWN
        }
    }

    private fun parseDateTime(dateTimeString: String): LocalDateTime {
        val parts = dateTimeString.split(" ")
        val dateParts = parts[0].split("-")
        val timeParts = parts.getOrNull(1)?.split(":") ?: listOf("00", "00")

        return LocalDateTime(year = dateParts[0].toInt(),
            month = dateParts[1].toInt(),
            day = dateParts[2].toInt(),
            hour = timeParts[0].toInt(),
            minute = timeParts[1].toInt()
        )
    }
}