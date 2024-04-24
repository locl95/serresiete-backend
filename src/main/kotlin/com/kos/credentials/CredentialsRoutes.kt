package com.kos.credentials

import com.kos.activities.Activities
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.credentialsRouting(credentialsService: CredentialsService) {
    route("/credentials") {
        authenticate("auth-bearer") {
            post {
                when (val id = call.principal<UserIdPrincipal>()) {
                    null -> call.respond(HttpStatusCode.Unauthorized)
                    else -> {
                        if (credentialsService.hasPermissions(id.name, Activities.createCredentials)) {
                            credentialsService.createCredentials(call.receive())
                            call.respond(HttpStatusCode.Created)
                        } else call.respond(HttpStatusCode.Forbidden)
                    }
                }
            }
        }
        authenticate("auth-bearer") {
            put {
                when (val id = call.principal<UserIdPrincipal>()) {
                    null -> call.respond(HttpStatusCode.Unauthorized)
                    else -> {
                        if (credentialsService.hasPermissions(id.name, Activities.editCredentials)) {
                            credentialsService.editCredentials(call.receive())
                            call.respond(HttpStatusCode.NoContent)
                        } else call.respond(HttpStatusCode.Forbidden)
                    }
                }
            }
        }
        authenticate("auth-bearer") {
            delete("/{user}") {
                when (val id = call.principal<UserIdPrincipal>()) {
                    null -> call.respond(HttpStatusCode.Unauthorized)
                    else -> {
                        if (credentialsService.hasPermissions(id.name, Activities.deleteCredentials)) {
                            val userName = call.parameters["user"].orEmpty()
                            credentialsService.deleteCredentials(userName)
                            call.respond(HttpStatusCode.NoContent)
                        } else call.respond(HttpStatusCode.Forbidden)
                    }
                }
            }
        }
        authenticate("auth-bearer") {
            get {
                when (val id = call.principal<UserIdPrincipal>()) {
                    null -> call.respond(HttpStatusCode.Unauthorized)
                    else -> {
                        if (credentialsService.hasPermissions(id.name, Activities.getAnyCredentials)) {
                            call.respond(HttpStatusCode.OK, credentialsService.getCredentials())
                        } else call.respond(HttpStatusCode.Forbidden)
                    }
                }
            }
        }
        route("/{user}") {
            route("/roles") {
                authenticate("auth-bearer") {
                    get {
                        when (val id = call.principal<UserIdPrincipal>()) {
                            null -> call.respond(HttpStatusCode.Unauthorized)
                            else -> {
                                val userName = call.parameters["user"].orEmpty()
                                if ((userName == id.name && credentialsService.hasPermissions(
                                        userName,
                                        Activities.getOwnCredentialsRoles
                                    )) || credentialsService.hasPermissions(
                                        id.name,
                                        Activities.getAnyCredentialsRoles
                                    )
                                ) {
                                    call.respond(HttpStatusCode.OK, credentialsService.getUserRoles(id.name))
                                } else call.respond(HttpStatusCode.Forbidden)
                            }
                        }
                    }
                }
                authenticate("auth-bearer") {
                    post {
                        when (val id = call.principal<UserIdPrincipal>()) {
                            null -> call.respond(HttpStatusCode.Unauthorized)
                            else -> {
                                val userName = call.parameters["user"].orEmpty()
                                if (credentialsService.hasPermissions(id.name, Activities.addRoleToUser)) {
                                    val body = call.receive<RoleRequest>()
                                    credentialsService.addRoleToUser(userName, body.role)
                                    call.respond(HttpStatusCode.Created)
                                } else call.respond(HttpStatusCode.Forbidden)
                            }
                        }
                    }
                }
                authenticate("auth-bearer") {
                    delete("/{role}") {
                        when (val id = call.principal<UserIdPrincipal>()) {
                            null -> call.respond(HttpStatusCode.Unauthorized)
                            else -> {
                                val userName = call.parameters["user"].orEmpty()
                                val role = call.parameters["role"].orEmpty()
                                if (credentialsService.hasPermissions(id.name, Activities.addRoleToUser)) {
                                    credentialsService.deleteRoleFromUser(userName, role)
                                    call.respond(HttpStatusCode.NoContent)
                                } else call.respond(HttpStatusCode.Forbidden)
                            }
                        }
                    }
                }
            }
        }
    }
}