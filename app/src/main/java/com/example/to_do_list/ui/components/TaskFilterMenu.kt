package com.example.to_do_list.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.to_do_list.model.State
import com.example.to_do_list.utils.SortOption

@Composable
fun TaskFilterMenu(
    selectedFilter: State?,
    onFilterChange: (State?) -> Unit,
    selectedSort: SortOption = SortOption.None,
    onSortChange: (SortOption) -> Unit = {}
) {
    var showFilterMenu by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { showFilterMenu = !showFilterMenu }) {
            Icon(Icons.Default.FilterList, contentDescription = "Filtrer / Trier")
        }
        DropdownMenu(
            expanded = showFilterMenu,
            onDismissRequest = { showFilterMenu = false }
        ) {
            // ── Section Filtres ──
            Text(
                text = "  FILTRER PAR ÉTAT",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            DropdownMenuItem(
                text = { Text(if (selectedFilter == null) "✔ Toutes les tâches" else "Toutes les tâches") },
                onClick = {
                    onFilterChange(null)
                    showFilterMenu = false
                }
            )
            State.entries.forEach { state ->
                DropdownMenuItem(
                    text = { Text(if (selectedFilter == state) "✔ ${getStateLabel(state)}" else getStateLabel(state)) },
                    onClick = {
                        onFilterChange(state)
                        showFilterMenu = false
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))

            // ── Section Tri ──
            Text(
                text = "  TRIER PAR",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            DropdownMenuItem(
                text = { Text(if (selectedSort == SortOption.None) "✔ Ordre par défaut" else "Ordre par défaut") },
                onClick = {
                    onSortChange(SortOption.None)
                    showFilterMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text(if (selectedSort == SortOption.PriorityAsc) "✔ Priorité ↑ (Haute → Basse)" else "Priorité ↑ (Haute → Basse)") },
                onClick = {
                    onSortChange(SortOption.PriorityAsc)
                    showFilterMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text(if (selectedSort == SortOption.PriorityDesc) "✔ Priorité ↓ (Basse → Haute)" else "Priorité ↓ (Basse → Haute)") },
                onClick = {
                    onSortChange(SortOption.PriorityDesc)
                    showFilterMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text(if (selectedSort == SortOption.Periodicity) "✔ Périodicité" else "Périodicité") },
                onClick = {
                    onSortChange(SortOption.Periodicity)
                    showFilterMenu = false
                }
            )
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
