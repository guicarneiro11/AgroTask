package com.guicarneirodev.agrotask.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guicarneirodev.agrotask.domain.model.HourlyForecast
import com.guicarneirodev.agrotask.domain.model.Weather
import com.guicarneirodev.agrotask.domain.model.WeatherCondition
import com.guicarneirodev.agrotask.domain.model.getDescription
import com.guicarneirodev.agrotask.domain.network.NetworkStatus
import com.guicarneirodev.agrotask.domain.network.isConnected
import com.guicarneirodev.agrotask.presentation.components.StandardHeader
import com.guicarneirodev.agrotask.presentation.components.WeatherIcon
import com.guicarneirodev.agrotask.presentation.theme.Amber60
import com.guicarneirodev.agrotask.presentation.theme.Blue40
import com.guicarneirodev.agrotask.presentation.theme.Green50
import com.guicarneirodev.agrotask.presentation.theme.Green60
import com.guicarneirodev.agrotask.presentation.theme.Grey20
import com.guicarneirodev.agrotask.presentation.theme.Grey30
import com.guicarneirodev.agrotask.presentation.theme.Grey40
import com.guicarneirodev.agrotask.presentation.theme.Grey80
import com.guicarneirodev.agrotask.presentation.theme.Red40
import com.guicarneirodev.agrotask.presentation.viewmodel.WeatherViewModel
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.ExperimentalTime

@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = koinViewModel()
) {
    val weather by viewModel.weather.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val networkStatus by viewModel.networkStatus.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedVisibility(
            visible = !networkStatus.isConnected() && weather != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        ) {
            NetworkStatusBanner()
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isLoading && weather == null -> {
                    LoadingWeatherState()
                }
                weather != null -> {
                    WeatherContent(
                        weather = weather!!,
                        networkStatus = networkStatus,
                        onRefresh = { viewModel.refreshWeather() },
                        isRefreshing = isLoading
                    )
                }
                error != null -> {
                    ErrorWeatherState(
                        error = error!!,
                        isOffline = !networkStatus.isConnected(),
                        onRetry = { viewModel.refreshWeather() }
                    )
                }
                else -> {
                    LoadingWeatherState()
                }
            }
        }
    }
}

@Composable
fun NetworkStatusBanner(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Amber60.copy(alpha = 0.15f)
        ),
        border = BorderStroke(1.dp, Amber60.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.WifiOff,
                contentDescription = null,
                tint = Amber60,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Modo offline - Mostrando dados salvos",
                fontSize = 12.sp,
                color = Amber60,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun WeatherContent(
    weather: Weather,
    networkStatus: NetworkStatus,
    onRefresh: () -> Unit,
    isRefreshing: Boolean
) {
    var selectedForecast by remember { mutableStateOf<HourlyForecast?>(null) }

    val normalizedData = remember(weather) {
        WeatherDataHelper.weatherData(weather)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        WeatherHeader(
            networkStatus = networkStatus,
            onRefresh = onRefresh,
            isRefreshing = isRefreshing
        )

        Crossfade(
            targetState = selectedForecast,
            modifier = Modifier.fillMaxWidth()
        ) { forecast ->
            if (forecast == null) {
                CurrentWeatherCard(
                    temperature = normalizedData.current.temperature,
                    humidity = normalizedData.current.humidity,
                    condition = normalizedData.current.condition,
                    description = normalizedData.current.condition.getDescription(),
                    isFromCache = normalizedData.isFromCache,
                    lastUpdated = normalizedData.lastUpdated
                )
            } else {
                SelectedForecastCard(
                    forecast = forecast,
                    onDismiss = { selectedForecast = null }
                )
            }
        }

        if (normalizedData.hourlyForecasts.isNotEmpty()) {
            HourlyForecastSection(
                forecasts = normalizedData.hourlyForecasts,
                selectedForecast = selectedForecast,
                onForecastClick = { forecast ->
                    selectedForecast = if (selectedForecast == forecast) null else forecast
                }
            )
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun WeatherHeader(
    networkStatus: NetworkStatus,
    onRefresh: () -> Unit,
    isRefreshing: Boolean
) {
    StandardHeader(
        title = "Condições do Tempo",
        subtitle = "Previsão local",
        isOnline = networkStatus.isConnected(),
        trailing = {
            IconButton(
                onClick = onRefresh,
                enabled = !isRefreshing && networkStatus.isConnected()
            ) {
                val rotation by animateFloatAsState(
                    targetValue = if (isRefreshing) 360f else 0f,
                    animationSpec = if (isRefreshing) {
                        infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        )
                    } else {
                        tween(0)
                    }
                )
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Atualizar",
                    tint = if (networkStatus.isConnected()) Green60 else Grey40,
                    modifier = Modifier.rotate(rotation)
                )
            }
        }
    )
}

@Composable
fun CurrentWeatherCard(
    temperature: Double,
    humidity: Int,
    condition: WeatherCondition,
    description: String,
    isFromCache: Boolean,
    lastUpdated: LocalDateTime
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(320.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Grey30
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                WeatherIcon(
                    condition = condition,
                    size = 80.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "${temperature.toInt()}",
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Light,
                        color = Color.White
                    )
                    Text(
                        "°C",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Light,
                        color = Grey80,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Text(
                    description,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Grey80,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Grey40.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.WaterDrop,
                            contentDescription = "Umidade",
                            tint = Blue40,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "Umidade: $humidity%",
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }

            if (isFromCache) {
                Text(
                    "Última atualização: ${formatLastUpdate(lastUpdated)}",
                    fontSize = 11.sp,
                    color = Grey80.copy(alpha = 0.7f),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                )
            }
        }
    }
}

@Composable
fun SelectedForecastCard(
    forecast: HourlyForecast,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(320.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Grey20
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        border = BorderStroke(2.dp, Green50)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Green50.copy(alpha = 0.2f)
                ) {
                    Text(
                        "${forecast.time.hour}:00",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Green50
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                WeatherIcon(
                    condition = forecast.condition,
                    size = 80.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "${forecast.temperature.toInt()}",
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Light,
                        color = Color.White
                    )
                    Text(
                        "°C",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Light,
                        color = Grey80,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Text(
                    forecast.condition.getDescription(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Grey80,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Grey40.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.WaterDrop,
                            contentDescription = "Umidade",
                            tint = Blue40,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "Umidade: ${forecast.humidity}%",
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(32.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Grey40.copy(alpha = 0.5f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = Grey80,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalTime::class)
@Composable
fun HourlyForecastSection(
    forecasts: List<HourlyForecast>,
    selectedForecast: HourlyForecast?,
    onForecastClick: (HourlyForecast) -> Unit
) {
    val currentTime = kotlin.time.Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val currentHour = currentTime.hour
    val currentDay = currentTime.day

    val nextHours = mutableListOf<HourlyForecast>()
    var foundStart = false

    for (forecast in forecasts.sortedBy { it.time }) {
        val forecastHour = forecast.time.hour
        val forecastDay = forecast.time.day

        if (!foundStart) {
            if (forecastDay == currentDay && forecastHour >= currentHour) {
                foundStart = true
                nextHours.add(forecast)
            } else if (forecastDay > currentDay) {
                foundStart = true
                nextHours.add(forecast)
            }
        } else {
            nextHours.add(forecast)
            if (nextHours.size >= 24) break
        }
    }

    if (nextHours.isEmpty() && forecasts.isNotEmpty()) {
        nextHours.addAll(forecasts.take(24))
    }

    Column(
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Próximas Horas",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Green60
            )

            if (nextHours.isNotEmpty()) {
                Text(
                    "${nextHours.size}h de previsão",
                    fontSize = 12.sp,
                    color = Grey80
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(nextHours) { forecast ->
                val isCurrentHour = forecast.time.hour == currentHour &&
                        forecast.time.day == currentDay
                val isSelected = selectedForecast == forecast

                HourlyForecastCardClickable(
                    forecast = forecast,
                    isCurrentHour = isCurrentHour,
                    isSelected = isSelected,
                    onClick = { onForecastClick(forecast) }
                )
            }
        }
    }
}


@Composable
fun HourlyForecastCardClickable(
    forecast: HourlyForecast,
    isCurrentHour: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val hour = forecast.time.hour
    val isDayTime = hour in 6..17

    val cardBackgroundColor = when {
        isSelected -> Green50.copy(alpha = 0.3f)
        isCurrentHour -> Green50.copy(alpha = 0.15f)
        !isDayTime -> Grey30.copy(alpha = 0.9f)
        else -> Grey30.copy(alpha = 0.7f)
    }

    val borderColor = when {
        isSelected -> Green50
        isCurrentHour -> Green50.copy(alpha = 0.5f)
        else -> null
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBackgroundColor
        ),
        border = borderColor?.let { BorderStroke(1.dp, it) },
        modifier = Modifier
            .width(60.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                when {
                    isCurrentHour -> "Agora"
                    else -> "${forecast.time.hour}h"
                },
                fontSize = 11.sp,
                color = when {
                    isSelected -> Green50
                    isCurrentHour -> Green50
                    !isDayTime -> Color(0xFFB3B3CC)
                    else -> Grey80
                },
                fontWeight = if (isCurrentHour || isSelected) FontWeight.Bold else FontWeight.Normal
            )

            WeatherIcon(
                condition = forecast.condition,
                size = 28.dp
            )

            Text(
                "${forecast.temperature.toInt()}°",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

@Composable
fun LoadingWeatherState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = Green50,
                modifier = Modifier.size(48.dp)
            )
            Text(
                "Carregando dados do clima...",
                fontSize = 16.sp,
                color = Grey80
            )
        }
    }
}

@Composable
fun ErrorWeatherState(
    error: String,
    isOffline: Boolean,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            val isLocationError = error.contains("localização", ignoreCase = true) ||
                    error.contains("permissão", ignoreCase = true)

            Icon(
                when {
                    isLocationError -> Icons.Default.LocationOff
                    isOffline -> Icons.Default.WifiOff
                    else -> Icons.Default.CloudOff
                },
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = when {
                    isLocationError -> Amber60
                    isOffline -> Grey40
                    else -> Red40
                }
            )

            Text(
                when {
                    isLocationError -> "Localização Necessária"
                    isOffline -> "Sem conexão"
                    else -> "Erro ao carregar clima"
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )

            Text(
                when {
                    isLocationError -> "Ative a permissão de localização nas configurações do dispositivo para ver o clima da sua região"
                    else -> error
                },
                fontSize = 14.sp,
                color = Grey80,
                textAlign = TextAlign.Center
            )

            if (!isOffline || isLocationError) {
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green50
                    )
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isLocationError) "Tentar Novamente" else "Recarregar")
                }
            }
        }
    }
}

fun formatLastUpdate(dateTime: LocalDateTime): String {
    return "${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')} - ${dateTime.day}/${dateTime.month.number}"
}