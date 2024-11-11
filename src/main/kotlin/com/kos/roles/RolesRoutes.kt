package com.kos.roles

import com.kos.common.respondWithHandledError
import com.kos.plugins.UserWithActivities
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.rolesRouting(
    rolesController: RolesController
) {
    route("/roles") {
        authenticate("auth-jwt") {
            get {
                val userWithActivities = call.principal<UserWithActivities>()
                rolesController.getRoles(userWithActivities?.name, userWithActivities?.activities.orEmpty()).fold({
                    call.respondWithHandledError(it)
                }, {
                    call.respond(HttpStatusCode.OK, it)
                })
            }
            route("/{role}") {
                route("/activities") {
                    authenticate("auth-jwt") {
                        post {
                            val userWithActivities = call.principal<UserWithActivities>()
                            rolesController.addActivityToRole(
                                userWithActivities?.name,
                                call.receive(),
                                Role.fromString(call.parameters["role"].orEmpty()),
                                userWithActivities?.activities.orEmpty()
                            ).fold({
                                call.respondWithHandledError(it)
                            }, {
                                call.respond(HttpStatusCode.Created)
                            })
                        }
                    }
                    route("/{activity}") {
                        authenticate("auth-jwt") {
                            delete {
                                val userWithActivities = call.principal<UserWithActivities>()
                                rolesController.deleteActivityFromRole(
                                    userWithActivities?.name,
                                    Role.fromString(call.parameters["role"].orEmpty()),
                                    call.parameters["activity"].orEmpty(),
                                    userWithActivities?.activities.orEmpty()
                                ).fold({
                                    call.respondWithHandledError(it)
                                }, {
                                    call.respond(HttpStatusCode.NoContent)
                                })
                            }
                        }
                    }
                }
            }
        }
    }
}