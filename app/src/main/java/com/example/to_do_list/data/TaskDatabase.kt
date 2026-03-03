package com.example.to_do_list.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.to_do_list.model.Periodicity
import com.example.to_do_list.model.Priority
import com.example.to_do_list.model.State
import com.example.to_do_list.model.Task

class Converters {
    @TypeConverter
    fun fromPriority(value: Priority): String = value.name

    @TypeConverter
    fun toPriority(value: String): Priority = Priority.valueOf(value)

    @TypeConverter
    fun fromState(value: State): String = value.name

    @TypeConverter
    fun toState(value: String): State = State.valueOf(value)

    @TypeConverter
    fun fromPeriodicity(value: Periodicity): String = value.name

    @TypeConverter
    fun toPeriodicity(value: String): Periodicity = Periodicity.valueOf(value)
}

@Database(entities = [Task::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        const val DATABASE_NAME = "task_database"
    }
}

