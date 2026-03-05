package com.example.to_do_list.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.to_do_list.model.State

@Composable
fun TaskFilterMenu(
    selectedFilter: State?,
    onFilterChange: (State?) -> Unit
) {
    var showFilterMenu by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { showFilterMenu = !showFilterMenu }) {
            Icon(Icons.Default.FilterList, contentDescription = "Filtrer")
        }
        DropdownMenu(
            expanded = showFilterMenu,
            onDismissRequest = { showFilterMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Toutes les tâches") },
                onClick = {
                    onFilterChange(null)
                    showFilterMenu = false
                }
            )

            State.entries.forEach { state ->
                DropdownMenuItem(
                    text = { Text(getStateLabel(state)) },
                    onClick = {
                        onFilterChange(state)
                        showFilterMenu = false
                    }
                )
            }
        }
    }
}

fun getStateLabel(state: State): String {
    return when (state) {
        State.Todo -> "À faire"
        State.Overdue -> "En retard"
        State.Done -> "Terminée"
    }
}
