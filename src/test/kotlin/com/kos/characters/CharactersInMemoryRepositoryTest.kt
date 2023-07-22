package com.kos.characters

import com.kos.characters.repository.CharactersInMemoryRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class CharactersInMemoryRepositoryTest: CharactersRepositoryTest {
    @Test
    override fun ICanInsertCharacters() {
        val repository = CharactersInMemoryRepository()
        val request = CharacterRequest("kakarona", "eu", "zuljin")
        val expected = Character(1, "kakarona", "eu", "zuljin")

        assertEquals(expected, repository.insert(request))
    }
}