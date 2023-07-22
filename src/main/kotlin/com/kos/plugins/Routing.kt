package com.kos.plugins

import com.kos.auth.AuthService
import com.kos.auth.authRouting
import com.kos.views.ViewsService
import com.kos.views.viewsRouting
import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import io.ktor.server.routing.route

fun Application.configureRouting(authService: AuthService, viewsService: ViewsService) {

    routing {
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
        route("/api") {
            viewsRouting(viewsService)
            authRouting(authService)
        }
    }
}
