package com.kos.tasks

import com.kos.auth.AuthService
import com.kos.tasks.repository.TasksRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.time.OffsetDateTime


data class TokenCleanupTask(
    val tasksRepository: TasksRepository,
    val authService: AuthService,
    val coroutineScope: CoroutineScope
) : Runnable {


    private val logger = LoggerFactory.getLogger(TokenCleanupTask::class.java)

    override fun run() {
        coroutineScope.launch {
            logger.info("Running token cleanup task")
            val deletedTokens = authService.deleteExpiredTokens()
            logger.info("Deleted $deletedTokens expired tokens")
            tasksRepository.insertTask(
                Task.apply(
                    TaskType.TOKEN_CLEANUP_TASK,
                    TaskStatus(Status.SUCCESSFUL, "Deleted $deletedTokens expired tokens"),
                    OffsetDateTime.now()
                )
            )
        }
    }
}



