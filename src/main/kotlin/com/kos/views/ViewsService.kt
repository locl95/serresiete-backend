package com.kos.views

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.sequence
import arrow.core.traverse
import com.kos.characters.CharactersService
import com.kos.characters.WowCharacter
import com.kos.common.*
import com.kos.credentials.CredentialsService
import com.kos.datacache.DataCacheService
import com.kos.eventsourcing.events.*
import com.kos.eventsourcing.events.ViewPatchedEvent
import com.kos.eventsourcing.events.repository.EventStore
import com.kos.httpclients.domain.Data
import com.kos.httpclients.domain.RaiderIoData
import com.kos.httpclients.raiderio.RaiderIoClient
import com.kos.views.Game.LOL
import com.kos.views.Game.WOW
import com.kos.views.repository.ViewsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

class ViewsService(
    private val viewsRepository: ViewsRepository,
    private val charactersService: CharactersService,
    private val dataCacheService: DataCacheService,
    private val raiderIoClient: RaiderIoClient,
    private val credentialsService: CredentialsService,
    private val eventStore: EventStore
) {

    suspend fun getOwnViews(owner: String): List<SimpleView> = viewsRepository.getOwnViews(owner)
    suspend fun getViews(game: Game?): List<SimpleView> = viewsRepository.getViews(game)
    suspend fun get(id: String): View? {
        return when (val simpleView = viewsRepository.get(id)) {
            null -> null
            else -> {
                View(
                    simpleView.id,
                    simpleView.name,
                    simpleView.owner,
                    simpleView.published,
                    simpleView.characterIds.mapNotNull {
                        charactersService.get(it, simpleView.game)
                    },
                    simpleView.game
                )
            }
        }
    }

    suspend fun getSimple(id: String): SimpleView? = viewsRepository.get(id)

    suspend fun create(owner: String, request: ViewRequest): Either<ControllerError, Operation> {
        return either {
            val ownerMaxViews = getMaxNumberOfViewsByRole(owner).bind()
            ensure(viewsRepository.getOwnViews(owner).size < ownerMaxViews) { TooMuchViews }
            val ownerMaxCharacters = getMaxNumberOfCharactersByRole(owner).bind()
            ensure(request.characters.size <= ownerMaxCharacters) { TooMuchCharacters }
            val operationId = UUID.randomUUID().toString()
            val aggregateRoot = "/credentials/$owner"
            val event = Event(
                aggregateRoot,
                operationId,
                ViewToBeCreatedEvent(
                    operationId,
                    request.name,
                    request.published,
                    request.characters,
                    request.game,
                    owner
                )
            )
            eventStore.save(event)
        }
    }

    suspend fun createView(
        operationId: String,
        aggregateRoot: String,
        viewToBeCreatedEvent: ViewToBeCreatedEvent
    ): Either<InsertError, Operation> {
        return either {
            val characterIds =
                charactersService.createAndReturnIds(viewToBeCreatedEvent.characters, viewToBeCreatedEvent.game).bind()
            val view = viewsRepository.create(
                viewToBeCreatedEvent.id,
                viewToBeCreatedEvent.name,
                viewToBeCreatedEvent.owner,
                characterIds,
                viewToBeCreatedEvent.game
            )
            val event = Event(
                aggregateRoot,
                operationId,
                ViewCreatedEvent.fromSimpleView(view)
            )
            eventStore.save(event)
        }
    }

    suspend fun edit(client: String, id: String, request: ViewRequest): Either<ControllerError, Operation> {
        return either {
            val ownerMaxCharacters = getMaxNumberOfCharactersByRole(client).bind()
            ensure(request.characters.size <= ownerMaxCharacters) { TooMuchCharacters }
            val aggregateRoot = "/credentials/$client"
            val event = Event(
                aggregateRoot,
                id,
                ViewToBeEditedEvent(
                    id,
                    request.name,
                    request.published,
                    request.characters,
                    request.game
                )
            )
            eventStore.save(event)
        }
    }

    suspend fun editView(
        operationId: String,
        aggregateRoot: String,
        viewToBeEditedEvent: ViewToBeEditedEvent
    ): Either<ControllerError, Operation> {
        return either {
            val characters =
                charactersService.createAndReturnIds(viewToBeEditedEvent.characters, viewToBeEditedEvent.game).bind()
            val viewModified =
                viewsRepository.edit(
                    viewToBeEditedEvent.id,
                    viewToBeEditedEvent.name,
                    viewToBeEditedEvent.published,
                    characters
                )
            val event = Event(
                aggregateRoot,
                operationId,
                ViewEditedEvent.fromViewModified(operationId, viewToBeEditedEvent.game, viewModified)
            )
            eventStore.save(event)
        }

    }

    suspend fun patch(client: String, id: String, request: ViewPatchRequest): Either<ControllerError, Operation> {
        return either {
            val ownerMaxCharacters = getMaxNumberOfCharactersByRole(client).bind()
            request.characters?.let { charactersToInsert ->
                ensure(charactersToInsert.size <= ownerMaxCharacters) { TooMuchCharacters }
            }
            val aggregateRoot = "/credentials/$client"
            val event = Event(
                aggregateRoot,
                id,
                ViewToBePatchedEvent(
                    id,
                    request.name,
                    request.published,
                    request.characters,
                    request.game
                )
            )

            eventStore.save(event)
        }
    }

    suspend fun patchView(
        operationId: String,
        aggregateRoot: String,
        viewToBePatchedEvent: ViewToBePatchedEvent
    ): Either<InsertError, Operation> {
        return either {
            val charactersToInsert = viewToBePatchedEvent.characters?.let { charactersToInsert ->
                charactersService.createAndReturnIds(charactersToInsert, viewToBePatchedEvent.game).bind()
            }
            val patchedView = viewsRepository.patch(
                viewToBePatchedEvent.id,
                viewToBePatchedEvent.name,
                viewToBePatchedEvent.published,
                charactersToInsert
            )
            val event = Event(
                aggregateRoot,
                operationId,
                ViewPatchedEvent.fromViewPatched(operationId,viewToBePatchedEvent.game, patchedView)
            )
            eventStore.save(event)
        }
    }

    suspend fun delete(id: String): ViewDeleted {
        return viewsRepository.delete(id)
    }

    suspend fun getData(view: View): Either<HttpError, List<Data>> {
        return when (view.game) {
            WOW -> getWowData(view)
            LOL -> getLolData(view)
        }
    }


    private suspend fun getLolData(view: View): Either<HttpError, List<Data>> {
        return dataCacheService.getData(view.characters.map { it.id }, oldFirst = false)
    }

    private suspend fun getWowData(view: View): Either<HttpError, List<RaiderIoData>> = coroutineScope {
        val eitherJsonErrorOrData = when (val cutoffOrError = raiderIoClient.cutoff()) {
            is Either.Left -> Either.Left(cutoffOrError.value)
            is Either.Right -> {
                val eitherErrorOrResponse = view.characters.map { char ->
                    async {
                        raiderIoClient.get(char as WowCharacter).map {//TODO: Fix
                            Pair(char.id, it)
                        }
                    }
                }.awaitAll().sequence()
                when (eitherErrorOrResponse) {
                    is Either.Left -> eitherErrorOrResponse
                    is Either.Right -> {
                        eitherErrorOrResponse.value.traverse {
                            val quantile =
                                BigDecimal(it.second.profile.mythicPlusRanks.overall.region.toDouble() / cutoffOrError.value.totalPopulation * 100).setScale(
                                    2,
                                    RoundingMode.HALF_EVEN
                                )
                            Either.Right(
                                it.second.profile.toRaiderIoData(
                                    it.first,
                                    quantile.toDouble(),
                                    it.second.specs
                                )
                            )
                        }
                    }
                }
            }
        }
        eitherJsonErrorOrData
    }

    suspend fun getCachedData(simpleView: SimpleView) =
        dataCacheService.getData(simpleView.characterIds, oldFirst = true)

    private suspend fun getMaxNumberOfViewsByRole(owner: String): Either<UserWithoutRoles, Int> =
        when (val maxNumberOfViews = credentialsService.getUserRoles(owner).maxOfOrNull { it.maxNumberOfViews }) {
            null -> Either.Left(UserWithoutRoles)
            else -> Either.Right(maxNumberOfViews)
        }

    private suspend fun getMaxNumberOfCharactersByRole(owner: String): Either<UserWithoutRoles, Int> =
        when (val maxNumberOfCharacters =
            credentialsService.getUserRoles(owner).maxOfOrNull { it.maxNumberOfCharacters }) {
            null -> Either.Left(UserWithoutRoles)
            else -> Either.Right(maxNumberOfCharacters)
        }
}
