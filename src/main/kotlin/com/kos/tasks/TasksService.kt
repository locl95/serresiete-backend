package com.kos.tasks

import com.kos.auth.AuthService
import com.kos.characters.CharactersService
import com.kos.common.WithLogger
import com.kos.datacache.DataCacheService
import com.kos.tasks.repository.TasksRepository
import com.kos.views.Game
import java.time.OffsetDateTime

data class TasksService(
    private val tasksRepository: TasksRepository,
    private val dataCacheService: DataCacheService,
    private val charactersService: CharactersService,
    private val authService: AuthService
) : WithLogger("tasksService") {

    private val olderThanDays: Long = 7

    suspend fun getTasks(taskType: TaskType?) = tasksRepository.getTasks(taskType)

    suspend fun getTask(id: String) = tasksRepository.getTask(id)

    suspend fun runTask(taskType: TaskType, taskId: String) {
        when (taskType) {
            TaskType.TOKEN_CLEANUP_TASK -> tokenCleanup(taskId)
            TaskType.CACHE_LOL_DATA_TASK -> cacheDataTask(Game.LOL, taskType, taskId)
            TaskType.CACHE_WOW_DATA_TASK -> cacheDataTask(Game.WOW, taskType, taskId)
            TaskType.TASK_CLEANUP_TASK -> taskCleanup(taskId)
        }
    }

    suspend fun taskCleanup(id: String) {
        logger.info("Running task cleanup task")
        val deletedTasks = tasksRepository.deleteOldTasks(olderThanDays)
        logger.info("Deleted $deletedTasks old tasks")
        tasksRepository.insertTask(
            Task(
                id,
                TaskType.TASK_CLEANUP_TASK,
                TaskStatus(Status.SUCCESSFUL, "Deleted $deletedTasks old tasks"),
                OffsetDateTime.now()
            )
        )
    }

    suspend fun tokenCleanup(id: String) {
        logger.info("Running token cleanup task")
        val deletedTokens = authService.deleteExpiredTokens()
        logger.info("Deleted $deletedTokens expired tokens")
        tasksRepository.insertTask(
            Task(
                id,
                TaskType.TOKEN_CLEANUP_TASK,
                TaskStatus(Status.SUCCESSFUL, "Deleted $deletedTokens expired tokens"),
                OffsetDateTime.now()
            )
        )
    }

    suspend fun cacheDataTask(game: Game, taskType: TaskType, id: String) {
        logger.info("Running $taskType")
        val characters = charactersService.get(game)
        val errors = dataCacheService.cache(characters, game)
        if (errors.isEmpty()) {
            tasksRepository.insertTask(
                Task(
                    id,
                    taskType,
                    TaskStatus(Status.SUCCESSFUL, null),
                    OffsetDateTime.now()
                )
            )
        } else {
            tasksRepository.insertTask(
                Task(
                    id,
                    taskType,
                    TaskStatus(Status.ERROR, errors.joinToString(",\n") { it.error() }),
                    OffsetDateTime.now()
                )
            )
        }
    }
}