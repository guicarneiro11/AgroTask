package com.guicarneirodev.agrotask.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Dehaze
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.guicarneirodev.agrotask.domain.model.WeatherCondition
import com.guicarneirodev.agrotask.presentation.theme.Amber60
import com.guicarneirodev.agrotask.presentation.theme.Blue40
import com.guicarneirodev.agrotask.presentation.theme.Grey40
import com.guicarneirodev.agrotask.presentation.theme.Grey80

@Composable
fun WeatherIcon(
    condition: WeatherCondition,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    tint: Color? = null
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        when (condition) {
            WeatherCondition.CLEAR_DAY -> {
                Icon(
                    Icons.Default.WbSunny,
                    contentDescription = "Limpo",
                    tint = tint ?: Amber60,
                    modifier = Modifier.size(size)
                )
            }

            WeatherCondition.CLEAR_NIGHT -> {
                Icon(
                    Icons.Default.Bedtime,
                    contentDescription = "Limpo",
                    tint = tint ?: Color(0xFF9C88FF),
                    modifier = Modifier.size(size)
                )
            }

            WeatherCondition.PARTLY_CLOUDY_DAY -> {
                Icon(
                    Icons.Default.WbCloudy,
                    contentDescription = "Parcialmente nublado",
                    tint = tint ?: Grey80,
                    modifier = Modifier.size(size)
                )
            }

            WeatherCondition.PARTLY_CLOUDY_NIGHT -> {
                Box {
                    Icon(
                        Icons.Default.Bedtime,
                        contentDescription = null,
                        tint = tint ?: Color(0xFF9C88FF),
                        modifier = Modifier
                            .size(size * 0.6f)
                            .offset(x = -(size.value * 0.15).dp, y = -(size.value * 0.1).dp)
                    )
                    Icon(
                        Icons.Default.CloudQueue,
                        contentDescription = "Parcialmente nublado",
                        tint = tint ?: Grey80,
                        modifier = Modifier
                            .size(size * 0.7f)
                            .offset(x = (size.value * 0.1).dp, y = (size.value * 0.1).dp)
                    )
                }
            }

            WeatherCondition.CLOUDY -> {
                Icon(
                    Icons.Default.Cloud,
                    contentDescription = "Nublado",
                    tint = tint ?: Grey40,
                    modifier = Modifier.size(size)
                )
            }

            WeatherCondition.LIGHT_RAIN -> {
                Box {
                    Icon(
                        Icons.Default.Cloud,
                        contentDescription = null,
                        tint = tint ?: Grey40,
                        modifier = Modifier
                            .size(size * 0.9f)
                            .offset(y = -(size.value * 0.1).dp)
                    )
                    Icon(
                        Icons.Default.Grain,
                        contentDescription = "Chuva leve",
                        tint = tint ?: Blue40,
                        modifier = Modifier
                            .size(size * 0.4f)
                            .offset(y = (size.value * 0.25).dp)
                    )
                }
            }

            WeatherCondition.RAIN -> {
                Icon(
                    Icons.Default.WaterDrop,
                    contentDescription = "Chuva",
                    tint = tint ?: Blue40,
                    modifier = Modifier.size(size)
                )
            }

            WeatherCondition.HEAVY_RAIN -> {
                Icon(
                    Icons.Default.WaterDrop,
                    contentDescription = "Chuva forte",
                    tint = tint ?: Color(0xFF1565C0),
                    modifier = Modifier.size(size)
                )
            }

            WeatherCondition.STORM -> {
                Icon(
                    Icons.Default.Thunderstorm,
                    contentDescription = "Tempestade",
                    tint = tint ?: Color(0xFF7B68EE),
                    modifier = Modifier.size(size)
                )
            }

            WeatherCondition.SNOW -> {
                Icon(
                    Icons.Default.AcUnit,
                    contentDescription = "Neve",
                    tint = tint ?: Color(0xFFB3E5FC),
                    modifier = Modifier.size(size)
                )
            }

            WeatherCondition.FOG -> {
                Icon(
                    Icons.Default.Dehaze,
                    contentDescription = "Neblina",
                    tint = tint ?: Grey80.copy(alpha = 0.7f),
                    modifier = Modifier.size(size)
                )
            }

            WeatherCondition.UNKNOWN -> {
                Icon(
                    Icons.AutoMirrored.Filled.Help,
                    contentDescription = "Desconhecido",
                    tint = tint ?: Grey80,
                    modifier = Modifier.size(size)
                )
            }
        }
    }
}