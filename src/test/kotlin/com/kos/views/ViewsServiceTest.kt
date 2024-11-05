package com.kos.views

import arrow.core.Either
import com.kos.activities.Activity
import com.kos.characters.CharactersService
import com.kos.characters.CharactersTestHelper.basicLolCharacter
import com.kos.characters.CharactersTestHelper.basicWowCharacter
import com.kos.characters.CharactersTestHelper.basicWowCharacter2
import com.kos.characters.CharactersTestHelper.emptyCharactersState
import com.kos.characters.LolCharacterRequest
import com.kos.characters.WowCharacterRequest
import com.kos.characters.repository.CharactersInMemoryRepository
import com.kos.characters.repository.CharactersState
import com.kos.common.TooMuchCharacters
import com.kos.common.TooMuchViews
import com.kos.common.UserWithoutRoles
import com.kos.credentials.Credentials
import com.kos.credentials.CredentialsService
import com.kos.credentials.CredentialsTestHelper.basicCredentialsWithRolesInitialState
import com.kos.credentials.CredentialsTestHelper.emptyCredentialsInitialState
import com.kos.credentials.CredentialsTestHelper.password
import com.kos.credentials.CredentialsTestHelper.user
import com.kos.credentials.repository.CredentialsInMemoryRepository
import com.kos.credentials.repository.CredentialsRepositoryState
import com.kos.datacache.DataCache
import com.kos.datacache.DataCacheService
import com.kos.datacache.RiotMockHelper.anotherRiotData
import com.kos.datacache.TestHelper.anotherLolDataCache
import com.kos.datacache.TestHelper.lolDataCache
import com.kos.datacache.repository.DataCacheInMemoryRepository
import com.kos.eventsourcing.events.EventType
import com.kos.eventsourcing.events.ViewToBeCreated
import com.kos.eventsourcing.events.ViewToBeEdited
import com.kos.eventsourcing.events.ViewToBePatched
import com.kos.eventsourcing.events.repository.EventStoreInMemory
import com.kos.httpclients.domain.GetPUUIDResponse
import com.kos.httpclients.domain.GetSummonerResponse
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
import org.mockito.Mockito.*
import java.time.OffsetDateTime
import java.util.*
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
            val (_, viewsService) = createService(
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
            val (_, viewsService) = createService(
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
            val (_, viewsService) = createService(
                listOf(),
                emptyCharactersState,
                listOf(),
                CredentialsRepositoryState(listOf(Credentials(owner, password)), mapOf(owner to listOf(Role.USER))),
                mapOf()
            )

            val id = UUID.randomUUID().toString()
            val aggregateRoot = "credentials/owner"
            viewsService.createView(
                id,
                aggregateRoot,
                ViewToBeCreated(id, name, published, listOf(), Game.WOW, owner)
            ).onRight {
                assertEquals(id, it.id)
                assertEquals(aggregateRoot, it.aggregateRoot)
                assertEquals(it.type, EventType.VIEW_CREATED)
            }.onLeft {
                fail(it.toStr())
            }
        }
    }

    @Test
    fun `i can create a lol view`() {
        runBlocking {
            val (_, viewsService) = createService(
                listOf(),
                emptyCharactersState,
                listOf(),
                CredentialsRepositoryState(listOf(Credentials(owner, password)), mapOf(owner to listOf(Role.USER))),
                mapOf()
            )

            val aggregateRoot = "credentials/owner"
            viewsService.createView(
                id, aggregateRoot,
                ViewToBeCreated(id, name, published, listOf(), Game.LOL, owner)
            ).onRight {
                assertEquals(id, it.id)
                assertEquals(aggregateRoot, it.aggregateRoot)
                assertEquals(it.type, EventType.VIEW_CREATED)
            }.onLeft {
                fail(it.toStr())
            }

        }
    }

    @Test
    fun `i can create a wow view`() {
        runBlocking {
            val (_, viewsService) = createService(
                listOf(),
                emptyCharactersState,
                listOf(),
                basicCredentialsWithRolesInitialState,
                mapOf()
            )

            val request = ViewRequest(name, published, listOf(), Game.WOW)
            viewsService.create(user, request).onRight {
                assertEquals(request.name, it.name)
                assertEquals(request.published, it.published)
                assertEquals(request.game, it.game)
            }.onLeft {
                fail(it.toStr())
            }

            assertTrue(viewsService.create(user, ViewRequest(name, published, listOf(), Game.WOW)).isRight())
        }
    }

    @Test
    fun `i can create a lol view with some characters`() {
        runBlocking {
            val (_, viewsService) = createService(
                listOf(),
                emptyCharactersState,
                listOf(),
                CredentialsRepositoryState(listOf(Credentials(owner, password)), mapOf(owner to listOf(Role.USER))),
                mapOf()
            )

            val charactersRequest = (1..10).map { LolCharacterRequest(it.toString(), it.toString()) }

            `when`(riotClient.getPUUIDByRiotId(anyString(), anyString())).thenAnswer { invocation ->
                val name = invocation.getArgument<String>(0)
                val tag = invocation.getArgument<String>(1)
                Either.Right(GetPUUIDResponse(UUID.randomUUID().toString(), name, tag))
            }

            `when`(riotClient.getSummonerByPuuid(anyString())).thenAnswer { invocation ->
                val puuid = invocation.getArgument<String>(0)
                Either.Right(
                    GetSummonerResponse(
                        UUID.randomUUID().toString(),
                        UUID.randomUUID().toString(),
                        puuid,
                        10,
                        10L,
                        200
                    )
                )
            }

            val request = ViewRequest(name, published, charactersRequest, Game.LOL)
            viewsService.create(owner, request).onRight {
                assertEquals(request.name, it.name)
                assertEquals(request.published, it.published)
                assertEquals(request.game, it.game)
                assertEquals(request.characters.size, it.characterIds.size)
            }.onLeft {
                fail(it.toStr())
            }

            assertTrue(viewsService.create(owner, ViewRequest(name, published, listOf(), Game.LOL)).isRight())
        }
    }

    @Test
    fun `i can't create a lol view because too many characters provided`() {
        runBlocking {
            val (_, viewsService) = createService(
                listOf(),
                emptyCharactersState,
                listOf(),
                CredentialsRepositoryState(listOf(Credentials(owner, password)), mapOf(owner to listOf(Role.USER))),
                mapOf()
            )

            val charactersRequest = (1..11).map { LolCharacterRequest(it.toString(), it.toString()) }

            val request = ViewRequest(name, published, charactersRequest, Game.WOW)
            viewsService.create(owner, request).onRight {
                fail()
            }.onLeft {
                assertTrue(it is TooMuchCharacters)
            }
        }
    }

    @Test
    fun `i can edit a lol view`() {
        runBlocking {
            val (_, viewsService) = createService(
                listOf(basicSimpleLolView),
                emptyCharactersState,
                listOf(),
                basicCredentialsWithRolesInitialState,
                mapOf()
            )

            val newName = "new-name"
            viewsService.editView(
                ViewToBeEdited(id, newName, published, listOf(), Game.LOL)
            ).onRight {
                assertEquals(id, it.viewId)
                assertEquals(listOf(), it.characters)
                assertEquals(newName, it.name)
                assertEquals(published, it.published)
            }.onLeft {
                fail(it.toStr())
            }
        }
    }

    @Test
    fun `i can't edit a lol view because too many characters provided`() {
        runBlocking {
            val (_, viewsService) = createService(
                listOf(basicSimpleLolView),
                emptyCharactersState,
                listOf(),
                basicCredentialsWithRolesInitialState,
                mapOf()
            )

            val newName = "new-name"
            val charactersRequest = (1..11).map { LolCharacterRequest(it.toString(), it.toString()) }

            val request = ViewRequest(newName, published, charactersRequest, Game.LOL)
            viewsService.edit(basicSimpleLolView.id, user, request).onRight {
                assertEquals(request.name, it.name)
                assertEquals(request.published, it.published)
            }.onLeft {
                assertTrue(it is TooMuchCharacters)
            }
        }
    }

    @Test
    fun `users cant create more than maximum views`() {
        runBlocking {
            val (_, viewsService) = createService(
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
            val (_, viewsService) = createService(
                (1..100).map { SimpleView(it.toStr(), it.toStr(), owner, true, listOf(), Game.WOW) },
                emptyCharactersState,
                listOf(),
                CredentialsRepositoryState(listOf(Credentials(owner, password)), mapOf(owner to listOf(Role.ADMIN))),
                mapOf()
            )

            val request = ViewRequest(name, published, listOf(), Game.WOW)
            viewsService.create(owner, request).onRight {
                assertTrue(it.id.isNotEmpty())
                assertEquals("/credentials/owner", it.aggregateRoot)
                assertEquals(EventType.VIEW_TO_BE_CREATED, it.type)
            }.onLeft {
                fail(it.toStr())
            }
        }
    }

    @Test
    fun `user without role can't create a view`() {
        runBlocking {
            val (_, viewsService) = createService(
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

            val (viewsRepository, viewsService) = createService(
                listOf(basicSimpleWowView),
                emptyCharactersState,
                listOf(),
                basicCredentialsWithRolesInitialState,
                mapOf()
            )

            assertTrue(viewsRepository.state().all { it.characterIds.isEmpty() })

            viewsService.editView(
                ViewToBeEdited(id, name, published, listOf(request1, request2, request3, request4), Game.WOW)
            ).onRight {
                assertEquals(id, it.viewId)
                assertEquals(listOf<Long>(1, 2, 3, 4), it.characters)
                assertEquals(name, it.name)
                assertEquals(published, it.published)
            }.onLeft {
                fail(it.toStr())
            }

            assertTrue(viewsRepository.state().all { it.characterIds.size == 4 })
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

            val (viewsRepository, viewsService) = createService(
                listOf(basicSimpleWowView.copy(characterIds = listOf(1))),
                CharactersState(
                    listOf(basicWowCharacter, basicWowCharacter2),
                    listOf()
                ),
                listOf(),
                basicCredentialsWithRolesInitialState,
                mapOf()
            )

            assertTrue(viewsRepository.state().all { it.characterIds.size == 1 })

            viewsService.editView(
                ViewToBeEdited(id, name, published, listOf(request1, request2, request3, request4), Game.WOW)
            ).onRight {
                assertEquals(listOf<Long>(3, 4, 5, 6), it.characters)
                assertEquals(name, it.name)
                assertEquals(id, it.viewId)
                assertEquals(published, it.published)
            }.onLeft {
                fail(it.toStr())
            }

            assertEquals(listOf<Long>(3, 4, 5, 6), viewsRepository.state().first().characterIds)
        }
    }

    @Test
    fun `i can delete a view`() {
        runBlocking {

            val (viewsRepository, viewsService) = createService(
                listOf(basicSimpleWowView),
                emptyCharactersState,
                listOf(),
                emptyCredentialsInitialState,
                mapOf()
            )

            assertTrue(viewsRepository.state().size == 1)
            assertEquals(viewsService.delete("1"), ViewDeleted("1"))
            assertTrue(viewsRepository.state().isEmpty())
        }
    }

    @Test
    fun `i can patch a view`() {
        runBlocking {
            val patchedName = "new-name"

            val (_, viewsService) = createService(
                listOf(basicSimpleWowView),
                emptyCharactersState,
                listOf(),
                basicCredentialsWithRolesInitialState,
                mapOf()
            )

            val patch = viewsService.patchView(
                ViewToBePatched(
                    basicSimpleWowView.id,
                    patchedName,
                    null,
                    null,
                    Game.WOW
                )
            )
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
    fun `i can't patch a view because too many characters`() {
        runBlocking {
            val patchedName = "new-name"

            val (_, viewsService) = createService(
                listOf(basicSimpleWowView),
                emptyCharactersState,
                listOf(),
                basicCredentialsWithRolesInitialState,
                mapOf()
            )

            val charactersRequest = (1..11).map { LolCharacterRequest(it.toString(), it.toString()) }

            val patch = viewsService.patch(
                basicSimpleWowView.id,
                user,
                ViewPatchRequest(patchedName, null, charactersRequest, Game.WOW)
            )
            patch.onRight {
                assertEquals(basicSimpleWowView.id, it.viewId)
                assertEquals(null, it.published)
                assertEquals(null, it.characters)
                assertEquals(patchedName, it.name)
            }.onLeft {
                assertTrue(it is TooMuchCharacters)
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

            val (viewsRepository, viewsService) = createService(
                listOf(basicSimpleLolView.copy(characterIds = listOf(1))),
                emptyCharactersState,
                listOf(),
                basicCredentialsWithRolesInitialState,
                mapOf()
            )

            assertTrue(viewsRepository.state().all { it.characterIds.size == 1 })

            viewsService.patchView(
                ViewToBePatched(
                    id,
                    null,
                    null,
                    listOf(request1, request2, request3, request4),
                    Game.WOW
                )
            ).onRight {
                assertEquals(basicSimpleWowView.id, it.viewId)
                assertEquals(null, it.published)
                assertEquals(listOf<Long>(1, 2, 3, 4), it.characters)
                assertEquals(null, it.name)
            }.onLeft {
                fail(it.toStr())
            }

            assertTrue(viewsRepository.state().all { it.characterIds.size == 4 })
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

            val (_, viewsService) = createService(
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
    ): Pair<ViewsInMemoryRepository, ViewsService> {
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
        val service =
            ViewsService(
                viewsRepository,
                charactersService,
                dataCacheService,
                raiderIoClient,
                credentialsService,
                EventStoreInMemory()
            )

        return Pair(viewsRepository, service)
    }
}