

package com.kos.tasks

import com.kos.characters.CharactersService
import com.kos.datacache.DataCacheService
import com.kos.tasks.repository.TasksRepository
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

    override fun run() {
        coroutineScope.launch {
            logger.info("Running filling cache data task")
            val characters = charactersService.get()
            val errors = dataCacheService.cache(characters)
            val deletedRecords = dataCacheService.clear()
            logger.info("Deleted $deletedRecords cached records")
            if (errors.isEmpty()) {
                tasksRepository.insertTask(
                    Task.apply(
                        TaskType.CACHE_DATA_TASK,
                        TaskStatus(Status.SUCCESSFUL, "Deleted $deletedRecords cached records"),
                        OffsetDateTime.now()
                    )
                )
            }
            else {
                tasksRepository.insertTask(
                    Task.apply(
                        TaskType.CACHE_DATA_TASK,
                        TaskStatus(Status.ERROR, errors.joinToString(",\n") { it.error() }),
                        OffsetDateTime.now()
                    )
                )
            }
        }
    }
}
