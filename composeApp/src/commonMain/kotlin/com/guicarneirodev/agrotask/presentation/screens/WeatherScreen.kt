package com.guicarneirodev.agrotask.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
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
import com.guicarneirodev.agrotask.domain.network.NetworkStatus
import com.guicarneirodev.agrotask.domain.network.isConnected
import com.guicarneirodev.agrotask.presentation.components.ConnectionStatusChip
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
import org.koin.compose.viewmodel.koinViewModel

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
        border = androidx.compose.foundation.BorderStroke(1.dp, Amber60.copy(alpha = 0.3f))
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        WeatherHeader(
            weather = weather,
            networkStatus = networkStatus,
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
    networkStatus: NetworkStatus,
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

                    ConnectionStatusChip(
                        isOnline = networkStatus.isConnected()
                    )
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

            if (weather.isFromCache) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Última atualização: ${formatLastUpdate(weather.lastUpdated)}",
                    fontSize = 12.sp,
                    color = Grey80.copy(alpha = 0.7f)
                )
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
            Icon(
                if (isOffline) Icons.Default.WifiOff else Icons.Default.CloudOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = if (isOffline) Grey40 else Red40
            )
            Text(
                if (isOffline) "Sem conexão" else "Erro ao carregar clima",
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
            if (!isOffline) {
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
}

fun formatLastUpdate(dateTime: kotlinx.datetime.LocalDateTime): String {
    return "${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')} - ${dateTime.dayOfMonth}/${dateTime.monthNumber}"
}