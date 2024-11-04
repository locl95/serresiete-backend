package com.kos.tasks.runnables

import com.kos.tasks.TasksService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import java.util.*


data class TokenCleanupRunnable(
    val tasksService: TasksService,
    val coroutineScope: CoroutineScope
) : Runnable {

    override fun run() {
        coroutineScope.launch { tasksService.tokenCleanup(UUID.randomUUID().toString()) }
    }
}



