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
                viewsController.getViewData(call.principal<UserIdPrincipal>()?.name, call.parameters["id"].orEmpty())
                    .fold({
                        call.respondWithHandledError(it)
                    }, {
                        call.respond(HttpStatusCode.OK, it)
                    })
            }
        }
        authenticate("auth-bearer") {
            get("/{id}/cached-data") {
                viewsController.getViewCachedData(
                    call.principal<UserIdPrincipal>()?.name,
                    call.parameters["id"].orEmpty()
                ).fold({
                    call.respondWithHandledError(it)
                }, {
                    call.respond(HttpStatusCode.OK, it)
                })
            }
        }
        authenticate("auth-bearer") {
            post {
                viewsController.createView(call.principal<UserIdPrincipal>()?.name, call.receive()).fold({
                    call.respondWithHandledError(it)
                }, {
                    call.respond(HttpStatusCode.OK, it)
                })
            }
        }
        authenticate("auth-bearer") {
            put("/{id}") {
                viewsController.editView(
                    call.principal<UserIdPrincipal>()?.name,
                    call.receive(),
                    call.parameters["id"].orEmpty()
                ).fold({
                    call.respondWithHandledError(it)
                }, {
                    call.respond(HttpStatusCode.OK, it)
                })
            }
        }
        authenticate("auth-bearer") {
            patch("/{id}") {
                viewsController.patchView(
                    call.principal<UserIdPrincipal>()?.name,
                    call.receive(),
                    call.parameters["id"].orEmpty()
                ).fold({
                    call.respondWithHandledError(it)
                }, {
                    call.respond(HttpStatusCode.OK, it)
                })
            }
        }
        authenticate("auth-bearer") {
            delete("/{id}") {
                viewsController.deleteView(
                    call.principal<UserIdPrincipal>()?.name,
                    call.parameters["id"].orEmpty()
                ).fold({
                    call.respondWithHandledError(it)
                }, {
                    call.respond(HttpStatusCode.OK, it)
                })
            }
        }
    }
}