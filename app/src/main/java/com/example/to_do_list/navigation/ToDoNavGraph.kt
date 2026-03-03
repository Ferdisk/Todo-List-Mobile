package com.example.to_do_list.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.to_do_list.ui.screens.AddTaskScreen
import com.example.to_do_list.ui.screens.EditTaskScreen
import com.example.to_do_list.ui.screens.TaskListScreen
import com.example.to_do_list.viewmodel.TaskViewModel

@Composable
fun ToDoNavGraph() {
    val navController = rememberNavController()
    val viewModel: TaskViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.TaskList.route
    ) {
        composable(Screen.TaskList.route) {
            TaskListScreen(
                viewModel = viewModel,
                onEditTask = { taskId ->
                    navController.navigate(Screen.EditTask.createRoute(taskId))
                },
                onAddTask = {
                    navController.navigate(Screen.AddTask.route)
                }
            )
        }

        composable(
            route = Screen.EditTask.route,
            arguments = listOf(navArgument("taskId") { type = NavType.IntType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("taskId") ?: return@composable
            EditTaskScreen(
                taskId = taskId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AddTask.route) {
            AddTaskScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

