package com.guicarneirodev.agrotask.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guicarneirodev.agrotask.presentation.theme.*

@Composable
fun StandardHeader(
    title: String,
    subtitle: String,
    isOnline: Boolean,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Grey20,
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Grey20, Grey30.copy(alpha = 0.5f))
                    )
                )
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Green60
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        subtitle,
                        fontSize = 14.sp,
                        color = Grey80,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ConnectionStatusChip(isOnline = isOnline)
                    trailing?.invoke()
                }
            }
        }
    }
}