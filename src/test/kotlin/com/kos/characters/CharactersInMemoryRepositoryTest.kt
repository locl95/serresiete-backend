package com.kos.characters

import com.kos.characters.repository.CharactersInMemoryRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class CharactersInMemoryRepositoryTest: CharactersRepositoryTest {
    @Test
    override fun ICanInsertCharacters() {
        val repository = CharactersInMemoryRepository()
        val request = CharacterRequest(CharactersTestHelper.basicCharacter.name, CharactersTestHelper.basicCharacter.region, CharactersTestHelper.basicCharacter.realm)
        val expected = listOf(CharactersTestHelper.basicCharacter)

        runBlocking { assertEquals(expected, repository.insert(listOf(request))) }
    }
}