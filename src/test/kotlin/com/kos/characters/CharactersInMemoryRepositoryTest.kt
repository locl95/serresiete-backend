package com.kos.characters

import com.kos.characters.repository.CharactersInMemoryRepository
import com.kos.common.DatabaseFactory
import kotlinx.coroutines.runBlocking
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class CharactersInMemoryRepositoryTest: CharactersRepositoryTest {
    @Test
    override fun ICanInsertCharacters() {
        val repository = CharactersInMemoryRepository()
        val request = CharacterRequest("kakarona", "eu", "zuljin")
        val expected = listOf(Character(1, "kakarona", "eu", "zuljin"))

        runBlocking { assertEquals(expected, repository.insert(listOf(request))) }
    }
}