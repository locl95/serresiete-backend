package com.kos.roles

import com.kos.common.respondWithHandledError
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
        authenticate("auth-bearer") {
            get {
                rolesController.getRoles(call.principal<UserIdPrincipal>()?.name).fold({
                    call.respondWithHandledError(it)
                }, {
                    call.respond(HttpStatusCode.OK, it)
                })
            }

            authenticate("auth-bearer") {
                post {
                    rolesController.createRole(call.principal<UserIdPrincipal>()?.name, call.receive()).fold({
                        call.respondWithHandledError(it)
                    }, {
                        call.respond(HttpStatusCode.Created)
                    })
                }
            }

            route("/{role}") {
                authenticate("auth-bearer") {
                    delete {
                        rolesController.deleteRole(
                            call.principal<UserIdPrincipal>()?.name,
                            call.parameters["role"].orEmpty()
                        ).fold({
                            call.respondWithHandledError(it)
                        }, {
                            call.respond(HttpStatusCode.NoContent)
                        })
                    }
                }
                route("/activities") {
                    authenticate("auth-bearer") {
                        post {
                            rolesController.addActivityToRole(
                                call.principal<UserIdPrincipal>()?.name,
                                call.receive(),
                                call.parameters["role"].orEmpty()
                            ).fold({
                                call.respondWithHandledError(it)
                            }, {
                                call.respond(HttpStatusCode.Created)
                            })
                        }
                    }
                    route("/{activity}") {
                        authenticate("auth-bearer") {
                            delete {
                                rolesController.deleteActivityFromRole(
                                    call.principal<UserIdPrincipal>()?.name,
                                    call.parameters["role"].orEmpty(),
                                    call.parameters["activity"].orEmpty()
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