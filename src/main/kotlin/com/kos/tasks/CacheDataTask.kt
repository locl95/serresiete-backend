package com.kos.tasks

import com.kos.characters.CharactersService
import com.kos.common.HttpError
import com.kos.datacache.DataCacheService
import com.kos.tasks.repository.TasksRepository
import com.kos.views.Game
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.time.OffsetDateTime

data class CacheDataTask(
    val tasksRepository: TasksRepository,
    val dataCacheService: DataCacheService,
    val charactersService: CharactersService,
    val coroutineScope: CoroutineScope
) : Runnable {

    private val logger = LoggerFactory.getLogger(CacheDataTask::class.java)
    private suspend fun dealWithErrors(errors: List<HttpError>, type: TaskType, successMessage: String) {
        if (errors.isEmpty()) {
            tasksRepository.insertTask(
                Task.apply(
                    type,
                    TaskStatus(Status.SUCCESSFUL, successMessage),
                    OffsetDateTime.now()
                )
            )
        } else {
            tasksRepository.insertTask(
                Task.apply(
                    type,
                    TaskStatus(Status.ERROR, errors.joinToString(",\n") { it.error() }),
                    OffsetDateTime.now()
                )
            )
        }
    }

    override fun run() {
        coroutineScope.launch {
            logger.info("Running filling cache data task")
            val wowCharacters = charactersService.get(Game.WOW)
            val lolCharacters = charactersService.get(Game.LOL)
            val wowErrors = dataCacheService.cache(wowCharacters, Game.WOW)
            val lolErrors = dataCacheService.cache(lolCharacters, Game.LOL)
            val deletedRecords = dataCacheService.clear()
            val deletionMessage = "Deleted $deletedRecords cached records"
            logger.info(deletionMessage)
            dealWithErrors(wowErrors, TaskType.CACHE_WOW_DATA_TASK, deletionMessage)
            dealWithErrors(lolErrors, TaskType.CACHE_LOL_DATA_TASK, deletionMessage)
        }
    }
}