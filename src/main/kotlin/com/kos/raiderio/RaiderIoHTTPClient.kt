package com.kos.raiderio

import arrow.core.Either
import com.kos.characters.WowCharacter
import com.kos.characters.WowCharacterRequest
import com.kos.common.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.net.URI

data class RaiderIoHTTPClient(val client: HttpClient) : RaiderIoClient, WithLogger("RaiderioClient") {
    private val baseURI = URI("https://raider.io/api/v1")
    private val partialProfileUri = "/characters/profile"
    private val json = Json {
        ignoreUnknownKeys = true
    }

    private fun responseToEitherErrorOrProfile(jsonString: String) = try {
        Either.Right(json.decodeFromString<RaiderIoProfile>(jsonString))
    } catch (e: SerializationException) {
        Either.Left(JsonParseError(jsonString, e.stackTraceToString()))
    } catch (e: IllegalArgumentException) {
        val error = json.decodeFromString<RaiderIoError>(jsonString)
        Either.Left(error)
    }

    private suspend fun getRaiderioProfile(region: String, realm: String, name: String): HttpResponse =
        client.get(baseURI.toString() + partialProfileUri) {
            headers {
                append(HttpHeaders.Accept, "*/*")
            }
            url {
                parameters.append("region", region)
                parameters.append("realm", realm)
                parameters.append("name", name)
                parameters.append(
                    "fields",
                    "mythic_plus_scores_by_season:current,mythic_plus_best_runs:all,mythic_plus_ranks"
                )
            }
        }

    override suspend fun get(wowCharacter: WowCharacter): Either<HttpError, RaiderIoResponse> {
        val response = getRaiderioProfile(wowCharacter.region, wowCharacter.realm, wowCharacter.name)
        val jsonString = response.body<String>()
        val decodedResponse: Either<HttpError, RaiderIoProfile> = responseToEitherErrorOrProfile(jsonString)

        return decodedResponse.fold({ httpError -> Either.Left(httpError) }) {
            RaiderIoProtocol.parseMythicPlusRanks(
                jsonString,
                wowCharacter.specsWithName(it.`class`),
                it.seasonScores[0].scores
            ).fold({ jsonError -> Either.Left(jsonError) }) { specsWithName ->
                Either.Right(RaiderIoResponse(it, specsWithName))
            }
        }
    }

    override suspend fun exists(wowCharacterRequest: WowCharacterRequest): Boolean {
        val response = getRaiderioProfile(wowCharacterRequest.region, wowCharacterRequest.realm, wowCharacterRequest.name)
        return response.status.value < 300
    }

    override suspend fun cutoff(): Either<HttpError, RaiderIoCutoff> {
        val partialUri = "/mythic-plus/season-cutoffs"
        val response = client.get(baseURI.toString() + partialUri) {
            headers {
                append(HttpHeaders.Accept, "*/*")
            }
            url {
                parameters.append("region", "eu")
                parameters.append("season", "season-df-3")
            }
        }
        val jsonString = response.body<String>()
        return RaiderIoProtocol.parseCutoffJson(jsonString)
    }
}