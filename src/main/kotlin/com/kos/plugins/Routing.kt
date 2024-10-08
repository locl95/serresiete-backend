package com.kos.plugins

import com.kos.activities.ActivitiesController
import com.kos.activities.ActivitiesService
import com.kos.activities.activitiesRouting
import com.kos.auth.AuthController
import com.kos.auth.authRouting
import com.kos.credentials.CredentialsController
import com.kos.credentials.CredentialsService
import com.kos.credentials.credentialsRouting
import com.kos.roles.RolesController
import com.kos.roles.rolesRouting
import com.kos.tasks.TasksController
import com.kos.tasks.tasksRouting
import com.kos.views.ViewsController
import com.kos.views.viewsRouting
import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import io.ktor.server.routing.route

fun Application.configureRouting(
    activitiesController: ActivitiesController,
    authController: AuthController,
    credentialsController: CredentialsController,
    rolesController: RolesController,
    viewsController: ViewsController,
    tasksController: TasksController
) {

    routing {
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
        route("/api") {
            viewsRouting(viewsController)
            authRouting(authController)
            rolesRouting(rolesController)
            activitiesRouting(activitiesController)
            credentialsRouting(credentialsController)
            tasksRouting(tasksController)
        }
    }
}
