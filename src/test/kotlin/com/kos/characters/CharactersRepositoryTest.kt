package com.kos.characters

import com.kos.characters.repository.CharactersDatabaseRepository
import com.kos.characters.repository.CharactersInMemoryRepository
import com.kos.characters.repository.CharactersRepository
import com.kos.common.DatabaseFactory
import kotlinx.coroutines.runBlocking
import kotlin.test.*

abstract class CharactersRepositoryTestCommon {

    abstract val repository: CharactersRepository

    @BeforeTest
    abstract fun beforeEach()

    @Test
    fun `given an empty repository i can insert characters`() {
        runBlocking {
            val request = CharacterRequest(
                CharactersTestHelper.basicCharacter.name,
                CharactersTestHelper.basicCharacter.region,
                CharactersTestHelper.basicCharacter.realm
            )
            val expected = listOf(CharactersTestHelper.basicCharacter)
            repository.insert(listOf(request)).fold({ fail() }) { assertEquals(expected, it) }

        }
    }

    @Test
    fun `given an empty repository inserting a character that already exists fails`() {
        runBlocking {
            val character = CharacterRequest(
                CharactersTestHelper.basicCharacter.name,
                CharactersTestHelper.basicCharacter.region,
                CharactersTestHelper.basicCharacter.realm
            )

            val initialState = repository.state()
            assertEquals(listOf(), initialState)
            assertTrue(repository.insert(listOf(character, character)).isLeft())

            val finalState = repository.state()
            assertEquals(listOf(), finalState)
        }
    }

    @Test
    fun `given a repository that includes a character, adding the same one fails`() {
        runBlocking {
            val repo =
                repository.withState(listOf(CharactersTestHelper.basicCharacter, CharactersTestHelper.basicCharacter2))
            assertTrue(repo.insert(listOf(CharactersTestHelper.basicRequest)).isLeft())
            assertEquals(
                listOf(CharactersTestHelper.basicCharacter, CharactersTestHelper.basicCharacter2),
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
