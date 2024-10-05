package com.kos.tasks

import com.kos.auth.AuthService
import com.kos.characters.CharactersService
import com.kos.common.WithLogger
import com.kos.datacache.DataCacheService
import com.kos.tasks.repository.TasksRepository
import kotlinx.coroutines.CoroutineScope
import java.time.Duration
import java.time.OffsetDateTime
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

data class TasksService(
    private val tasksRepository: TasksRepository,
    private val executorService: ScheduledExecutorService,
    private val authService: AuthService,
    private val dataCacheService: DataCacheService,
    private val charactersService: CharactersService,
    private val coroutineScope: CoroutineScope
) : WithLogger("tasksService") {

    suspend fun launchTasks() {
        fun calculateTaskDelay(lastInsertion: OffsetDateTime, now: OffsetDateTime): Long {
            val differenceBetweenNowAndLastExecutionTime = Duration.between(lastInsertion, now).toMinutes()
            return if (differenceBetweenNowAndLastExecutionTime >= 60) 0
            else 60 - differenceBetweenNowAndLastExecutionTime
        }

        val now = OffsetDateTime.now()

        val cacheDataTaskInitDelay: Long =
            tasksRepository.getLastExecution(TaskType.CACHE_LOL_DATA_TASK)
                .also { tasksRepository.getLastExecution(TaskType.CACHE_WOW_DATA_TASK) }?.inserted?.let {
                    calculateTaskDelay(it, now)
                } ?: 0

        val tokenCleanupInitDelay: Long =
            tasksRepository.getLastExecution(TaskType.TOKEN_CLEANUP_TASK)?.inserted?.let {
                calculateTaskDelay(it, now)
            } ?: 0

        logger.info("Setting $cacheDataTaskInitDelay minutes of delay before launching ${TaskType.CACHE_WOW_DATA_TASK} and ${TaskType.CACHE_LOL_DATA_TASK}")
        logger.info("Setting $tokenCleanupInitDelay minutes of delay before launching ${TaskType.TOKEN_CLEANUP_TASK}")


        executorService.scheduleAtFixedRate(
            TokenCleanupTask(tasksRepository, authService, coroutineScope),
            tokenCleanupInitDelay, 60, TimeUnit.MINUTES
        )

        executorService.scheduleAtFixedRate(
            CacheDataTask(tasksRepository, dataCacheService, charactersService, coroutineScope),
            cacheDataTaskInitDelay, 60, TimeUnit.MINUTES
        )

        Runtime.getRuntime().addShutdownHook(Thread {
            executorService.shutdown()
        })
    }
}