package com.kos.roles

import com.kos.activities.Activities
import com.kos.credentials.CredentialsService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.rolesRouting(rolesService: RolesService, credentialsService: CredentialsService) {
    route("/roles") {
        authenticate("auth-bearer") {
            get {
                when (val id = call.principal<UserIdPrincipal>()) {
                    null -> call.respond(HttpStatusCode.Unauthorized)
                    else -> {
                        if (credentialsService.hasPermissions(id.name, Activities.getAnyRoles)) {
                            call.respond(HttpStatusCode.OK, rolesService.getRoles())
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
                        if (credentialsService.hasPermissions(id.name, Activities.createRoles)) {
                            rolesService.createRole(call.receive())
                        } else call.respond(HttpStatusCode.Forbidden)
                    }
                }
            }
        }

        authenticate("auth-bearer") {
            delete {
                when (val id = call.principal<UserIdPrincipal>()) {
                    null -> call.respond(HttpStatusCode.Unauthorized)
                    else -> {
                        if (credentialsService.hasPermissions(id.name, Activities.deleteRoles)) {
                            rolesService.deleteRole(call.receive())
                        } else call.respond(HttpStatusCode.Forbidden)
                    }
                }
            }
        }
        route("/{role}/activities") {
            authenticate("auth-bearer") {
                post {
                    when (val id = call.principal<UserIdPrincipal>()) {
                        null -> call.respond(HttpStatusCode.Unauthorized)
                        else -> {
                            val role = call.parameters["role"].orEmpty()
                            if (credentialsService.hasPermissions(id.name, Activities.addActivityToRole)) {
                                rolesService.addActivityToRole(call.receive(), role)
                                call.respond(HttpStatusCode.Created)
                            } else call.respond(HttpStatusCode.Forbidden)
                        }
                    }
                }
            }
        }
        route("/{role}/activities/{activity}") {
            authenticate("auth-bearer") {
                delete {
                    when (val id = call.principal<UserIdPrincipal>()) {
                        null -> call.respond(HttpStatusCode.Unauthorized)
                        else -> {
                            val role = call.parameters["role"].orEmpty()
                            val activity = call.parameters["activity"].orEmpty()
                            if (credentialsService.hasPermissions(id.name, Activities.deleteActivityFromRole)) {
                                rolesService.removeActivityFromRole(activity, role)
                            } else call.respond(HttpStatusCode.Forbidden)
                        }
                    }
                }
            }
        }
    }
}