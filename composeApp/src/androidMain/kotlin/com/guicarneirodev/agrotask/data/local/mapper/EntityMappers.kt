package com.guicarneirodev.agrotask.data.local.mapper

import com.guicarneirodev.agrotask.data.local.entity.ActivityRecordEntity
import com.guicarneirodev.agrotask.data.local.entity.TaskEntity
import com.guicarneirodev.agrotask.data.local.entity.WeatherCacheEntity
import com.guicarneirodev.agrotask.domain.model.*
import kotlinx.datetime.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun TaskEntity.toDomain(): Task {
    return Task(
        id = id,
        activityName = activityName,
        field = field,
        scheduledTime = Instant.fromEpochMilliseconds(scheduledTime)
            .toLocalDateTime(TimeZone.currentSystemDefault()),
        status = status,
        syncedWithFirebase = syncedWithFirebase,
        createdAt = Instant.fromEpochMilliseconds(createdAt)
            .toLocalDateTime(TimeZone.currentSystemDefault()),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt)
            .toLocalDateTime(TimeZone.currentSystemDefault())
    )
}

@OptIn(ExperimentalTime::class)
fun Task.toEntity(): TaskEntity {
    return TaskEntity(
        id = id,
        activityName = activityName,
        field = field,
        scheduledTime = scheduledTime.toInstant(TimeZone.currentSystemDefault())
            .toEpochMilliseconds(),
        status = status,
        syncedWithFirebase = syncedWithFirebase,
        createdAt = createdAt.toInstant(TimeZone.currentSystemDefault())
            .toEpochMilliseconds(),
        updatedAt = updatedAt.toInstant(TimeZone.currentSystemDefault())
            .toEpochMilliseconds()
    )
}

@OptIn(ExperimentalTime::class)
fun ActivityRecordEntity.toDomain(): ActivityRecord {
    return ActivityRecord(
        id = id,
        activityType = activityType,
        field = field,
        startTime = Instant.fromEpochMilliseconds(startTime)
            .toLocalDateTime(TimeZone.currentSystemDefault()),
        endTime = Instant.fromEpochMilliseconds(endTime)
            .toLocalDateTime(TimeZone.currentSystemDefault()),
        observations = observations,
        syncedWithFirebase = syncedWithFirebase,
        createdAt = Instant.fromEpochMilliseconds(createdAt)
            .toLocalDateTime(TimeZone.currentSystemDefault())
    )
}

@OptIn(ExperimentalTime::class)
fun ActivityRecord.toEntity(): ActivityRecordEntity {
    return ActivityRecordEntity(
        id = id,
        activityType = activityType,
        field = field,
        startTime = startTime.toInstant(TimeZone.currentSystemDefault())
            .toEpochMilliseconds(),
        endTime = endTime.toInstant(TimeZone.currentSystemDefault())
            .toEpochMilliseconds(),
        observations = observations,
        syncedWithFirebase = syncedWithFirebase,
        createdAt = createdAt.toInstant(TimeZone.currentSystemDefault())
            .toEpochMilliseconds()
    )
}

@OptIn(ExperimentalTime::class)
fun WeatherCacheEntity.toDomain(): Weather {
    val hourlyForecast = try {
        val forecasts = Json.decodeFromString<List<HourlyForecast>>(hourlyForecastJson)
        forecasts.map { forecast ->
            val hour = forecast.time.hour
            val isDayTime = hour in 6..17

            val updatedCondition = when (forecast.condition) {
                WeatherCondition.CLEAR_DAY, WeatherCondition.CLEAR_NIGHT -> {
                    if (isDayTime) WeatherCondition.CLEAR_DAY else WeatherCondition.CLEAR_NIGHT
                }
                WeatherCondition.PARTLY_CLOUDY_DAY, WeatherCondition.PARTLY_CLOUDY_NIGHT -> {
                    if (isDayTime) WeatherCondition.PARTLY_CLOUDY_DAY else WeatherCondition.PARTLY_CLOUDY_NIGHT
                }
                else -> forecast.condition
            }

            forecast.copy(
                condition = updatedCondition,
                isDay = isDayTime
            )
        }
    } catch (e: Exception) {
        emptyList()
    }

    val currentHour = kotlin.time.Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()).hour
    val isCurrentlyDay = currentHour in 6..17

    val weatherCondition = try {
        val condition = WeatherCondition.valueOf(condition)
        when (condition) {
            WeatherCondition.CLEAR_DAY, WeatherCondition.CLEAR_NIGHT -> {
                if (isCurrentlyDay) WeatherCondition.CLEAR_DAY else WeatherCondition.CLEAR_NIGHT
            }
            WeatherCondition.PARTLY_CLOUDY_DAY, WeatherCondition.PARTLY_CLOUDY_NIGHT -> {
                if (isCurrentlyDay) WeatherCondition.PARTLY_CLOUDY_DAY else WeatherCondition.PARTLY_CLOUDY_NIGHT
            }
            else -> condition
        }
    } catch (e: Exception) {
        WeatherCondition.UNKNOWN
    }

    return Weather(
        temperature = temperature,
        humidity = humidity,
        condition = weatherCondition,
        description = weatherCondition.getDescription(),
        iconUrl = iconUrl,
        hourlyForecast = hourlyForecast,
        isFromCache = true,
        lastUpdated = Instant.fromEpochMilliseconds(lastUpdated)
            .toLocalDateTime(TimeZone.currentSystemDefault()),
        isDay = isCurrentlyDay
    )
}

@OptIn(ExperimentalTime::class)
fun Weather.toEntity(): WeatherCacheEntity {
    return WeatherCacheEntity(
        temperature = temperature,
        humidity = humidity,
        condition = condition.name,
        description = description,
        iconUrl = iconUrl,
        hourlyForecastJson = Json.encodeToString(hourlyForecast),
        lastUpdated = lastUpdated.toInstant(TimeZone.currentSystemDefault())
            .toEpochMilliseconds()
    )
}