package com.kos.httpclients.blizzard

import arrow.core.Either
import arrow.core.raise.either
import com.kos.characters.WowCharacterRequest
import com.kos.common.HttpError
import com.kos.common.JsonParseError
import com.kos.httpclients.domain.GetPUUIDResponse
import com.kos.httpclients.domain.GetWowCharacterResponse
import com.kos.httpclients.domain.RiotError
import com.kos.httpclients.domain.TokenResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.net.URI

class BlizzardHttpClient(private val client: HttpClient, private val blizzardAuthClient: BlizzardAuthClient) :
    BlizzardClient {
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

    override suspend fun exists(wowCharacterRequest: WowCharacterRequest): Boolean {
        return either {
            val tokenResponse = blizzardAuthClient.getAccessToken().bind()
            val partialURI =
                URI("/profile/wow/character/${wowCharacterRequest.realm}/${wowCharacterRequest.name}?locale=en_US")
            val response = getWowProfile(wowCharacterRequest.region, partialURI, tokenResponse)
            response.status.value < 300
        }.fold({ false }, { it })
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