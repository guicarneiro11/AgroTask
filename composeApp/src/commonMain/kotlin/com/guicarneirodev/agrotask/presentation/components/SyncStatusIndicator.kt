package com.guicarneirodev.agrotask.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guicarneirodev.agrotask.domain.sync.SyncEvent
import com.guicarneirodev.agrotask.domain.sync.SyncState
import com.guicarneirodev.agrotask.presentation.theme.*
import kotlinx.coroutines.delay
import kotlin.time.ExperimentalTime

@Composable
fun SyncStatusIndicator(
    syncState: SyncState,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
    showText: Boolean = true
) {
    Row(
        modifier = modifier.clickable(
            enabled = syncState.lastSyncError != null || !syncState.isSyncing,
            onClick = onRetryClick
        ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        SyncIcon(syncState)

        if (showText) {
            Column(
                modifier = Modifier.widthIn(max = 100.dp)
            ) {
                Text(
                    text = getSyncStatusText(syncState),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = getSyncStatusColor(syncState),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                syncState.lastSyncTime?.let { time ->
                    if (!syncState.isSyncing) {
                        Text(
                            text = getRelativeTimeString(time),
                            fontSize = 9.sp,
                            color = Grey80,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncIcon(syncState: SyncState) {
    Box(
        modifier = Modifier.size(24.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            syncState.isSyncing -> {
                val infiniteTransition = rememberInfiniteTransition()
                val rotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )

                if (syncState.syncProgress > 0f && syncState.syncProgress < 1f) {
                    CircularProgressIndicator(
                    progress = { syncState.syncProgress },
                    modifier = Modifier.size(20.dp),
                    color = Blue40,
                    strokeWidth = 2.dp,
                    trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
                    strokeCap = ProgressIndicatorDefaults.CircularDeterminateStrokeCap,
                    )
                } else {
                    Icon(
                        Icons.Default.Sync,
                        contentDescription = "Sincronizando",
                        tint = Blue40,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(rotation)
                    )
                }
            }
            !syncState.isOnline -> {
                Icon(
                    Icons.Default.CloudOff,
                    contentDescription = "Offline",
                    tint = Grey80,
                    modifier = Modifier.size(20.dp)
                )
            }
            syncState.lastSyncError != null -> {
                Icon(
                    Icons.Default.SyncProblem,
                    contentDescription = "Erro de sincronização",
                    tint = Red40,
                    modifier = Modifier.size(20.dp)
                )
            }
            else -> {
                Icon(
                    Icons.Default.CloudDone,
                    contentDescription = "Sincronizado",
                    tint = Green50,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun SyncStatusBanner(
    syncState: SyncState,
    syncEvent: SyncEvent?,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showBanner by remember { mutableStateOf(false) }
    var bannerContent by remember { mutableStateOf<BannerContent?>(null) }

    LaunchedEffect(syncState.isOnline) {
        if (syncState.isOnline && syncState.lastSyncError == null) {
            delay(500)
            if (bannerContent?.autoDismiss == false) {
                showBanner = false
            }
        }
    }

    LaunchedEffect(syncEvent) {
        syncEvent?.let { event ->
            bannerContent = when (event) {
                is SyncEvent.NetworkRestored -> {
                    showBanner = true
                    BannerContent(
                        message = "Conexão restaurada",
                        color = Green50,
                        icon = Icons.Default.Wifi,
                        autoDismiss = true,
                        dismissDelay = 2000
                    )
                }
                is SyncEvent.NetworkLost -> {
                    showBanner = true
                    BannerContent(
                        message = "Sem conexão - Modo offline",
                        color = Amber60,
                        icon = Icons.Default.WifiOff,
                        autoDismiss = false
                    )
                }
                is SyncEvent.NetworkLosing -> {
                    showBanner = true
                    BannerContent(
                        message = "Conexão instável",
                        color = Amber60,
                        icon = Icons.Default.NetworkCheck,
                        autoDismiss = true,
                        dismissDelay = 2000
                    )
                }
                is SyncEvent.SyncCompleted -> {
                    showBanner = true
                    BannerContent(
                        message = "Sincronização concluída",
                        color = Green50,
                        icon = Icons.Default.CheckCircle,
                        autoDismiss = true,
                        dismissDelay = 2000
                    )
                }
                is SyncEvent.SyncFailed -> {
                    if (syncState.isOnline) {
                        showBanner = true
                        BannerContent(
                            message = "Erro: ${event.error}",
                            color = Red40,
                            icon = Icons.Default.Error,
                            autoDismiss = false,
                            showRetry = true
                        )
                    } else {
                        null
                    }
                }
                is SyncEvent.SyncFailedNoNetwork -> {
                    null
                }
                else -> null
            }

            bannerContent?.let { content ->
                if (content.autoDismiss) {
                    delay(content.dismissDelay)
                    showBanner = false
                }
            }
        }
    }

    AnimatedVisibility(
        visible = showBanner && bannerContent != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        bannerContent?.let { content ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = content.color.copy(alpha = 0.15f)
                ),
                border = BorderStroke(1.dp, content.color.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = content.icon,
                            contentDescription = null,
                            tint = content.color,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            content.message,
                            fontSize = 14.sp,
                            color = content.color,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (content.showRetry) {
                        TextButton(
                            onClick = {
                                onRetryClick()
                                showBanner = false
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = content.color
                            )
                        ) {
                            Text("Tentar novamente", fontSize = 12.sp)
                        }
                    }

                    IconButton(
                        onClick = { showBanner = false },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = content.color.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingSyncButton(
    syncState: SyncState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedRotation by animateFloatAsState(
        targetValue = if (syncState.isSyncing) 360f else 0f,
        animationSpec = if (syncState.isSyncing) {
            infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        } else {
            snap()
        }
    )

    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = when {
            !syncState.isOnline -> Grey40
            syncState.lastSyncError != null -> Red40
            syncState.isSyncing -> Blue40
            else -> Green50
        },
        contentColor = Color.White
    ) {
        Icon(
            imageVector = when {
                !syncState.isOnline -> Icons.Default.CloudOff
                syncState.lastSyncError != null -> Icons.Default.SyncProblem
                else -> Icons.Default.Sync
            },
            contentDescription = "Sincronizar",
            modifier = if (syncState.isSyncing) {
                Modifier.rotate(animatedRotation)
            } else {
                Modifier
            }
        )
    }
}

@Composable
fun ConnectionStatusChip(
    isOnline: Boolean,
    modifier: Modifier = Modifier
) {
    val pulseAnimation = rememberInfiniteTransition()
    val pulseAlpha by pulseAnimation.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (isOnline) Green50.copy(alpha = 0.1f) else Grey40.copy(alpha = 0.1f),
        border = BorderStroke(
            width = 1.dp,
            color = if (isOnline) Green50.copy(alpha = 0.3f) else Grey40.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(
                        if (isOnline) Green50.copy(alpha = pulseAlpha) else Grey40
                    )
            )
            Text(
                text = if (isOnline) "Online" else "Offline",
                fontSize = 11.sp,
                color = if (isOnline) Green50 else Grey40,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun getSyncStatusText(syncState: SyncState): String {
    return when {
        syncState.isSyncing -> "Sincronizando"
        !syncState.isOnline -> "Offline"
        syncState.lastSyncError != null -> "Erro"
        else -> "Sincronizado"
    }
}

private fun getSyncStatusColor(syncState: SyncState): Color {
    return when {
        syncState.isSyncing -> Blue40
        !syncState.isOnline -> Grey80
        syncState.lastSyncError != null -> Red40
        else -> Green50
    }
}

@OptIn(ExperimentalTime::class)
private fun getRelativeTimeString(timestamp: Long): String {
    val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "Agora"
        diff < 3_600_000 -> "${diff / 60_000} min atrás"
        diff < 86_400_000 -> "${diff / 3_600_000}h atrás"
        else -> "${diff / 86_400_000}d atrás"
    }
}

private data class BannerContent(
    val message: String,
    val color: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val autoDismiss: Boolean = false,
    val dismissDelay: Long = 3000,
    val showRetry: Boolean = false
)