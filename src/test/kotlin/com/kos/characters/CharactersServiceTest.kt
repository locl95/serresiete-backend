package com.kos.characters

import com.kos.characters.repository.CharactersInMemoryRepository
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals

class CharactersServiceTest {
    @Test
    fun CreateAndReturnIds() {
        val charactersRepository = CharactersInMemoryRepository()
        val charactersService = CharactersService(charactersRepository)

        val request = listOf(
            CharacterRequest("kakarona", "eu", "zuljin"),
            CharacterRequest("kakarøna", "eu", "zuljin"),
        )
        val expected: List<Long> = listOf(1,2)

        runBlocking { assertEquals(expected, charactersService.createAndReturnIds(request)) }

    }
}