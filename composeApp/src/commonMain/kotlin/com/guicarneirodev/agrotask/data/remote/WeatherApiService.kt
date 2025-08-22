package com.guicarneirodev.agrotask.data.remote

import com.guicarneirodev.agrotask.data.remote.dto.WeatherApiResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class WeatherApiService(
    private val client: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                coerceInputValues = true
            })
        }
        install(Logging) {
            level = LogLevel.ALL
            logger = object : Logger {
                override fun log(message: String) {
                    println("HTTP Client: $message")
                }
            }
        }
    }
) {
    suspend fun getCurrentWeather(lat: Double, lon: Double, apiKey: String): WeatherApiResponse {
        val url = "https://api.weatherapi.com/v1/forecast.json"

        val response = client.get(url) {
            parameter("key", apiKey)
            parameter("q", "$lat,$lon")
            parameter("days", 2)
            parameter("aqi", "no")
            parameter("alerts", "no")
            parameter("lang", "pt")
        }.body<WeatherApiResponse>()

        return response
    }
}