package com.kos

import com.kos.activities.ActivitiesController
import com.kos.activities.ActivitiesService
import com.kos.activities.repository.ActivitiesDatabaseRepository
import com.kos.auth.AuthController
import com.kos.auth.AuthService
import com.kos.auth.repository.AuthDatabaseRepository
import com.kos.characters.CharactersService
import com.kos.characters.repository.CharactersDatabaseRepository
import com.kos.common.DatabaseFactory
import com.kos.credentials.CredentialsController
import com.kos.credentials.CredentialsService
import com.kos.credentials.repository.CredentialsDatabaseRepository
import com.kos.datacache.DataCacheService
import com.kos.datacache.repository.DataCacheDatabaseRepository
import com.kos.plugins.*
import com.kos.httpclients.raiderio.RaiderIoHTTPClient
import com.kos.httpclients.riot.RiotHTTPClient
import com.kos.roles.RolesController
import com.kos.roles.RolesService
import com.kos.roles.repository.RolesActivitiesDatabaseRepository
import com.kos.roles.repository.RolesDatabaseRepository
import com.kos.tasks.TasksController
import com.kos.tasks.TasksLauncher
import com.kos.tasks.TasksService
import com.kos.tasks.repository.TasksDatabaseRepository
import com.kos.views.ViewsController
import com.kos.views.ViewsService
import com.kos.views.repository.ViewsDatabaseRepository
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val riotApiKey = System.getenv("RIOT_API_KEY")

    val coroutineScope = CoroutineScope(Dispatchers.Default)

    DatabaseFactory.init(mustClean = false)

    val client = HttpClient(CIO)
    val raiderIoHTTPClient = RaiderIoHTTPClient(client)
    val riotHTTPClient = RiotHTTPClient(client, riotApiKey)

    val rolesActivitiesRepository = RolesActivitiesDatabaseRepository()

    val credentialsRepository = CredentialsDatabaseRepository()
    val credentialsService = CredentialsService(credentialsRepository, rolesActivitiesRepository)
    val credentialsController = CredentialsController(credentialsService)

    val authRepository = AuthDatabaseRepository()
    val authService = AuthService(authRepository)
    val authController = AuthController(authService, credentialsService)

    val activitiesRepository = ActivitiesDatabaseRepository()
    val activitiesService = ActivitiesService(activitiesRepository)
    val activitiesController = ActivitiesController(activitiesService, credentialsService)

    val rolesRepository = RolesDatabaseRepository()
    val rolesService = RolesService(rolesRepository, rolesActivitiesRepository)
    val rolesController = RolesController(rolesService, credentialsService)

    val charactersRepository = CharactersDatabaseRepository()
    val charactersService = CharactersService(charactersRepository, raiderIoHTTPClient, riotHTTPClient)

    val viewsRepository = ViewsDatabaseRepository()
    val dataCacheRepository = DataCacheDatabaseRepository()
    val dataCacheService = DataCacheService(dataCacheRepository, raiderIoHTTPClient, riotHTTPClient)
    val viewsService = ViewsService(viewsRepository, charactersService, dataCacheService, raiderIoHTTPClient)
    val viewsController = ViewsController(viewsService, credentialsService)

    val executorService: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    val tasksRepository = TasksDatabaseRepository()
    val tasksService =
        TasksService(tasksRepository, dataCacheService, charactersService, authService)
    val tasksLauncher =
        TasksLauncher(tasksService, tasksRepository, executorService, authService, dataCacheService, coroutineScope)
    val tasksController = TasksController(tasksService, credentialsService)

    coroutineScope.launch { tasksLauncher.launchTasks() }
    configureAuthentication(authService, credentialsService)
    configureCors()
    configureRouting(activitiesController, authController, credentialsController, rolesController, viewsController, tasksController)
    configureSerialization()
    configureLogging()
}
