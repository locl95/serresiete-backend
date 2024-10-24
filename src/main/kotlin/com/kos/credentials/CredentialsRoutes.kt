package com.kos.credentials

import com.kos.common.respondWithHandledError
import com.kos.roles.RoleRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.credentialsRouting(credentialsController: CredentialsController) {
    route("/credentials") {
        authenticate("auth-bearer") {
            post {
                credentialsController.createCredential(call.principal<UserIdPrincipal>()?.name, call.receive()).fold({
                    call.respondWithHandledError(it)
                }, {
                    call.respond(HttpStatusCode.Created)
                })
            }
        }
        authenticate("auth-bearer") {
            put {
                credentialsController.editCredential(call.principal<UserIdPrincipal>()?.name, call.receive()).fold({
                    call.respondWithHandledError(it)
                }, {
                    call.respond(HttpStatusCode.NoContent)
                })
            }
        }
        authenticate("auth-bearer") {
            get {
                credentialsController.getCredentials(
                    call.principal<UserIdPrincipal>()?.name,
                ).fold({
                    call.respondWithHandledError(it)
                }, {
                    call.respond(HttpStatusCode.OK, it)
                })
            }
        }
        route("/{user}") {
            authenticate("auth-bearer") {
                delete {
                    credentialsController.deleteCredential(
                        call.principal<UserIdPrincipal>()?.name,
                        call.parameters["user"].orEmpty()
                    ).fold({
                        call.respondWithHandledError(it)
                    }, {
                        call.respond(HttpStatusCode.NoContent)
                    })
                }
            }
            authenticate("auth-bearer") {
                get {
                    credentialsController.getCredential(
                        call.principal<UserIdPrincipal>()?.name,
                        call.parameters["user"].orEmpty()
                    ).fold({
                        call.respondWithHandledError(it)
                    }, {
                        call.respond(HttpStatusCode.OK, it)
                    })
                }
            }
            route("/roles") {
                authenticate("auth-bearer") {
                    get {
                        credentialsController.getUserRoles(
                            call.principal<UserIdPrincipal>()?.name,
                            call.parameters["user"].orEmpty()
                        ).fold({
                            call.respondWithHandledError(it)
                        }, {
                            call.respond(HttpStatusCode.OK, it)
                        })
                    }
                }
                authenticate("auth-bearer") {
                    post {
                        credentialsController.addRoleToUser(
                            call.principal<UserIdPrincipal>()?.name,
                            call.parameters["user"].orEmpty(),
                            call.receive<RoleRequest>().role
                        ).fold({
                            call.respondWithHandledError(it)
                        }, {
                            call.respond(HttpStatusCode.Created)
                        })
                    }
                }
                authenticate("auth-bearer") {
                    delete("/{role}") {
                        credentialsController.deleteRoleFromUser(
                            call.principal<UserIdPrincipal>()?.name,
                            call.parameters["user"].orEmpty(),
                            call.parameters["role"].orEmpty()
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