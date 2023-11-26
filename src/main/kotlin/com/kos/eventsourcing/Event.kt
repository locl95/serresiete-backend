package com.kos.eventsourcing

interface Event<T> {
    val aggregateRoot: String
    val eventType: String
    val eventData: T
}

data class EventWithVersion<T>(val version: Long, val event: Event<T>)