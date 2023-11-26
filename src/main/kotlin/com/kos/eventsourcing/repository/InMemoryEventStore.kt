package com.kos.eventsourcing.repository

import com.kos.common.InMemoryRepository
import com.kos.common.WithState
import com.kos.eventsourcing.Event
import com.kos.eventsourcing.EventWithVersion

class InMemoryEventStore : EventStore, InMemoryRepository {
    private val events = mutableListOf<EventWithVersion<*>>()
    private var currentVersion = 1L // Assuming versions start from 1 and increment

    override suspend fun <T> save(event: Event<T>): EventWithVersion<T> {
        val eventWithVersion = EventWithVersion(currentVersion++, event)
        events.add(eventWithVersion)
        return eventWithVersion
    }

    override suspend fun state(): List<EventWithVersion<*>> {
        return events
    }

    override suspend fun withState(initialState: List<EventWithVersion<*>>): EventStore {
        currentVersion = initialState.map { it.version }.maxByOrNull { it } ?: 1
        events.addAll(initialState)
        return this
    }

    override fun clear() {
        events.clear()
    }
}