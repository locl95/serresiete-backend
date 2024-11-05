package com.kos.eventsourcing.events

import com.kos.characters.CharacterCreateRequest
import com.kos.views.Game
import com.kos.views.SimpleView
import kotlinx.serialization.Serializable

enum class EventType {
    VIEW_TO_BE_CREATED {
        override fun toString(): String = "viewToBeCreated"
    },
    VIEW_TO_BE_EDITED {
        override fun toString(): String = "viewToBeEdited"
    },
    VIEW_TO_BE_PATCHED {
        override fun toString(): String = "viewToBePatched"
    },
    VIEW_CREATED {
        override fun toString(): String = "viewCreated"
    },
    VIEW_EDITED {
        override fun toString(): String = "viewEdited"
    },
    VIEW_PATCHED {
        override fun toString(): String = "viewPatched"
    };

    companion object {
        fun fromString(string: String): EventType {
            return when (string) {
                "viewToBeCreated" -> VIEW_TO_BE_CREATED
                "viewToBeEdited" -> VIEW_TO_BE_EDITED
                "viewToBePatched" -> VIEW_TO_BE_PATCHED
                "viewCreated" -> VIEW_CREATED
                "viewEdited" -> VIEW_EDITED
                "viewPatched" -> VIEW_PATCHED
                else -> throw IllegalArgumentException("error parsing EventType: $string")
            }
        }
    }
}

sealed interface EventData {
    val eventType: EventType
}
@Serializable
data class ViewToBeCreated(
    val id: String,
    val name: String,
    val published: Boolean,
    val characters: List<CharacterCreateRequest>,
    val game: Game,
    val owner: String
) : EventData {
    override val eventType: EventType = EventType.VIEW_TO_BE_CREATED
}

@Serializable
data class ViewToBeEdited(
    val id: String,
    val name: String,
    val published: Boolean,
    val characters: List<CharacterCreateRequest>,
    val game: Game
) : EventData {
    override val eventType: EventType = EventType.VIEW_TO_BE_EDITED
}

@Serializable
data class ViewToBePatched(
    val id: String,
    val name: String?,
    val published: Boolean?,
    val characters: List<CharacterCreateRequest>?,
    val game: Game
) : EventData {
    override val eventType: EventType = EventType.VIEW_TO_BE_PATCHED
}

@Serializable
data class ViewCreated(
    val id: String,
    val name: String,
    val owner: String,
    val characters: List<Long>,
    val published: Boolean,
    val game: Game
): EventData {
    override val eventType: EventType = EventType.VIEW_CREATED

    companion object {
        fun fromSimpleView(simpleView: SimpleView) = ViewCreated(
            simpleView.id,
            simpleView.name,
            simpleView.owner,
            simpleView.characterIds,
            simpleView.published,
            simpleView.game
        )
    }
}

data class ViewEdited(
    val id: String
): EventData {
    override val eventType: EventType = EventType.VIEW_EDITED
}

data class ViewPatched(
    val id: String
): EventData {
    override val eventType: EventType = EventType.VIEW_PATCHED
}

@Serializable
data class Event(
    val aggregateRoot: String,
    val operationId: String,
    val eventData: EventData
)

@Serializable
data class Operation(
    val id: String,
    val aggregateRoot: String,
    val type: EventType
)

data class EventWithVersion(val version: Long, val event: Event)