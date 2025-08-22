package com.guicarneirodev.agrotask.data.mapper

import com.guicarneirodev.agrotask.data.remote.dto.*
import com.guicarneirodev.agrotask.domain.model.WeatherCondition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeatherMapperTest {
    @Test
    fun `mapear resposta da API para dominio com condicoes de dia`() {
        val response = createWeatherResponse(
            code = 1000,
            tempC = 25.5,
            humidity = 65,
            hour = 14
        )

        val result = WeatherMapper.mapToDomain(response)

        assertEquals(25.5, result.temperature)
        assertEquals(65, result.humidity)
        assertEquals(WeatherCondition.CLEAR_DAY, result.condition)
        assertTrue(result.isDay)
    }

    @Test
    fun `mapear resposta da API para dominio com condicoes de noite`() {
        val response = createWeatherResponse(
            code = 1000,
            tempC = 18.0,
            humidity = 70,
            hour = 22
        )

        val result = WeatherMapper.mapToDomain(response)

        assertEquals(18.0, result.temperature)
        assertEquals(70, result.humidity)
        assertEquals(WeatherCondition.CLEAR_NIGHT, result.condition)
        assertEquals(false, result.isDay)
    }

    @Test
    fun `mapear codigo de chuva forte corretamente`() {
        val response = createWeatherResponse(code = 1246)

        val result = WeatherMapper.mapToDomain(response)

        assertEquals(WeatherCondition.HEAVY_RAIN, result.condition)
    }

    private fun createWeatherResponse(
        code: Int = 1000,
        tempC: Double = 20.0,
        humidity: Int = 50,
        hour: Int = 12
    ): WeatherApiResponse {
        val isDayValue = if (hour in 6..17) 1 else 0

        return WeatherApiResponse(
            location = Location(
                name = "Test",
                region = "Test",
                country = "Test",
                lat = 0.0,
                lon = 0.0,
                localTime = "2024-08-20 $hour:00"
            ),
            current = Current(
                tempC = tempC,
                tempF = 68.0,
                humidity = humidity,
                condition = Condition("Clear", "//icon.png", code),
                feelsLikeC = tempC,
                windKph = 10.0,
                isDay = isDayValue
            ),
            forecast = Forecast(
                forecastDay = listOf(
                    ForecastDay(
                        date = "2024-08-20",
                        hour = listOf(
                            Hour(
                                timeEpoch = 1724140800L + (hour * 3600L),
                                time = "2024-08-20 $hour:00",
                                tempC = tempC,
                                humidity = humidity,
                                condition = Condition("Clear", "//icon.png", code),
                                isDay = isDayValue
                            )
                        )
                    )
                )
            )
        )
    }
}