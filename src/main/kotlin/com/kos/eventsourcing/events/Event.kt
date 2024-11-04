package com.kos.eventsourcing.events

import com.kos.views.ViewRequest
import kotlinx.serialization.Serializable

enum class EventType {
    VIEW_TO_BE_CREATED {
        override fun toString(): String = "viewToBeCreated"
    };

    companion object {
        fun fromString(string: String): EventType {
            return when (string) {
                "viewToBeCreated" -> VIEW_TO_BE_CREATED
                else -> throw IllegalArgumentException("error parsing EventType: $string")
            }
        }
    }
}

sealed interface EventData {
    val eventType: EventType
}

@Serializable
data class ViewToBeCreated(val viewRequest: ViewRequest, val id: String) : EventData {
    override val eventType: EventType = EventType.VIEW_TO_BE_CREATED
}

@Serializable
data class Event(
    val aggregateRoot: String,
    val eventData: EventData
)

data class EventWithVersion(val version: Long, val event: Event)