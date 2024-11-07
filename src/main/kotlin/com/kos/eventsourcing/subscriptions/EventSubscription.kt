package com.kos.eventsourcing.subscriptions

import arrow.core.Either
import arrow.core.raise.either
import com.kos.characters.CharactersService
import com.kos.characters.LolCharacter
import com.kos.common.ControllerError
import com.kos.common.OffsetDateTimeSerializer
import com.kos.common.Retry.retryEitherWithExponentialBackoff
import com.kos.common.RetryConfig
import com.kos.common.WithLogger
import com.kos.eventsourcing.events.*
import com.kos.eventsourcing.events.repository.EventStore
import com.kos.eventsourcing.subscriptions.repository.SubscriptionsRepository
import com.kos.views.Game
import com.kos.views.ViewsService
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

enum class SubscriptionStatus {
    WAITING,
    RUNNING,
    FAILED
}


@Serializable
data class SubscriptionState(
    val status: SubscriptionStatus,
    val version: Long,
    @Serializable(with = OffsetDateTimeSerializer::class)
    val time: OffsetDateTime,
    val lastError: String? = null
)

class EventSubscription(
    private val subscriptionName: String,
    private val eventStore: EventStore,
    private val subscriptionsRepository: SubscriptionsRepository,
    private val retryConfig: RetryConfig,
    private val process: suspend (EventWithVersion) -> Either<ControllerError, Unit>,
) : WithLogger("event-subscription-$subscriptionName") {

    init {
        logger.info("started subscription")
    }

    suspend fun processPendingEvents(): Unit {
        val initialState: SubscriptionState =
            subscriptionsRepository.getState(subscriptionName)
                ?: throw Exception("Not found subscription $subscriptionName")
        val hasSucceededWithVersion =
            eventStore.getEvents(initialState.version).fold(Pair(false, initialState.version)) { _, event ->
                try {
                    retryEitherWithExponentialBackoff(retryConfig) { process(event) }
                        .onLeft { throw Exception(it.toString()) }
                    subscriptionsRepository.setState(
                        subscriptionName,
                        SubscriptionState(SubscriptionStatus.RUNNING, event.version, OffsetDateTime.now())
                    )
                    Pair(true, event.version)
                } catch (e: Exception) {
                    subscriptionsRepository.setState(
                        subscriptionName,
                        SubscriptionState(SubscriptionStatus.FAILED, event.version - 1, OffsetDateTime.now(), e.message)
                    )
                    logger.error("processing event ${event.version} has failed because ${e.message}")
                    Pair(false, event.version)
                }
            }
        if (hasSucceededWithVersion.first) {
            subscriptionsRepository.setState(
                subscriptionName,
                SubscriptionState(SubscriptionStatus.WAITING, hasSucceededWithVersion.second, OffsetDateTime.now())
            )
        }
    }

    companion object {
        suspend fun viewsProcessor(
            eventWithVersion: EventWithVersion,
            viewsService: ViewsService,
        ): Either<ControllerError, Unit> {
            return when (eventWithVersion.event.eventData.eventType) {
                EventType.VIEW_TO_BE_CREATED -> {
                    either {
                        val payload = eventWithVersion.event.eventData as ViewToBeCreatedEvent
                        val aggregateRoot = eventWithVersion.event.aggregateRoot
                        val operationId = eventWithVersion.event.operationId
                        viewsService.createView(operationId, aggregateRoot, payload).bind()
                    }
                }

                EventType.VIEW_TO_BE_EDITED -> {
                    either {
                        val payload = eventWithVersion.event.eventData as ViewToBeEditedEvent
                        val aggregateRoot = eventWithVersion.event.aggregateRoot
                        val operationId = eventWithVersion.event.operationId
                        viewsService.editView(operationId, aggregateRoot, payload).bind()
                    }
                }

                EventType.VIEW_TO_BE_PATCHED -> {
                    either {
                        val payload = eventWithVersion.event.eventData as ViewToBePatchedEvent
                        val aggregateRoot = eventWithVersion.event.aggregateRoot
                        val operationId = eventWithVersion.event.operationId
                        viewsService.patchView(operationId, aggregateRoot, payload).bind()
                    }
                }

                else -> Either.Right(Unit)
            }
        }

        @Suppress("UNCHECKED_CAST")
        suspend fun syncLolCharactersProcessor(
            eventWithVersion: EventWithVersion,
            charactersService: CharactersService
        ): Either<ControllerError, Unit> {
            return when (eventWithVersion.event.eventData.eventType) {
                EventType.VIEW_CREATED -> {
                    val payload = eventWithVersion.event.eventData as ViewCreatedEvent
                    return when(payload.game) {
                        Game.LOL -> {
                            val viewCharacters = payload.characters.mapNotNull { charactersService.get(it, Game.LOL) } as List<LolCharacter>
                            charactersService.updateLolCharacters(viewCharacters)
                            Either.Right(Unit)
                        }
                        Game.WOW -> {
                            Either.Right(Unit)
                        }
                    }
                }

                EventType.VIEW_EDITED -> {
                    val payload = eventWithVersion.event.eventData as ViewEditedEvent
                    return when(payload.game) {
                        Game.LOL -> {
                            val viewCharacters = payload.characters.mapNotNull { charactersService.get(it, Game.LOL) } as List<LolCharacter>
                            charactersService.updateLolCharacters(viewCharacters)
                            Either.Right(Unit)
                        }
                        Game.WOW -> {
                            Either.Right(Unit)
                        }
                    }
                }

                EventType.VIEW_PATCHED -> {
                    val payload = eventWithVersion.event.eventData as ViewPatchedEvent
                    return when(payload.game) {
                        Game.LOL -> {
                            payload.characters?.mapNotNull { charactersService.get(it, Game.LOL) }?.let {
                                it as List<LolCharacter>
                                charactersService.updateLolCharacters(it)
                            }
                            Either.Right(Unit)
                        }
                        Game.WOW -> {
                            Either.Right(Unit)
                        }
                    }
                }
                else -> Either.Right(Unit)
            }
        }
    }
}