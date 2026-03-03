package com.example.to_do_list.navigation

sealed class Screen(val route: String) {
    object TaskList : Screen("task_list")
    object EditTask : Screen("edit_task/{taskId}") {
        fun createRoute(taskId: Int) = "edit_task/$taskId"
    }
    object AddTask : Screen("add_task")
}

