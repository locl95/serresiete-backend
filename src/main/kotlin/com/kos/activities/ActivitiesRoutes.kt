package com.kos.activities

import com.kos.common.respondWithHandledError
import com.kos.roles.Role
import com.kos.plugins.UserWithActivities
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
        authenticate("auth-jwt") {
            get {
                val userWithActivities = call.principal<UserWithActivities>()
                activitiesController.getActivities(userWithActivities?.name, userWithActivities?.activities.orEmpty())
                    .fold({
                        call.respondWithHandledError(it)
                    }, {
                        call.respond(HttpStatusCode.OK, it)
                    })
            }
        }

        authenticate("auth-jwt") {
            post {
                val userWithActivities = call.principal<UserWithActivities>()
                activitiesController.createActivity(
                    userWithActivities?.name,
                    call.receive(),
                    userWithActivities?.activities.orEmpty()
                ).fold({
                    call.respondWithHandledError(it)
                }, {
                    call.respond(HttpStatusCode.Created)
                })
            }
        }
        authenticate("auth-jwt") {
            delete("/{id}") {
                val userWithActivities = call.principal<UserWithActivities>()
                activitiesController.deleteActivity(
                    userWithActivities?.name,
                    call.parameters["activity"].orEmpty(),
                    userWithActivities?.activities.orEmpty()
                ).fold({
                    call.respondWithHandledError(it)
                }, {
                    call.respond(HttpStatusCode.NoContent)
                })
            }
        }

        authenticate("auth-jwt") {
            get("/{role}") {
                val userWithActivities = call.principal<UserWithActivities>()
                activitiesController.getActivitiesFromRole(
                    userWithActivities?.name,
                    Role.fromString(call.parameters["role"].orEmpty()),
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