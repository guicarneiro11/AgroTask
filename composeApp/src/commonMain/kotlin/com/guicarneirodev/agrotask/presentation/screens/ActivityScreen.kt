package com.guicarneirodev.agrotask.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.guicarneirodev.agrotask.domain.model.ActivityRecord
import com.guicarneirodev.agrotask.presentation.theme.*
import com.guicarneirodev.agrotask.presentation.viewmodel.ActivityViewModel
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class, ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    viewModel: ActivityViewModel = koinViewModel()
) {
    val activityRecords by viewModel.activityRecords.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentActivity by viewModel.currentActivity.collectAsState()
    var showHistorySheet by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            ActivityHeader()

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Grey30
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        "Registrar Nova Atividade",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Green60
                    )

                    ActivityTypeSelector(
                        selectedType = currentActivity.activityType,
                        onTypeSelected = viewModel::updateActivityType
                    )

                    OutlinedTextField(
                        value = currentActivity.field,
                        onValueChange = viewModel::updateField,
                        label = { Text("Talhão/Área") },
                        placeholder = { Text("Ex: Talhão B2") },
                        leadingIcon = {
                            Icon(Icons.Default.Landscape, contentDescription = null, tint = Green50)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Green50,
                            unfocusedBorderColor = Grey40,
                            focusedLabelColor = Green60
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TimePickerCard(
                            label = "Início",
                            time = currentActivity.startTime,
                            onClick = { showStartTimePicker = true },
                            modifier = Modifier.weight(1f)
                        )

                        TimePickerCard(
                            label = "Fim",
                            time = currentActivity.endTime,
                            onClick = { showEndTimePicker = true },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = currentActivity.observations,
                        onValueChange = viewModel::updateObservations,
                        label = { Text("Observações") },
                        placeholder = { Text("Adicione detalhes importantes...") },
                        leadingIcon = {
                            Icon(Icons.AutoMirrored.Filled.Note, contentDescription = null, tint = Green50)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Green50,
                            unfocusedBorderColor = Grey40,
                            focusedLabelColor = Green60
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = { viewModel.saveActivityRecord() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = currentActivity.isValid() && !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Green50,
                            disabledContainerColor = Grey40
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Salvar Atividade",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            RecentActivitiesPreview(
                activities = activityRecords.take(3),
                onViewAllClick = { showHistorySheet = true }
            )
        }

        if (showStartTimePicker) {
            TimePickerDialog(
                initialTime = currentActivity.startTime ?: kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                onTimeSelected = { time ->
                    viewModel.updateStartTime(time)
                    showStartTimePicker = false
                },
                onDismiss = { showStartTimePicker = false },
                title = "Horário de Início"
            )
        }

        if (showEndTimePicker) {
            TimePickerDialog(
                initialTime = currentActivity.endTime ?: kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                onTimeSelected = { time ->
                    viewModel.updateEndTime(time)
                    showEndTimePicker = false
                },
                onDismiss = { showEndTimePicker = false },
                title = "Horário de Fim"
            )
        }

        if (showHistorySheet) {
            ModalBottomSheet(
                onDismissRequest = { showHistorySheet = false },
                containerColor = Grey20,
                contentColor = Color.White
            ) {
                ActivityHistorySheet(
                    activities = activityRecords,
                    onClose = { showHistorySheet = false }
                )
            }
        }
    }
}

@Composable
fun TimePickerDialog(
    initialTime: LocalDateTime,
    onTimeSelected: (LocalDateTime) -> Unit,
    onDismiss: () -> Unit,
    title: String
) {
    var selectedHour by remember { mutableStateOf(initialTime.hour) }
    var selectedMinute by remember { mutableStateOf(initialTime.minute) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Grey30)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Green60
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(
                            onClick = {
                                selectedHour = (selectedHour + 1) % 24
                            }
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowUp,
                                contentDescription = null,
                                tint = Green60
                            )
                        }

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Grey40
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                selectedHour.toString().padStart(2, '0'),
                                modifier = Modifier.padding(16.dp),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        IconButton(
                            onClick = {
                                selectedHour = if (selectedHour == 0) 23 else selectedHour - 1
                            }
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Green60
                            )
                        }
                    }

                    Text(
                        ":",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(
                            onClick = {
                                selectedMinute = (selectedMinute + 5) % 60
                            }
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowUp,
                                contentDescription = null,
                                tint = Green60
                            )
                        }

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Grey40
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                selectedMinute.toString().padStart(2, '0'),
                                modifier = Modifier.padding(16.dp),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        IconButton(
                            onClick = {
                                selectedMinute = if (selectedMinute < 5) 55 else selectedMinute - 5
                            }
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Green60
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Grey80
                        )
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            val newTime = LocalDateTime(
                                initialTime.year,
                                initialTime.month.number,
                                initialTime.day,
                                selectedHour,
                                selectedMinute
                            )
                            onTimeSelected(newTime)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Green50
                        )
                    ) {
                        Text("Confirmar")
                    }
                }
            }
        }
    }
}

@Composable
fun TimePickerCard(
    label: String,
    time: LocalDateTime?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Grey40.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                fontSize = 12.sp,
                color = Grey80,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = if (time != null) Green50 else Grey80,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    if (time != null) {
                        "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"
                    } else {
                        "--:--"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (time != null) Color.White else Grey80
                )
            }
        }
    }
}

@Composable
fun ActivityHeader() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
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
                .padding(20.dp)
        ) {
            Column {
                Text(
                    "Registro de Atividades",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Green60
                )
                Text(
                    "Documente o trabalho realizado no campo",
                    fontSize = 14.sp,
                    color = Grey80
                )
            }
        }
    }
}

@Composable
fun ActivityTypeSelector(
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    val activityTypes = listOf(
        "Plantio" to Icons.Default.Grass,
        "Irrigação" to Icons.Default.Water,
        "Aplicação" to Icons.Default.Vaccines,
        "Colheita" to Icons.Default.Agriculture,
        "Preparo Solo" to Icons.Default.Landscape,
        "Manutenção" to Icons.Default.Build
    )

    Column {
        Text(
            "Tipo de Atividade",
            fontSize = 14.sp,
            color = Grey80,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(activityTypes) { (type, icon) ->
                ActivityTypeChip(
                    type = type,
                    icon = icon,
                    isSelected = selectedType == type,
                    onClick = { onTypeSelected(type) }
                )
            }
        }
    }
}

@Composable
fun ActivityTypeChip(
    type: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedColor by animateColorAsState(
        targetValue = if (isSelected) Green50 else Grey40,
        animationSpec = tween(300)
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Green50.copy(alpha = 0.15f) else Grey40.copy(alpha = 0.1f),
        border = BorderStroke(
            width = 1.dp,
            color = animatedColor
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = animatedColor,
                modifier = Modifier.size(18.dp)
            )
            Text(
                type,
                color = if (isSelected) Green50 else Grey80,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun RecentActivitiesPreview(
    activities: List<ActivityRecord>,
    onViewAllClick: () -> Unit
) {
    if (activities.isNotEmpty()) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Atividades Recentes",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Green60
                )
                TextButton(onClick = onViewAllClick) {
                    Text("Ver Todas", color = Green50)
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Green50,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            activities.forEach { activity ->
                ActivityRecordCard(activity)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ActivityRecordCard(activity: ActivityRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Grey30.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    activity.activityType,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    "${activity.field} • ${formatActivityTime(activity.startTime)} - ${formatActivityTime(activity.endTime)}",
                    fontSize = 12.sp,
                    color = Grey80
                )
            }
            if (activity.syncedWithFirebase) {
                Icon(
                    Icons.Default.CloudDone,
                    contentDescription = "Sincronizado",
                    tint = Green50,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Icon(
                    Icons.Default.CloudOff,
                    contentDescription = "Não sincronizado",
                    tint = Amber60,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ActivityHistorySheet(
    activities: List<ActivityRecord>,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Histórico de Atividades",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Green60
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Grey80)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.heightIn(max = 400.dp)
        ) {
            items(activities) { activity ->
                ActivityRecordCard(activity)
            }
        }
    }
}

fun formatActivityTime(dateTime: LocalDateTime): String {
    return "${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
}