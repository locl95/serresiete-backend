package com.kos.tasks

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
                val client = call.principal<UserIdPrincipal>()?.name
                val taskType = call.request.queryParameters["taskType"]

                tasksController.get(client, taskType)
                    .fold({
                        call.respondWithHandledError(it)
                    }, {
                        call.respond(OK, it)
                    })
            }
        }
        route("/{id}") {
            authenticate("auth-bearer") {
                get {
                    tasksController.get(call.principal<UserIdPrincipal>()?.name, call.parameters["id"].orEmpty())
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