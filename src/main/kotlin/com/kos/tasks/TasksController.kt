package com.kos.tasks

import arrow.core.Either
import com.kos.activities.Activities
import com.kos.common.ControllerError
import com.kos.common.NotAuthorized
import com.kos.common.NotEnoughPermissions
import com.kos.credentials.CredentialsService
import com.kos.roles.Role

class TasksController(private val tasksService: TasksService, private val credentialsService: CredentialsService) {
    suspend fun runTask(client: String?, taskRequest: TaskRequest): Either<ControllerError, Unit> {
        return when (client) {
            null -> Either.Left(NotAuthorized())
            else -> {
                if (credentialsService.hasPermissions(client, Activities.runTask)) {
                    Either.Right(tasksService.runTask(taskRequest.type))
                } else Either.Left(NotEnoughPermissions(client))
            }
        }
    }
}