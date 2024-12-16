package com.kos.eventsourcing


import arrow.core.Either
import com.kos.assertTrue
import com.kos.characters.CharactersService
import com.kos.characters.CharactersTestHelper
import com.kos.characters.repository.CharactersInMemoryRepository
import com.kos.characters.repository.CharactersState
import com.kos.clients.blizzard.BlizzardClient
import com.kos.clients.raiderio.RaiderIoClient
import com.kos.clients.riot.RiotClient
import com.kos.common.NotFound
import com.kos.common.RetryConfig
import com.kos.credentials.CredentialsService
import com.kos.credentials.CredentialsTestHelper
import com.kos.credentials.repository.CredentialsInMemoryRepository
import com.kos.credentials.repository.CredentialsRepositoryState
import com.kos.datacache.DataCache
import com.kos.datacache.DataCacheService
import com.kos.datacache.repository.DataCacheInMemoryRepository
import com.kos.eventsourcing.events.*
import com.kos.eventsourcing.events.repository.EventStore
import com.kos.eventsourcing.events.repository.EventStoreInMemory
import com.kos.eventsourcing.subscriptions.EventSubscription
import com.kos.eventsourcing.subscriptions.SubscriptionState
import com.kos.eventsourcing.subscriptions.SubscriptionStatus
import com.kos.eventsourcing.subscriptions.repository.SubscriptionsInMemoryRepository
import com.kos.views.Game
import com.kos.views.SimpleView
import com.kos.views.ViewsService
import com.kos.views.ViewsTestHelper
import com.kos.views.repository.ViewsInMemoryRepository
import com.kos.views.repository.ViewsRepository
import io.mockk.coVerify
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EventSubscriptionTest {
    private val retryConfig = RetryConfig(1, 1)
    private val raiderIoClient = Mockito.mock(RaiderIoClient::class.java)
    private val riotClient = Mockito.mock(RiotClient::class.java)
    private val blizzardClient = Mockito.mock(BlizzardClient::class.java)

    @Nested
    inner class BehaviorOfProcessPendingEvents {
        @Test
        fun `processPendingEvents throws exception if subscription is not found`() {
            runBlocking {
                val eventStore = EventStoreInMemory()
                val subscriptionsRepository = SubscriptionsInMemoryRepository()

                val subscription = EventSubscription(
                    subscriptionName = "testSubscription",
                    eventStore = eventStore,
                    subscriptionsRepository = subscriptionsRepository,
                    retryConfig = retryConfig,
                    process = { Either.Right(Unit) }
                )

                assertThrows<Exception> {
                    subscription.processPendingEvents()
                }
            }
        }

        @Test
        fun `processPendingEvents updates state to WAITING on successful processing`() {
            runBlocking {
                val eventData = ViewToBeCreatedEvent("id", "name", true, listOf(), Game.LOL, "owner", false)
                val event = Event("root", "id", eventData)
                val eventWithVersion = EventWithVersion(1, event)

                val eventStore = EventStoreInMemory().withState(listOf(eventWithVersion))

                val initialSubscriptionStateTime = OffsetDateTime.now()
                val subscriptionState = SubscriptionState(
                    SubscriptionStatus.WAITING,
                    version = 0,
                    time = initialSubscriptionStateTime
                )

                val subscriptionsRepository = SubscriptionsInMemoryRepository().withState(
                    mapOf(
                        "testSubscription" to subscriptionState
                    )
                )

                val subscription = EventSubscription(
                    subscriptionName = "testSubscription",
                    eventStore = eventStore,
                    subscriptionsRepository = subscriptionsRepository,
                    retryConfig = retryConfig,
                    process = { Either.Right(Unit) }
                )

                subscription.processPendingEvents()

                val finalSubscriptionState = subscriptionsRepository.getState("testSubscription")

                assertEquals(SubscriptionStatus.WAITING, finalSubscriptionState?.status)
                assertEquals(1, finalSubscriptionState?.version)
                assertTrue(initialSubscriptionStateTime.isBefore(finalSubscriptionState?.time))
            }
        }

        @Test
        fun `processPendingEvents sets state to FAILED on processing error`() {
            runBlocking {
                val eventData = ViewToBeCreatedEvent("id", "name", true, listOf(), Game.LOL, "owner", false)
                val event = Event("root", "id", eventData)
                val eventWithVersion = EventWithVersion(1, event)

                val eventStore = EventStoreInMemory().withState(listOf(eventWithVersion))

                val initialSubscriptionStateTime = OffsetDateTime.now()
                val subscriptionState = SubscriptionState(
                    SubscriptionStatus.WAITING,
                    version = 0,
                    time = initialSubscriptionStateTime
                )

                val subscriptionsRepository = SubscriptionsInMemoryRepository().withState(
                    mapOf(
                        "testSubscription" to subscriptionState
                    )
                )

                val subscription = EventSubscription(
                    subscriptionName = "testSubscription",
                    eventStore = eventStore,
                    subscriptionsRepository = subscriptionsRepository,
                    retryConfig = retryConfig,
                    process = { Either.Left(NotFound("Simulated error")) }
                )

                subscription.processPendingEvents()

                val finalSubscriptionState = subscriptionsRepository.getState("testSubscription")

                assertEquals(SubscriptionStatus.FAILED, finalSubscriptionState?.status)
                assertEquals(0, finalSubscriptionState?.version)
                assertTrue(initialSubscriptionStateTime.isBefore(finalSubscriptionState?.time))
            }
        }

        @Test
        fun `processPendingEvents sets state to FAILED on processing error and stops processing further events`() {
            runBlocking {
                val eventData = ViewToBeCreatedEvent("id", "name", true, listOf(), Game.LOL, "owner", false)
                val event = Event("root", "id", eventData)

                val events = (1L..10L).map { EventWithVersion(it, event) }

                val eventStore = EventStoreInMemory().withState(events)

                val initialSubscriptionStateTime = OffsetDateTime.now()
                val subscriptionState = SubscriptionState(
                    SubscriptionStatus.WAITING,
                    version = 0,
                    time = initialSubscriptionStateTime
                )

                val subscriptionsRepository = SubscriptionsInMemoryRepository().withState(
                    mapOf(
                        "testSubscription" to subscriptionState
                    )
                )

                val subscription = EventSubscription(
                    subscriptionName = "testSubscription",
                    eventStore = eventStore,
                    subscriptionsRepository = subscriptionsRepository,
                    retryConfig = retryConfig,
                    process = { Either.Left(NotFound("Simulated error")) }
                )

                subscription.processPendingEvents()

                val finalSubscriptionState = subscriptionsRepository.getState("testSubscription")

                assertEquals(SubscriptionStatus.FAILED, finalSubscriptionState?.status)
                assertEquals(0, finalSubscriptionState?.version)
                assertTrue(initialSubscriptionStateTime.isBefore(finalSubscriptionState?.time))
            }
        }

        @Test
        fun `processPendingEvents sets state to FAILED on processing error and stops processing further events when some events were processed`() {
            runBlocking {
                val eventData = ViewToBeCreatedEvent("id", "name", true, listOf(), Game.LOL, "owner", false)
                val event = Event("root", "id", eventData)

                val events = (1L..10L).map { EventWithVersion(it, event) }

                val eventStore = EventStoreInMemory().withState(events)

                val initialSubscriptionStateTime = OffsetDateTime.now()
                val subscriptionState = SubscriptionState(
                    SubscriptionStatus.WAITING,
                    version = 0,
                    time = initialSubscriptionStateTime
                )

                val subscriptionsRepository = SubscriptionsInMemoryRepository().withState(
                    mapOf(
                        "testSubscription" to subscriptionState
                    )
                )

                val subscription = EventSubscription(
                    subscriptionName = "testSubscription",
                    eventStore = eventStore,
                    subscriptionsRepository = subscriptionsRepository,
                    retryConfig = retryConfig,
                    process = {
                        if (it.version <= 5) Either.Right(Unit)
                        else Either.Left(NotFound("Simulated error"))
                    }
                )

                subscription.processPendingEvents()

                val finalSubscriptionState = subscriptionsRepository.getState("testSubscription")

                assertEquals(SubscriptionStatus.FAILED, finalSubscriptionState?.status)
                assertEquals(5, finalSubscriptionState?.version)
                assertTrue(initialSubscriptionStateTime.isBefore(finalSubscriptionState?.time))
            }
        }

        @Test
        fun `processPendingEvents retries to process the events even when in FAILED state`() {
            runBlocking {
                val eventData = ViewToBeCreatedEvent("id", "name", true, listOf(), Game.LOL, "owner", false)
                val event = Event("root", "id", eventData)

                val events = (1L..10L).map { EventWithVersion(it, event) }

                val eventStore = EventStoreInMemory().withState(events)

                val initialSubscriptionStateTime = OffsetDateTime.now()
                val subscriptionState = SubscriptionState(
                    SubscriptionStatus.FAILED,
                    version = 2,
                    time = initialSubscriptionStateTime
                )

                val subscriptionsRepository = SubscriptionsInMemoryRepository().withState(
                    mapOf(
                        "testSubscription" to subscriptionState
                    )
                )

                val subscription = EventSubscription(
                    subscriptionName = "testSubscription",
                    eventStore = eventStore,
                    subscriptionsRepository = subscriptionsRepository,
                    retryConfig = retryConfig,
                    process = { Either.Right(Unit) }
                )

                subscription.processPendingEvents()

                val finalSubscriptionState = subscriptionsRepository.getState("testSubscription")

                assertEquals(SubscriptionStatus.WAITING, finalSubscriptionState?.status)
                assertEquals(10, finalSubscriptionState?.version)
                assertTrue(initialSubscriptionStateTime.isBefore(finalSubscriptionState?.time))
            }
        }
    }

    @Nested
    inner class BehaviorOfViewsProcessor {
        private val aggregateRoot = "/credentials/owner"

        @Test
        fun `viewsProcessor calls createView on VIEW_TO_BE_CREATED event, creates a view and stores an event`() {
            runBlocking {
                val (eventStore, viewsService, viewsRepository) = createService(
                    listOf(),
                    CharactersTestHelper.emptyCharactersState,
                    listOf(),
                    CredentialsTestHelper.emptyCredentialsInitialState
                )

                val spiedService = spyk(viewsService)

                val eventData =
                    ViewToBeCreatedEvent(ViewsTestHelper.id, "name", true, listOf(), Game.LOL, "owner", false)
                val eventWithVersion = EventWithVersion(
                    1L,
                    Event(aggregateRoot, ViewsTestHelper.id, eventData)
                )

                EventSubscription.viewsProcessor(eventWithVersion, spiedService)
                    .onLeft { fail("Expected success") }
                    .onRight {
                        coVerify {
                            spiedService.createView(
                                eq(ViewsTestHelper.id),
                                eq(aggregateRoot),
                                eq(eventData)
                            )
                        }
                    }


                assertEventStoredCorrectly(
                    eventStore,
                    ViewCreatedEvent(
                        ViewsTestHelper.id,
                        ViewsTestHelper.name,
                        ViewsTestHelper.owner, listOf(), true, Game.LOL, false
                    )
                )

                assertView(viewsRepository, ViewsTestHelper.name)
            }
        }

        @Test
        fun `viewsProcessor calls edit view on VIEW_TO_BE_EDITED event, edits a view and stores an event`() {
            runBlocking {
                val (eventStore, viewsService, viewsRepository) = createService(
                    listOf(ViewsTestHelper.basicSimpleLolView),
                    CharactersTestHelper.emptyCharactersState,
                    listOf(),
                    CredentialsTestHelper.emptyCredentialsInitialState
                )

                val spiedService = spyk(viewsService)

                val newName = "new-name"
                val eventData = ViewToBeEditedEvent(ViewsTestHelper.id, newName, true, listOf(), Game.LOL, false)
                val eventWithVersion = EventWithVersion(
                    1L,
                    Event(aggregateRoot, ViewsTestHelper.id, eventData)
                )

                EventSubscription.viewsProcessor(eventWithVersion, spiedService)
                    .onLeft { fail("Expected success") }
                    .onRight {
                        coVerify {
                            spiedService.editView(
                                eq(ViewsTestHelper.id),
                                eq(aggregateRoot),
                                eq(eventData)
                            )
                        }
                    }

                assertEventStoredCorrectly(
                    eventStore,
                    ViewEditedEvent(ViewsTestHelper.id, newName, listOf(), true, Game.LOL, false)
                )

                assertView(viewsRepository, newName)
            }
        }

        @Test
        fun `viewsProcessor calls patch view on VIEW_TO_BE_PATCHED event, patches a view and stores an event`() {
            runBlocking {
                val (eventStore, viewsService, viewsRepository) = createService(
                    listOf(ViewsTestHelper.basicSimpleLolView),
                    CharactersTestHelper.emptyCharactersState,
                    listOf(),
                    CredentialsTestHelper.emptyCredentialsInitialState,
                )

                val spiedService = spyk(viewsService)
                val newName = "newName"
                val eventData = ViewToBePatchedEvent(ViewsTestHelper.id, newName, null, null, Game.LOL, false)
                val eventWithVersion = EventWithVersion(
                    1L,
                    Event(aggregateRoot, ViewsTestHelper.id, eventData)
                )

                EventSubscription.viewsProcessor(eventWithVersion, spiedService)
                    .onLeft { fail("Expected success") }
                    .onRight {
                        coVerify {
                            spiedService.patchView(
                                eq(ViewsTestHelper.id),
                                eq(aggregateRoot),
                                eq(eventData)
                            )
                        }
                    }


                assertEventStoredCorrectly(
                    eventStore,
                    ViewPatchedEvent(ViewsTestHelper.id, newName, null, null, Game.LOL, false)
                )

                assertView(viewsRepository, newName)
            }
        }

        private suspend fun createService(
            viewsState: List<SimpleView>,
            charactersState: CharactersState,
            dataCacheState: List<DataCache>,
            credentialState: CredentialsRepositoryState,
        ): Triple<EventStore, ViewsService, ViewsRepository> {
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

            return Triple(eventStore, service, viewsRepository)
        }

        private suspend fun assertEventStoredCorrectly(eventStore: EventStore, eventData: EventData) {
            val events = eventStore.getEvents(null).toList()

            val expectedEvent = Event(
                aggregateRoot,
                ViewsTestHelper.id,
                eventData
            )

            assertEquals(1, events.size)
            assertEquals(EventWithVersion(1, expectedEvent), events.first())
        }

        private suspend fun assertView(viewsRepository: ViewsRepository, name: String) {
            assertEquals(1, viewsRepository.state().size)
            val insertedView = viewsRepository.state().first()
            assertEquals(ViewsTestHelper.id, insertedView.id)
            assertEquals(name, insertedView.name)
            assertEquals(ViewsTestHelper.owner, insertedView.owner)
            assertEquals(listOf(), insertedView.characterIds)
            assertTrue(insertedView.published)
        }
    }

}