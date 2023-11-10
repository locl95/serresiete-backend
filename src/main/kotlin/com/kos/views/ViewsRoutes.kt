package com.kos.views

import arrow.core.Either
import com.kos.credentials.Activities
import com.kos.credentials.CredentialsService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Route.viewsRouting(viewsService: ViewsService, credentialsService: CredentialsService) {
    route("/views") {
        authenticate("auth-bearer") {
            get {
                when (val id = call.principal<UserIdPrincipal>()) {
                    null -> call.respond(HttpStatusCode.Unauthorized)
                    else -> {
                        if (credentialsService.hasPermissions(id.name, Activities.getAnyViews)) call.respond(
                            HttpStatusCode.OK,
                            viewsService.getViews()
                        )
                        else if (credentialsService.hasPermissions(id.name, Activities.getOwnViews)) call.respond(
                            HttpStatusCode.OK,
                            viewsService.getOwnViews(id.name)
                        )
                        else call.respond(HttpStatusCode.Forbidden)
                    }
                }
            }
        }
        authenticate("auth-bearer") {
            get("/{id}") {
                when (val userId = call.principal<UserIdPrincipal>()) {
                    null -> call.respond(HttpStatusCode.Unauthorized)
                    else -> {
                        val id = call.parameters["id"].orEmpty()
                        when (val maybeView = viewsService.get(id)) {
                            null -> call.respond(HttpStatusCode.NotFound, ViewNotFound(id))
                            else -> {
                                if ((maybeView.owner == userId.name && credentialsService.hasPermissions(
                                        userId.name,
                                        Activities.getOwnView
                                    ) || credentialsService.hasPermissions(userId.name, Activities.getAnyView))
                                ) call.respond(
                                    HttpStatusCode.OK,
                                    maybeView
                                )
                                else call.respond(HttpStatusCode.Forbidden)
                            }
                        }
                    }
                }
            }
        }
        authenticate("auth-bearer") {
            get("/{id}/data") {
                when (val userId = call.principal<UserIdPrincipal>()) {
                    null -> call.respond(HttpStatusCode.Unauthorized)
                    else -> {
                        val id = call.parameters["id"].orEmpty()
                        when (val maybeView = viewsService.get(id)) {
                            null -> call.respond(HttpStatusCode.NotFound, ViewNotFound(id))
                            else -> {
                                if (credentialsService.hasPermissions(userId.name, Activities.getViewData)) {
                                    viewsService.getData(maybeView).fold({
                                        call.respond(HttpStatusCode.InternalServerError, it.error())
                                    }, {
                                        call.respond(HttpStatusCode.OK, it)
                                    })
                                } else call.respond(HttpStatusCode.Forbidden)
                            }
                        }
                    }
                }
            }
        }
        authenticate("auth-bearer") {
            get("/{id}/cached-data") {
                when (val userId = call.principal<UserIdPrincipal>()) {
                    null -> call.respond(HttpStatusCode.Unauthorized)
                    else -> {
                        val id = call.parameters["id"].orEmpty()
                        when (val maybeView = viewsService.getSimple(id)) {
                            null -> call.respond(HttpStatusCode.NotFound, ViewNotFound(id))
                            else -> {
                                if (credentialsService.hasPermissions(userId.name, Activities.getViewCachedData)) {
                                    viewsService.getCachedData(maybeView).fold({
                                        call.respond(HttpStatusCode.InternalServerError, it.error())
                                    }, {
                                        call.respond(HttpStatusCode.OK, it)
                                    })
                                } else call.respond(HttpStatusCode.Forbidden)
                            }
                        }
                    }
                }
            }
        }
        authenticate("auth-bearer") {
            post {
                when (val id = call.principal<UserIdPrincipal>()) {
                    null -> call.respond(HttpStatusCode.Unauthorized)
                    else ->
                        if (credentialsService.hasPermissions(id.name, Activities.createViews)) {
                            when (val res = viewsService.create(id.name, call.receive())) {
                                is Either.Right -> call.respond(HttpStatusCode.OK, res.value)
                                is Either.Left -> call.respond(HttpStatusCode.BadRequest, "Too much views")
                            }
                        } else call.respond(HttpStatusCode.Forbidden)
                }
            }
        }
        authenticate("auth-bearer") {
            put("/{id}") {
                when (val userId = call.principal<UserIdPrincipal>()) {
                    null -> call.respond(HttpStatusCode.Unauthorized)
                    else -> {
                        val id = call.parameters["id"].orEmpty()
                        when (val maybeView = viewsService.get(id)) {
                            null -> call.respond(HttpStatusCode.NotFound, ViewNotFound(id))
                            else -> {
                                if ((maybeView.owner == userId.name && credentialsService.hasPermissions(
                                        userId.name,
                                        Activities.editOwnView
                                    )) || credentialsService.hasPermissions(userId.name, Activities.editAnyView)
                                ) {
                                    val res = viewsService.edit(maybeView.id, call.receive())
                                    call.respond(HttpStatusCode.OK, res)
                                } else call.respond(HttpStatusCode.Forbidden)
                            }
                        }
                    }
                }
            }
        }
        authenticate("auth-bearer") {
            delete("/{id}") {
                when (val userId = call.principal<UserIdPrincipal>()) {
                    null -> call.respond(HttpStatusCode.Unauthorized)
                    else -> {
                        val id = call.parameters["id"].orEmpty()
                        when (val maybeView = viewsService.get(id)) {
                            null -> call.respond(HttpStatusCode.NotFound, ViewNotFound(id))
                            else -> {
                                if ((maybeView.owner == userId.name && credentialsService.hasPermissions(
                                        userId.name,
                                        Activities.deleteOwnView
                                    )) || credentialsService.hasPermissions(userId.name, Activities.deleteAnyView)
                                ) {
                                    val res = viewsService.delete(maybeView.id)
                                    call.respond(HttpStatusCode.OK, res)
                                } else call.respond(HttpStatusCode.Forbidden)
                            }
                        }
                    }
                }
            }
        }

    }
}