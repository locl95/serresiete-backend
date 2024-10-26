package com.kos.activities

import com.kos.common.respondWithHandledError
import com.kos.roles.Role
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.activitiesRouting(
    activitiesController: ActivitiesController
) {
    route("/activities") {
        authenticate("auth-bearer") {
            get {
                activitiesController.getActivities(call.principal<UserIdPrincipal>()?.name).fold({
                    call.respondWithHandledError(it)
                }, {
                    call.respond(HttpStatusCode.OK, it)
                })
            }
        }

        authenticate("auth-bearer") {
            post {
                activitiesController.createActivity(call.principal<UserIdPrincipal>()?.name, call.receive()).fold({
                    call.respondWithHandledError(it)
                }, {
                    call.respond(HttpStatusCode.Created)
                })
            }
        }
        route("/{activity}") {
            authenticate("auth-bearer") {
                delete {
                    activitiesController.deleteActivity(
                        call.principal<UserIdPrincipal>()?.name,
                        call.parameters["activity"].orEmpty()
                    ).fold({
                        call.respondWithHandledError(it)
                    }, {
                        call.respond(HttpStatusCode.NoContent)
                    })
                }
            }
        }

        authenticate("auth-bearer") {
            get("/{role}") {
                activitiesController.getActivitiesFromRole(
                    call.principal<UserIdPrincipal>()?.name,
                    Role.fromString(call.parameters["role"].orEmpty())
                ).fold({
                    call.respondWithHandledError(it)
                }, {
                    call.respond(HttpStatusCode.OK, it)
                })
            }
        }
    }
}