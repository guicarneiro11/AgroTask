package com.guicarneirodev.agrotask.presentation.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Green10 = Color(0xFF0D2000)
val Green20 = Color(0xFF1A3300)
val Green30 = Color(0xFF274D00)
val Green40 = Color(0xFF366800)
val Green50 = Color(0xFF4CAF50)
val Green60 = Color(0xFF66BB6A)
val Green70 = Color(0xFF81C784)
val Green80 = Color(0xFFA5D6A7)
val Green90 = Color(0xFFC8E6C9)
val Green95 = Color(0xFFE8F5E9)

val Lime40 = Color(0xFF7CB342)
val Lime50 = Color(0xFF8BC34A)
val Lime60 = Color(0xFF9CCC65)

val Amber50 = Color(0xFFFFC107)
val Amber60 = Color(0xFFFFCA28)

val Grey10 = Color(0xFF0E0E0E)
val Grey20 = Color(0xFF1C1C1C)
val Grey30 = Color(0xFF282828)
val Grey40 = Color(0xFF404040)
val Grey80 = Color(0xFFCCCCCC)
val Grey90 = Color(0xFFE0E0E0)

val Red40 = Color(0xFFCF6679)
val Blue40 = Color(0xFF4FC3F7)

private val AgroColorScheme = darkColorScheme(
    primary = Green50,
    onPrimary = Color.White,
    primaryContainer = Green30,
    onPrimaryContainer = Green90,

    secondary = Lime50,
    onSecondary = Grey10,
    secondaryContainer = Green40,
    onSecondaryContainer = Green90,

    tertiary = Blue40,
    onTertiary = Grey10,
    tertiaryContainer = Color(0xFF004C6A),
    onTertiaryContainer = Color(0xFFB3E5FC),

    background = Grey10,
    onBackground = Grey90,

    surface = Grey20,
    onSurface = Grey90,
    surfaceVariant = Grey30,
    onSurfaceVariant = Grey80,

    error = Red40,
    onError = Color.White,

    outline = Grey40,
    outlineVariant = Grey30
)

@Composable
fun AgroTaskTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AgroColorScheme,
        typography = Typography(),
        content = content
    )
}