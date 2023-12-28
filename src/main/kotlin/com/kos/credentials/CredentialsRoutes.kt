package com.kos.credentials

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
            put {
                when (val id = call.principal<UserIdPrincipal>()) {
                    null -> call.respond(HttpStatusCode.Unauthorized)
                    else -> {
                        if (credentialsService.hasPermissions(id.name, Activities.getAnyCredentialsRoles)) {
                            call.respond(HttpStatusCode.OK, credentialsService.getRoles(id.name))
                        } else call.respond(HttpStatusCode.Forbidden)
                    }
                }
            }
        }
    }
}