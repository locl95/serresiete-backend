package com.kos.tasks

import arrow.core.raise.either
import com.kos.common.InvalidQueryParameter
import com.kos.common.recoverToEither
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
                either {
                    val taskTypeParameter = "type"
                    val taskType: TaskType? =
                        call.request.queryParameters[taskTypeParameter].recoverToEither(
                            { InvalidQueryParameter(taskTypeParameter, it, TaskType.entries.map { taskTypes -> taskTypes.toString() }) },
                            { TaskType.fromString(it) }
                        ).bind()

                    tasksController.getTasks(call.principal<UserIdPrincipal>()?.name, taskType).bind()
                }.fold({
                    call.respondWithHandledError(it)
                }, {
                    call.respond(OK, it)
                })
            }
        }
        route("/{id}") {
            authenticate("auth-bearer") {
                get {
                    tasksController.getTask(
                        call.principal<UserIdPrincipal>()?.name,
                        call.parameters["id"].orEmpty()
                    ).fold({
                        call.respondWithHandledError(it)
                    }, {
                        call.respond(OK, it)
                    })
                }
            }
        }
    }
}