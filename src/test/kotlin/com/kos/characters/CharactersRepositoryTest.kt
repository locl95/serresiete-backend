package com.kos.characters

import com.kos.characters.CharactersTestHelper.emptyCharactersState
import com.kos.characters.repository.CharactersDatabaseRepository
import com.kos.characters.repository.CharactersInMemoryRepository
import com.kos.characters.repository.CharactersRepository
import com.kos.characters.repository.CharactersState
import com.kos.common.DatabaseFactory
import com.kos.views.Game
import kotlinx.coroutines.runBlocking
import kotlin.test.*

abstract class CharactersRepositoryTestCommon {

    abstract val repository: CharactersRepository

    @BeforeTest
    abstract fun beforeEach()

    @Test
    fun `given an empty repository i can insert characters`() {
        runBlocking {
            val request = WowCharacterRequest(
                CharactersTestHelper.basicCharacter.name,
                CharactersTestHelper.basicCharacter.region,
                CharactersTestHelper.basicCharacter.realm
            )
            val expected = listOf(CharactersTestHelper.basicCharacter)
            repository.insert(listOf(request), Game.WOW).fold({ fail() }) { assertEquals(expected, it) }

        }
    }

    @Test
    fun `given an empty repository inserting a character that already exists fails`() {
        runBlocking {
            val character = WowCharacterRequest(
                CharactersTestHelper.basicCharacter.name,
                CharactersTestHelper.basicCharacter.region,
                CharactersTestHelper.basicCharacter.realm
            )

            val initialState = repository.state()
            assertEquals(emptyCharactersState, initialState)
            assertTrue(repository.insert(listOf(character, character), Game.WOW).isLeft())

            val finalState = repository.state()
            assertEquals(emptyCharactersState, finalState)
        }
    }

    @Test
    fun `given a repository that includes a character, adding the same one fails`() {
        runBlocking {
            val repo =
                repository.withState(
                    CharactersState(
                        listOf(
                            CharactersTestHelper.basicCharacter,
                            CharactersTestHelper.basicWowCharacter2
                        ), listOf()
                    )
                )
            assertTrue(repo.insert(listOf(CharactersTestHelper.basicRequest), Game.WOW).isLeft())
            assertEquals(
                CharactersState(listOf(CharactersTestHelper.basicCharacter, CharactersTestHelper.basicWowCharacter2), listOf()),
                repository.state()
            )
        }
    }
}

class CharactersInMemoryRepositoryTest : CharactersRepositoryTestCommon() {
    override val repository = CharactersInMemoryRepository()
    override fun beforeEach() {
        repository.clear()
    }
}

class CharactersDatabaseRepositoryTest : CharactersRepositoryTestCommon() {
    override val repository = CharactersDatabaseRepository()
    override fun beforeEach() {
        DatabaseFactory.init(mustClean = true)
    }
}
