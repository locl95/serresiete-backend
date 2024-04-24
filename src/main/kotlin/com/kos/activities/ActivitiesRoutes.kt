package com.kos.activities

import com.kos.credentials.CredentialsService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.activitiesRouting(activitiesService: ActivitiesService, credentialsService: CredentialsService) {
    route("/activities") {
        authenticate("auth-bearer") {
            get {
                when (val id = call.principal<UserIdPrincipal>()) {
                    null -> call.respond(HttpStatusCode.Unauthorized)
                    else -> {
                        if (credentialsService.hasPermissions(id.name, Activities.getAnyActivities)) {
                            call.respond(HttpStatusCode.OK, activitiesService.getActivities())
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
                        if (credentialsService.hasPermissions(id.name, Activities.createActivities)) {
                            activitiesService.createActivity(call.receive())
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
                        if (credentialsService.hasPermissions(id.name, Activities.deleteActivities)) {
                            activitiesService.deleteActivity(call.receive())
                        } else call.respond(HttpStatusCode.Forbidden)
                    }
                }
            }
        }

        authenticate("auth-bearer") {
            get("/{role}") {
                when (val id = call.principal<UserIdPrincipal>()) {
                    null -> call.respond(HttpStatusCode.Unauthorized)
                    else -> {
                        val role = call.parameters["role"].orEmpty()
                        if (credentialsService.hasPermissions(id.name, Activities.getAnyActivities)) {
                            credentialsService.getRoleActivities(role)
                        } else call.respond(HttpStatusCode.Forbidden)
                    }
                }
            }
        }
    }
}