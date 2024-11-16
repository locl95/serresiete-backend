package com.kos.clients.blizzard

import com.kos.clients.domain.GetWowCharacterResponse
import com.kos.datacache.BlizzardMockHelper.getWowCharacterResponse
import com.kos.datacache.BlizzardMockHelper.getWowCharacterResponseString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test

class BlizzardDomainTest {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    @Test
    fun `i can deserialize a character response`() {
        assertEquals(getWowCharacterResponse, json.decodeFromString<GetWowCharacterResponse>(getWowCharacterResponseString))
    }
}