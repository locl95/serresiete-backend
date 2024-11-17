package com.kos.clients.blizzard

import arrow.core.Either
import arrow.core.raise.either
import com.kos.characters.WowCharacterRequest
import com.kos.clients.domain.*
import com.kos.common.HttpError
import com.kos.common.JsonParseError
import com.kos.common.WithLogger
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.net.URI

class BlizzardHttpClient(private val client: HttpClient, private val blizzardAuthClient: BlizzardAuthClient) :
    BlizzardClient, WithLogger("blizzardClient") {
    private val baseURI: (String) -> URI = { region -> URI("https://$region.api.blizzard.com") }
    private val json = Json {
        ignoreUnknownKeys = true
    }

    override suspend fun getCharacterProfile(
        region: String,
        realm: String,
        character: String
    ): Either<HttpError, GetWowCharacterResponse> {
        return either {
            logger.debug("getCharacterProfile for $realm $realm $character")
            val tokenResponse = blizzardAuthClient.getAccessToken().bind()
            val partialURI = URI("/profile/wow/character/$realm/$character?locale=en_US")
            val response = getWowProfile(region, partialURI, tokenResponse)
            val jsonString = response.body<String>()
            try {
                json.decodeFromString<GetWowCharacterResponse>(jsonString)
            } catch (e: SerializationException) {
                raise(JsonParseError(jsonString, e.stackTraceToString()))
            } catch (e: IllegalArgumentException) {
                raise(json.decodeFromString<RiotError>(jsonString))
            }
        }
    }

    override suspend fun getCharacterMedia(
        region: String,
        realm: String,
        character: String
    ): Either<HttpError, GetWowMediaResponse> {
        return either {
            logger.debug("getCharacterMedia for $realm $realm $character")
            val tokenResponse = blizzardAuthClient.getAccessToken().bind()
            val partialURI = URI("/profile/wow/character/$realm/$character/character-media?locale=en_US")
            val response = client.get(baseURI(region).toString() + partialURI.toString()) {
                headers {
                    append(HttpHeaders.Authorization, "Bearer ${tokenResponse.accessToken}")
                    append(HttpHeaders.Accept, "*/*")
                    append("Battlenet-Namespace", "profile-classic1x-eu")
                }
            }
            val jsonString = response.body<String>()
            try {
                json.decodeFromString<GetWowMediaResponse>(jsonString)
            } catch (e: SerializationException) {
                raise(JsonParseError(jsonString, e.stackTraceToString()))
            } catch (e: IllegalArgumentException) {
                raise(json.decodeFromString<RiotError>(jsonString))
            }
        }
    }

    override suspend fun getRealm(
        region: String,
        id: Long
    ): Either<HttpError, GetWowRealmResponse> {
        return either {
            val tokenResponse = blizzardAuthClient.getAccessToken().bind()
            val partialUri = URI("/data/wow/realm/$id?locale=en_US")
            val response = client.get(baseURI(region).toString() + partialUri.toString()) {
                headers {
                    append(HttpHeaders.Authorization, "Bearer ${tokenResponse.accessToken}")
                    append(HttpHeaders.Accept, "*/*")
                    append("Battlenet-Namespace", "dynamic-classic1x-eu")
                }
            }
            val jsonString = response.body<String>()
            try {
                json.decodeFromString<GetWowRealmResponse>(jsonString)
            } catch (e: SerializationException) {
                raise(JsonParseError(jsonString, e.stackTraceToString()))
            } catch (e: IllegalArgumentException) {
                raise(json.decodeFromString<RiotError>(jsonString))
            }
        }
    }

    private suspend fun getWowProfile(
        region: String,
        partialURI: URI,
        tokenResponse: TokenResponse
    ) = client.get(baseURI(region).toString() + partialURI.toString()) {
        headers {
            append(HttpHeaders.Authorization, "Bearer ${tokenResponse.accessToken}")
            append(HttpHeaders.Accept, "*/*")
            append("Battlenet-Namespace", "profile-classic1x-eu")
        }
    }
}