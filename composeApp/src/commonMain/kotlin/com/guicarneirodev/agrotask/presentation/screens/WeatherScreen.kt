package com.guicarneirodev.agrotask.presentation.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guicarneirodev.agrotask.domain.model.HourlyForecast
import com.guicarneirodev.agrotask.domain.model.Weather
import com.guicarneirodev.agrotask.presentation.components.WeatherIcon
import com.guicarneirodev.agrotask.presentation.theme.*
import com.guicarneirodev.agrotask.presentation.viewmodel.WeatherViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = koinViewModel()
) {
    val weather by viewModel.weather.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            isLoading && weather == null -> {
                LoadingWeatherState()
            }
            weather != null -> {
                WeatherContent(
                    weather = weather!!,
                    onRefresh = { viewModel.refreshWeather() },
                    isRefreshing = isLoading
                )
            }
            error != null -> {
                ErrorWeatherState(
                    error = error!!,
                    onRetry = { viewModel.refreshWeather() }
                )
            }
            else -> {
                LoadingWeatherState()
            }
        }
    }
}

@Composable
fun WeatherContent(
    weather: Weather,
    onRefresh: () -> Unit,
    isRefreshing: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        WeatherHeader(
            weather = weather,
            onRefresh = onRefresh,
            isRefreshing = isRefreshing
        )

        CurrentWeatherCard(weather)

        if (weather.hourlyForecast.isNotEmpty()) {
            HourlyForecastSection(weather.hourlyForecast)
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun WeatherHeader(
    weather: Weather,
    onRefresh: () -> Unit,
    isRefreshing: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Grey20,
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        "Condições do Tempo",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Green60
                    )
                    Text(
                        "Previsão local",
                        fontSize = 14.sp,
                        color = Grey80
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onRefresh,
                        enabled = !isRefreshing
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
                            tint = Green60,
                            modifier = Modifier.rotate(rotation)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (weather.isFromCache) Amber60 else Green60
                                )
                        )
                        Text(
                            if (weather.isFromCache) "Cache" else "Online",
                            fontSize = 12.sp,
                            color = if (weather.isFromCache) Amber60 else Green60,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CurrentWeatherCard(weather: Weather) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Grey30
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WeatherIcon(
                condition = weather.condition,
                size = 100.dp,
                tint = Green60
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "${weather.temperature.toInt()}",
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White
                )
                Text(
                    "°C",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Light,
                    color = Grey80,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Text(
                weather.description,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Grey80,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

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
                        "Umidade: ${weather.humidity}%",
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun HourlyForecastSection(forecasts: List<HourlyForecast>) {
    Column(
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        Text(
            "Próximas Horas",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Green60,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(forecasts.take(12)) { forecast ->
                HourlyForecastCard(forecast)
            }
        }
    }
}

@Composable
fun HourlyForecastCard(forecast: HourlyForecast) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Grey30.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "${forecast.time.hour}:00",
                fontSize = 12.sp,
                color = Grey80
            )

            WeatherIcon(
                condition = forecast.condition,
                size = 32.dp,
                tint = Green60
            )

            Text(
                "${forecast.temperature.toInt()}°",
                fontSize = 16.sp,
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
            Icon(
                Icons.Default.CloudOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Red40
            )
            Text(
                "Erro ao carregar clima",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Text(
                error,
                fontSize = 14.sp,
                color = Grey80,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green50
                )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tentar Novamente")
            }
        }
    }
}