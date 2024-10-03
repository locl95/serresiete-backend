package com.kos.datacache

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.raise.either
import arrow.core.sequence
import com.kos.characters.Character
import com.kos.characters.LolCharacter
import com.kos.characters.WowCharacter
import com.kos.common.HttpError
import com.kos.common.JsonParseError
import com.kos.common.WithLogger
import com.kos.common.split
import com.kos.datacache.repository.DataCacheRepository
import com.kos.httpclients.HttpUtils.retryWithFixedDelay
import com.kos.httpclients.domain.*
import com.kos.httpclients.raiderio.RaiderIoClient
import com.kos.httpclients.riot.RiotClient
import com.kos.views.Game
import com.kos.views.SimpleView
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.OffsetDateTime

data class DataCacheService(
    private val dataCacheRepository: DataCacheRepository,
    private val raiderIoClient: RaiderIoClient,
    private val riotClient: RiotClient
) : WithLogger("DataCacheService") {

    private val ttl: Long = 24
    private val json = Json {
        serializersModule = SerializersModule {
            polymorphic(Data::class) {
                subclass(RaiderIoData::class, RaiderIoData.serializer())
                subclass(RiotData::class, RiotData.serializer())
            }
        }
        ignoreUnknownKeys = true
    }


    suspend fun get(characterId: Long) = dataCacheRepository.get(characterId)
    suspend fun getData(characterIds: List<Long>): Either<JsonParseError, List<Data>> {
        return characterIds.mapNotNull {
            when (val data = get(it).minByOrNull { dc -> dc.inserted }) {
                null -> null
                else -> {
                    try {
                        Either.Right(json.decodeFromString<Data>(data.data))
                    } catch (se: SerializationException) {
                        Either.Left(JsonParseError(data.data, "", se.stackTraceToString()))
                    } catch (iae: IllegalArgumentException) {
                        Either.Left(JsonParseError(data.data, "", iae.stackTraceToString()))
                    }
                }
            }
        }.sequence()
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun cache(characters: List<Character>, game: Game) {
        when (game) {
            Game.WOW -> cacheWowCharacters(characters as List<WowCharacter>)
            Game.LOL -> cacheLolCharacters(characters as List<LolCharacter>)
        }
    }

    private suspend fun cacheLolCharacters(lolCharacters: List<LolCharacter>) {
        coroutineScope {
            val errorsAndData: Pair<List<HttpError>, List<Pair<Long, RiotData>>> = lolCharacters.map {
                async {
                    val leagues: Either<HttpError, List<LeagueEntryResponse>> =
                        retryWithFixedDelay(5, 1200L) { riotClient.getLeagueEntriesBySummonerId(it.summonerId) }
                    val matches: Either<HttpError, List<GetMatchResponse>> =
                        retryWithFixedDelay(5, 1200L) { riotClient.getMatchesByPuuid(it.puuid) }.flatMap { m ->
                            m.map {
                                async { retryWithFixedDelay(5, 1200L) { riotClient.getMatchById(it) } }
                            }.awaitAll().sequence()
                        }
                    either {
                        val leagueEntries = leagues.bind()
                        val matchEntries = matches.bind()
                        Pair(it.id, RiotData.apply(it, leagueEntries, matchEntries))
                    }

                }

            }.awaitAll().split()
            errorsAndData.first.forEach { logger.error(it.error()) }
            val data = errorsAndData.second.map {
                DataCache(it.first, json.encodeToString<Data>(it.second), OffsetDateTime.now())
            }
            dataCacheRepository.insert(data)
            data.forEach { logger.info("Cached character ${it.characterId}") }
        }
    }

    private suspend fun cacheWowCharacters(wowCharacters: List<WowCharacter>) = coroutineScope {
        when (val cutoffOrError = raiderIoClient.cutoff()) {
            is Either.Left -> logger.error(cutoffOrError.value.error())
            is Either.Right -> {
                val errorsAndData =
                    wowCharacters.map { async { retryWithFixedDelay(3, 1000L) { raiderIoClient.get(it).map { r -> Pair(it.id, r) } } } }
                        .awaitAll()
                        .split()
                errorsAndData.first.forEach { logger.error(it.error()) }
                val data = errorsAndData.second.map {
                    DataCache(
                        it.first, json.encodeToString<Data>(
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