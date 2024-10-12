package com.kos.tasks.runnables

import com.kos.tasks.TasksService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

data class TasksCleanupRunnable(
    val tasksService: TasksService,
    val coroutineScope: CoroutineScope
) : Runnable {
    override fun run() {
        coroutineScope.launch { tasksService.taskCleanup() }
    }
}