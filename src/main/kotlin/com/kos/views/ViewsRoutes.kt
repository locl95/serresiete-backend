package com.kos.views

import arrow.core.Either
import com.kos.activities.Activities
import com.kos.common.HttpError
import com.kos.credentials.CredentialsService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Route.viewsRouting(
    viewsController: ViewsController,
    viewsService: ViewsService,
    credentialsService: CredentialsService
) {
    route("/views") {
        authenticate("auth-bearer") {
            get {
                viewsController.getViews(call.principal<UserIdPrincipal>()?.name).fold({
                    when (it) {
                        is NotAuthorized -> call.respond(HttpStatusCode.Unauthorized)
                        is NotEnoughPermissions -> call.respond(HttpStatusCode.Forbidden)
                    }
                }, {
                    call.respond(HttpStatusCode.OK, it)
                })
            }
        }
        authenticate("auth-bearer") {
            get("/{id}") {
                viewsController.getView(call.principal<UserIdPrincipal>()?.name, call.parameters["id"].orEmpty()).fold({
                    when (it) {
                        is NotFound -> call.respond(HttpStatusCode.NotFound, it.id)
                        is NotAuthorized -> call.respond(HttpStatusCode.Unauthorized)
                        is NotEnoughPermissions -> call.respond(HttpStatusCode.Forbidden)
                    }
                }, {
                    call.respond(HttpStatusCode.OK, it)
                })
            }
        }
        authenticate("auth-bearer") {
            get("/{id}/data") {
                viewsController.getViewData(call.principal<UserIdPrincipal>()?.name, call.parameters["id"].orEmpty()).fold({
                    when (it) {
                        is NotFound -> call.respond(HttpStatusCode.NotFound, it.id)
                        is NotAuthorized -> call.respond(HttpStatusCode.Unauthorized)
                        is NotEnoughPermissions -> call.respond(HttpStatusCode.Forbidden)
                        is NotPublished -> call.respond(HttpStatusCode.BadRequest)
                        is HttpError -> call.respond(HttpStatusCode.InternalServerError, it.error())
                    }
                }, {
                    call.respond(HttpStatusCode.OK, it)
                })
            }
        }
        authenticate("auth-bearer") {
            get("/{id}/cached-data") {
                viewsController.getViewCachedData(call.principal<UserIdPrincipal>()?.name, call.parameters["id"].orEmpty()).fold({
                    when (it) {
                        is NotFound -> call.respond(HttpStatusCode.NotFound, it.id)
                        is NotAuthorized -> call.respond(HttpStatusCode.Unauthorized)
                        is NotEnoughPermissions -> call.respond(HttpStatusCode.Forbidden)
                        is NotPublished -> call.respond(HttpStatusCode.BadRequest)
                        is HttpError -> call.respond(HttpStatusCode.InternalServerError, it.error())
                    }
                }, {
                    call.respond(HttpStatusCode.OK, it)
                })
            }
        }
        authenticate("auth-bearer") {
            post {
                viewsController.createView(call.principal<UserIdPrincipal>()?.name, call.receive()).fold({
                    when (it) {
                        is NotAuthorized -> call.respond(HttpStatusCode.Unauthorized)
                        is NotEnoughPermissions -> call.respond(HttpStatusCode.Forbidden)
                        is TooMuchViews -> call.respond(HttpStatusCode.BadRequest)
                    }
                }, {
                    call.respond(HttpStatusCode.OK, it)
                })
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