package com.example.to_do_list.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.to_do_list.model.Periodicity
import com.example.to_do_list.model.Priority
import com.example.to_do_list.model.State
import com.example.to_do_list.model.Task
import com.example.to_do_list.ui.components.TaskFilterMenu
import com.example.to_do_list.utils.SortOption
import com.example.to_do_list.utils.TaskFilter
import com.example.to_do_list.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskViewModel,
    onEditTask: (Int) -> Unit,
    onAddTask: () -> Unit
) {
    val tasks by viewModel.tasks.collectAsState()
    var selectedFilter by remember { mutableStateOf<State?>(null) }
    var selectedSort by remember { mutableStateOf(SortOption.None) }
    val filteredTasks = TaskFilter.sort(TaskFilter.filterByState(tasks, selectedFilter), selectedSort)
    var showDeleteDoneDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val hasDoneTasks = tasks.any { it.state == State.Done }

    LaunchedEffect(Unit) {
        viewModel.overdueEvent.collect { taskTitle ->
            snackbarHostState.showSnackbar("⚠️ \"$taskTitle\" est en retard !")
        }
    }

    if (showDeleteDoneDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDoneDialog = false },
            title = { Text("Effacer les tâches effectuées") },
            text = { Text("Toutes les tâches terminées seront supprimées définitivement.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCompletedTasks()
                    showDeleteDoneDialog = false
                }) {
                    Text("Effacer", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDoneDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📋 Mes Tâches", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    if (hasDoneTasks) {
                        IconButton(onClick = { showDeleteDoneDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Effacer les tâches effectuées",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    TaskFilterMenu(
                        selectedFilter = selectedFilter,
                        onFilterChange = { selectedFilter = it },
                        selectedSort = selectedSort,
                        onSortChange = { selectedSort = it }
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTask) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter une tâche")
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color(0xFFB71C1C),
                    contentColor = Color.White
                )
            }
        }

    ) { padding ->
        if (filteredTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aucune tâche.\nAppuyez sur + pour en créer une !",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredTasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onEdit = { onEditTask(task.id) },
                        onDelete = { viewModel.deleteTask(task) }
                    )
                }
            }
        }
    }
}

@Composable
fun TaskCard(
    task: Task,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isDone = task.state == State.Done
    val priorityColor = when (task.priority) {
        Priority.High -> Color(0xFFE53935)
        Priority.Mid -> Color(0xFFFB8C00)
        Priority.Low -> Color(0xFF43A047)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDone)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Badge(
                containerColor = priorityColor,
                modifier = Modifier.padding(end = 12.dp)
            ) {
                val priorityLabel = when (task.priority) {
                    Priority.High -> "⚡ Haute"
                    Priority.Mid -> "➡ Moyenne"
                    Priority.Low -> "↓ Basse"
                }
                Text(
                    text = priorityLabel,
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (isDone) TextDecoration.LineThrough else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (task.description.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(4.dp))
                StateChip(state = task.state)

                if (task.periodicity != Periodicity.None) {
                    Spacer(Modifier.height(4.dp))
                    PeriodicityChip(periodicity = task.periodicity)
                }

                if (task.dateLimit != null || task.hourLimit != null) {
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (task.dateLimit != null) {
                            val dateText = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
                                .format(Date(task.dateLimit))
                            Text(
                                text = "📅 $dateText",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        if (task.hourLimit != null) {
                            val cal = Calendar.getInstance().apply { timeInMillis = task.hourLimit }
                            val timeText = String.format(
                                Locale.FRANCE, "%02d:%02d",
                                cal.get(Calendar.HOUR_OF_DAY),
                                cal.get(Calendar.MINUTE)
                            )
                            Text(
                                text = "🕒 $timeText",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            if (isDone) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Terminée",
                    tint = Color(0xFF43A047),
                    modifier = Modifier.padding(start = 8.dp)
                )
            } else {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Modifier")
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Supprimer",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun StateChip(state: State) {
    val (label, color) = when (state) {
        State.Todo -> "À faire" to Color(0xFF1E88E5)
        State.Overdue -> "En retard" to Color(0xFFE53935)
        State.Done -> "Terminée" to Color(0xFF43A047)
    }
    Badge(containerColor = color) {
        Text(text = label, color = Color.White, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun PeriodicityChip(periodicity: Periodicity) {
    val (label, color) = when (periodicity) {
        Periodicity.Daily -> "🔁 Quotidien" to Color(0xFF8E24AA)
        Periodicity.Weekly -> "🔁 Hebdomadaire" to Color(0xFF5E35B1)
        Periodicity.Monthly -> "🔁 Mensuel" to Color(0xFF1E88E5)
        Periodicity.None -> return
    }
    Badge(containerColor = color) {
        Text(text = label, color = Color.White, style = MaterialTheme.typography.labelSmall)
    }
}

