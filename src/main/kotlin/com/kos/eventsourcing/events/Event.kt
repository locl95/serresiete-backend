package com.kos.eventsourcing.events

import com.kos.characters.CharacterCreateRequest
import com.kos.views.Game
import com.kos.views.SimpleView
import com.kos.views.ViewModified
import com.kos.views.ViewPatched
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
data class ViewToBeCreatedEvent(
    val id: String,
    val name: String,
    val published: Boolean,
    val characters: List<CharacterCreateRequest>,
    val game: Game,
    val owner: String,
    val featured: Boolean
) : EventData {
    override val eventType: EventType = EventType.VIEW_TO_BE_CREATED
}

@Serializable
data class ViewToBeEditedEvent(
    val id: String,
    val name: String,
    val published: Boolean,
    val characters: List<CharacterCreateRequest>,
    val game: Game,
    val featured: Boolean
) : EventData {
    override val eventType: EventType = EventType.VIEW_TO_BE_EDITED
}

@Serializable
data class ViewToBePatchedEvent(
    val id: String,
    val name: String?,
    val published: Boolean?,
    val characters: List<CharacterCreateRequest>?,
    val game: Game,
    val featured: Boolean?
) : EventData {
    override val eventType: EventType = EventType.VIEW_TO_BE_PATCHED
}

@Serializable
data class ViewCreatedEvent(
    val id: String,
    val name: String,
    val owner: String,
    val characters: List<Long>,
    val published: Boolean,
    val game: Game,
    val featured: Boolean
) : EventData {
    override val eventType: EventType = EventType.VIEW_CREATED

    companion object {
        fun fromSimpleView(simpleView: SimpleView) = ViewCreatedEvent(
            simpleView.id,
            simpleView.name,
            simpleView.owner,
            simpleView.characterIds,
            simpleView.published,
            simpleView.game,
            simpleView.featured
        )
    }
}

@Serializable
data class ViewEditedEvent(
    val id: String,
    val name: String,
    val characters: List<Long>,
    val published: Boolean,
    val game: Game,
    val featured: Boolean
) : EventData {
    override val eventType: EventType = EventType.VIEW_EDITED

    companion object {
        fun fromViewModified(id: String, game: Game, viewModified: ViewModified) = ViewEditedEvent(
            id,
            viewModified.name,
            viewModified.characters,
            viewModified.published,
            game,
            viewModified.featured
        )
    }
}

@Serializable
data class ViewPatchedEvent(
    val id: String,
    val name: String?,
    val characters: List<Long>?,
    val published: Boolean?,
    val game: Game,
    val featured: Boolean?
) : EventData {
    override val eventType: EventType = EventType.VIEW_PATCHED

    companion object {
        fun fromViewPatched(id: String, game: Game, viewPatched: ViewPatched) = ViewPatchedEvent(
            id,
            viewPatched.name,
            viewPatched.characters,
            viewPatched.published,
            game,
            viewPatched.featured
        )
    }
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