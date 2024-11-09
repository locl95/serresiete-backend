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
import com.kos.common.JWTConfig
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


    val jwtConfig = JWTConfig(
        System.getenv("JWT_ISSUER"),
        System.getenv("JWT_SECRET")
    )

    val coroutineScope = CoroutineScope(Dispatchers.Default)

    val db = DatabaseFactory.pooledDatabase()

    val client = HttpClient(CIO)
    val raiderIoHTTPClient = RaiderIoHTTPClient(client)
    val riotHTTPClient = RiotHTTPClient(client, riotApiKey)

    val rolesActivitiesRepository = RolesActivitiesDatabaseRepository(db)

    val credentialsRepository = CredentialsDatabaseRepository(db)
    val credentialsService = CredentialsService(credentialsRepository, rolesActivitiesRepository)
    val credentialsController = CredentialsController(credentialsService)

    val authRepository = AuthDatabaseRepository(db)
    val authService = AuthService(authRepository, credentialsService, jwtConfig)
    val authController = AuthController(authService)

    val activitiesRepository = ActivitiesDatabaseRepository(db)
    val activitiesService = ActivitiesService(activitiesRepository)
    val activitiesController = ActivitiesController(activitiesService, credentialsService)

    val rolesRepository = RolesDatabaseRepository(db)
    val rolesService = RolesService(rolesRepository, rolesActivitiesRepository)
    val rolesController = RolesController(rolesService)

    val charactersRepository = CharactersDatabaseRepository(db)
    val charactersService = CharactersService(charactersRepository, raiderIoHTTPClient, riotHTTPClient)

    val viewsRepository = ViewsDatabaseRepository(db)
    val dataCacheRepository = DataCacheDatabaseRepository(db)
    val dataCacheService = DataCacheService(dataCacheRepository, raiderIoHTTPClient, riotHTTPClient)
    val viewsService = ViewsService(viewsRepository, charactersService, dataCacheService, raiderIoHTTPClient, credentialsService)
    val viewsController = ViewsController(viewsService)

    val executorService: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    val tasksRepository = TasksDatabaseRepository(db)
    val tasksService =
        TasksService(tasksRepository, dataCacheService, charactersService, authService)
    val tasksLauncher =
        TasksLauncher(tasksService, tasksRepository, executorService, authService, dataCacheService, coroutineScope)
    val tasksController = TasksController(tasksService)

    coroutineScope.launch { tasksLauncher.launchTasks() }
    configureAuthentication(credentialsService, jwtConfig)
    configureCors()
    configureRouting(
        activitiesController,
        authController,
        credentialsController,
        rolesController,
        viewsController,
        tasksController
    )
    configureSerialization()
    configureLogging()
}
