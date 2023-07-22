package com.kos.auth

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRouting(authService: AuthService) {

    route("/auth") {
        authenticate("auth-basic") {
            post {
                when (val userIdPrincipal = call.principal<UserIdPrincipal>()) {
                    null -> call.respond(HttpStatusCode.Unauthorized)
                    else -> {
                        when (val token = authService.login(userIdPrincipal.name)) {
                            null -> call.respond(HttpStatusCode.BadRequest)
                            else -> call.respond(HttpStatusCode.OK, token)
                        }
                    }
                }
            }
        }
        authenticate("auth-bearer") {
            delete {
                when(val id = call.principal<UserIdPrincipal>()?.name) {
                    null -> call.respond(HttpStatusCode.Unauthorized)
                    else -> {
                        authService.logout(id)
                        call.respond(HttpStatusCode.OK)
                    }
                }
            }
        }
    }
}