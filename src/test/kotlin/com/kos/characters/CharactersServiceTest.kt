package com.kos.characters

import com.kos.characters.repository.CharactersInMemoryRepository
import org.junit.Test
import kotlin.test.assertEquals

class CharactersServiceTest {
    @Test
    fun foo() {
        val charactersRepository = CharactersInMemoryRepository()
        val charactersService = CharactersService(charactersRepository)

        val request = listOf(
            CharacterRequest("kakarona", "eu", "zuljin"),
            CharacterRequest("kakarøna", "eu", "zuljin"),
        )
        val expected: List<Long> = listOf(1,2)

        assertEquals(expected, charactersService.create(request))

    }
}