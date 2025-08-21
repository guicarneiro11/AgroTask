package com.guicarneirodev.agrotask.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guicarneirodev.agrotask.domain.model.Task
import com.guicarneirodev.agrotask.domain.model.TaskStatus
import com.guicarneirodev.agrotask.domain.sync.SyncState
import com.guicarneirodev.agrotask.presentation.components.ConnectionStatusChip
import com.guicarneirodev.agrotask.presentation.components.ExtendedFABMenu
import com.guicarneirodev.agrotask.presentation.components.SyncStatusBanner
import com.guicarneirodev.agrotask.presentation.components.SyncStatusIndicator
import com.guicarneirodev.agrotask.presentation.theme.Amber60
import com.guicarneirodev.agrotask.presentation.theme.Blue40
import com.guicarneirodev.agrotask.presentation.theme.Green40
import com.guicarneirodev.agrotask.presentation.theme.Green50
import com.guicarneirodev.agrotask.presentation.theme.Green60
import com.guicarneirodev.agrotask.presentation.theme.Grey20
import com.guicarneirodev.agrotask.presentation.theme.Grey30
import com.guicarneirodev.agrotask.presentation.theme.Grey40
import com.guicarneirodev.agrotask.presentation.theme.Grey80
import com.guicarneirodev.agrotask.presentation.theme.Red40
import com.guicarneirodev.agrotask.presentation.viewmodel.TaskViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Composable
fun LongPressButton(
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var job by remember { mutableStateOf<Job?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        job = coroutineScope.launch {
                            delay(2000)
                            if (isPressed) {
                                while (isPressed) {
                                    onLongPress()
                                    delay(150)
                                }
                            }
                        }
                        tryAwaitRelease()
                        job?.cancel()
                        isPressed = false
                    },
                    onTap = {
                        onClick()
                    }
                )
            }
            .scale(if (isPressed) 0.9f else 1f)
    ) {
        content()
    }
}

@Composable
fun TaskScreen(
    viewModel: TaskViewModel = koinViewModel()
) {
    val tasks by viewModel.todayTasks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val syncEvent by viewModel.lastSyncEvent.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TaskHeader(
                syncState = syncState,
                onSyncClick = { viewModel.syncTasks() }
            )

            SyncStatusBanner(
                syncState = syncState,
                syncEvent = syncEvent,
                onRetryClick = { viewModel.retrySync() }
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
                            },
                            onDeleteClick = {
                                taskToDelete = task
                            }
                        )
                    }
                }
            }
        }

        /* Menu FAB expansível com opções de sync e adicionar */
        ExtendedFABMenu(
            syncState = syncState,
            onAddClick = { showAddDialog = true },
            onSyncClick = { viewModel.syncTasks() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )

        if (showAddDialog) {
            AddTaskDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { activity, field, time, status ->
                    viewModel.createTask(activity, field, time, status)
                    showAddDialog = false
                }
            )
        }

        taskToDelete?.let { task ->
            AlertDialog(
                onDismissRequest = { taskToDelete = null },
                containerColor = Grey30,
                title = {
                    Text(
                        "Confirmar Exclusão",
                        color = Red40,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        "Deseja excluir a tarefa '${task.activityName}'?",
                        color = Grey80
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteTask(task.id)
                            taskToDelete = null
                        }
                    ) {
                        Text("Excluir", color = Red40)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { taskToDelete = null }) {
                        Text("Cancelar", color = Grey80)
                    }
                }
            )
        }
    }
}

@Composable
fun TaskHeader(
    syncState: SyncState,
    onSyncClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Grey20,
        shadowElevation = 4.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "Tarefas de Hoje",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Green60
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            "Gerencie suas atividades",
                            fontSize = 14.sp,
                            color = Grey80,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        ConnectionStatusChip(
                            isOnline = syncState.isOnline
                        )
                    }
                }

                Box(
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    SyncStatusIndicator(
                        syncState = syncState,
                        onRetryClick = onSyncClick,
                        showText = true
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TaskCard(
    task: Task,
    onStatusChange: (TaskStatus) -> Unit,
    onDeleteClick: () -> Unit
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TaskStatus.entries.forEach { status ->
                                FilterChip(
                                    selected = task.status == status,
                                    onClick = { onStatusChange(status) },
                                    label = { Text(getStatusText(status), fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = getStatusColor(status).copy(alpha = 0.2f),
                                        selectedLabelColor = getStatusColor(status)
                                    ),
                                    modifier = Modifier.height(32.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Excluir",
                                tint = Red40
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
    onConfirm: (String, String, LocalDateTime, TaskStatus) -> Unit
) {
    var activityName by remember { mutableStateOf("") }
    var field by remember { mutableStateOf("") }
    var selectedHour by remember { mutableStateOf(0) }
    var selectedMinute by remember { mutableStateOf(0) }
    var selectedStatus by remember { mutableStateOf(TaskStatus.PENDING) }

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
                    label = { Text("Nome da atividade") },
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
                    label = { Text("Talhão ou área") },
                    placeholder = { Text("Ex: Talhão A1") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green50,
                        unfocusedBorderColor = Grey40
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Column {
                    Text(
                        "Hora prevista",
                        fontSize = 12.sp,
                        color = Grey80,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Grey40.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                LongPressButton(
                                    onClick = { selectedHour = (selectedHour - 1 + 24) % 24 },
                                    onLongPress = {
                                        selectedHour = (selectedHour - 1 + 24) % 24
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Remove, null, tint = Green60)
                                }
                                Text(
                                    selectedHour.toString().padStart(2, '0'),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                LongPressButton(
                                    onClick = { selectedHour = (selectedHour + 1) % 24 },
                                    onLongPress = {
                                        selectedHour = (selectedHour + 1) % 24
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Add, null, tint = Green60)
                                }
                            }
                        }

                        Text(
                            ":",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Grey40.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                LongPressButton(
                                    onClick = { selectedMinute = (selectedMinute - 1 + 60) % 60 },
                                    onLongPress = {
                                        selectedMinute = (selectedMinute - 1 + 60) % 60
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Remove, null, tint = Green60)
                                }
                                Text(
                                    selectedMinute.toString().padStart(2, '0'),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                LongPressButton(
                                    onClick = { selectedMinute = (selectedMinute + 1) % 60 },
                                    onLongPress = {
                                        selectedMinute = (selectedMinute + 1) % 60
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Add, null, tint = Green60)
                                }
                            }
                        }
                    }
                }

                Column {
                    Text(
                        "Status inicial",
                        fontSize = 12.sp,
                        color = Grey80,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TaskStatus.entries.forEach { status ->
                            Surface(
                                onClick = { selectedStatus = status },
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedStatus == status)
                                    getStatusColor(status).copy(alpha = 0.15f)
                                else
                                    Grey40.copy(alpha = 0.1f),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (selectedStatus == status)
                                        getStatusColor(status)
                                    else
                                        Grey40.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (selectedStatus == status)
                                                    getStatusColor(status)
                                                else
                                                    Grey40
                                            )
                                    )
                                    Text(
                                        getStatusText(status),
                                        color = if (selectedStatus == status)
                                            getStatusColor(status)
                                        else
                                            Grey80,
                                        fontSize = 14.sp,
                                        fontWeight = if (selectedStatus == status)
                                            FontWeight.Medium
                                        else
                                            FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (activityName.isNotBlank() && field.isNotBlank()) {
                        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                        val scheduledTime = LocalDateTime(
                            now.year,
                            now.month.number,
                            now.day,
                            selectedHour,
                            selectedMinute
                        )
                        onConfirm(activityName, field, scheduledTime, selectedStatus)
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