package com.kos.auth

import com.kos.credentials.Activities
import com.kos.credentials.CredentialsService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRouting(authService: AuthService, credentialsService: CredentialsService) {

    route("/auth") {
        authenticate("auth-basic") {
            post {
                when (val userIdPrincipal = call.principal<UserIdPrincipal>()) {
                    null -> call.respond(HttpStatusCode.Unauthorized)
                    else -> {
                        if (credentialsService.hasPermissions(userIdPrincipal.name, Activities.login)) {
                            when (val token = authService.login(userIdPrincipal.name)) {
                                null -> call.respond(HttpStatusCode.BadRequest)
                                else -> call.respond(HttpStatusCode.OK, token)
                            }
                        } else call.respond(HttpStatusCode.Forbidden)
                    }
                }
            }
        }
        authenticate("auth-bearer") {
            delete {
                when (val id = call.principal<UserIdPrincipal>()) {
                    null -> call.respond(HttpStatusCode.Unauthorized)
                    else -> if (credentialsService.hasPermissions(id.name, Activities.logout)) {
                        authService.logout(id.name)
                        call.respond(HttpStatusCode.OK)
                    } else call.respond(HttpStatusCode.Forbidden)
                }
            }
        }
    }
}