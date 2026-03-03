package com.example.to_do_list.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.to_do_list.model.Periodicity
import com.example.to_do_list.model.Priority
import com.example.to_do_list.model.State
import com.example.to_do_list.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskScreen(
    taskId: Int,
    viewModel: TaskViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.editState.collectAsState()

    // Chargement de la tâche au lancement de l'écran
    LaunchedEffect(taskId) {
        viewModel.loadTask(taskId)
    }

    // Navigation automatique après sauvegarde
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            viewModel.resetEditState()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Modifier la tâche", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ─── Titre ───────────────────────────────────────────────────────
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text("Titre *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // ─── Description ─────────────────────────────────────────────────
            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            // ─── Priorité ────────────────────────────────────────────────────
            EnumDropdown(
                label = "Priorité",
                options = Priority.entries,
                selected = state.priority,
                displayName = { it.name },
                onSelect = viewModel::onPriorityChange
            )

            // ─── Périodicité ──────────────────────────────────────────────────
            EnumDropdown(
                label = "Périodicité",
                options = Periodicity.entries,
                selected = state.periodicity,
                displayName = {
                    when (it) {
                        Periodicity.None -> "Aucune"
                        Periodicity.Daily -> "Quotidienne"
                        Periodicity.Weekly -> "Hebdomadaire"
                        Periodicity.Monthly -> "Mensuelle"
                    }
                },
                onSelect = viewModel::onPeriodicityChange
            )

            // ─── État actuel (lecture seule si Done) ─────────────────────────
            if (state.state != State.Done) {
                EnumDropdown(
                    label = "État",
                    options = State.entries.filter { it != State.Done },
                    selected = state.state,
                    displayName = {
                        when (it) {
                            State.Todo -> "À faire"
                            State.Overdue -> "En retard"
                            State.Done -> "Terminée"
                        }
                    },
                    onSelect = viewModel::onStateChange
                )
            }

            Spacer(Modifier.height(8.dp))

            // ─── Bouton Sauvegarder ───────────────────────────────────────────
            FilledTonalButton(
                onClick = viewModel::saveEditedTask,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.title.isNotBlank()
            ) {
                Text("Sauvegarder les modifications")
            }

            // ─── Bouton Terminer la tâche ────────────────────────────────────
            if (state.state != State.Done) {
                Button(
                    onClick = viewModel::markTaskAsDone,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF43A047)
                    ),
                    enabled = state.title.isNotBlank()
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Text("Terminer la tâche", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Text(
                    text = "✅ Cette tâche est déjà terminée.",
                    color = Color(0xFF43A047),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> EnumDropdown(
    label: String,
    options: List<T>,
    selected: T,
    displayName: (T) -> String,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = displayName(selected),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(displayName(option)) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

