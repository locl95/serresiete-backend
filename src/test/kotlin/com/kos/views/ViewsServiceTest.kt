package com.kos.views

import com.kos.activities.Activity
import com.kos.characters.CharactersService
import com.kos.characters.CharactersTestHelper.basicLolCharacter
import com.kos.characters.CharactersTestHelper.basicWowCharacter
import com.kos.characters.CharactersTestHelper.basicWowCharacter2
import com.kos.characters.CharactersTestHelper.emptyCharactersState
import com.kos.characters.WowCharacterRequest
import com.kos.characters.repository.CharactersInMemoryRepository
import com.kos.characters.repository.CharactersState
import com.kos.common.TooMuchViews
import com.kos.common.UserWithoutRoles
import com.kos.credentials.Credentials
import com.kos.credentials.CredentialsService
import com.kos.credentials.CredentialsTestHelper.emptyCredentialsInitialState
import com.kos.credentials.CredentialsTestHelper.password
import com.kos.credentials.repository.CredentialsInMemoryRepository
import com.kos.credentials.repository.CredentialsRepositoryState
import com.kos.datacache.DataCache
import com.kos.datacache.DataCacheService
import com.kos.datacache.RiotMockHelper.anotherRiotData
import com.kos.datacache.TestHelper.anotherLolDataCache
import com.kos.datacache.TestHelper.lolDataCache
import com.kos.datacache.repository.DataCacheInMemoryRepository
import com.kos.httpclients.raiderio.RaiderIoClient
import com.kos.httpclients.riot.RiotClient
import com.kos.roles.Role
import com.kos.roles.repository.RolesActivitiesInMemoryRepository
import com.kos.views.ViewsTestHelper.basicSimpleLolView
import com.kos.views.ViewsTestHelper.basicSimpleWowView
import com.kos.views.ViewsTestHelper.id
import com.kos.views.ViewsTestHelper.name
import com.kos.views.ViewsTestHelper.owner
import com.kos.views.ViewsTestHelper.published
import com.kos.views.repository.ViewsInMemoryRepository
import io.mockk.InternalPlatformDsl.toStr
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

//TODO: Add testing for:
//TODO: getCachedData(simpleView: SimpleView)
//TODO: getData when view is from WOW
//TODO: getData when view is from LOL

class ViewsServiceTest {
    private val raiderIoClient = mock(RaiderIoClient::class.java)
    private val riotClient = mock(RiotClient::class.java)

    @Test
    fun `i can get own views`() {
        runBlocking {
            val viewsService = createService(
                listOf(basicSimpleWowView),
                emptyCharactersState,
                listOf(),
                emptyCredentialsInitialState,
                mapOf()
            )

            assertEquals(listOf(basicSimpleWowView), viewsService.getOwnViews(owner))
        }
    }

    @Test
    fun `i can get a simple view`() {
        runBlocking {
            val viewsService = createService(
                listOf(basicSimpleWowView),
                emptyCharactersState,
                listOf(),
                emptyCredentialsInitialState,
                mapOf()
            )

            assertEquals(basicSimpleWowView, viewsService.getSimple("1"))
        }
    }

    @Test
    fun `i can create views`() {
        runBlocking {
            val viewsService = createService(
                listOf(),
                emptyCharactersState,
                listOf(),
                CredentialsRepositoryState(listOf(Credentials(owner, password)), mapOf(owner to listOf(Role.USER))),
                mapOf()
            )

            val request = ViewRequest(name, published, listOf(), Game.WOW)
            viewsService.create(owner, request).onRight {
                assertEquals(request.name, it.name)
                assertEquals(request.published, it.published)
                assertEquals(request.game, it.game)
            }.onLeft {
                fail(it.toStr())
            }
        }
    }

    @Test
    fun `i can create a lol view`() {
        runBlocking {
            val viewsService = createService(
                listOf(),
                emptyCharactersState,
                listOf(),
                CredentialsRepositoryState(listOf(Credentials(owner, password)), mapOf(owner to listOf(Role.USER))),
                mapOf()
            )

            val request = ViewRequest(name, published, listOf(), Game.LOL)
            viewsService.create(owner, request).onRight {
                assertEquals(request.name, it.name)
                assertEquals(request.published, it.published)
                assertEquals(request.game, it.game)
            }.onLeft {
                fail(it.toStr())
            }

            assertTrue(viewsService.create(owner, ViewRequest(name, published, listOf(), Game.LOL)).isRight())
        }
    }

    @Test
    fun `i can edit a lol view`() {
        runBlocking {
            val viewsService = createService(
                listOf(basicSimpleLolView),
                emptyCharactersState,
                listOf(),
                emptyCredentialsInitialState,
                mapOf()
            )

            val newName = "new-name"
            val request = ViewRequest(newName, published, listOf(), Game.LOL)
            viewsService.edit(basicSimpleLolView.id, request).onRight {
                assertEquals(request.name, it.name)
                assertEquals(request.published, it.published)
            }.onLeft {
                fail(it.toStr())
            }
        }
    }

    @Test
    fun `users cant create more than maximum views`() {
        runBlocking {
            val viewsService = createService(
                listOf(basicSimpleLolView, basicSimpleLolView),
                emptyCharactersState,
                listOf(),
                CredentialsRepositoryState(listOf(Credentials(owner, password)), mapOf(owner to listOf(Role.USER))),
                mapOf()
            )

            val request = ViewRequest(name, published, listOf(), Game.WOW)
            viewsService.create(owner, request).onRight {
                fail()
            }.onLeft {
                assertTrue(it is TooMuchViews)
            }
        }
    }

    @Test
    fun `admins can create a huge amount of views`() {
        runBlocking {
            val viewsService = createService(
                (1..100).map { SimpleView(it.toStr(), it.toStr(), owner, true, listOf(), Game.WOW) },
                emptyCharactersState,
                listOf(),
                CredentialsRepositoryState(listOf(Credentials(owner, password)), mapOf(owner to listOf(Role.ADMIN))),
                mapOf()
            )

            val request = ViewRequest(name, published, listOf(), Game.WOW)
            viewsService.create(owner, request).onRight {
                assertEquals(request.name, it.name)
                assertEquals(request.published, it.published)
                assertEquals(request.game, it.game)
            }.onLeft {
                fail(it.toStr())
            }
        }
    }

    @Test
    fun `user without role can't create a view`() {
        runBlocking {
            val viewsService = createService(
                listOf(),
                emptyCharactersState,
                listOf(),
                CredentialsRepositoryState(listOf(Credentials(owner, password)), mapOf(owner to listOf())),
                mapOf()
            )

            val request = ViewRequest(name, published, listOf(), Game.WOW)
            viewsService.create(owner, request).onRight {
                fail()
            }.onLeft {
                assertTrue(it is UserWithoutRoles)
            }
        }
    }

    @Test
    fun `i can edit a view modifying more than one character`() {
        runBlocking {

            val request1 = WowCharacterRequest("a", "r", "r")
            val request2 = WowCharacterRequest("b", "r", "r")
            val request3 = WowCharacterRequest("c", "r", "r")
            val request4 = WowCharacterRequest("d", "r", "r")

            `when`(raiderIoClient.exists(request1)).thenReturn(true)
            `when`(raiderIoClient.exists(request2)).thenReturn(true)
            `when`(raiderIoClient.exists(request3)).thenReturn(true)
            `when`(raiderIoClient.exists(request4)).thenReturn(true)

            val viewsService = createService(
                listOf(basicSimpleWowView),
                emptyCharactersState,
                listOf(),
                emptyCredentialsInitialState,
                mapOf()
            )


            viewsService.edit(
                id, ViewRequest(name, published, listOf(request1, request2, request3, request4), Game.WOW)
            ).onRight {
                assertEquals(id, it.viewId)
                assertEquals(listOf<Long>(1, 2, 3, 4), it.characters)
                assertEquals(name, it.name)
                assertEquals(published, it.published)
            }.onLeft {
                fail(it.toStr())
            }
        }
    }

    @Test
    fun `when editing a view, I return the actual characters of the view`() {
        runBlocking {
            val request1 = WowCharacterRequest("a", "r", "r")
            val request2 = WowCharacterRequest("b", "r", "r")
            val request3 = WowCharacterRequest("c", "r", "r")
            val request4 = WowCharacterRequest("d", "r", "r")

            `when`(raiderIoClient.exists(request1)).thenReturn(true)
            `when`(raiderIoClient.exists(request2)).thenReturn(true)
            `when`(raiderIoClient.exists(request3)).thenReturn(true)
            `when`(raiderIoClient.exists(request4)).thenReturn(true)

            val viewsService = createService(
                listOf(basicSimpleWowView.copy(characterIds = listOf(1))),
                CharactersState(
                    listOf(basicWowCharacter, basicWowCharacter2),
                    listOf()
                ),
                listOf(),
                emptyCredentialsInitialState,
                mapOf()
            )
            
            viewsService.edit(
                id, ViewRequest(name, published, listOf(request1, request2, request3, request4), Game.WOW)
            ).onRight {
                assertEquals(listOf<Long>(3, 4, 5, 6), it.characters)
                assertEquals(name, it.name)
                assertEquals(id, it.viewId)
                assertEquals(published, it.published)
            }.onLeft {
                fail(it.toStr())
            }
        }
    }

    @Test
    fun `i can delete a view`() {
        runBlocking {

            val viewsService = createService(
                listOf(basicSimpleWowView),
                emptyCharactersState,
                listOf(),
                emptyCredentialsInitialState,
                mapOf()
            )

            assertEquals(viewsService.delete("1"), ViewDeleted("1"))
        }
    }

    @Test
    fun `i can patch a view`() {
        runBlocking {
            val patchedName = "new-name"

            val viewsService = createService(
                listOf(basicSimpleWowView),
                emptyCharactersState,
                listOf(),
                emptyCredentialsInitialState,
                mapOf()
            )

            val patch = viewsService.patch(basicSimpleWowView.id, ViewPatchRequest(patchedName, null, null, Game.WOW))
            patch.onRight {
                assertEquals(basicSimpleWowView.id, it.viewId)
                assertEquals(null, it.published)
                assertEquals(null, it.characters)
                assertEquals(patchedName, it.name)
            }.onLeft {
                fail(it.toStr())
            }
        }
    }

    @Test
    fun `i can patch a view modifying more than one character`() {
        runBlocking {

            val request1 = WowCharacterRequest("a", "r", "r")
            val request2 = WowCharacterRequest("b", "r", "r")
            val request3 = WowCharacterRequest("c", "r", "r")
            val request4 = WowCharacterRequest("d", "r", "r")

            `when`(raiderIoClient.exists(request1)).thenReturn(true)
            `when`(raiderIoClient.exists(request2)).thenReturn(true)
            `when`(raiderIoClient.exists(request3)).thenReturn(true)
            `when`(raiderIoClient.exists(request4)).thenReturn(true)

            val viewsService = createService(
                listOf(basicSimpleLolView.copy(characterIds = listOf(1))),
                emptyCharactersState,
                listOf(),
                emptyCredentialsInitialState,
                mapOf()
            )
            
            viewsService.patch(
                id, ViewPatchRequest(null, null, listOf(request1, request2, request3, request4), Game.WOW)
            ).onRight {
                assertEquals(basicSimpleWowView.id, it.viewId)
                assertEquals(null, it.published)
                assertEquals(listOf<Long>(1, 2, 3, 4), it.characters)
                assertEquals(null, it.name)
            }.onLeft {
                fail(it.toStr())
            }
        }
    }

    @Test
    fun `lol view data returns newest cached data`() {
        runBlocking {

            val simpleView = basicSimpleLolView.copy(characterIds = listOf(1))
            val view = View(
                simpleView.id, simpleView.name, simpleView.owner, simpleView.published, listOf(
                    basicLolCharacter
                ), simpleView.game
            )
            val moreRecentDataCache =
                anotherLolDataCache.copy(characterId = 1, inserted = OffsetDateTime.now().plusHours(2))

            val viewsService = createService(
                listOf(simpleView),
                CharactersState(listOf(), listOf(basicLolCharacter)),
                listOf(
                    lolDataCache.copy(characterId = 1),
                    moreRecentDataCache
                ),
                emptyCredentialsInitialState,
                mapOf()
            )

            viewsService.getData(view)
                .onLeft { fail(it.error()) }
                .onRight { assertEquals(listOf(anotherRiotData), it) }
        }
    }

    private suspend fun createService(
        viewsState: List<SimpleView>,
        charactersState: CharactersState,
        dataCacheState: List<DataCache>,
        credentialState: CredentialsRepositoryState,
        rolesActivitiesState: Map<Role, Set<Activity>>
    ): ViewsService {
        val viewsRepository = ViewsInMemoryRepository()
            .withState(viewsState)
        val charactersRepository = CharactersInMemoryRepository()
            .withState(charactersState)
        val dataCacheRepository = DataCacheInMemoryRepository()
            .withState(dataCacheState)
        val credentialsRepository = CredentialsInMemoryRepository()
            .withState(credentialState)
        val rolesActivitiesRepository = RolesActivitiesInMemoryRepository()
            .withState(rolesActivitiesState)

        val credentialsService = CredentialsService(credentialsRepository, rolesActivitiesRepository)
        val charactersService = CharactersService(charactersRepository, raiderIoClient, riotClient)
        val dataCacheService = DataCacheService(dataCacheRepository, raiderIoClient, riotClient)

        return ViewsService(viewsRepository, charactersService, dataCacheService, raiderIoClient, credentialsService)
    }
}