package com.kos.tasks

import arrow.core.Either
import com.kos.activities.Activities
import com.kos.common.ControllerError
import com.kos.common.NotAuthorized
import com.kos.common.NotEnoughPermissions
import com.kos.credentials.CredentialsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TasksController(private val tasksService: TasksService, private val credentialsService: CredentialsService) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    suspend fun runTask(client: String?, taskRequest: TaskRequest): Either<ControllerError, Unit> {
        return when (client) {
            null -> Either.Left(NotAuthorized())
            else -> {
                if (credentialsService.hasPermissions(client, Activities.runTask)) {
                    scope.launch {
                        tasksService.runTask(taskRequest.type)
                    }
                    Either.Right(Unit)
                } else Either.Left(NotEnoughPermissions(client))
            }
        }
    }
}