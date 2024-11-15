package com.kos.clients.blizzard

import com.kos.datacache.BlizzardMockHelper.getWowCharacterResponseString
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*

object BlizzardHttpClientHelper {

    val client = HttpClient(MockEngine) {
        install(ContentNegotiation) {
            json()
        }
        engine {
            addHandler { request ->
                when (request.url.encodedPath) {
                    "/profile/wow/character/realm/name" -> respond(getWowCharacterResponseString, HttpStatusCode.OK)
                    else -> error("Unhandled ${request.url.encodedPath}")
                }
            }
        }
    }
}