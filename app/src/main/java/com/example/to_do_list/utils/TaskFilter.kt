package com.example.to_do_list.utils

import com.example.to_do_list.model.State
import com.example.to_do_list.model.Task

/**
 * Classe utilitaire pour gérer le filtrage des tâches
 */
object TaskFilter {
    /**
     * Filtre une liste de tâches selon l'état sélectionné
     * @param tasks Liste complète des tâches
     * @param filterState État de filtre sélectionné (null = toutes les tâches)
     * @return Liste filtrée des tâches
     */
    fun filterByState(tasks: List<Task>, filterState: State?): List<Task> {
        return if (filterState != null) {
            tasks.filter { it.state == filterState }
        } else {
            tasks
        }
    }
}
