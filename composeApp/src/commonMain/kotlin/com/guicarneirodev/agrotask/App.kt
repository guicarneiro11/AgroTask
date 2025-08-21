package com.guicarneirodev.agrotask

import androidx.compose.runtime.Composable
import com.guicarneirodev.agrotask.presentation.navigation.AgroTaskNavigation
import com.guicarneirodev.agrotask.presentation.theme.AgroTaskTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    AgroTaskTheme {
        AgroTaskNavigation()
    }
}