package com.kos.plugins

import com.kos.activities.ActivitiesService
import com.kos.activities.activitiesRouting
import com.kos.auth.AuthService
import com.kos.auth.authRouting
import com.kos.credentials.CredentialsService
import com.kos.credentials.credentialsRouting
import com.kos.roles.RolesService
import com.kos.roles.rolesRouting
import com.kos.views.ViewsController
import com.kos.views.ViewsService
import com.kos.views.viewsRouting
import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import io.ktor.server.routing.route

fun Application.configureRouting(
    authService: AuthService,
    viewsService: ViewsService,
    credentialsService: CredentialsService,
    activitiesService: ActivitiesService,
    rolesService: RolesService,
    viewsController: ViewsController
) {

    routing {
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
        route("/api") {
            viewsRouting(viewsController, viewsService, credentialsService)
            authRouting(authService, credentialsService)
            rolesRouting(rolesService, credentialsService)
            activitiesRouting(activitiesService, credentialsService)
            credentialsRouting(credentialsService)
        }
    }
}
