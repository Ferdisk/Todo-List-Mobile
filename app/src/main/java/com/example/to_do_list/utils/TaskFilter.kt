package com.example.to_do_list.utils

import com.example.to_do_list.model.Periodicity
import com.example.to_do_list.model.Priority
import com.example.to_do_list.model.State
import com.example.to_do_list.model.Task

enum class SortOption {
    None,
    PriorityAsc,
    PriorityDesc,
    Periodicity
}

/**
 * Classe utilitaire pour gérer le filtrage et le tri des tâches
 */
object TaskFilter {

    fun filterByState(tasks: List<Task>, filterState: State?): List<Task> {
        return if (filterState != null) {
            tasks.filter { it.state == filterState }
        } else {
            tasks
        }
    }

    fun sort(tasks: List<Task>, sortOption: SortOption): List<Task> {
        val priorityOrder = listOf(Priority.High, Priority.Mid, Priority.Low)
        val periodicityOrder = listOf(
            Periodicity.Daily,
            Periodicity.Weekly,
            Periodicity.Monthly,
            Periodicity.None
        )
        return when (sortOption) {
            SortOption.None -> tasks
            SortOption.PriorityAsc -> tasks.sortedBy { priorityOrder.indexOf(it.priority) }
            SortOption.PriorityDesc -> tasks.sortedByDescending { priorityOrder.indexOf(it.priority) }
            SortOption.Periodicity -> tasks.sortedBy { periodicityOrder.indexOf(it.periodicity) }
        }
    }
}
