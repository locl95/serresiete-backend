package com.kos.views

import arrow.core.Either
import com.kos.datacache.DataCacheService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Route.viewsRouting(viewsService: ViewsService) {

    route("/views") {
        authenticate("auth-bearer") {
            get {
                when (val id = call.principal<UserIdPrincipal>()) {
                    null -> call.respond(HttpStatusCode.Unauthorized)
                    else -> {
                        val views = viewsService.getOwnViews(id.name)
                        call.respond(HttpStatusCode.OK, views)
                    }
                }
            }
        }
        authenticate("auth-bearer") {
            get("/{id}") {
                val id = call.parameters["id"].orEmpty()
                when (val maybeView = viewsService.get(id)) {
                    null -> call.respond(HttpStatusCode.NotFound, ViewNotFound(id))
                    else -> {
                        if (maybeView.owner == call.principal<UserIdPrincipal>()?.name) call.respond(
                            HttpStatusCode.OK,
                            maybeView
                        )
                        else call.respond(HttpStatusCode.Forbidden)
                    }
                }
            }
        }
        authenticate("auth-bearer") {
            get("/{id}/data") {
                val id = call.parameters["id"].orEmpty()
                when (val maybeView = viewsService.get(id)) {
                    null -> call.respond(HttpStatusCode.NotFound, ViewNotFound(id))
                    else -> {
                        if (maybeView.owner == call.principal<UserIdPrincipal>()?.name) {
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
        authenticate("auth-bearer") {
            get("/{id}/cached-data") {
                val id = call.parameters["id"].orEmpty()
                when (val maybeView = viewsService.getSimple(id)) {
                    null -> call.respond(HttpStatusCode.NotFound, ViewNotFound(id))
                    else -> {
                        if (maybeView.owner == call.principal<UserIdPrincipal>()?.name) {
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
        authenticate("auth-bearer") {
            post {
                when (val id = call.principal<UserIdPrincipal>()) {
                    null -> call.respond(HttpStatusCode.Unauthorized)
                    else ->
                        when(val res = viewsService.create(id.name, call.receive())) {
                            is Either.Right -> call.respond(HttpStatusCode.OK, res.value)
                            is Either.Left -> call.respond(HttpStatusCode.BadRequest, "Too much views")
                        }
                }
            }
        }
        authenticate("auth-bearer") {
            put("/{id}") {
                val id = call.parameters["id"].orEmpty()
                when (val maybeView = viewsService.get(id)) {
                    null -> call.respond(HttpStatusCode.NotFound, ViewNotFound(id))
                    else -> {
                        if (maybeView.owner == call.principal<UserIdPrincipal>()?.name) {
                            when (viewsService.edit(maybeView.id, call.receive())) {
                                is Either.Right -> call.respond(HttpStatusCode.OK)
                                is Either.Left -> call.respond(HttpStatusCode.NotFound)
                            }
                        } else call.respond(HttpStatusCode.Forbidden)
                    }
                }
            }
        }

    }
}