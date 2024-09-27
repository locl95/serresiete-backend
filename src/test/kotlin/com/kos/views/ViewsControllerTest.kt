package com.kos.views

import com.kos.activities.Activities
import com.kos.activities.Activity
import com.kos.characters.Character
import com.kos.characters.CharactersService
import com.kos.characters.repository.CharactersInMemoryRepository
import com.kos.common.NotEnoughPermissions
import com.kos.common.NotFound
import com.kos.common.getLeftOrNull
import com.kos.credentials.CredentialsService
import com.kos.credentials.CredentialsTestHelper.basicCredentials
import com.kos.credentials.repository.CredentialsInMemoryRepository
import com.kos.credentials.repository.CredentialsRepositoryState
import com.kos.datacache.DataCache
import com.kos.datacache.DataCacheService
import com.kos.datacache.repository.DataCacheInMemoryRepository
import com.kos.raiderio.RaiderIoClient
import com.kos.roles.Role
import com.kos.roles.RolesTestHelper.role
import com.kos.roles.repository.RolesActivitiesInMemoryRepository
import com.kos.views.ViewsTestHelper.basicSimpleView
import com.kos.views.repository.ViewsInMemoryRepository
import kotlinx.coroutines.runBlocking
import org.mockito.Mockito.mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import com.kos.assertTrue
import com.kos.common.TooMuchViews
import kotlin.test.assertIs

//TODO: Behaviour of get data
//TODO: Behaviour of get cached data

class ViewsControllerTest {
    private val raiderIoClient = mock(RaiderIoClient::class.java)
    private val viewsRepository = ViewsInMemoryRepository()
    private val charactersRepository = CharactersInMemoryRepository()
    private val dataCacheRepository = DataCacheInMemoryRepository()
    private val credentialsRepository = CredentialsInMemoryRepository()
    private val rolesActivitiesRepository = RolesActivitiesInMemoryRepository()

    private suspend fun createController(
        credentialsState: CredentialsRepositoryState,
        viewsState: List<SimpleView>,
        charactersState: List<Character>,
        dataCacheState: List<DataCache>,
        rolesActivitiesState: Map<Role, List<Activity>>
    ): ViewsController {
        val viewsRepositoryWithState = viewsRepository.withState(viewsState)
        val charactersRepositoryWithState = charactersRepository.withState(charactersState)
        val dataCacheRepositoryWithState = dataCacheRepository.withState(dataCacheState)
        val credentialsRepositoryWithState = credentialsRepository.withState(credentialsState)
        val rolesActivitiesRepositoryWithState = rolesActivitiesRepository.withState(rolesActivitiesState)

        val dataCacheService = DataCacheService(dataCacheRepositoryWithState, raiderIoClient)
        val charactersService = CharactersService(charactersRepositoryWithState, raiderIoClient)
        val viewsService = ViewsService(viewsRepositoryWithState, charactersService, dataCacheService, raiderIoClient)
        val credentialsService = CredentialsService(credentialsRepositoryWithState, rolesActivitiesRepositoryWithState)
        return ViewsController(viewsService, credentialsService)
    }


    @BeforeTest
    fun beforeEach() {
        viewsRepository.clear()
        charactersRepository.clear()
        dataCacheRepository.clear()
        credentialsRepository.clear()
        rolesActivitiesRepository.clear()
    }

    @Test
    fun `i can get views returns only owner views`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = "owner")),
                mapOf(Pair("owner", listOf(role)))
            )

            val controller = createController(
                credentialsState,
                listOf(basicSimpleView, basicSimpleView.copy(owner = "not-owner")),
                listOf(),
                listOf(),
                mapOf(Pair(role, listOf(Activities.getOwnViews)))
            )
            assertEquals(listOf(basicSimpleView), controller.getViews("owner").getOrNull())
        }
    }

    @Test
    fun `i can get views returns all views if perms are given`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = "owner")),
                mapOf(Pair("owner", listOf(role)))
            )

            val notOwnerView = basicSimpleView.copy(owner = "not-owner")
            val controller = createController(
                credentialsState,
                listOf(basicSimpleView, notOwnerView),
                listOf(),
                listOf(),
                mapOf(Pair(role, listOf(Activities.getAnyViews)))
            )
            assertEquals(listOf(basicSimpleView, notOwnerView), controller.getViews("owner").getOrNull())
        }
    }

    @Test
    fun `i can get view returns view only if i own it`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = "owner")),
                mapOf(Pair("owner", listOf(role)))
            )

            val notOwnerView = basicSimpleView.copy(owner = "not-owner", id = "2")
            val controller = createController(
                credentialsState,
                listOf(basicSimpleView, notOwnerView),
                listOf(),
                listOf(),
                mapOf(Pair(role, listOf(Activities.getOwnView)))
            )
            assertEquals(basicSimpleView, controller.getView("owner", basicSimpleView.id).getOrNull()?.toSimple())
            assertEquals(NotEnoughPermissions("owner"), controller.getView("owner", notOwnerView.id).getLeftOrNull())
        }
    }

    @Test
    fun `i can't get view that does not exist`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = "owner")),
                mapOf(Pair("owner", listOf(role)))
            )

            val controller = createController(
                credentialsState,
                listOf(),
                listOf(),
                listOf(),
                mapOf(Pair(role, listOf(Activities.getOwnView)))
            )
            assertEquals(
                NotFound(basicSimpleView.id),
                controller.getView("owner", basicSimpleView.id).getLeftOrNull()
            )
        }
    }

    @Test
    fun `i can create views`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = "owner")),
                mapOf(Pair("owner", listOf(role)))
            )

            val controller = createController(
                credentialsState,
                listOf(),
                listOf(),
                listOf(),
                mapOf(Pair(role, listOf(Activities.createViews)))
            )
            val res = controller.createView("owner", ViewRequest(basicSimpleView.name, true, listOf(), Game.WOW)).getOrNull()

            assertTrue(res?.viewId?.isNotEmpty())
            assertEquals(listOf(), res?.characters)
        }
    }

    @Test
    fun `i can't create too much views`() {
        runBlocking {
            val credentialsState = CredentialsRepositoryState(
                listOf(basicCredentials.copy(userName = "owner")),
                mapOf(Pair("owner", listOf(role)))
            )

            val controller = createController(
                credentialsState,
                listOf(basicSimpleView, basicSimpleView),
                listOf(),
                listOf(),
                mapOf(Pair(role, listOf(Activities.createViews)))
            )

            assertIs<TooMuchViews>(controller.createView("owner", ViewRequest(basicSimpleView.name, true, listOf(), Game.WOW)).getLeftOrNull())
        }
    }


}