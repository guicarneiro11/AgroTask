package com.guicarneirodev.agrotask.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guicarneirodev.agrotask.domain.model.Task
import com.guicarneirodev.agrotask.domain.model.TaskStatus
import com.guicarneirodev.agrotask.presentation.theme.*
import com.guicarneirodev.agrotask.presentation.viewmodel.TaskViewModel
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Composable
fun TaskScreen(
    viewModel: TaskViewModel = koinViewModel()
) {
    val tasks by viewModel.todayTasks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TaskHeader(
                onSyncClick = { viewModel.syncTasks() },
                isSyncing = isSyncing
            )

            if (tasks.isEmpty() && !isLoading) {
                EmptyTasksState(
                    onAddClick = { showAddDialog = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            onStatusChange = { newStatus ->
                                viewModel.updateTaskStatus(task.id, newStatus)
                            }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Green50,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, "Adicionar Tarefa")
        }

        if (showAddDialog) {
            AddTaskDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { activity, field, time ->
                    viewModel.createTask(activity, field, time)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun TaskHeader(
    onSyncClick: () -> Unit,
    isSyncing: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Grey20,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Tarefas de Hoje",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Green60
                )
                Text(
                    "Gerencie suas atividades do campo",
                    fontSize = 14.sp,
                    color = Grey80
                )
            }

            IconButton(
                onClick = onSyncClick,
                enabled = !isSyncing
            ) {
                val rotation by animateFloatAsState(
                    targetValue = if (isSyncing) 360f else 0f,
                    animationSpec = if (isSyncing) {
                        infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        )
                    } else {
                        tween(0)
                    }
                )
                Icon(
                    Icons.Default.Sync,
                    contentDescription = "Sincronizar",
                    tint = if (isSyncing) Green60 else Grey80,
                    modifier = Modifier.rotate(rotation)
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TaskCard(
    task: Task,
    onStatusChange: (TaskStatus) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (expanded) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Grey30
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        task.activityName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Landscape,
                            contentDescription = null,
                            tint = Green60,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            task.field,
                            fontSize = 14.sp,
                            color = Grey80
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Blue40,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            formatTime(task.scheduledTime),
                            fontSize = 14.sp,
                            color = Grey80
                        )
                    }
                }

                StatusChip(
                    status = task.status,
                    onClick = {
                        val nextStatus = when (task.status) {
                            TaskStatus.PENDING -> TaskStatus.IN_PROGRESS
                            TaskStatus.IN_PROGRESS -> TaskStatus.COMPLETED
                            TaskStatus.COMPLETED -> TaskStatus.PENDING
                        }
                        onStatusChange(nextStatus)
                    }
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, color = Grey40)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TaskStatus.entries.forEach { status ->
                            FilterChip(
                                selected = task.status == status,
                                onClick = { onStatusChange(status) },
                                label = { Text(getStatusText(status)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = getStatusColor(status).copy(alpha = 0.2f),
                                    selectedLabelColor = getStatusColor(status)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(
    status: TaskStatus,
    onClick: () -> Unit
) {
    val color = getStatusColor(status)
    val text = getStatusText(status)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text,
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun EmptyTasksState(onAddClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Agriculture,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Green40
            )
            Text(
                "Nenhuma tarefa para hoje",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Grey80
            )
            Text(
                "Adicione suas atividades do campo",
                fontSize = 14.sp,
                color = Grey80.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green50
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Adicionar Tarefa")
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, LocalDateTime) -> Unit
) {
    var activityName by remember { mutableStateOf("") }
    var field by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Grey30,
        title = {
            Text(
                "Nova Tarefa",
                color = Green60,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = activityName,
                    onValueChange = { activityName = it },
                    label = { Text("Atividade") },
                    placeholder = { Text("Ex: Plantio de Milho") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green50,
                        unfocusedBorderColor = Grey40
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = field,
                    onValueChange = { field = it },
                    label = { Text("Talhão/Área") },
                    placeholder = { Text("Ex: Talhão A1") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green50,
                        unfocusedBorderColor = Grey40
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (activityName.isNotBlank() && field.isNotBlank()) {
                        onConfirm(
                            activityName,
                            field,
                            Clock.System.now()
                                .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                        )
                    }
                },
                enabled = activityName.isNotBlank() && field.isNotBlank()
            ) {
                Text("Adicionar", color = Green60)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Grey80)
            }
        }
    )
}

fun getStatusColor(status: TaskStatus): Color = when (status) {
    TaskStatus.PENDING -> Amber60
    TaskStatus.IN_PROGRESS -> Blue40
    TaskStatus.COMPLETED -> Green60
}

fun getStatusText(status: TaskStatus): String = when (status) {
    TaskStatus.PENDING -> "Pendente"
    TaskStatus.IN_PROGRESS -> "Em Andamento"
    TaskStatus.COMPLETED -> "Concluída"
}

fun formatTime(dateTime: LocalDateTime): String {
    return "${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
}