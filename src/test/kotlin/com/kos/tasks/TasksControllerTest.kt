package com.kos.tasks

import com.kos.activities.Activities
import com.kos.activities.Activity
import com.kos.auth.AuthService
import com.kos.auth.Authorization
import com.kos.auth.repository.AuthInMemoryRepository
import com.kos.characters.CharactersService
import com.kos.characters.CharactersTestHelper.emptyCharactersState
import com.kos.characters.repository.CharactersInMemoryRepository
import com.kos.characters.repository.CharactersState
import com.kos.credentials.CredentialsService
import com.kos.credentials.CredentialsTestHelper
import com.kos.credentials.repository.CredentialsInMemoryRepository
import com.kos.credentials.repository.CredentialsRepositoryState
import com.kos.datacache.DataCache
import com.kos.datacache.DataCacheService
import com.kos.datacache.repository.DataCacheInMemoryRepository
import com.kos.httpclients.raiderio.RaiderIoClient
import com.kos.httpclients.riot.RiotClient
import com.kos.roles.Role
import com.kos.roles.RolesTestHelper
import com.kos.roles.repository.RolesActivitiesInMemoryRepository
import com.kos.tasks.TasksTestHelper.task
import com.kos.tasks.repository.TasksInMemoryRepository
import kotlinx.coroutines.runBlocking
import org.mockito.Mockito
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class TasksControllerTest {
    private val raiderIoClient = Mockito.mock(RaiderIoClient::class.java)
    private val riotClient = Mockito.mock(RiotClient::class.java)
    private val charactersRepository = CharactersInMemoryRepository()
    private val dataCacheRepository = DataCacheInMemoryRepository()
    private val credentialsRepository = CredentialsInMemoryRepository()
    private val rolesActivitiesRepository = RolesActivitiesInMemoryRepository()
    private val tasksRepository = TasksInMemoryRepository()
    private val authRepository = AuthInMemoryRepository()

    private suspend fun createController(
        credentialsState: CredentialsRepositoryState,
        tasksState: List<Task>,
        charactersState: CharactersState,
        dataCacheState: List<DataCache>,
        authState: List<Authorization>,
        rolesActivitiesState: Map<Role, Set<Activity>>
    ): TasksController {
        val charactersRepositoryWithState = charactersRepository.withState(charactersState)
        val dataCacheRepositoryWithState = dataCacheRepository.withState(dataCacheState)
        val credentialsRepositoryWithState = credentialsRepository.withState(credentialsState)
        val rolesActivitiesRepositoryWithState = rolesActivitiesRepository.withState(rolesActivitiesState)
        val tasksRepositoryWithState = tasksRepository.withState(tasksState)
        val authRepositoryWithState = authRepository.withState(authState)

        val credentialsService = CredentialsService(credentialsRepositoryWithState, rolesActivitiesRepositoryWithState)
        val dataCacheService = DataCacheService(dataCacheRepositoryWithState, raiderIoClient, riotClient)
        val charactersService = CharactersService(charactersRepositoryWithState, raiderIoClient, riotClient)
        val authService = AuthService(authRepositoryWithState, credentialsService)
        val tasksService = TasksService(tasksRepositoryWithState, dataCacheService, charactersService, authService)

        return TasksController(tasksService, credentialsService)
    }

    @Test
    fun `i can get tasks`() {
        runBlocking {
            val now = OffsetDateTime.now()
            val credentialsState = CredentialsRepositoryState(
                listOf(CredentialsTestHelper.basicCredentials.copy(userName = "owner")),
                mapOf(Pair("owner", listOf(RolesTestHelper.role)))
            )

            val task = task(now)
            val controller = createController(
                credentialsState,
                listOf(task),
                emptyCharactersState,
                listOf(),
                listOf(),
                mapOf(Pair(RolesTestHelper.role, setOf(Activities.getTasks)))
            )
            assertEquals(listOf(task), controller.get("owner").getOrNull())
        }
    }

    @Test
    fun `i can get task by id`() {
        runBlocking {
            val now = OffsetDateTime.now()
            val credentialsState = CredentialsRepositoryState(
                listOf(CredentialsTestHelper.basicCredentials.copy(userName = "owner")),
                mapOf(Pair("owner", listOf(RolesTestHelper.role)))
            )

            val knownId = "1"
            val task = task(now).copy(id = knownId)
            val controller = createController(
                credentialsState,
                listOf(task),
                emptyCharactersState,
                listOf(),
                listOf(),
                mapOf(Pair(RolesTestHelper.role, setOf(Activities.getTask)))
            )
            assertEquals(task, controller.get("owner", knownId).getOrNull())
        }
    }
}