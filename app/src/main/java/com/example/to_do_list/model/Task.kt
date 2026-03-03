package com.example.to_do_list.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val priority: Priority = Priority.Low,
    val state: State = State.Todo,
    val dateLimit: Long? = null,
    val hourLimit: Long? = null,
    val periodicity: Periodicity = Periodicity.None,
    val photoPath: String? = null
)

