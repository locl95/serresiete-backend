package com.kos.clients.blizzard

import com.kos.clients.domain.GetWowCharacterResponse
import com.kos.clients.domain.GetWowEquipmentResponse
import com.kos.datacache.BlizzardMockHelper.getWowCharacterResponse
import com.kos.datacache.BlizzardMockHelper.getWowCharacterResponseString
import com.kos.datacache.BlizzardMockHelper.getWowEquipmentResponse
import com.kos.datacache.BlizzardMockHelper.getWowEquipmentResponseString
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

    @Test
    fun `i can deserialize a character equipment response`() {
        assertEquals(getWowEquipmentResponse, json.decodeFromString<GetWowEquipmentResponse>(getWowEquipmentResponseString))
    }
}