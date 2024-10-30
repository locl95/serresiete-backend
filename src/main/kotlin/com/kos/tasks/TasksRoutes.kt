package com.kos.tasks

import com.kos.common.respondWithHandledError
import com.kos.plugins.UserWithActivities
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.tasksRouting(tasksController: TasksController) {
    route("/tasks") {
        authenticate("auth-jwt") {
            post {
                val userWithActivities = call.principal<UserWithActivities>()
                tasksController.runTask(
                    userWithActivities?.name,
                    call.receive(),
                    userWithActivities?.activities.orEmpty()
                ).fold({
                    call.respondWithHandledError(it)
                }, {
                    call.response.headers.append(HttpHeaders.Location, "/tasks/$it")
                    call.respond(HttpStatusCode.Created)
                })
            }
        }
        authenticate("auth-jwt") {
            get {
                val userWithActivities = call.principal<UserWithActivities>()
                tasksController.get(userWithActivities?.name, userWithActivities?.activities.orEmpty())
                    .fold({
                        call.respondWithHandledError(it)
                    }, {
                        call.respond(HttpStatusCode.OK, it)
                    })
            }
        }
        route("/{id}") {
            authenticate("auth-jwt") {
                get {
                    val userWithActivities = call.principal<UserWithActivities>()
                    tasksController.get(
                        userWithActivities?.name,
                        call.parameters["id"].orEmpty(),
                        userWithActivities?.activities.orEmpty()
                    ).fold({
                        call.respondWithHandledError(it)
                    }, {
                        call.respond(HttpStatusCode.OK, it)
                    })
                }
            }
        }
    }
}