package com.kos.tasks

import com.kos.common.respondWithHandledError
import com.kos.roles.RoleRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.tasksRouting(tasksController: TasksController) {
    route("/tasks") {
        route("/run") {
            authenticate("auth-bearer") {
                post {
                    tasksController.runTask(call.principal<UserIdPrincipal>()?.name, call.receive())
                        .fold({
                            call.respondWithHandledError(it)
                        }, {
                            call.respond(HttpStatusCode.NoContent)
                        })
                }
            }
        }
    }
}