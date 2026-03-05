package com.example.to_do_list.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.to_do_list.data.TaskRepository
import com.example.to_do_list.model.Periodicity
import com.example.to_do_list.model.Priority
import com.example.to_do_list.model.State
import com.example.to_do_list.model.Task
import com.example.to_do_list.notification.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
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
    private val repository: TaskRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    /** Événement émis quand une tâche passe en retard (pour Snackbar dans l'UI) */
    private val _overdueEvent = MutableSharedFlow<String>()
    val overdueEvent = _overdueEvent.asSharedFlow()

    /** Ticker qui émet toutes les 30 secondes pour forcer la vérification du retard */
    private val ticker = flow {
        while (true) {
            emit(System.currentTimeMillis())
            kotlinx.coroutines.delay(30_000L)
        }
    }

    /** Liste de toutes les tâches, avec détection automatique du retard toutes les 30s */
    val tasks: StateFlow<List<Task>> = combine(
        repository.getAllTasksFlow(),
        ticker
    ) { list, now ->
        list.map { task ->
            if (task.state == State.Todo && isOverdue(task, now)) {
                val overdue = task.copy(state = State.Overdue)
                viewModelScope.launch {
                    repository.updateTask(overdue)
                    NotificationHelper.sendOverdueNotification(context, task.id, task.title)
                    _overdueEvent.emit(task.title)
                }
                overdue
            } else {
                task
            }
        }
    }
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
            repository.markTaskAsDone(task)
            _editState.update { it.copy(state = State.Done, isSaved = true) }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { repository.deleteTask(task) }
    }

    // ─── Logique de retard ───────────────────────────────────────────────────

    private fun isOverdue(task: Task, now: Long): Boolean {
        val dateLimit = task.dateLimit ?: return false

        return if (task.hourLimit != null) {
            // Combiner la date limite avec l'heure limite
            val dateCal = Calendar.getInstance().apply { timeInMillis = dateLimit }
            val hourCal = Calendar.getInstance().apply { timeInMillis = task.hourLimit }
            val deadline = Calendar.getInstance().apply {
                set(Calendar.YEAR, dateCal.get(Calendar.YEAR))
                set(Calendar.MONTH, dateCal.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, dateCal.get(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, hourCal.get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, hourCal.get(Calendar.MINUTE))
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            now > deadline.timeInMillis
        } else {
            // Pas d'heure → retard à la fin du jour limite
            val dateCal = Calendar.getInstance().apply {
                timeInMillis = dateLimit
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            now > dateCal.timeInMillis
        }
    }
}
