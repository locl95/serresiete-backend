package com.kos.tasks

import arrow.core.Either
import arrow.core.raise.either
import com.kos.common.InvalidQueryParameter
import com.kos.common.fold
import com.kos.common.respondWithHandledError
import io.ktor.http.*
import io.ktor.http.HttpHeaders.Location
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.tasksRouting(tasksController: TasksController) {

    route("/tasks") {
        authenticate("auth-bearer") {
            post {
                tasksController.runTask(call.principal<UserIdPrincipal>()?.name, call.receive())
                    .fold({
                        call.respondWithHandledError(it)
                    }, {
                        call.response.headers.append(Location, "/tasks/$it")
                        call.respond(HttpStatusCode.Created)
                    })
            }
        }
        authenticate("auth-bearer") {
            get {
                val taskTypeParameter = "taskType"

                call.request.queryParameters[taskTypeParameter]?.let { TaskType.fromString(it) }.fold(
                    {
                        tasksController.getTasks(
                            call.principal<UserIdPrincipal>()?.name,
                            null
                        )
                    },
                    { providedTaskTypeParameter ->
                        providedTaskTypeParameter.fold({
                            Either.Left(
                                InvalidQueryParameter(
                                    taskTypeParameter,
                                    it.type
                                )
                            )
                        },
                            {
                                tasksController.getTasks(
                                    call.principal<UserIdPrincipal>()?.name,
                                    it
                                )
                            })
                    }
                )
                    .fold({
                        call.respondWithHandledError(it)
                    }, {
                        call.respond(OK, it)
                    })


                // Collect authentication and query parameter errors in an Either
                val result = either {
                    val taskType = call.request.queryParameters[taskTypeParameter]?.let {
                        TaskType.fromString(it)
                    }
                    client to taskType // Successful case as a pair (clientName, taskType)
                }

                // Now we can call `getTasks` and fold on the overall result
                result.flatMap { (client, taskType) ->
                    tasksController.getTasks(client, taskType)
                }.fold(
                    { error -> call.respondWithHandledError(error) },
                    { tasks -> call.respond(OK, tasks) }
                )
            }
            }
        }
        route("/{id}") {
            authenticate("auth-bearer") {
                get {
                    tasksController.getTask(
                        call.principal<UserIdPrincipal>()?.name,
                        call.parameters["id"].orEmpty()
                    )
                        .fold({
                            call.respondWithHandledError(it)
                        }, {
                            call.respond(OK, it)
                        })
                }
            }
        }
    }
}