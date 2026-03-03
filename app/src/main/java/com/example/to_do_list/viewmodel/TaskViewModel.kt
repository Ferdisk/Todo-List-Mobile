package com.example.to_do_list.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.to_do_list.data.TaskRepository
import com.example.to_do_list.model.Periodicity
import com.example.to_do_list.model.Priority
import com.example.to_do_list.model.State
import com.example.to_do_list.model.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditTaskUiState(
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val priority: Priority = Priority.Low,
    val state: State = State.Todo,
    val dateLimit: Long? = null,
    val hourLimit: Long? = null,
    val periodicity: Periodicity = Periodicity.None,
    val photoPath: String? = null,
    val isSaved: Boolean = false
)

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    /** Liste de toutes les tâches, exposée en StateFlow */
    val tasks: StateFlow<List<Task>> = repository.getAllTasksFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /** État de l'écran d'édition */
    private val _editState = MutableStateFlow(EditTaskUiState())
    val editState: StateFlow<EditTaskUiState> = _editState.asStateFlow()

    // ─── Chargement d'une tâche dans le formulaire ───────────────────────────

    fun loadTask(taskId: Int) {
        viewModelScope.launch {
            val task = repository.getTaskById(taskId) ?: return@launch
            _editState.update {
                EditTaskUiState(
                    id = task.id,
                    title = task.title,
                    description = task.description,
                    priority = task.priority,
                    state = task.state,
                    dateLimit = task.dateLimit,
                    hourLimit = task.hourLimit,
                    periodicity = task.periodicity,
                    photoPath = task.photoPath,
                    isSaved = false
                )
            }
        }
    }

    fun resetEditState() {
        _editState.update { EditTaskUiState() }
    }

    // ─── Mise à jour des champs du formulaire ────────────────────────────────

    fun onTitleChange(value: String) = _editState.update { it.copy(title = value) }
    fun onDescriptionChange(value: String) = _editState.update { it.copy(description = value) }
    fun onPriorityChange(value: Priority) = _editState.update { it.copy(priority = value) }
    fun onStateChange(value: State) = _editState.update { it.copy(state = value) }
    fun onDateLimitChange(value: Long?) = _editState.update { it.copy(dateLimit = value) }
    fun onHourLimitChange(value: Long?) = _editState.update { it.copy(hourLimit = value) }
    fun onPeriodicityChange(value: Periodicity) = _editState.update { it.copy(periodicity = value) }
    fun onPhotoPathChange(value: String?) = _editState.update { it.copy(photoPath = value) }

    // ─── Actions CRUD ────────────────────────────────────────────────────────

    fun addTask(task: Task) {
        viewModelScope.launch { repository.insertTask(task) }
    }

    /** Sauvegarde les modifications du formulaire en base */
    fun saveEditedTask() {
        val s = _editState.value
        val updatedTask = Task(
            id = s.id,
            title = s.title.trim(),
            description = s.description.trim(),
            priority = s.priority,
            state = s.state,
            dateLimit = s.dateLimit,
            hourLimit = s.hourLimit,
            periodicity = s.periodicity,
            photoPath = s.photoPath
        )
        viewModelScope.launch {
            repository.updateTask(updatedTask)
            _editState.update { it.copy(isSaved = true) }
        }
    }

    /** Met la tâche en état 'Done' et sauvegarde */
    fun markTaskAsDone() {
        val s = _editState.value
        val task = Task(
            id = s.id,
            title = s.title.trim(),
            description = s.description.trim(),
            priority = s.priority,
            state = State.Done,
            dateLimit = s.dateLimit,
            hourLimit = s.hourLimit,
            periodicity = s.periodicity,
            photoPath = s.photoPath
        )
        viewModelScope.launch {
            repository.updateTask(task)
            _editState.update { it.copy(state = State.Done, isSaved = true) }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { repository.deleteTask(task) }
    }
}

