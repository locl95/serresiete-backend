package com.kos.views

import arrow.core.Either
import com.kos.characters.CharacterCreateRequest
import com.kos.characters.CharactersService
import com.kos.characters.CharactersTestHelper.basicLolCharacter
import com.kos.characters.CharactersTestHelper.basicLolCharacter2
import com.kos.characters.CharactersTestHelper.basicWowCharacter
import com.kos.characters.CharactersTestHelper.basicWowCharacter2
import com.kos.characters.CharactersTestHelper.emptyCharactersState
import com.kos.characters.LolCharacterRequest
import com.kos.characters.WowCharacterRequest
import com.kos.characters.repository.CharactersInMemoryRepository
import com.kos.characters.repository.CharactersState
import com.kos.clients.blizzard.BlizzardClient
import com.kos.clients.domain.GetPUUIDResponse
import com.kos.clients.domain.GetSummonerResponse
import com.kos.clients.raiderio.RaiderIoClient
import com.kos.clients.riot.RiotClient
import com.kos.common.RetryConfig
import com.kos.common.TooMuchCharacters
import com.kos.common.TooMuchViews
import com.kos.common.UserWithoutRoles
import com.kos.credentials.Credentials
import com.kos.credentials.CredentialsService
import com.kos.credentials.CredentialsTestHelper.basicCredentialsWithRolesInitialState
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
import com.kos.eventsourcing.events.*
import com.kos.eventsourcing.events.repository.EventStore
import com.kos.eventsourcing.events.repository.EventStoreInMemory
import com.kos.roles.Role
import com.kos.views.ViewsTestHelper.basicSimpleGameViews
import com.kos.views.ViewsTestHelper.basicSimpleLolView
import com.kos.views.ViewsTestHelper.basicSimpleLolViews
import com.kos.views.ViewsTestHelper.basicSimpleWowView
import com.kos.views.ViewsTestHelper.id
import com.kos.views.ViewsTestHelper.name
import com.kos.views.ViewsTestHelper.owner
import com.kos.views.ViewsTestHelper.published
import com.kos.views.repository.ViewsInMemoryRepository
import io.mockk.InternalPlatformDsl.toStr
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
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
    private val blizzardClient = mock(BlizzardClient::class.java)
    private val retryConfig = RetryConfig(1, 1)

    private val aggregateRoot = "/credentials/owner"
    private val defaultCredentialsState = CredentialsRepositoryState(
        listOf(Credentials(owner, password)),
        mapOf(owner to listOf(Role.USER))
    )

    @Nested
    inner class BehaviorOfGetViews {
        @Test
        fun `i can get own views`() {
            runBlocking {
                val (_, viewsService) = createService(
                    listOf(basicSimpleWowView),
                    emptyCharactersState,
                    listOf(),
                    emptyCredentialsInitialState
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
                    emptyCredentialsInitialState
                )

                assertEquals(basicSimpleWowView, viewsService.getSimple("1"))
            }
        }

        @Test
        fun `i can get views of a game`() {
            runBlocking {
                val (_, viewsService) = createService(
                    basicSimpleGameViews,
                    emptyCharactersState,
                    listOf(),
                    emptyCredentialsInitialState
                )

                assertEquals(basicSimpleLolViews, viewsService.getViews(Game.LOL, false))
            }
        }

        @Test
        fun `i can get views returns only wow featured views`() {
            runBlocking {
                val (_, viewsService) = createService(
                    basicSimpleGameViews,
                    emptyCharactersState,
                    listOf(),
                    emptyCredentialsInitialState
                )

                assertEquals(
                    listOf(basicSimpleWowView.copy(id = "3", featured = true)),
                    viewsService.getViews(Game.WOW, true)
                )
            }
        }
    }

    @Nested
    inner class BehaviorOfCreateView {

        @Test
        fun `create a wow view stores a create view event`() {
            runBlocking {
                val (eventStore, viewsService) = createService(
                    listOf(),
                    emptyCharactersState,
                    listOf(),
                    defaultCredentialsState,
                )

                viewsService.create(
                    owner,
                    ViewRequest(name, published, listOf(), Game.WOW, false)
                ).onRight {
                    assertOperation(it, EventType.VIEW_TO_BE_CREATED)
                }.onLeft {
                    fail(it.toStr())
                }

                assertEventStoredCorrectly(
                    eventStore,
                    aggregateRoot,
                    name,
                    published,
                    listOf(),
                    Game.WOW, false
                )
            }
        }

        @Test
        fun `create a lol view stores a create view event`() {
            runBlocking {
                val (eventStore, viewsService) = createService(
                    listOf(),
                    emptyCharactersState,
                    listOf(),
                    defaultCredentialsState,
                )

                viewsService.create(
                    owner,
                    ViewRequest(name, published, listOf(), Game.LOL, false)
                ).onRight {
                    assertOperation(it, EventType.VIEW_TO_BE_CREATED)
                }.onLeft {
                    fail(it.toStr())
                }

                assertEventStoredCorrectly(
                    eventStore,
                    aggregateRoot,
                    name,
                    published,
                    listOf(),
                    Game.LOL,
                    false
                )
            }
        }

        @Test
        fun `create a lol view with some characters stores a create view event`() {
            runBlocking {
                val (eventStore, viewsService) = createService(
                    listOf(),
                    emptyCharactersState,
                    listOf(),
                    defaultCredentialsState
                )

                val charactersRequest = (1..10).map { LolCharacterRequest(it.toString(), it.toString()) }

                viewsService.create(
                    owner,
                    ViewRequest(name, published, charactersRequest, Game.LOL, false)
                ).onRight {
                    assertOperation(it, EventType.VIEW_TO_BE_CREATED)
                }.onLeft {
                    fail(it.toStr())
                }

                assertEventStoredCorrectly(
                    eventStore,
                    aggregateRoot,
                    name,
                    published,
                    charactersRequest,
                    Game.LOL, false
                )

            }
        }

        @Test
        fun `trying to exceed the maximum number of views allowed does not store an event`() {
            runBlocking {
                val (eventStore, viewsService) = createService(
                    listOf(basicSimpleLolView, basicSimpleLolView),
                    emptyCharactersState,
                    listOf(),
                    defaultCredentialsState
                )

                viewsService.create(
                    owner,
                    ViewRequest(name, published, listOf(), Game.WOW, false)
                ).onRight {
                    fail()
                }.onLeft {
                    assertTrue(it is TooMuchViews)
                }

                assertNoEventsStored(eventStore)
            }
        }

        @Test
        fun `create a lol view with too many characters does not store an event`() {
            runBlocking {
                val (eventStore, viewsService) = createService(
                    listOf(),
                    emptyCharactersState,
                    listOf(),
                    defaultCredentialsState
                )

                val charactersRequest = (1..11).map { LolCharacterRequest(it.toString(), it.toString()) }

                viewsService.create(owner, ViewRequest(name, published, charactersRequest, Game.WOW, false)).onRight {
                    fail()
                }.onLeft {
                    assertTrue(it is TooMuchCharacters)
                }

                assertNoEventsStored(eventStore)
            }
        }

        @Test
        fun `admins can create a huge amount of views and an event gets stored`() {
            runBlocking {
                val (eventStore, viewsService) = createService(
                    (1..100).map { SimpleView(it.toStr(), it.toStr(), owner, true, listOf(), Game.WOW, false) },
                    emptyCharactersState,
                    listOf(),
                    CredentialsRepositoryState(
                        listOf(Credentials(owner, password)),
                        mapOf(owner to listOf(Role.ADMIN))
                    )
                )

                viewsService.create(
                    owner,
                    ViewRequest(name, published, listOf(), Game.WOW, false)
                ).onRight {
                    assertOperation(it, EventType.VIEW_TO_BE_CREATED)
                }.onLeft {
                    fail(it.toStr())
                }

                assertEventStoredCorrectly(
                    eventStore,
                    aggregateRoot,
                    name,
                    published,
                    listOf(),
                    Game.WOW, false
                )
            }
        }

        @Test
        fun `user without role trying to create a view does not store an event`() {
            runBlocking {
                val (eventStore, viewsService) = createService(
                    listOf(),
                    emptyCharactersState,
                    listOf(),
                    CredentialsRepositoryState(listOf(Credentials(owner, password)), mapOf(owner to listOf()))
                )

                viewsService.create(
                    owner,
                    ViewRequest(name, published, listOf(), Game.WOW, false)
                ).onRight {
                    fail()
                }.onLeft {
                    assertTrue(it is UserWithoutRoles)
                }

                assertNoEventsStored(eventStore)
            }
        }

        @Test
        fun `create view processing view to be created event stores an event`() {
            runBlocking {
                val (eventStore, viewsService) = createService(
                    listOf(),
                    emptyCharactersState,
                    listOf(),
                    defaultCredentialsState
                )

                createViewFromEventAndAssert(
                    viewsService,
                    ViewToBeCreatedEvent(id, name, published, listOf(), Game.LOL, owner, false)
                )

                assertEventStoredCorrectly(
                    eventStore,
                    ViewCreatedEvent(id, name, owner, listOf(), published, Game.LOL, false)
                )
            }
        }

        private suspend fun createViewFromEventAndAssert(
            viewsService: ViewsService,
            viewToBeCreatedEvent: ViewToBeCreatedEvent
        ) {
            viewsService.createView(
                id,
                aggregateRoot,
                viewToBeCreatedEvent
            ).onRight {
                assertOperation(it, EventType.VIEW_CREATED)
            }.onLeft {
                fail(it.message)
            }
        }
    }

    @Nested
    inner class BehaviorOfEditView {

        @Test
        fun `editing a lol view stores an event`() {
            runBlocking {
                val (eventStore, viewsService) = createService(
                    listOf(basicSimpleLolView),
                    emptyCharactersState,
                    listOf(),
                    defaultCredentialsState
                )

                val newName = "new-name"
                viewsService.edit(
                    owner,
                    id,
                    ViewRequest(newName, published, listOf(), Game.LOL, false)
                ).onRight {
                    assertOperation(it, EventType.VIEW_TO_BE_EDITED)
                }.onLeft {
                    fail(it.toStr())
                }

                assertEventStoredCorrectly(
                    eventStore,
                    ViewToBeEditedEvent(id, newName, published, listOf(), Game.LOL, false)
                )
            }
        }

        @Test
        fun `editing a lol view with too many characters does not store an event`() {
            runBlocking {
                val (eventStore, viewsService) = createService(
                    listOf(basicSimpleLolView),
                    emptyCharactersState,
                    listOf(),
                    CredentialsRepositoryState(listOf(Credentials(owner, password)), mapOf(owner to listOf(Role.USER)))
                )
                val charactersRequest = (1..11).map { LolCharacterRequest(it.toString(), it.toString()) }

                viewsService.edit(
                    owner, id,
                    ViewRequest(name, published, charactersRequest, Game.LOL, false)
                ).onRight {
                    fail()
                }.onLeft {
                    assertTrue(it is TooMuchCharacters)
                }

                assertNoEventsStored(eventStore)
            }
        }

        @Test
        fun `editing a wow view with more than one character stores an event`() {
            runBlocking {

                val request1 = WowCharacterRequest("a", "r", "r")
                val request2 = WowCharacterRequest("b", "r", "r")
                val request3 = WowCharacterRequest("c", "r", "r")
                val request4 = WowCharacterRequest("d", "r", "r")

                val (eventStore, viewsService) = createService(
                    listOf(basicSimpleWowView),
                    emptyCharactersState,
                    listOf(),
                    defaultCredentialsState
                )

                val charactersRequest = listOf(request1, request2, request3, request4)
                viewsService.edit(
                    owner,
                    id,
                    ViewRequest(name, published, charactersRequest, Game.WOW, false)
                ).onRight {
                    assertOperation(it, EventType.VIEW_TO_BE_EDITED)
                }.onLeft {
                    fail(it.toStr())
                }

                assertEventStoredCorrectly(
                    eventStore,
                    ViewToBeEditedEvent(id, name, published, charactersRequest, Game.WOW, false)
                )

            }
        }

        @Test
        fun `editing a lol view processing view to be edited stores an event`() {
            runBlocking {
                val (eventStore, viewsService) = createService(
                    listOf(basicSimpleLolView),
                    emptyCharactersState,
                    listOf(),
                    defaultCredentialsState
                )

                val newName = "new-name"
                viewsService.editView(
                    id,
                    aggregateRoot,
                    ViewToBeEditedEvent(id, newName, published, listOf(), Game.LOL, false)
                ).onRight {
                    assertOperation(it, EventType.VIEW_EDITED)
                }.onLeft {
                    fail(it.toStr())
                }

                assertEventStoredCorrectly(
                    eventStore,
                    ViewEditedEvent(id, newName, listOf(), published, Game.LOL, false)
                )
            }
        }

        @Test
        fun `editing a view processing view to be edited, an event is stored with the actual characters of the view`() {
            runBlocking {
                val request1 = WowCharacterRequest("a", "r", "r")
                val request2 = WowCharacterRequest("b", "r", "r")
                val request3 = WowCharacterRequest("c", "r", "r")
                val request4 = WowCharacterRequest("d", "r", "r")

                `when`(raiderIoClient.exists(request1)).thenReturn(true)
                `when`(raiderIoClient.exists(request2)).thenReturn(true)
                `when`(raiderIoClient.exists(request3)).thenReturn(true)
                `when`(raiderIoClient.exists(request4)).thenReturn(true)

                val (eventStore, viewsService) = createService(
                    listOf(basicSimpleWowView.copy(characterIds = listOf(1))),
                    CharactersState(
                        listOf(basicWowCharacter, basicWowCharacter2),
                        listOf(),
                        listOf()
                    ),
                    listOf(),
                    basicCredentialsWithRolesInitialState
                )

                viewsService.editView(
                    id,
                    aggregateRoot,
                    ViewToBeEditedEvent(
                        id,
                        name,
                        published,
                        listOf(request1, request2, request3, request4),
                        Game.WOW,
                        false
                    )
                ).onRight {
                    assertOperation(it, EventType.VIEW_EDITED)
                }.onLeft {
                    fail(it.toStr())
                }

                assertEventStoredCorrectly(
                    eventStore,
                    ViewEditedEvent(id, name, listOf(3, 4, 5, 6), published, Game.WOW, false)
                )
            }
        }

        @Test
        fun `editing a lol view processing view to be edited, an event is stored with the actual characters of the view`() {
            runBlocking {
                val charactersRequest = (3..6).map { LolCharacterRequest(it.toString(), it.toString()) }

                val (eventStore, viewsService) = createService(
                    listOf(basicSimpleLolView.copy(characterIds = listOf(1))),
                    CharactersState(
                        listOf(),
                        listOf(),
                        listOf(basicLolCharacter, basicLolCharacter2)
                    ),
                    listOf(),
                    basicCredentialsWithRolesInitialState
                )

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

                viewsService.editView(
                    id,
                    aggregateRoot,
                    ViewToBeEditedEvent(id, name, published, charactersRequest, Game.LOL, false)
                ).onRight {
                    assertOperation(it, EventType.VIEW_EDITED)
                }.onLeft {
                    fail(it.toStr())
                }

                assertEventStoredCorrectly(
                    eventStore,
                    ViewEditedEvent(id, name, listOf(3, 4, 5, 6), published, Game.LOL, false)
                )
            }
        }
    }

    @Nested
    inner class BehaviorOfDeleteView {
        @Test
        fun `i can delete a view`() {
            runBlocking {

                val (_, viewsService) = createService(
                    listOf(basicSimpleWowView),
                    emptyCharactersState,
                    listOf(),
                    emptyCredentialsInitialState
                )

                assertEquals(viewsService.delete("1"), ViewDeleted("1"))
            }
        }
    }

    @Nested
    inner class BehaviorOfPatchView {
        @Test
        fun `patch a view stores an event`() {
            runBlocking {
                val patchedName = "new-name"

                val (eventStore, viewsService) = createService(
                    listOf(basicSimpleWowView),
                    emptyCharactersState,
                    listOf(),
                    CredentialsRepositoryState(listOf(Credentials(owner, password)), mapOf(owner to listOf(Role.USER)))
                )

                viewsService.patch(
                    owner,
                    id,
                    ViewPatchRequest(patchedName, null, null, Game.WOW, false)
                ).onRight {
                    assertOperation(it, EventType.VIEW_TO_BE_PATCHED)
                }.onLeft {
                    fail(it.toStr())
                }

                assertEventStoredCorrectly(
                    eventStore,
                    ViewToBePatchedEvent(id, patchedName, null, null, Game.WOW, false)
                )
            }
        }

        @Test
        fun `trying to patch a view with too many characters fails without storing an event`() {
            runBlocking {
                val (eventStore, viewsService) = createService(
                    listOf(basicSimpleWowView),
                    emptyCharactersState,
                    listOf(),
                    CredentialsRepositoryState(listOf(Credentials(owner, password)), mapOf(owner to listOf(Role.USER)))
                )

                val id = UUID.randomUUID().toString()

                val charactersRequest = (1..11).map { LolCharacterRequest(it.toString(), it.toString()) }

                viewsService.patch(
                    owner,
                    id,
                    ViewPatchRequest(null, null, charactersRequest, Game.WOW, false)
                ).onRight {
                    fail()
                }.onLeft {
                    assertTrue(it is TooMuchCharacters)
                }

                assertNoEventsStored(eventStore)
            }
        }

        @Test
        fun `patching a view patching more than one character stores an event`() {
            runBlocking {

                val request1 = WowCharacterRequest("a", "r", "r")
                val request2 = WowCharacterRequest("b", "r", "r")
                val request3 = WowCharacterRequest("c", "r", "r")
                val request4 = WowCharacterRequest("d", "r", "r")

                val (eventStore, viewsService) = createService(
                    listOf(basicSimpleLolView.copy(characterIds = listOf(1))),
                    emptyCharactersState,
                    listOf(),
                    CredentialsRepositoryState(listOf(Credentials(owner, password)), mapOf(owner to listOf(Role.USER)))
                )

                val characterRequests = listOf(request1, request2, request3, request4)
                viewsService.patch(
                    owner,
                    id,
                    ViewPatchRequest(null, null, characterRequests, Game.WOW, false)
                ).onRight {
                    assertOperation(it, EventType.VIEW_TO_BE_PATCHED)
                }.onLeft {
                    fail(it.toStr())
                }

                val expectedEvent = Event(
                    aggregateRoot,
                    id,
                    ViewToBePatchedEvent(id, null, null, characterRequests, Game.WOW, false)
                )

                val events = eventStore.getEvents(null).toList()

                assertEquals(1, events.size)
                assertEquals(EventWithVersion(1, expectedEvent), events.first())

            }
        }

        @Test
        fun `patching a lol view processing event stores an event`() {
            runBlocking {
                val charactersRequest = (3..6).map { LolCharacterRequest(it.toString(), it.toString()) }

                val (eventStore, viewsService) = createService(
                    listOf(basicSimpleLolView.copy(characterIds = listOf(1))),
                    emptyCharactersState,
                    listOf(),
                    CredentialsRepositoryState(listOf(Credentials(owner, password)), mapOf(owner to listOf(Role.USER)))
                )

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

                viewsService.patchView(
                    id,
                    aggregateRoot,
                    ViewToBePatchedEvent(id, null, null, charactersRequest, Game.LOL, false)
                ).onRight {
                    assertOperation(it, EventType.VIEW_PATCHED)
                }.onLeft {
                    fail(it.toStr())
                }

                assertEventStoredCorrectly(
                    eventStore,
                    ViewPatchedEvent(id, null, listOf(1, 2, 3, 4), null, Game.LOL, false)
                )
            }
        }

        @Test
        fun `patching a wow view processing event stores an event`() {
            runBlocking {

                val request1 = WowCharacterRequest("a", "r", "r")
                val request2 = WowCharacterRequest("b", "r", "r")
                val request3 = WowCharacterRequest("c", "r", "r")
                val request4 = WowCharacterRequest("d", "r", "r")

                `when`(raiderIoClient.exists(request1)).thenReturn(true)
                `when`(raiderIoClient.exists(request2)).thenReturn(true)
                `when`(raiderIoClient.exists(request3)).thenReturn(true)
                `when`(raiderIoClient.exists(request4)).thenReturn(true)


                val (eventStore, viewsService) = createService(
                    listOf(basicSimpleLolView.copy(characterIds = listOf(1))),
                    emptyCharactersState,
                    listOf(),
                    CredentialsRepositoryState(listOf(Credentials(owner, password)), mapOf(owner to listOf(Role.USER)))
                )

                val charactersRequest = listOf(request1, request2, request3, request4)

                viewsService.patchView(
                    id,
                    aggregateRoot,
                    ViewToBePatchedEvent(id, null, null, charactersRequest, Game.WOW, false)
                ).onRight {
                    assertOperation(it, EventType.VIEW_PATCHED)
                }.onLeft {
                    fail(it.toStr())
                }

                assertEventStoredCorrectly(
                    eventStore,
                    ViewPatchedEvent(id, null, listOf(1, 2, 3, 4), null, Game.WOW, false)
                )
            }
        }
    }

    @Nested
    inner class BehaviorOfGetData {
        @Test
        fun `lol view data returns newest cached data`() {
            runBlocking {

                val simpleView = basicSimpleLolView.copy(characterIds = listOf(1))
                val view = View(
                    simpleView.id, simpleView.name, simpleView.owner, simpleView.published, listOf(
                        basicLolCharacter
                    ), simpleView.game, simpleView.featured
                )
                val moreRecentDataCache =
                    anotherLolDataCache.copy(characterId = 1, inserted = OffsetDateTime.now().plusHours(2))

                val (_, viewsService) = createService(
                    listOf(simpleView),
                    CharactersState(listOf(), listOf(), listOf(basicLolCharacter)),
                    listOf(
                        lolDataCache.copy(characterId = 1),
                        moreRecentDataCache
                    ),
                    emptyCredentialsInitialState
                )

                viewsService.getData(view)
                    .onLeft { fail(it.error()) }
                    .onRight { assertEquals(listOf(anotherRiotData), it) }
            }
        }
    }

    private suspend fun createService(
        viewsState: List<SimpleView>,
        charactersState: CharactersState,
        dataCacheState: List<DataCache>,
        credentialState: CredentialsRepositoryState,
    ): Pair<EventStore, ViewsService> {
        val viewsRepository = ViewsInMemoryRepository()
            .withState(viewsState)
        val charactersRepository = CharactersInMemoryRepository()
            .withState(charactersState)
        val dataCacheRepository = DataCacheInMemoryRepository()
            .withState(dataCacheState)
        val credentialsRepository = CredentialsInMemoryRepository()
            .withState(credentialState)
        val eventStore = EventStoreInMemory()

        val credentialsService = CredentialsService(credentialsRepository)
        val charactersService = CharactersService(charactersRepository, raiderIoClient, riotClient, blizzardClient)
        val dataCacheService =
            DataCacheService(dataCacheRepository, raiderIoClient, riotClient, blizzardClient, retryConfig)
        val service =
            ViewsService(
                viewsRepository,
                charactersService,
                dataCacheService,
                credentialsService,
                eventStore
            )

        return Pair(eventStore, service)
    }

    private fun assertOperation(operation: Operation, expectedType: EventType) {
        assertTrue(operation.id.isNotEmpty())
        assertEquals(aggregateRoot, operation.aggregateRoot)
        assertEquals(expectedType, operation.type)
    }

    private suspend fun assertEventStoredCorrectly(
        eventStore: EventStore,
        aggregateRoot: String,
        viewName: String,
        published: Boolean,
        characters: List<CharacterCreateRequest>,
        game: Game,
        featured: Boolean,
    ) {
        val events = eventStore.getEvents(null).toList()
        assertEquals(1, events.size)
        val actual = events.first().event
        val data = actual.eventData as ViewToBeCreatedEvent

        assertTrue(actual.operationId.isNotEmpty())
        assertEquals(aggregateRoot, actual.aggregateRoot)
        assertEquals(data.id, actual.operationId)
        assertEquals(viewName, data.name)
        assertEquals(published, data.published)
        assertEquals(characters, data.characters)
        assertEquals(game, data.game)
        assertEquals(owner, data.owner)
        assertEquals(featured, data.featured)
    }

    private suspend fun assertEventStoredCorrectly(eventStore: EventStore, eventData: EventData) {
        val events = eventStore.getEvents(null).toList()

        val expectedEvent = Event(
            aggregateRoot,
            id,
            eventData
        )

        assertEquals(1, events.size)
        assertEquals(EventWithVersion(1, expectedEvent), events.first())
    }

    private suspend fun assertNoEventsStored(eventStore: EventStore) {
        val events = eventStore.getEvents(null).toList()
        assertEquals(0, events.size)
    }
}