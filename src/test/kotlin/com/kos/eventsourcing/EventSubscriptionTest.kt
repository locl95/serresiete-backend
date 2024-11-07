package com.kos.eventsourcing


import arrow.core.Either
import com.kos.assertTrue
import com.kos.common.ControllerError
import com.kos.common.NotFound
import com.kos.common.RetryConfig
import com.kos.eventsourcing.events.Event
import com.kos.eventsourcing.events.EventWithVersion
import com.kos.eventsourcing.events.ViewToBeCreatedEvent
import com.kos.eventsourcing.events.repository.EventStoreInMemory
import com.kos.eventsourcing.subscriptions.EventSubscription
import com.kos.eventsourcing.subscriptions.SubscriptionState
import com.kos.eventsourcing.subscriptions.SubscriptionStatus
import com.kos.eventsourcing.subscriptions.repository.SubscriptionsInMemoryRepository
import com.kos.views.Game
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.assertThrows
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class EventSubscriptionTest {
    private val retryConfig = RetryConfig(1, 1)

    @Test
    fun `processPendingEvents throws exception if subscription is not found`(): Unit = runBlocking {
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

    @Test
    fun `processPendingEvents updates state to WAITING on successful processing`() = runBlocking {
        val eventData = ViewToBeCreatedEvent("id", "name", true, listOf(), Game.LOL, "owner")
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

    @Test
    fun `processPendingEvents sets state to FAILED on processing error`() = runBlocking {
        // Arrange
        val eventData = ViewToBeCreatedEvent("id", "name", true, listOf(), Game.LOL, "owner")
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