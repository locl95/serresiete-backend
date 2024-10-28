package com.kos.tasks

import arrow.core.Either
import com.kos.activities.Activities
import com.kos.common.ControllerError
import com.kos.common.NotAuthorized
import com.kos.common.NotEnoughPermissions
import com.kos.common.NotFound
import com.kos.credentials.CredentialsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.*

class TasksController(private val tasksService: TasksService, private val credentialsService: CredentialsService) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    suspend fun runTask(client: String?, taskRequest: TaskRequest): Either<ControllerError, String> {
        return when (client) {
            null -> Either.Left(NotAuthorized())
            else -> {
                if (credentialsService.hasPermissions(client, Activities.runTask)) {
                    val taskId = UUID.randomUUID().toString()
                    scope.launch {
                        tasksService.runTask(taskRequest.type, taskId)
                    }
                    Either.Right(taskId)
                } else Either.Left(NotEnoughPermissions(client))
            }
        }
    }

    suspend fun get(client: String?, taskType: String?): Either<ControllerError, List<Task>> {
        return when (client) {
            null -> Either.Left(NotAuthorized())
            else -> {
                if (credentialsService.hasPermissions(client, Activities.getTasks)) {
                    Either.Right(tasksService.getTasks(taskType))
                } else Either.Left(NotEnoughPermissions(client))
            }
        }
    }

    suspend fun get(client: String?, id: String): Either<ControllerError, Task> {
        return when (client) {
            null -> Either.Left(NotAuthorized())
            else -> {
                return when (val maybeTask = tasksService.getTask(id)) {
                    null -> Either.Left(NotFound(id))
                    else -> {
                        if (credentialsService.hasPermissions(client, Activities.getTask)) {
                            Either.Right(maybeTask)
                        } else Either.Left(NotEnoughPermissions(client))
                    }
                }
            }
        }
    }
}