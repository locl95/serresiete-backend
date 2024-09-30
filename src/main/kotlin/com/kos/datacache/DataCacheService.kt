package com.kos.datacache

import arrow.core.Either
import arrow.core.sequence
import com.kos.characters.WowCharacter
import com.kos.characters.Character
import com.kos.characters.LolCharacter
import com.kos.common.JsonParseError
import com.kos.common.WithLogger
import com.kos.common.split
import com.kos.datacache.repository.DataCacheRepository
import com.kos.raiderio.RaiderIoClient
import com.kos.raiderio.RaiderIoData
import com.kos.views.Game
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
        return when(simpleView.game) {
            Game.WOW -> getWowData(simpleView)
            Game.LOL -> getLolData(simpleView)
        }
    }

    suspend fun getLolData(simpleView: SimpleView): Either<JsonParseError, List<RaiderIoData>> = TODO()
    suspend fun getWowData(simpleView: SimpleView): Either<JsonParseError, List<RaiderIoData>> {
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

    @Suppress("UNCHECKED_CAST")
    suspend fun cache(characters: List<Character>, game: Game) {
        when(game) {
            Game.WOW -> cacheWowCharacters(characters as List<WowCharacter>)
            Game.LOL -> cacheLolCharacters(characters as List<LolCharacter>)
        }
    }

    private suspend fun cacheLolCharacters(lolCharacters: List<LolCharacter>) = logger.info("lol cache not implemented")

    private suspend fun cacheWowCharacters(wowCharacters: List<WowCharacter>) = coroutineScope {
        when (val cutoffOrError = raiderIoClient.cutoff()) {
            is Either.Left -> logger.error(cutoffOrError.value.error())
            is Either.Right -> {
                val errorsAndData = wowCharacters.map { async { raiderIoClient.get(it).map { r -> Pair(it.id, r) } } }
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
            }
        }
    }

    suspend fun clear(): Int = dataCacheRepository.deleteExpiredRecord(ttl)
}