package com.kos.raiderio

import arrow.core.Either
import com.kos.characters.Character
import com.kos.common.JsonParseError
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import java.net.URI

data class RaiderIoHTTPClient(val client: HttpClient) : RaiderIoClient {
    private val baseURI = URI("https://raider.io/api/v1")

    private val json = Json {
        ignoreUnknownKeys = true
    }

    override suspend fun get(character: Character): Either<JsonParseError, RaiderIoResponse>  {
        val partialUri = "/characters/profile"
        val response = client.get(baseURI.toString() + partialUri) {
            headers {
                append(HttpHeaders.Accept, "*/*")
            }
            url {
                parameters.append("region", character.region)
                parameters.append("realm", character.realm)
                parameters.append("name", character.name)
                parameters.append("fields", "mythic_plus_scores_by_season:current,mythic_plus_best_runs:all,mythic_plus_ranks,mythic_plus_alternate_runs:all")
            }
        }
        val jsonString = response.body<String>()
        val profile = json.decodeFromString<RaiderIoProfile>(jsonString)
        return when (val specRanksOrError = RaiderIoProtocol.parseMythicPlusRanks(jsonString, character.specsWithName(profile.`class`), profile.seasonScores[0].scores)) {
            is Either.Left -> specRanksOrError
            is Either.Right -> {
                Either.Right(RaiderIoResponse(profile, specRanksOrError.value))
            }
        }
    }

    override suspend fun cutoff(): Either<JsonParseError,RaiderIoCutoff> {
        val partialUri = "/mythic-plus/season-cutoffs"
        val response = client.get(baseURI.toString() + partialUri) {
            headers {
                append(HttpHeaders.Accept, "*/*")
            }
            url {
                parameters.append("region", "eu")
                parameters.append("season", "season-df-2")
            }
        }
        val jsonString = response.body<String>()
        return RaiderIoProtocol.parseCutoffJson(jsonString)
    }
}