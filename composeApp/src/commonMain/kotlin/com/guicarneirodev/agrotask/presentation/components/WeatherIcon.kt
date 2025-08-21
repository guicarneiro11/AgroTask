package com.guicarneirodev.agrotask.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.guicarneirodev.agrotask.domain.model.WeatherCondition
import com.guicarneirodev.agrotask.presentation.theme.Green60

@Composable
fun WeatherIcon(
    condition: WeatherCondition,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    tint: Color = Green60
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = when (condition) {
                WeatherCondition.SUNNY -> Icons.Default.WbSunny
                WeatherCondition.PARTLY_CLOUDY -> Icons.Default.CloudQueue
                WeatherCondition.CLOUDY -> Icons.Default.Cloud
                WeatherCondition.RAINY -> Icons.Default.Umbrella
                WeatherCondition.STORMY -> Icons.Default.Thunderstorm
                WeatherCondition.SNOWY -> Icons.Default.AcUnit
                WeatherCondition.FOGGY -> Icons.Default.Dehaze
                WeatherCondition.UNKNOWN -> Icons.AutoMirrored.Filled.Help
            },
            contentDescription = condition.name,
            tint = tint,
            modifier = Modifier.size(size)
        )
    }
}