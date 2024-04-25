package com.kos

import com.kos.activities.ActivitiesService
import com.kos.activities.repository.ActivitiesDatabaseRepository
import com.kos.activities.repository.ActivitiesInMemoryRepository
import com.kos.auth.AuthService
import com.kos.auth.repository.AuthDatabaseRepository
import com.kos.characters.CharactersService
import com.kos.characters.repository.CharactersDatabaseRepository
import com.kos.common.DatabaseFactory
import com.kos.credentials.CredentialsService
import com.kos.credentials.repository.CredentialsDatabaseRepository
import com.kos.datacache.DataCacheService
import com.kos.datacache.repository.DataCacheDatabaseRepository
import com.kos.plugins.*
import com.kos.raiderio.RaiderIoHTTPClient
import com.kos.roles.RolesService
import com.kos.roles.repository.RolesActivitiesDatabaseRepository
import com.kos.roles.repository.RolesActivitiesInMemoryRepository
import com.kos.roles.repository.RolesDatabaseRepository
import com.kos.roles.repository.RolesInMemoryRepository
import com.kos.views.ViewsService
import com.kos.views.repository.ViewsDatabaseRepository
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val coroutineScope = CoroutineScope(Dispatchers.Default)

    DatabaseFactory.init(mustClean = false)

    val client = HttpClient(CIO)
    val raiderIoHTTPClient = RaiderIoHTTPClient(client)

    val authRepository = AuthDatabaseRepository()
    val authService = AuthService(authRepository)

    val rolesActivitiesRepository = RolesActivitiesDatabaseRepository()

    val credentialsRepository = CredentialsDatabaseRepository()
    val credentialsService = CredentialsService(credentialsRepository, rolesActivitiesRepository)

    val activitiesRepository = ActivitiesDatabaseRepository()
    val activitiesService = ActivitiesService(activitiesRepository)

    val rolesRepository = RolesDatabaseRepository()
    val rolesService = RolesService(rolesRepository, rolesActivitiesRepository)

    val charactersRepository = CharactersDatabaseRepository()
    val charactersService = CharactersService(charactersRepository, raiderIoHTTPClient)

    val viewsRepository = ViewsDatabaseRepository()
    val dataCacheRepository = DataCacheDatabaseRepository()
    val dataCacheService = DataCacheService(dataCacheRepository, raiderIoHTTPClient)
    val viewsService = ViewsService(viewsRepository, charactersService, dataCacheService, raiderIoHTTPClient)

    val executorService: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    executorService.scheduleAtFixedRate(
        TokenCleanupTask(authService, coroutineScope),
        0, 60, TimeUnit.MINUTES
    )

    executorService.scheduleAtFixedRate(
        CacheDataTask(dataCacheService, charactersService, coroutineScope),
        0, 60, TimeUnit.MINUTES
    )

    Runtime.getRuntime().addShutdownHook(Thread {
        executorService.shutdown()
    })


    configureAuthentication(authService, credentialsService)
    configureCors()
    configureRouting(authService, viewsService, credentialsService, activitiesService, rolesService)
    configureSerialization()
    configureLogging()
}
