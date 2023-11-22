package com.kos.characters

import com.kos.characters.CharactersTestHelper.basicCharacter
import com.kos.characters.repository.CharactersInMemoryRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class CharactersServiceTest {
    @Test
    fun `inserting two characters over an empty repository returns the ids of both new characters`() {
        val charactersRepository = CharactersInMemoryRepository()
        val charactersService = CharactersService(charactersRepository)

        val request = listOf(
            CharacterRequest(basicCharacter.name, basicCharacter.region, basicCharacter.realm),
            CharacterRequest("kakarøna", basicCharacter.region, basicCharacter.realm),
        )
        val expected: List<Long> = listOf(1,2)

        runBlocking { assertEquals(expected, charactersService.createAndReturnIds(request)) }

    }
}