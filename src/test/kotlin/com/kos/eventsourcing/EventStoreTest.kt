package com.kos.eventsourcing

import com.kos.eventsourcing.repository.EventStore
import com.kos.eventsourcing.repository.InMemoryEventStore
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

data class TestData(val int: Int, val string: String)

data class TestDataEvent(override val eventData: TestData) : Event<TestData> {
    override val aggregateRoot: String = "TestAggregate"
    override val eventType: String = "TestDataEvent"
}

abstract class EventStoreTest {

    abstract val store: EventStore

    @BeforeTest
    abstract fun beforeEach()

    @Test
    fun `given an empty store i can save events`() {
        runBlocking {
            val event1 = TestDataEvent(TestData(1, "Data"))
            val event2 = TestDataEvent(TestData(2, "Data"))
            val savedEvent1 = store.save(event1)
            val savedEvent2 = store.save(event2)
            val expected = listOf(savedEvent1, savedEvent2)

            assertEquals(expected, store.state())
        }
    }
}

class InMemoryEventStoreTest : EventStoreTest() {
    override val store = InMemoryEventStore()
    override fun beforeEach() {
        store.clear()
    }
}