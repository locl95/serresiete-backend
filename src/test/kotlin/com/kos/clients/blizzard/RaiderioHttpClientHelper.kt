package com.kos.clients.blizzard

import com.kos.clients.domain.*
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import java.nio.charset.StandardCharsets

object RaiderioHttpClientHelper {

    val getWowCharacterResponse = GetWowCharacterResponse(
        id = 30758927,
        name = "Kumii",
        level = 60,
        isDead = false,
        averageItemLevel = 59,
        equippedItemLevel = 57,
        characterClass = "Hunter",
        race = "Night Elf",
        realm = "Stitches",
        guild = "I CANT RELEASE",
        experience = 0
    )

    val client = HttpClient(MockEngine) {
        install(ContentNegotiation) {
            json()
        }
        engine {
            addHandler { request ->
                when (request.url.encodedPath) {
                    "/mythic-plus/season-cutoffs" -> respond("content", HttpStatusCode.OK)
                    "/api/v1/characters/profile" -> {
                        val response = javaClass.classLoader
                            .getResourceAsStream("raiderio-profile-response.json")!!
                            .bufferedReader(StandardCharsets.UTF_8)
                            .use { it.readText() }
                        respond(response, HttpStatusCode.OK)
                    }

                    else -> error("Unhandled ${request.url.encodedPath}")
                }
            }
        }
    }
}