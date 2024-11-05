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
import com.kos.eventsourcing.events.repository.EventStore
import com.kos.httpclients.domain.Data
import com.kos.httpclients.domain.RaiderIoData
import com.kos.httpclients.raiderio.RaiderIoClient
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
    suspend fun getViews(): List<SimpleView> = viewsRepository.getViews()
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
            val operationId = UUID.randomUUID().toString()
            val aggregateRoot = "/credentials/$owner"
            val event = Event(
                aggregateRoot,
                operationId,
                ViewToBeCreated(
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

    suspend fun createView(operationId: String, aggregateRoot: String, viewToBeCreated: ViewToBeCreated): Either<InsertCharacterError, Operation> {
        return either {
            val characterIds =
                charactersService.createAndReturnIds(viewToBeCreated.characters, viewToBeCreated.game).bind()
            val view = viewsRepository.create(
                viewToBeCreated.id,
                viewToBeCreated.name,
                viewToBeCreated.owner,
                characterIds,
                viewToBeCreated.game
            )
            val event = Event(
                aggregateRoot,
                operationId,
                ViewCreated.fromSimpleView(view)
            )
            eventStore.save(event)
        }
    }

    suspend fun edit(owner: String, id: String, request: ViewRequest): Either<ControllerError, Operation> {
        val operationId = UUID.randomUUID().toString()
        val aggregateRoot = "/credentials/$owner"
        val event = Event(
            aggregateRoot,
            operationId,
            ViewToBeEdited(
                id,
                request.name,
                request.published,
                request.characters,
                request.game
            )
        )
        return Either.Right(eventStore.save(event))
    }

    suspend fun editView(viewToBeEdited: ViewToBeEdited): Either<ControllerError, ViewModified> {
        val characters = charactersService.createAndReturnIds(viewToBeEdited.characters, viewToBeEdited.game)
        return characters.map {
            viewsRepository.edit(
                viewToBeEdited.id,
                viewToBeEdited.name,
                viewToBeEdited.published,
                it
            )
        }
    }

    suspend fun patch(owner: String, id: String, request: ViewPatchRequest): Either<ControllerError, Operation> {
        val operationId = UUID.randomUUID().toString()
        val aggregateRoot = "/credentials/$owner"
        val event = Event(
            aggregateRoot,
            id,
            ViewToBePatched(
                operationId,
                request.name,
                request.published,
                request.characters,
                request.game
            )
        )

        return Either.Right(eventStore.save(event))
    }

    suspend fun patchView(viewToBePatched: ViewToBePatched): Either<InsertCharacterError, ViewPatched> {
        return when (val characters: Either<InsertCharacterError, List<Long>>? =
            viewToBePatched.characters.fold({ null },
                { charactersRequest ->
                    charactersService.createAndReturnIds(
                        charactersRequest,
                        viewToBePatched.game
                    )
                })) {
            null -> Either.Right(
                viewsRepository.patch(
                    viewToBePatched.id,
                    viewToBePatched.name,
                    viewToBePatched.published,
                    null
                )
            )

            else -> characters.map {
                viewsRepository.patch(
                    viewToBePatched.id,
                    viewToBePatched.name,
                    viewToBePatched.published,
                    it
                )
            }
        }
    }

    suspend fun delete(id: String): ViewDeleted {
        return viewsRepository.delete(id)
    }

    suspend fun getData(view: View): Either<HttpError, List<Data>> {
        return when (view.game) {
            Game.WOW -> getWowData(view)
            Game.LOL -> getLolData(view)
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
        /*
        TODO: This will be used in the future again. We will make usage of every call to raiderio to avoid
        TODO: needing to retrieve data for every character again if it was called in a 1h period. This will lighten
        TODO: the scheduled task.
        eitherJsonErrorOrData.onRight {
            it.forEach { data ->
                dataCacheService.insert(
                    DataCache(
                        data.id,
                        json.encodeToString(data),
                        OffsetDateTime.now()
                    )
                )
            }
        }
        */
        eitherJsonErrorOrData
    }

    suspend fun getCachedData(simpleView: SimpleView) =
        dataCacheService.getData(simpleView.characterIds, oldFirst = true)

    private suspend fun getMaxNumberOfViewsByRole(owner: String): Either<UserWithoutRoles, Int> =
        when (val maxNumberOfViews = credentialsService.getUserRoles(owner).maxOfOrNull { it.maxNumberOfViews }) {
            null -> Either.Left(UserWithoutRoles)
            else -> Either.Right(maxNumberOfViews)
        }
}
