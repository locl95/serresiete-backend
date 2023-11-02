package com.kos.views

import arrow.core.Either
import arrow.core.traverse
import com.kos.characters.CharacterRequest
import com.kos.characters.CharactersService
import com.kos.common.JsonParseError
import com.kos.datacache.DataCache
import com.kos.datacache.DataCacheService
import com.kos.raiderio.RaiderIoClient
import com.kos.raiderio.RaiderIoData
import com.kos.views.repository.ViewsRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.OffsetDateTime

class ViewsService(
    private val viewsRepository: ViewsRepository,
    private val charactersService: CharactersService,
    private val dataCacheService: DataCacheService,
    private val raiderIoClient: RaiderIoClient
) {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val maxNumberOfViews: Int = 2

    suspend fun getOwnViews(owner: String): List<SimpleView> = viewsRepository.getOwnViews(owner)
    suspend fun get(id: String): View? {
        return when (val simpleView = viewsRepository.get(id)) {
            null -> null
            else -> {
                View(simpleView.id, simpleView.owner, simpleView.characterIds.mapNotNull {
                    charactersService.get(it)
                })
            }
        }
    }

    suspend fun getSimple(id: String): SimpleView? = viewsRepository.get(id)

    suspend fun create(owner: String, characters: List<CharacterRequest>): Either<TooMuchViews, ViewSuccess> {
        if (viewsRepository.getOwnViews(owner).size >= maxNumberOfViews) return Either.Left(TooMuchViews())
        val characterIds = charactersService.createAndReturnIds(characters)
        return Either.Right(viewsRepository.create(owner, characterIds))
    }

    suspend fun edit(id: String, request: ViewRequest): Either<ViewNotFound, ViewSuccess> {
        val characters = charactersService.createAndReturnIds(request.characters)
        return when (viewsRepository.get(id)) {
            null -> Either.Left(ViewNotFound(id))
            else -> Either.Right(viewsRepository.edit(id, characters))
        }

    }

    suspend fun getData(view: View): Either<JsonParseError, List<RaiderIoData>> {
        val eitherJsonErrorOrData = when (val cutoffOrError = raiderIoClient.cutoff()) {
            is Either.Left -> Either.Left(cutoffOrError.value)
            is Either.Right -> {
                when (val eitherErrorOrResponse = view.characters.traverse { char ->
                    raiderIoClient.get(char).map {
                        Pair(char.id, it)
                    }
                }) {
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
        return eitherJsonErrorOrData
    }

    suspend fun getCachedData(simpleView: SimpleView) = dataCacheService.getData(simpleView)
}
