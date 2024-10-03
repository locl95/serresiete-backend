package com.kos.httpclients.riot

import arrow.core.Either
import com.kos.common.HttpError
import com.kos.common.JsonParseError
import com.kos.httpclients.domain.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

//TODO: This must be removed i guess, just for testing rn

data class RiotHTTPClient(val client: HttpClient, val apiKey: String) : RiotClient {
    val baseURI: (String) -> URI = { region -> URI("https://$region.api.riotgames.com") }
    private val json = Json {
        ignoreUnknownKeys = true
    }

    override suspend fun getPUUIDByRiotId(riotName: String, riotTag: String): Either<HttpError, GetPUUIDResponse> {
        val encodedRiotName = URLEncoder.encode(riotName, StandardCharsets.UTF_8.toString())

        val partialURI = URI("/riot/account/v1/accounts/by-riot-id/$encodedRiotName/$riotTag")
        val response = client.get(baseURI("europe").toString() + partialURI.toString()) {
            headers {
                append(HttpHeaders.Accept, "*/*")
                append("X-Riot-Token", apiKey)
            }
        }
        val jsonString = response.body<String>()
        return try {
            Either.Right(json.decodeFromString<GetPUUIDResponse>(jsonString))
        } catch (e: SerializationException) {
            Either.Left(JsonParseError(jsonString, e.stackTraceToString()))
        } catch (e: IllegalArgumentException) {
            val error = json.decodeFromString<RiotError>(jsonString)
            Either.Left(error)
        }
    }

    override suspend fun getSummonerByPuuid(puuid: String): Either<HttpError, GetSummonerResponse> {
        val partialURI = URI("/lol/summoner/v4/summoners/by-puuid/$puuid")
        val response = client.get(baseURI("euw1").toString() + partialURI.toString()) {
            headers {
                append(HttpHeaders.Accept, "*/*")
                append("X-Riot-Token", apiKey)
            }
        }
        val jsonString = response.body<String>()
        return try {
            Either.Right(json.decodeFromString<GetSummonerResponse>(jsonString))
        } catch (e: SerializationException) {
            Either.Left(JsonParseError(jsonString, e.stackTraceToString()))
        } catch (e: IllegalArgumentException) {
            val error = json.decodeFromString<RiotError>(jsonString)
            Either.Left(error)
        }
    }

    override suspend fun getMatchesByPuuid(puuid: String): Either<HttpError, List<String>> {
        val partialURI = URI("/lol/match/v5/matches/by-puuid/$puuid/ids")
        val response = client.get(baseURI("europe").toString() + partialURI.toString()) {
            headers {
                append(HttpHeaders.Accept, "*/*")
                append("X-Riot-Token", apiKey)
            }
            url {
                //parameters.append("startTime", 1L) //Epoch timestamp in seconds. The matchlist started storing timestamps on June 16th, 2021. Any matches played before June 16th, 2021 won't be included in the results if the startTime filter is set.
                //parameters.append("endTime", 1L) //Epoch timestamp in seconds.
                //parameters.append("queue", 1) //Filter the list of match ids by a specific queue id. This filter is mutually inclusive of the type filter meaning any match ids returned must match both the queue and type filters.
                //parameters.append("type", "type") //Filter the list of match ids by the type of match. This filter is mutually inclusive of the queue filter meaning any match ids returned must match both the queue and type filters.
                //parameters.append("start", 1) //Defaults to 0. Start index.
                parameters.append("count", "5") //Defaults to 20. Valid values: 0 to 100. Number of match ids to return.
            }

        }
        val jsonString = response.body<String>()
        return try {
            Either.Right(json.decodeFromString<List<String>>(jsonString))
        } catch (e: SerializationException) {
            Either.Left(JsonParseError(jsonString, e.stackTraceToString()))
        } catch (e: IllegalArgumentException) {
            val error = json.decodeFromString<RiotError>(jsonString)
            Either.Left(error)
        }
    }

    override suspend fun getMatchById(matchId: String): Either<HttpError, GetMatchResponse> {
        val partialURI = URI("/lol/match/v5/matches/$matchId")
        val response = client.get(baseURI("europe").toString() + partialURI.toString()) {
            headers {
                append(HttpHeaders.Accept, "*/*")
                append("X-Riot-Token", apiKey)
            }
        }
        val jsonString = response.body<String>()
        return try {
            Either.Right(json.decodeFromString<GetMatchResponse>(jsonString))
        } catch (e: SerializationException) {
            Either.Left(JsonParseError(jsonString, e.stackTraceToString()))
        } catch (e: IllegalArgumentException) {
            val error = json.decodeFromString<RiotError>(jsonString)
            Either.Left(error)
        }
    }

    override suspend fun getLeagueEntriesBySummonerId(summonerId: String): Either<HttpError, List<LeagueEntryResponse>> {
        val partialURI = URI("/lol/league/v4/entries/by-summoner/$summonerId")
        val response = client.get(baseURI("euw1").toString() + partialURI.toString()) {
            headers {
                append(HttpHeaders.Accept, "*/*")
                append("X-Riot-Token", apiKey)
            }
        }
        val jsonString = response.body<String>()
        return try {
            Either.Right(json.decodeFromString<List<LeagueEntryResponse>>(jsonString))
        } catch (e: SerializationException) {
            Either.Left(JsonParseError(jsonString, e.stackTraceToString()))
        } catch (e: IllegalArgumentException) {
            val error = json.decodeFromString<RiotError>(jsonString)
            Either.Left(error)
        }
    }

}