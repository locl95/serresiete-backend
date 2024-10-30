package com.kos.tasks

import arrow.core.Either
import com.kos.activities.Activities
import com.kos.activities.Activity
import com.kos.common.ControllerError
import com.kos.common.NotAuthorized
import com.kos.common.NotEnoughPermissions
import com.kos.common.NotFound
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.*

class TasksController(private val tasksService: TasksService) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    suspend fun runTask(
        client: String?,
        taskRequest: TaskRequest,
        activities: Set<Activity>
    ): Either<ControllerError, String> {
        return when (client) {
            null -> Either.Left(NotAuthorized)
            else -> {
                if (activities.contains(Activities.runTask)) {
                    val taskId = UUID.randomUUID().toString()
                    scope.launch {
                        tasksService.runTask(taskRequest.type, taskId)
                    }
                    Either.Right(taskId)
                } else Either.Left(NotEnoughPermissions(client))
            }
        }
    }

    suspend fun get(client: String?, activities: Set<Activity>): Either<ControllerError, List<Task>> {
        return when (client) {
            null -> Either.Left(NotAuthorized)
            else -> {
                if (activities.contains(Activities.getTasks)) Either.Right(tasksService.get())
                else Either.Left(NotEnoughPermissions(client))
            }
        }
    }

    suspend fun get(client: String?, id: String, activities: Set<Activity>): Either<ControllerError, Task> {
        return when (client) {
            null -> Either.Left(NotAuthorized)
            else -> {
                return when (val maybeTask = tasksService.get(id)) {
                    null -> Either.Left(NotFound(id))
                    else -> {
                        if (activities.contains(Activities.getTask)) Either.Right(maybeTask)
                        else Either.Left(NotEnoughPermissions(client))
                    }
                }
            }
        }
    }
}