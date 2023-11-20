package com.kos.datacache

import arrow.core.Either
import arrow.core.sequence
import com.kos.characters.Character
import com.kos.common.JsonParseError
import com.kos.common.WithLogger
import com.kos.datacache.repository.DataCacheRepository
import com.kos.raiderio.RaiderIoClient
import com.kos.raiderio.RaiderIoData
import com.kos.views.SimpleView
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.OffsetDateTime

data class DataCacheService(
    private val dataCacheRepository: DataCacheRepository,
    private val raiderIoClient: RaiderIoClient
) : WithLogger("DataCacheService") {

    private val ttl: Long = 24
    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun insert(dataCache: DataCache): Boolean = dataCacheRepository.insert(dataCache)
    suspend fun get(characterId: Long) = dataCacheRepository.get(characterId)
    suspend fun getData(simpleView: SimpleView): Either<JsonParseError, List<RaiderIoData>> {
        return simpleView.characterIds.mapNotNull {
            when (val data = get(it).minByOrNull { dc -> dc.inserted }) {
                null -> null
                else -> {
                    try {
                        Either.Right(json.decodeFromString<RaiderIoData>(data.data))
                    } catch (se: SerializationException) {
                        Either.Left(JsonParseError(data.data, ""))
                    } catch (iae: IllegalArgumentException) {
                        Either.Left(JsonParseError(data.data, ""))
                    }
                }
            }
        }.sequence()
    }

    suspend fun cache(characters: List<Character>) = coroutineScope {
        when (val cutoffOrError = raiderIoClient.cutoff()) {
            is Either.Left -> logger.error(cutoffOrError.value.error())
            is Either.Right -> characters.map { async { raiderIoClient.get(it).map { r -> Pair(it.id, r) } } }
                .awaitAll()
                .forEach { eitherErrorOrData ->
                    when (eitherErrorOrData) {
                        is Either.Right -> {
                            val quantile =
                                BigDecimal(eitherErrorOrData.value.second.profile.mythicPlusRanks.overall.region.toDouble() / cutoffOrError.value.totalPopulation * 100).setScale(
                                    2,
                                    RoundingMode.HALF_EVEN
                                )
                            dataCacheRepository.insert(
                                DataCache(
                                    eitherErrorOrData.value.first,
                                    json.encodeToString(
                                        eitherErrorOrData.value.second.profile.toRaiderIoData(
                                            eitherErrorOrData.value.first,
                                            quantile.toDouble(),
                                            eitherErrorOrData.value.second.specs
                                        )
                                    ),
                                    OffsetDateTime.now()
                                )
                            )
                            logger.info("Cached character ${eitherErrorOrData.value.first} - ${eitherErrorOrData.value.second.profile.name}")
                        }

                        is Either.Left -> logger.error(eitherErrorOrData.value.error())
                    }
                }
        }
    }

    suspend fun clear(): Int = dataCacheRepository.deleteExpiredRecord(ttl)
}