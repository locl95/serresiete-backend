package com.kos.eventsourcing.repository

import com.kos.common.WithState
import com.kos.eventsourcing.Event
import com.kos.eventsourcing.EventWithVersion

interface EventStore : WithState<List<EventWithVersion<*>>, EventStore> {
    suspend fun <T> save(event: Event<T>): EventWithVersion<T>
}