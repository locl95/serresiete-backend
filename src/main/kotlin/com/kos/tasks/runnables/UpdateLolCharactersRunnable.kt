package com.kos.tasks.runnables

import com.kos.common.WithLogger
import com.kos.datacache.DataCacheService
import com.kos.tasks.*
import com.kos.views.Game
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*

data class UpdateLolCharactersRunnable(
    val tasksService: TasksService,
    val coroutineScope: CoroutineScope,
) : Runnable, WithLogger("updateLolCharacters") {

    override fun run() {
        coroutineScope.launch {
            logger.info("Running update lol characters task")
            tasksService.updateLolCharacters(UUID.randomUUID().toString())
            logger.info("Finished running lol characters task")
        }
    }
}