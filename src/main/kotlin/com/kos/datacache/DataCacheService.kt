package com.kos.datacache

import arrow.core.Either
import arrow.core.sequence
import com.kos.characters.Character
import com.kos.common.HttpError
import com.kos.common.JsonParseError
import com.kos.common.WithLogger
import com.kos.common.split
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

    suspend fun cache(characters: List<Character>): List<HttpError> = coroutineScope {
        when (val cutoffOrError = raiderIoClient.cutoff()) {
            is Either.Left -> {
                logger.error(cutoffOrError.value.error())
                listOf(cutoffOrError.value)
            }
            is Either.Right -> {
                val errorsAndData = characters.map { async { raiderIoClient.get(it).map { r -> Pair(it.id, r) } } }
                    .awaitAll()
                    .split()
                errorsAndData.first.forEach { logger.error(it.error()) }
                val data = errorsAndData.second.map {
                    DataCache(
                        it.first, json.encodeToString(
                            it.second.profile.toRaiderIoData(
                                it.first,
                                BigDecimal(it.second.profile.mythicPlusRanks.overall.region.toDouble() / cutoffOrError.value.totalPopulation * 100).setScale(
                                    2,
                                    RoundingMode.HALF_EVEN
                                ).toDouble(),
                                it.second.specs
                            )
                        ), OffsetDateTime.now()
                    )
                }
                dataCacheRepository.insert(data)
                data.forEach { logger.info("Cached character ${it.characterId}") }
                errorsAndData.first
            }
        }
    }



    suspend fun clear(): Int = dataCacheRepository.deleteExpiredRecord(ttl)
}