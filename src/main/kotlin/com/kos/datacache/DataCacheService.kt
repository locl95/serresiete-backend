package com.kos.datacache

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.flatten
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
import com.kos.httpclients.HttpUtils.retryEitherWithFixedDelay
import com.kos.httpclients.domain.*
import com.kos.httpclients.raiderio.RaiderIoClient
import com.kos.httpclients.riot.RiotClient
import com.kos.views.Game
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
    suspend fun getData(characterIds: List<Long>, oldFirst: Boolean): Either<JsonParseError, List<Data>> =
        either {
            val comparator: (List<DataCache>) -> DataCache? = if (oldFirst) {
                { it.minByOrNull { dc -> dc.inserted } }
            } else {
                { it.maxByOrNull { dc -> dc.inserted } }
            }

            characterIds.mapNotNull { id ->
                comparator(get(id))?.let { dataCache ->
                    try {
                        json.decodeFromString<Data>(dataCache.data)
                    } catch (se: SerializationException) {
                        raise(JsonParseError(dataCache.data, "", se.stackTraceToString()))
                    } catch (iae: IllegalArgumentException) {
                        raise(JsonParseError(dataCache.data, "", iae.stackTraceToString()))
                    }
                }
            }
        }

    @Suppress("UNCHECKED_CAST")
    suspend fun cache(characters: List<Character>, game: Game): List<HttpError> {
        return when (game) {
            Game.WOW -> cacheWowCharacters(characters as List<WowCharacter>)
            Game.LOL -> cacheLolCharacters(characters as List<LolCharacter>)
        }
    }

    private suspend fun cacheLolCharacters(lolCharacters: List<LolCharacter>): List<HttpError> = coroutineScope {
        val errorsAndData: Pair<List<HttpError>, List<Pair<Long, RiotData>>> =
            lolCharacters.map { lolCharacter ->
                async {
                    cacheLolCharacter(lolCharacter)
                }
            }.awaitAll().split()

        errorsAndData.first.forEach { logger.error(it.error()) }
        val data = errorsAndData.second.map { (id, riotData) ->
            DataCache(id, json.encodeToString<Data>(riotData), OffsetDateTime.now())
        }
        dataCacheRepository.insert(data)
        data.forEach { logger.info("Cached character ${it.characterId}") }
        errorsAndData.first
    }

    private suspend fun cacheLolCharacter(lolCharacter: LolCharacter): Either<HttpError, Pair<Long, RiotData>> =
        either {
            val leagues: List<LeagueEntryResponse> =
                retryEitherWithFixedDelay(5, 1200L, "getLeagueEntriesBySummonerId") {
                    riotClient.getLeagueEntriesBySummonerId(lolCharacter.summonerId)
                }.bind()

            val leagueWithMatches: List<Pair<LeagueEntryResponse, List<GetMatchResponse>>> =
                coroutineScope {
                    leagues.map { leagueEntry ->
                        async {
                            val matchIds: List<String> = retryEitherWithFixedDelay(5, 1200L, "getMatchesByPuuid") {
                                riotClient.getMatchesByPuuid(lolCharacter.puuid, leagueEntry.queueType.toInt())
                            }.bind()

                            val matchResponses: List<GetMatchResponse> = matchIds.map { matchId ->
                                retryEitherWithFixedDelay(5, 1200L, "getMatchById") {
                                    riotClient.getMatchById(matchId)
                                }.bind()
                            }

                            leagueEntry to matchResponses
                        }
                    }.awaitAll()
                }

            Pair(lolCharacter.id, RiotData.apply(lolCharacter, leagueWithMatches))
        }


    private suspend fun cacheWowCharacters(wowCharacters: List<WowCharacter>): List<HttpError> =
        coroutineScope {
            val cutoffErrorOrMaybeErrors = either {
                val cutoff = raiderIoClient.cutoff().bind()
                val errorsAndData =
                    wowCharacters.map {
                        async {
                            retryEitherWithFixedDelay(3, 1000L, "raiderIoGet") {
                                raiderIoClient.get(it).map { r -> Pair(it.id, r) }
                            }
                        }
                    }
                        .awaitAll()
                        .split()
                val data = errorsAndData.second.map {
                    DataCache(
                        it.first, json.encodeToString<Data>(
                            it.second.profile.toRaiderIoData(
                                it.first,
                                BigDecimal(it.second.profile.mythicPlusRanks.overall.region.toDouble() / cutoff.totalPopulation * 100).setScale(
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
            cutoffErrorOrMaybeErrors.mapLeft { listOf(it) }.fold({ it }, { it })
        }

    suspend fun clear(): Int = dataCacheRepository.deleteExpiredRecord(ttl)
}