package com.kos.characters

import com.kos.characters.repository.CharactersDatabaseRepository
import com.kos.characters.repository.CharactersInMemoryRepository
import com.kos.common.DatabaseFactory
import kotlinx.coroutines.runBlocking
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class CharactersDatabaseRepositoryTest: CharactersRepositoryTest {
    @Before
    fun beforeEach() {

        DatabaseFactory.init(
            "org.h2.Driver",
            "jdbc:h2:file:./build/db-test",
            "",
            "",
            mustClean = true
        )
    }

    @Test
    override fun ICanInsertCharacters() {
        val repository = CharactersDatabaseRepository()
        val request = CharacterRequest("kakarona", "eu", "zuljin")
        val expected = Character(1, "kakarona", "eu", "zuljin")

        runBlocking { assertEquals(expected, repository.insert(request)) }
    }

    @Test
    override fun InsertingCharactersThatAlreadyExistDoesNothing() {
        val repository = runBlocking { CharactersDatabaseRepository().withState(listOf(Character(1, "kakarona", "eu", "zuljin"))) }
        val request = CharacterRequest("kakarona", "eu", "zuljin")
        val expectedInitialState = listOf(Character(1, "kakarona", "eu", "zuljin"))

        runBlocking { assertEquals(expectedInitialState, repository.state()) }
        runBlocking { repository.insert(request) }
        runBlocking { assertEquals(expectedInitialState, repository.state()) }
    }
}