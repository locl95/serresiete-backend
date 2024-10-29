package com.kos.views

import com.kos.common.respondWithHandledError
import com.kos.plugins.UserWithActivities
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Route.viewsRouting(
    viewsController: ViewsController
) {
    route("/views") {
        authenticate("auth-jwt") {
            get {
                val userWithActivities = call.principal<UserWithActivities>()
                viewsController.getViews(userWithActivities?.name, userWithActivities?.activities.orEmpty()).fold({
                    call.respondWithHandledError(it)
                }, {
                    call.respond(HttpStatusCode.OK, it)
                })
            }
        }
        authenticate("auth-jwt") {
            get("/{id}") {
                val userWithActivities = call.principal<UserWithActivities>()
                viewsController.getView(
                    userWithActivities?.name,
                    call.parameters["id"].orEmpty(),
                    userWithActivities?.activities.orEmpty()
                ).fold({
                    call.respondWithHandledError(it)
                }, {
                    call.respond(HttpStatusCode.OK, it)
                })
            }
        }
        authenticate("auth-jwt") {
            get("/{id}/data") {
                val userWithActivities = call.principal<UserWithActivities>()
                viewsController.getViewData(
                    userWithActivities?.name,
                    call.parameters["id"].orEmpty(),
                    userWithActivities?.activities.orEmpty()
                )
                    .fold({
                        call.respondWithHandledError(it)
                    }, {
                        call.respond(HttpStatusCode.OK, it)
                    })
            }
        }
        authenticate("auth-jwt") {
            get("/{id}/cached-data") {
                val userWithActivities = call.principal<UserWithActivities>()
                viewsController.getViewCachedData(
                    userWithActivities?.name,
                    call.parameters["id"].orEmpty(),
                    userWithActivities?.activities.orEmpty()
                ).fold({
                    call.respondWithHandledError(it)
                }, {
                    call.respond(HttpStatusCode.OK, it)
                })
            }
        }
        authenticate("auth-jwt") {
            post {
                val userWithActivities = call.principal<UserWithActivities>()
                viewsController.createView(
                    userWithActivities?.name,
                    call.receive(),
                    userWithActivities?.activities.orEmpty()
                ).fold({
                    call.respondWithHandledError(it)
                }, {
                    call.respond(HttpStatusCode.OK, it)
                })
            }
        }
        authenticate("auth-jwt") {
            put("/{id}") {
                val userWithActivities = call.principal<UserWithActivities>()
                viewsController.editView(
                    userWithActivities?.name,
                    call.receive(),
                    call.parameters["id"].orEmpty(),
                    userWithActivities?.activities.orEmpty()
                ).fold({
                    call.respondWithHandledError(it)
                }, {
                    call.respond(HttpStatusCode.OK, it)
                })
            }
        }
        authenticate("auth-jwt") {
            patch("/{id}") {
                val userWithActivities = call.principal<UserWithActivities>()
                viewsController.patchView(
                    userWithActivities?.name,
                    call.receive(),
                    call.parameters["id"].orEmpty(),
                    userWithActivities?.activities.orEmpty()
                ).fold({
                    call.respondWithHandledError(it)
                }, {
                    call.respond(HttpStatusCode.OK, it)
                })
            }
        }
        authenticate("auth-jwt") {
            delete("/{id}") {
                val userWithActivities = call.principal<UserWithActivities>()
                viewsController.deleteView(
                    userWithActivities?.name,
                    call.parameters["id"].orEmpty(),
                    userWithActivities?.activities.orEmpty()
                ).fold({
                    call.respondWithHandledError(it)
                }, {
                    call.respond(HttpStatusCode.OK, it)
                })
            }
        }
    }
}