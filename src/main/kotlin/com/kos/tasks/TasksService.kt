package com.kos.tasks

import com.kos.auth.AuthService
import com.kos.characters.CharactersService
import com.kos.common.WithLogger
import com.kos.datacache.DataCacheService
import com.kos.tasks.repository.TasksRepository
import com.kos.views.Game
import java.time.OffsetDateTime
import java.util.*

data class TasksService(
    private val tasksRepository: TasksRepository,
    private val dataCacheService: DataCacheService,
    private val charactersService: CharactersService,
    private val authService: AuthService
) : WithLogger("tasksService") {

    private val olderThanDays: Long = 7

    suspend fun get() = tasksRepository.get()

    suspend fun get(id: String) = tasksRepository.get(id)

    suspend fun runTask(taskType: TaskType) {
        when (taskType) {
            TaskType.TOKEN_CLEANUP_TASK -> tokenCleanup()
            TaskType.CACHE_LOL_DATA_TASK -> cacheDataTask(Game.LOL, taskType)
            TaskType.CACHE_WOW_DATA_TASK -> cacheDataTask(Game.WOW, taskType)
            TaskType.TASK_CLEANUP_TASK -> taskCleanup()
        }
    }

    suspend fun taskCleanup() {
        logger.info("Running task cleanup task")
        val deletedTasks = tasksRepository.deleteOldTasks(olderThanDays)
        logger.info("Deleted $deletedTasks old tasks")
        tasksRepository.insertTask(
            Task.apply(
                UUID.randomUUID().toString(),
                TaskType.TASK_CLEANUP_TASK,
                TaskStatus(Status.SUCCESSFUL, "Deleted $deletedTasks old tasks"),
                OffsetDateTime.now()
            )
        )
    }

    suspend fun tokenCleanup() {
        logger.info("Running token cleanup task")
        val deletedTokens = authService.deleteExpiredTokens()
        logger.info("Deleted $deletedTokens expired tokens")
        tasksRepository.insertTask(
            Task.apply(
                UUID.randomUUID().toString(),
                TaskType.TOKEN_CLEANUP_TASK,
                TaskStatus(Status.SUCCESSFUL, "Deleted $deletedTokens expired tokens"),
                OffsetDateTime.now()
            )
        )
    }

    suspend fun cacheDataTask(game: Game, taskType: TaskType) {
        logger.info("Running $taskType")
        val characters = charactersService.get(game)
        val errors = dataCacheService.cache(characters, game)
        if (errors.isEmpty()) {
            tasksRepository.insertTask(
                Task.apply(
                    UUID.randomUUID().toString(),
                    taskType,
                    TaskStatus(Status.SUCCESSFUL, null),
                    OffsetDateTime.now()
                )
            )
        } else {
            tasksRepository.insertTask(
                Task.apply(
                    UUID.randomUUID().toString(),
                    taskType,
                    TaskStatus(Status.ERROR, errors.joinToString(",\n") { it.error() }),
                    OffsetDateTime.now()
                )
            )
        }
    }
}