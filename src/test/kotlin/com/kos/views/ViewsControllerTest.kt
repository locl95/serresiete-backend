package com.kos.views

import com.kos.activities.Activities
import com.kos.activities.Activity
import com.kos.assertTrue
import com.kos.characters.CharactersService
import com.kos.characters.CharactersTestHelper.basicLolCharacter
import com.kos.characters.CharactersTestHelper.basicWowCharacter
import com.kos.characters.CharactersTestHelper.basicWowRequest2
import com.kos.characters.CharactersTestHelper.emptyCharactersState
import com.kos.characters.repository.CharactersInMemoryRepository
import com.kos.characters.repository.CharactersState
import com.kos.common.NotEnoughPermissions
import com.kos.common.NotFound
import com.kos.common.TooMuchViews
import com.kos.common.getLeftOrNull
import com.kos.credentials.CredentialsService
import com.kos.credentials.CredentialsTestHelper.basicCredentials
import com.kos.credentials.CredentialsTestHelper.emptyCredentialsState
import com.kos.credentials.repository.CredentialsInMemoryRepository
import com.kos.credentials.repository.CredentialsRepositoryState
import com.kos.datacache.DataCache
import com.kos.datacache.DataCacheService
import com.kos.datacache.RaiderIoMockHelper
import com.kos.datacache.RaiderIoMockHelper.raiderIoData
import com.kos.datacache.RaiderIoMockHelper.raiderioCachedData
import com.kos.datacache.RiotMockHelper.riotData
import com.kos.datacache.TestHelper.lolDataCache
import com.kos.datacache.TestHelper.wowDataCache
import com.kos.datacache.repository.DataCacheInMemoryRepository
import com.kos.httpclients.raiderio.RaiderIoClient
import com.kos.httpclients.riot.RiotClient
import com.kos.roles.Role
import com.kos.roles.repository.RolesActivitiesInMemoryRepository
import com.kos.views.ViewsTestHelper.basicSimpleLolView
import com.kos.views.ViewsTestHelper.basicSimpleWowView
import com.kos.views.ViewsTestHelper.owner
import com.kos.views.repository.ViewsInMemoryRepository
import io.mockk.InternalPlatformDsl.toStr
import kotlinx.coroutines.runBlocking
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import kotlin.test.*

//TODO: Behaviour of get cached data

class ViewsControllerTest {
    private val raiderIoClient = mock(RaiderIoClient::class.java)
    private val riotClient = mock(RiotClient::class.java)
    private val viewsRepository = ViewsInMemoryRepository()
    private val charactersRepository = CharactersInMemoryRepository()
    private val dataCacheRepository = DataCacheInMemoryRepository()
    private val credentialsRepository = CredentialsInMemoryRepository()
    private val rolesActivitiesRepository = RolesActivitiesInMemoryRepository()

    private suspend fun createController(
        credentialsState: CredentialsRepositoryState,
        viewsState: List<SimpleView>,
        charactersState: CharactersState,
        dataCacheState: List<DataCache>,
        rolesActivitiesState: Map<Role, Set<Activity>>
    ): ViewsController {
        val viewsRepositoryWithState = viewsRepository.withState(viewsState)
        val charactersRepositoryWithState = charactersRepository.withState(charactersState)
        val dataCacheRepositoryWithState = dataCacheRepository.withState(dataCacheState)
        val credentialsRepositoryWithState = credentialsRepository.withState(credentialsState)
        val rolesActivitiesRepositoryWithState = rolesActivitiesRepository.withState(rolesActivitiesState)

        val dataCacheService = DataCacheService(dataCacheRepositoryWithState, raiderIoClient, riotClient)
        val charactersService = CharactersService(charactersRepositoryWithState, raiderIoClient, riotClient)
        val credentialsService = CredentialsService(credentialsRepositoryWithState, rolesActivitiesRepositoryWithState)
        val viewsService = ViewsService(viewsRepositoryWithState, charactersService, dataCacheService, raiderIoClient, credentialsService)

        return ViewsController(viewsService)
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
            val controller = createController(
                emptyCredentialsState,
                listOf(basicSimpleWowView, basicSimpleWowView.copy(owner = "not-owner")),
                emptyCharactersState,
                listOf(),
                mapOf()
            )
            assertEquals(
                listOf(basicSimpleWowView),
                controller.getViews("owner", setOf(Activities.getOwnViews)).getOrNull()
            )
        }
    }

    @Test
    fun `i can get views returns all views if perms are given`() {
        runBlocking {
            val notOwnerView = basicSimpleWowView.copy(owner = "not-owner")
            val controller = createController(
                emptyCredentialsState,
                listOf(basicSimpleWowView, notOwnerView),
                emptyCharactersState,
                listOf(),
                mapOf()
            )
            assertEquals(
                listOf(basicSimpleWowView, notOwnerView),
                controller.getViews("owner", setOf(Activities.getAnyViews)).getOrNull()
            )
        }
    }

    @Test
    fun `i can get view returns view only if i own it`() {
        runBlocking {
            val notOwnerView = basicSimpleWowView.copy(owner = "not-owner", id = "2")
            val controller = createController(
                emptyCredentialsState,
                listOf(basicSimpleWowView, notOwnerView),
                emptyCharactersState,
                listOf(),
                mapOf()
            )
            assertEquals(
                basicSimpleWowView,
                controller.getView("owner", basicSimpleWowView.id, setOf(Activities.getOwnView)).getOrNull()?.toSimple()
            )
            assertEquals(
                NotEnoughPermissions("owner"),
                controller.getView("owner", notOwnerView.id, setOf(Activities.getOwnView)).getLeftOrNull()
            )
        }
    }

    @Test
    fun `i can't get view that does not exist`() {
        runBlocking {
            val controller = createController(
                emptyCredentialsState,
                listOf(),
                emptyCharactersState,
                listOf(),
                mapOf()
            )
            assertEquals(
                NotFound(basicSimpleWowView.id),
                controller.getView("owner", basicSimpleWowView.id, setOf(Activities.getOwnView)).getLeftOrNull()
            )
        }
    }

    @Test
    fun `i can create views`() {
        runBlocking {

            val controller = createController(
                CredentialsRepositoryState(listOf(basicCredentials.copy(userName = "owner")) , mapOf(owner to listOf(Role.USER))),
                listOf(),
                emptyCharactersState,
                listOf(),
                mapOf()
            )
            val res =
                controller.createView(
                    "owner",
                    ViewRequest(basicSimpleWowView.name, true, listOf(), Game.WOW),
                    setOf(Activities.createViews)
                )
                    .getOrNull()

            assertTrue(res?.id?.isNotEmpty())
            assertEquals(listOf(), res?.characterIds)
        }
    }

    @Test
    fun `i can't create too much views`() {
        runBlocking {
            val controller = createController(
                CredentialsRepositoryState(listOf(basicCredentials.copy(userName = "owner")) , mapOf(owner to listOf(Role.USER))),
                listOf(basicSimpleWowView, basicSimpleWowView),
                emptyCharactersState,
                listOf(),
                mapOf()
            )

            assertIs<TooMuchViews>(
                controller.createView(
                    "owner",
                    ViewRequest(basicSimpleWowView.name, true, listOf(), Game.WOW),
                    setOf(Activities.createViews)
                ).getLeftOrNull()
            )
        }
    }

    @Test
    fun `i can get wow view data`() {
        runBlocking {

            val controller = createController(
                emptyCredentialsState,
                listOf(basicSimpleWowView.copy(characterIds = listOf(1))),
                CharactersState(listOf(basicWowCharacter), listOf()),
                listOf(),
                mapOf()
            )

            `when`(raiderIoClient.cutoff()).thenReturn(RaiderIoMockHelper.cutoff())
            `when`(raiderIoClient.get(basicWowCharacter)).thenReturn(RaiderIoMockHelper.get(basicWowCharacter))

            controller.getViewData("owner", basicSimpleWowView.id, setOf(Activities.getViewData))
                .onRight {
                    assertEquals(raiderIoData, it)
                }
                .onLeft { fail(it.toStr()) }
        }
    }

    @Test
    fun `i can get lol view data`() {
        runBlocking {
            val controller = createController(
                emptyCredentialsState,
                listOf(basicSimpleLolView.copy(characterIds = listOf(2))),
                CharactersState(listOf(), listOf(basicLolCharacter.copy(id = 2))),
                listOf(lolDataCache),
                mapOf()
            )

            controller.getViewData("owner", basicSimpleLolView.id, setOf(Activities.getViewData))
                .onRight {
                    assertEquals(listOf(riotData), it)
                }
                .onLeft { fail(it.toStr()) }
        }
    }

    @Test
    fun `i can get wow cached data`() {
        runBlocking {

            val controller = createController(
                emptyCredentialsState,
                listOf(basicSimpleWowView.copy(characterIds = listOf(1))),
                CharactersState(listOf(basicWowCharacter), listOf()),
                listOf(wowDataCache),
                mapOf()
            )

            controller.getViewCachedData("owner", basicSimpleWowView.id, setOf(Activities.getViewCachedData))
                .onRight {
                    assertEquals(listOf(raiderioCachedData), it)
                }
                .onLeft { fail(it.toStr()) }
        }
    }

    @Test
    fun `i can get lol cached data`() {
        runBlocking {

            val controller = createController(
                emptyCredentialsState,
                listOf(basicSimpleWowView.copy(characterIds = listOf(2))),
                CharactersState(listOf(), listOf(basicLolCharacter)),
                listOf(lolDataCache),
                mapOf()
            )

            controller.getViewCachedData("owner", basicSimpleLolView.id, setOf(Activities.getViewCachedData))
                .onRight {
                    assertEquals(listOf(riotData), it)
                }
                .onLeft { fail(it.toStr()) }
        }
    }

    @Test
    fun `i can edit wow data`() {
        runBlocking {

            val controller = createController(
                emptyCredentialsState,
                listOf(basicSimpleWowView),
                CharactersState(listOf(basicWowCharacter), listOf(basicLolCharacter)),
                listOf(lolDataCache),
                mapOf()
            )

            `when`(raiderIoClient.exists(basicWowRequest2)).thenReturn(true)

            val viewRequest = ViewRequest("new-name", false, characters = listOf(basicWowRequest2), Game.WOW)

            controller.editView("owner", viewRequest, basicSimpleWowView.id, setOf(Activities.editAnyView))
                .onRight {
                    assertEquals(viewRequest.name, it.name)
                    assertEquals(viewRequest.published, it.published)
                    assertEquals(listOf(2L), it.characters)
                }
                .onLeft { fail(it.toStr()) }
        }
    }
}